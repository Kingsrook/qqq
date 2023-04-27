/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.modules.authentication.implementations;


import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.net.Response;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.AccessTokenException;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.SimpleStateKey;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class Auth0AuthenticationModule implements QAuthenticationModuleInterface
{
   private static final QLogger LOG = QLogger.getLogger(Auth0AuthenticationModule.class);

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // 30 minutes - ideally this would be lower, but right now we've been dealing with re-validation issues... //
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public static final int ID_TOKEN_VALIDATION_INTERVAL_SECONDS = 1800;

   public static final String AUTH0_ACCESS_TOKEN_KEY = "sessionId";
   public static final String BASIC_AUTH_KEY         = "basicAuthString";

   public static final String TOKEN_NOT_PROVIDED_ERROR = "Access Token was not provided";
   public static final String COULD_NOT_DECODE_ERROR   = "Unable to decode access token";
   public static final String EXPIRED_TOKEN_ERROR      = "Token has expired";
   public static final String INVALID_TOKEN_ERROR      = "An invalid token was provided";


   ////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // this is how we allow the actions within this class to work without themselves having a logged-in user. //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private static QSession chickenAndEggSession = new QSession()
   {

   };



   /*******************************************************************************
    ** Getter for special session
    **
    *******************************************************************************/
   private QSession getChickenAndEggSession()
   {
      for(String typeName : QContext.getQInstance().getSecurityKeyTypes().keySet())
      {
         QSecurityKeyType keyType = QContext.getQInstance().getSecurityKeyType(typeName);
         if(StringUtils.hasContent(keyType.getAllAccessKeyName()))
         {
            chickenAndEggSession = chickenAndEggSession.withSecurityKeyValue(keyType.getAllAccessKeyName(), true);
         }
      }
      return (chickenAndEggSession);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSession createSession(QInstance qInstance, Map<String, String> context) throws QAuthenticationException
   {
      Auth0AuthenticationMetaData metaData = (Auth0AuthenticationMetaData) qInstance.getAuthentication();

      ///////////////////////////////////////////////////////////
      // check if we are processing a Basic Auth Session first //
      ///////////////////////////////////////////////////////////
      if(context.containsKey(BASIC_AUTH_KEY))
      {
         AuthAPI auth = new AuthAPI(metaData.getBaseUrl(), metaData.getClientId(), metaData.getClientSecret());
         try
         {
            /////////////////////////////////////////////////
            // decode the credentials from the header auth //
            /////////////////////////////////////////////////
            String base64Credentials = context.get(BASIC_AUTH_KEY).trim();
            String accessToken       = getAccessTokenFromBase64BasicAuthCredentials(metaData, auth, base64Credentials);
            context.put(AUTH0_ACCESS_TOKEN_KEY, accessToken);
         }
         catch(Auth0Exception e)
         {
            ////////////////
            // ¯\_(ツ)_/¯ //
            ////////////////
            String message = "Error handling basic authentication: " + e.getMessage();
            LOG.error(message, e);
            throw (new QAuthenticationException(message));
         }
      }

      ////////////////////////////////////////////////////////////////////
      // get the jwt id or qqq translated token from the context object //
      ////////////////////////////////////////////////////////////////////
      String accessToken = context.get(AUTH0_ACCESS_TOKEN_KEY);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check to see if the session id is a UUID, if so, that means we need to look up the 'actual' token in the access_token table //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(accessToken != null && StringUtils.isUUID(accessToken))
      {
         accessToken = lookupActualAccessToken(metaData, accessToken);
      }

      if(accessToken == null)
      {
         LOG.warn(TOKEN_NOT_PROVIDED_ERROR);
         throw (new QAuthenticationException(TOKEN_NOT_PROVIDED_ERROR));
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // decode the token locally to make sure it is valid and to look at when it expires //
      //////////////////////////////////////////////////////////////////////////////////////
      try
      {
         /////////////////////////////////////////////////////
         // try to build session to see if still valid      //
         // then call method to check more session validity //
         /////////////////////////////////////////////////////
         QSession qSession = buildQSessionFromToken(accessToken, qInstance);
         if(isSessionValid(qInstance, qSession))
         {
            return (qSession);
         }

         ///////////////////////////////////////////////////////////////////////////////////////
         // if we make it here it means we have never validated this token or its been a long //
         // enough duration so we need to re-verify the token                                 //
         ///////////////////////////////////////////////////////////////////////////////////////
         qSession = revalidateTokenAndBuildSession(qInstance, accessToken);

         ////////////////////////////////////////////////////////////////////
         // put now into state so we dont check until next interval passes //
         ///////////////////////////////////////////////////////////////////
         StateProviderInterface spi = getStateProvider();
         SimpleStateKey<String> key = new SimpleStateKey<>(qSession.getIdReference());
         spi.put(key, Instant.now());

         return (qSession);
      }
      catch(JWTDecodeException jde)
      {
         ////////////////////////////////
         // could not decode the token //
         ////////////////////////////////
         LOG.warn(COULD_NOT_DECODE_ERROR, jde);
         throw (new QAuthenticationException(COULD_NOT_DECODE_ERROR));
      }
      catch(TokenExpiredException tee)
      {
         LOG.info(EXPIRED_TOKEN_ERROR, tee);
         throw (new QAuthenticationException(EXPIRED_TOKEN_ERROR));
      }
      catch(JWTVerificationException | JwkException jve)
      {
         ///////////////////////////////////////////
         // token had invalid signature or claims //
         ///////////////////////////////////////////
         LOG.warn(INVALID_TOKEN_ERROR, jve);
         throw (new QAuthenticationException(INVALID_TOKEN_ERROR));
      }
      catch(Exception e)
      {
         ////////////////
         // ¯\_(ツ)_/¯ //
         ////////////////
         String message = "An unknown error occurred";
         LOG.error(message, e);
         throw (new QAuthenticationException(message));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getAccessTokenFromBase64BasicAuthCredentials(Auth0AuthenticationMetaData metaData, AuthAPI auth, String base64Credentials) throws Auth0Exception
   {
      ////////////////////////////////////////////////////////////////////////////////////
      // look for a fresh accessToken in the state provider for this set of credentials //
      ////////////////////////////////////////////////////////////////////////////////////
      SimpleStateKey<String> accessTokenStateKey = new SimpleStateKey<>(base64Credentials + ":accessToken");
      SimpleStateKey<String> timestampStateKey   = new SimpleStateKey<>(base64Credentials + ":timestamp");
      StateProviderInterface stateProvider       = getStateProvider();
      Optional<String>       cachedAccessToken   = stateProvider.get(String.class, accessTokenStateKey);
      Optional<Instant>      cachedTimestamp     = stateProvider.get(Instant.class, timestampStateKey);
      if(cachedAccessToken.isPresent() && cachedTimestamp.isPresent())
      {
         if(cachedTimestamp.get().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)))
         {
            return cachedAccessToken.get();
         }
      }

      //////////////////////////////////////////////////////////////////////////////////
      // not found in cache, make request to auth0 and cache the returned accessToken //
      //////////////////////////////////////////////////////////////////////////////////
      byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
      String credentials = new String(credDecoded, StandardCharsets.UTF_8);

      String accessToken = getAccessTokenFromAuth0(metaData, auth, credentials);
      stateProvider.put(accessTokenStateKey, accessToken);
      stateProvider.put(timestampStateKey, Instant.now());
      return (accessToken);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getAccessTokenFromAuth0(Auth0AuthenticationMetaData metaData, AuthAPI auth, String credentials) throws Auth0Exception
   {
      /////////////////////////////////////
      // call auth0 with a login request //
      /////////////////////////////////////
      Response<TokenHolder> result = auth.login(credentials.split(":")[0], credentials.split(":")[1].toCharArray())
         .setScope("openid email nickname")
         .setAudience(metaData.getAudience())
         .execute();

      return (result.getBody().getAccessToken());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean isSessionValid(QInstance instance, QSession session)
   {
      if(session == chickenAndEggSession)
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // this is how we allow the actions within this class to work without themselves having a logged-in user. //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////
         return (true);
      }

      if(session == null)
      {
         return (false);
      }

      if(session.getIdReference() == null)
      {
         return (false);
      }

      StateProviderInterface spi                     = getStateProvider();
      SimpleStateKey<String> key                     = new SimpleStateKey<>(session.getIdReference());
      Optional<Instant>      lastTimeCheckedOptional = spi.get(Instant.class, key);
      if(lastTimeCheckedOptional.isPresent())
      {
         Instant lastTimeChecked = lastTimeCheckedOptional.get();

         ///////////////////////////////////////////////////////////////////////////////////////////////////
         // returns negative int if less than compared duration, 0 if equal, positive int if greater than //
         // - so this is basically saying, if the time between the last time we checked the token and     //
         // right now is more than ID_TOKEN_VALIDATION_INTERVAL_SECTIONS, then session needs revalidated  //
         ///////////////////////////////////////////////////////////////////////////////////////////////////
         if(Duration.between(lastTimeChecked, Instant.now()).compareTo(Duration.ofSeconds(ID_TOKEN_VALIDATION_INTERVAL_SECONDS)) < 0)
         {
            return (true);
         }

         try
         {
            LOG.debug("Re-validating token due to validation interval being passed: " + session.getIdReference());
            revalidateTokenAndBuildSession(instance, session.getIdReference());

            //////////////////////////////////////////////////////////////////
            // update the timestamp in state provider, to avoid re-checking //
            //////////////////////////////////////////////////////////////////
            spi.put(key, Instant.now());

            return (true);
         }
         catch(Exception e)
         {
            LOG.warn(INVALID_TOKEN_ERROR, e);
            return (false);
         }
      }

      return (false);
   }



   /*******************************************************************************
    ** makes request to check if a token is still valid and build new qSession if it is
    **
    *******************************************************************************/
   private QSession revalidateTokenAndBuildSession(QInstance qInstance, String accessToken) throws JwkException
   {
      ///////////////////////////////////
      // make call to verify the token //
      ///////////////////////////////////
      validateToken(qInstance, accessToken);
      return (buildQSessionFromToken(accessToken, qInstance));
   }



   /*******************************************************************************
    ** tests validity of a token
    **
    *******************************************************************************/
   private void validateToken(QInstance qInstance, String tokenString) throws JwkException
   {
      Auth0AuthenticationMetaData metaData = (Auth0AuthenticationMetaData) qInstance.getAuthentication();

      DecodedJWT  idToken   = JWT.decode(tokenString);
      JwkProvider provider  = new UrlJwkProvider(metaData.getBaseUrl());
      Jwk         jwk       = provider.get(idToken.getKeyId());
      Algorithm   algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
      JWT.require(algorithm)
         .withIssuer(idToken.getIssuer())
         .build()
         .verify(idToken);
   }



   /*******************************************************************************
    ** extracts info from token creating a QSession
    **
    *******************************************************************************/
   private QSession buildQSessionFromToken(String accessToken, QInstance qInstance) throws JwkException
   {
      ////////////////////////////////////
      // decode and extract the payload //
      ////////////////////////////////////
      DecodedJWT     jwt           = JWT.decode(accessToken);
      Base64.Decoder decoder       = Base64.getUrlDecoder();
      String         payloadString = new String(decoder.decode(jwt.getPayload()));
      JSONObject     payload       = new JSONObject(payloadString);

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // create user object.  look for multiple possible keys in the jwt payload where the name & email may be //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      QUser qUser = new QUser();
      qUser.setFullName("Unknown");
      for(String key : List.of("name", "com.kingsrook.qqq.name", "com.kingsrook.qqq.client_name"))
      {
         if(payload.has(key))
         {
            qUser.setFullName(payload.getString(key));
            break;
         }
      }

      for(String key : List.of("email", "com.kingsrook.qqq.email", "sub"))
      {
         if(payload.has(key))
         {
            qUser.setIdReference(payload.getString(key));
            break;
         }
      }

      /////////////////////////////////////////////////////////
      // create session object - link to access token & user //
      /////////////////////////////////////////////////////////
      QSession qSession = new QSession();
      qSession.setIdReference(accessToken);
      qSession.setUser(qUser);

      /////////////////////////////////////////////////////////////////////
      // put the user id reference in security key value for usierId key //
      /////////////////////////////////////////////////////////////////////
      qSession.withSecurityKeyValue("userId", qUser.getIdReference());

      /////////////////////////////////////////////////
      // set permissions in the session from the JWT //
      /////////////////////////////////////////////////
      setPermissionsInSessionFromJwtPayload(payload, qSession);

      ///////////////////////////////////////////////////
      // set security keys in the session from the JWT //
      ///////////////////////////////////////////////////
      setSecurityKeysInSessionFromJwtPayload(qInstance, payload, qSession);

      return (qSession);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void setPermissionsInSessionFromJwtPayload(JSONObject payload, QSession qSession)
   {
      HashSet<String> permissions = new HashSet<>();
      if(payload.has("permissions"))
      {
         try
         {
            JSONArray jwtPermissions = payload.getJSONArray("permissions");
            for(int i = 0; i < jwtPermissions.length(); i++)
            {
               permissions.add(jwtPermissions.optString(i));
            }
         }
         catch(Exception e)
         {
            LOG.error("Error getting permissions from JWT", e);
         }
      }
      qSession.setPermissions(permissions);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void setSecurityKeysInSessionFromJwtPayload(QInstance qInstance, JSONObject payload, QSession qSession)
   {
      for(String payloadKey : List.of("com.kingsrook.qqq.app_metadata", "com.kingsrook.qqq.client_metadata"))
      {
         if(!payload.has(payloadKey))
         {
            continue;
         }

         try
         {
            JSONObject  appMetadata             = payload.getJSONObject(payloadKey);
            Set<String> allowedSecurityKeyNames = qInstance.getAllowedSecurityKeyNames();

            //////////////////////////////////////////////////////////////////////////////////
            // for users, they will have a map of securityKeyValues (in their app_metadata) //
            //////////////////////////////////////////////////////////////////////////////////
            JSONObject securityKeyValues = appMetadata.optJSONObject("securityKeyValues");
            if(securityKeyValues != null)
            {
               for(String keyName : securityKeyValues.keySet())
               {
                  setSecurityKeyValuesFromToken(allowedSecurityKeyNames, qSession, keyName, securityKeyValues, keyName);
               }
            }
            else
            {
               //////////////////////////////////////////////////////////////////////////////////////////////////
               // for system-logins, there will be keys prefixed by securityKeyValues: (under client_metadata) //
               //////////////////////////////////////////////////////////////////////////////////////////////////
               for(String appMetaDataKey : appMetadata.keySet())
               {
                  if(appMetaDataKey.startsWith("securityKeyValues:"))
                  {
                     String securityKeyName = appMetaDataKey.replace("securityKeyValues:", "");
                     setSecurityKeyValuesFromToken(allowedSecurityKeyNames, qSession, securityKeyName, appMetadata, appMetaDataKey);
                  }
               }
            }
         }
         catch(Exception e)
         {
            LOG.error("Error getting securityKey values from app_metadata from JWT", e);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void setSecurityKeyValuesFromToken(Set<String> allowedSecurityKeyNames, QSession qSession, String securityKeyName, JSONObject securityKeyValues, String jsonKey)
   {
      if(!allowedSecurityKeyNames.contains(securityKeyName))
      {
         QUser user = qSession.getUser();
         LOG.warn("Unrecognized security key name [" + securityKeyName + "] when creating session for user [" + user + "].  Allowed key names are: " + allowedSecurityKeyNames);
         return;
      }

      JSONArray valueArray = securityKeyValues.optJSONArray(jsonKey);
      if(valueArray != null)
      {
         // todo - types?
         for(int i = 0; i < valueArray.length(); i++)
         {
            Object optValue = valueArray.opt(i);
            if(optValue != null)
            {
               qSession.withSecurityKeyValue(securityKeyName, ValueUtils.getValueAsString(optValue));
            }
         }
      }
      else
      {
         String values = securityKeyValues.optString(jsonKey);
         if(StringUtils.hasContent(values))
         {
            for(String v : values.split(","))
            {
               qSession.withSecurityKeyValue(securityKeyName, v);
            }
         }
      }
   }



   /*******************************************************************************
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   private static StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      return (InMemoryStateProvider.getInstance());
   }



   /*******************************************************************************
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   public String createAccessToken(QAuthenticationMetaData metaData, String clientId, String clientSecret) throws AccessTokenException
   {
      QSession                    sessionBefore = QContext.getQSession();
      Auth0AuthenticationMetaData auth0MetaData = (Auth0AuthenticationMetaData) metaData;

      try
      {
         QContext.setQSession(getChickenAndEggSession());

         ///////////////////////////////////////////////////////////////////////////////////////
         // fetch the application from database, will throw accesstokenexception if not found //
         ///////////////////////////////////////////////////////////////////////////////////////
         QRecord clientAuth0Application = getClientAuth0Application(auth0MetaData, clientId);

         /////////////////////////////////////////////////////////////////////////////////////////////////
         // request access token from auth0 if exception is not thrown, that means 200OK, we want to    //
         // store the actual access token in the database, and return a unique value                    //
         // back to the user which will be what they use on subseqeunt requests (because token too big) //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         JSONObject accessTokenData = requestAccessTokenFromAuth0(auth0MetaData, clientId, clientSecret);

         Integer expiresInSeconds = accessTokenData.getInt("expires_in");
         String  accessToken      = accessTokenData.getString("access_token");
         String  uuid             = UUID.randomUUID().toString();

         /////////////////////////////////
         // store the details in the db //
         /////////////////////////////////
         QRecord accessTokenRecord = new QRecord()
            .withValue(auth0MetaData.getClientAuth0ApplicationIdField(), clientAuth0Application.getValue("id"))
            .withValue(auth0MetaData.getAuth0AccessTokenField(), accessToken)
            .withValue(auth0MetaData.getQqqAccessTokenField(), uuid)
            .withValue(auth0MetaData.getExpiresInSecondsField(), expiresInSeconds);
         InsertInput input = new InsertInput();
         input.setTableName(auth0MetaData.getAccessTokenTableName());
         input.setRecords(List.of(accessTokenRecord));
         new InsertAction().execute(input);

         //////////////////////////////////
         // update and send the response //
         //////////////////////////////////
         accessTokenData.put("access_token", uuid);
         accessTokenData.remove("scope");
         return (accessTokenData.toString());
      }
      catch(AccessTokenException ate)
      {
         throw (ate);
      }
      catch(Exception e)
      {
         throw (new AccessTokenException(e.getMessage(), e));
      }
      finally
      {
         QContext.setQSession(sessionBefore);
      }
   }



   /*******************************************************************************
    ** make http request to Auth0 for a new access token
    **
    *******************************************************************************/
   public JSONObject requestAccessTokenFromAuth0(Auth0AuthenticationMetaData auth0MetaData, String clientId, String clientSecret) throws AccessTokenException
   {
      ///////////////////////////////////////////////////////////////////
      // make a request to Auth0 using the client_id and client_secret //
      ///////////////////////////////////////////////////////////////////
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(List.of(
            new BasicNameValuePair("content-type", "application/x-www-form-urlencoded"),
            new BasicNameValuePair("grant_type", "client_credentials"),
            new BasicNameValuePair("audience", auth0MetaData.getAudience()),
            new BasicNameValuePair("client_id", clientId),
            new BasicNameValuePair("client_secret", clientSecret)));

         HttpPost request = new HttpPost(auth0MetaData.getBaseUrl() + "oauth/token");
         request.setEntity(urlEncodedFormEntity);

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            int    statusCode = response.getStatusLine().getStatusCode();
            String content    = EntityUtils.toString(response.getEntity());

            //////////////////////////////////////
            // if 200OK, return the json object //
            //////////////////////////////////////
            if(statusCode == 200)
            {
               return (JsonUtils.toJSONObject(content));
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if not 200, throw an access token exception with the message and status code of the non-200 response //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            throw (new AccessTokenException(content, statusCode));
         }
      }
      catch(AccessTokenException ate)
      {
         throw (ate);
      }
      catch(Exception e)
      {
         throw (new AccessTokenException(e.getMessage(), e));
      }
   }



   /*******************************************************************************
    ** Look up access_token record, return if found.
    **
    *******************************************************************************/
   String lookupActualAccessToken(Auth0AuthenticationMetaData metaData, String qqqAccessToken)
   {
      String   accessToken   = null;
      QSession beforeSession = QContext.getQSession();

      try
      {
         QContext.setQSession(getChickenAndEggSession());

         //////////////////////////////////////////////////////////////////////////////////////
         // try to look up existing auth0 application from database, insert one if not found //
         //////////////////////////////////////////////////////////////////////////////////////
         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(metaData.getAccessTokenTableName());
         queryInput.setFilter(new QQueryFilter(new QFilterCriteria(metaData.getQqqAccessTokenField(), QCriteriaOperator.EQUALS, qqqAccessToken)));
         QueryOutput queryOutput = new QueryAction().execute(queryInput);

         if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
         {
            accessToken = queryOutput.getRecords().get(0).getValueString(metaData.getAuth0AccessTokenField());
         }
      }
      catch(Exception e)
      {
         LOG.warn("Could not find Auth0 access token for provided qqq access token", e);
      }
      finally
      {
         QContext.setQSession(beforeSession);
      }

      return (accessToken);
   }



   /*******************************************************************************
    ** Look up client_auth0_application record, return if found.
    **
    *******************************************************************************/
   QRecord getClientAuth0Application(Auth0AuthenticationMetaData metaData, String clientId) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////
      // try to look up existing auth0 application from database, insert one if not found //
      //////////////////////////////////////////////////////////////////////////////////////
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(metaData.getClientAuth0ApplicationTableName());
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria(metaData.getAuth0ClientIdField(), QCriteriaOperator.EQUALS, clientId)));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
      {
         return (queryOutput.getRecords().get(0));
      }

      throw (new AccessTokenException("This client has not been configured to use the API.", HttpStatus.SC_UNAUTHORIZED));
   }

}
