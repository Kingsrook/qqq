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
import com.auth0.jwt.interfaces.JWTVerifier;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.SimpleStateKey;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
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


   private Instant now;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSession createSession(QInstance qInstance, Map<String, String> context) throws QAuthenticationException
   {
      ///////////////////////////////////////////////////////////
      // check if we are processing a Basic Auth Session first //
      ///////////////////////////////////////////////////////////
      if(context.containsKey(BASIC_AUTH_KEY))
      {
         Auth0AuthenticationMetaData metaData = (Auth0AuthenticationMetaData) qInstance.getAuthentication();
         AuthAPI                     auth     = new AuthAPI(metaData.getBaseUrl(), metaData.getClientId(), metaData.getClientSecret());
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

      //////////////////////////////////////////////////////
      // get the jwt access token from the context object //
      //////////////////////////////////////////////////////
      String accessToken = context.get(AUTH0_ACCESS_TOKEN_KEY);
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
         qSession = revalidateToken(qInstance, accessToken);

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
      TokenHolder result = auth.login(credentials.split(":")[0], credentials.split(":")[1].toCharArray())
         .setScope("openid email nickname")
         .setAudience(metaData.getAudience())
         .execute();

      return (result.getAccessToken());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean isSessionValid(QInstance instance, QSession session)
   {
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
            revalidateToken(instance, session.getIdReference());

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
   private QSession revalidateToken(QInstance qInstance, String accessToken) throws JwkException
   {
      Auth0AuthenticationMetaData metaData = (Auth0AuthenticationMetaData) qInstance.getAuthentication();

      DecodedJWT  jwt       = JWT.decode(accessToken);
      JwkProvider provider  = new UrlJwkProvider(metaData.getBaseUrl());
      Jwk         jwk       = provider.get(jwt.getKeyId());
      Algorithm   algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
      JWTVerifier verifier = JWT.require(algorithm)
         .withIssuer(metaData.getBaseUrl())
         .build();

      ///////////////////////////////////
      // make call to verify the token //
      ///////////////////////////////////
      verifier.verify(accessToken);

      return (buildQSessionFromToken(accessToken, qInstance));
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
      for(String key : List.of("name", "com.kingsrook.qqq.name"))
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
         String value = securityKeyValues.optString(jsonKey);
         if(value != null)
         {
            qSession.withSecurityKeyValue(securityKeyName, value);
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

}
