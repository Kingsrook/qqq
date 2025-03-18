/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.OAuth2AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.model.UserSession;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Implementation of OAuth2 authentication.
 *******************************************************************************/
public class OAuth2AuthenticationModule implements QAuthenticationModuleInterface
{
   private static final QLogger LOG = QLogger.getLogger(OAuth2AuthenticationModule.class);

   private static boolean mayMemoize = true;

   private static final Memoization<String, String> getAccessTokenFromSessionUUIDMemoization = new Memoization<String, String>()
      .withTimeout(Duration.of(1, ChronoUnit.MINUTES))
      .withMaxSize(1000);

   // todo wip
   private static Map<String, String> stateToRedirectUrl = new HashMap<>();



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QSession createSession(QInstance qInstance, Map<String, String> context) throws QAuthenticationException
   {
      try
      {
         OAuth2AuthenticationMetaData oauth2MetaData = (OAuth2AuthenticationMetaData) qInstance.getAuthentication();

         if(context.containsKey("code") && context.containsKey("state"))
         {
            AuthorizationCode code = new AuthorizationCode(context.get("code"));

            // todo - maybe this comes from lookup of state?
            URI redirectURI = new URI(stateToRedirectUrl.get(context.get("state")));

            ClientSecretBasic      clientSecretBasic = new ClientSecretBasic(new ClientID(oauth2MetaData.getClientId()), new Secret(oauth2MetaData.getClientSecret()));
            AuthorizationCodeGrant codeGrant         = new AuthorizationCodeGrant(code, redirectURI);

            URI           tokenEndpoint = new URI(oauth2MetaData.getTokenUrl());
            TokenRequest  tokenRequest  = new TokenRequest(tokenEndpoint, clientSecretBasic, codeGrant);
            TokenResponse tokenResponse = TokenResponse.parse(tokenRequest.toHTTPRequest().send());

            if(tokenResponse.indicatesSuccess())
            {
               AccessToken accessToken = tokenResponse.toSuccessResponse().getTokens().getAccessToken();
               // todo - what?? RefreshToken refreshToken = tokenResponse.toSuccessResponse().getTokens().getRefreshToken();

               QSession session = createSessionFromToken(accessToken.getValue());
               insertUserSession(accessToken.getValue(), session);
               return (session);
            }
            else
            {
               ErrorObject errorObject = tokenResponse.toErrorResponse().getErrorObject();
               LOG.info("Token request failed", logPair("code", errorObject.getCode()), logPair("description", errorObject.getDescription()));
               throw (new QAuthenticationException(errorObject.getDescription()));
            }
         }
         else if(context.containsKey("code") && context.containsKey("redirectUri") && context.containsKey("codeVerifier"))
         {
            AuthorizationCode  code         = new AuthorizationCode(context.get("code"));
            URI                callback     = new URI(context.get("redirectUri"));
            CodeVerifier       codeVerifier = new CodeVerifier(context.get("codeVerifier"));
            AuthorizationGrant codeGrant    = new AuthorizationCodeGrant(code, callback, codeVerifier);

            ClientID             clientID     = new ClientID(oauth2MetaData.getClientId());
            Secret               clientSecret = new Secret(oauth2MetaData.getClientSecret());
            ClientAuthentication clientAuth   = new ClientSecretBasic(clientID, clientSecret);

            URI          tokenEndpoint = new URI(oauth2MetaData.getTokenUrl());
            Scope        scope         = new Scope("openid profile email offline_access");
            TokenRequest tokenRequest  = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, scope);

            TokenResponse tokenResponse = TokenResponse.parse(tokenRequest.toHTTPRequest().send());

            if(tokenResponse.indicatesSuccess())
            {
               AccessToken accessToken = tokenResponse.toSuccessResponse().getTokens().getAccessToken();
               // todo - what?? RefreshToken refreshToken = tokenResponse.toSuccessResponse().getTokens().getRefreshToken();

               QSession session = createSessionFromToken(accessToken.getValue());
               insertUserSession(accessToken.getValue(), session);
               return (session);
            }
            else
            {
               ErrorObject errorObject = tokenResponse.toErrorResponse().getErrorObject();
               LOG.info("Token request failed", logPair("code", errorObject.getCode()), logPair("description", errorObject.getDescription()));
               throw (new QAuthenticationException(errorObject.getDescription()));
            }
         }
         else if(context.containsKey("sessionUUID") || context.containsKey("sessionId") || context.containsKey("uuid"))
         {
            String uuid = Objects.requireNonNullElseGet(context.get("sessionUUID"), () ->
               Objects.requireNonNullElseGet(context.get("sessionId"), () ->
                  context.get("uuid")));

            String   accessToken = getAccessTokenFromSessionUUID(uuid);
            QSession session     = createSessionFromToken(accessToken);
            session.setUuid(uuid);
            // todo - validate its age or against provider??
            return (session);
         }
         else
         {
            String message = "Did not receive recognized values in context for creating session";
            LOG.warn(message, logPair("contextKeys", context.keySet()));
            throw (new QAuthenticationException(message));
         }
      }
      catch(QAuthenticationException qae)
      {
         throw (qae);
      }
      catch(Exception e)
      {
         throw (new QAuthenticationException("Failed to create session (token)", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean isSessionValid(QInstance instance, QSession session)
   {
      if(session instanceof QSystemUserSession)
      {
         return (true);
      }

      try
      {
         String     accessToken = getAccessTokenFromSessionUUID(session.getUuid());
         DecodedJWT jwt         = JWT.decode(accessToken);
         if(jwt.getExpiresAtAsInstant().isBefore(Instant.now()))
         {
            LOG.debug("accessToken is expired", logPair("sessionUUID", session.getUuid()));
            return (false);
         }

         return true;
      }
      catch(QAuthenticationException e)
      {
         return (false);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getLoginRedirectUrl(String originalUrl)
   {
      QInstance                    qInstance      = QContext.getQInstance();
      OAuth2AuthenticationMetaData oauth2MetaData = (OAuth2AuthenticationMetaData) qInstance.getAuthentication();

      // todo wip - get from meta-data or from that thing that knows the other things?
      String authUrl = oauth2MetaData.getTokenUrl().replace("token", "authorize");

      String state = UUID.randomUUID().toString();
      stateToRedirectUrl.put(state, originalUrl);

      return authUrl +
         "?client_id=" + URLEncoder.encode(oauth2MetaData.getClientId(), StandardCharsets.UTF_8) +
         "&redirect_uri=" + URLEncoder.encode(originalUrl, StandardCharsets.UTF_8) +
         "&response_type=code" +
         "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8) +
         "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private QSession createSessionFromToken(String accessToken) throws QException
   {
      DecodedJWT     jwt           = JWT.decode(accessToken);
      Base64.Decoder decoder       = Base64.getUrlDecoder();
      String         payloadString = new String(decoder.decode(jwt.getPayload()));
      JSONObject     payload       = new JSONObject(payloadString);

      QSession session = new QSession();
      QUser    user    = new QUser();
      session.setUser(user);

      user.setFullName("Unknown");
      String email = Objects.requireNonNullElseGet(payload.optString("email", null), () -> payload.optString("sub", null));
      String name  = payload.optString("name", email);

      user.setIdReference(email);
      user.setFullName(name);

      ////////////////////////////////////////////////////////////
      // todo - this needs to be much better standardized w/ fe //
      ////////////////////////////////////////////////////////////
      session.withValueForFrontend("user", new HashMap<>(Map.of("name", name, "email", email)));

      return session;
   }



   /*******************************************************************************
    ** Insert a session as a new record into userSession table
    *******************************************************************************/
   private void insertUserSession(String accessToken, QSession qSession) throws QException
   {
      CapturedContext capturedContext = QContext.capture();
      try
      {
         QContext.init(capturedContext.qInstance(), new QSystemUserSession());

         UserSession userSession = new UserSession()
            .withUuid(qSession.getUuid())
            .withUserId(qSession.getUser().getIdReference())
            .withAccessToken(accessToken);

         new InsertAction().execute(new InsertInput(UserSession.TABLE_NAME).withRecordEntity(userSession));
      }
      finally
      {
         QContext.init(capturedContext);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QSession createAutomatedSessionForUser(QInstance qInstance, Serializable userId) throws QAuthenticationException
   {
      return QAuthenticationModuleInterface.super.createAutomatedSessionForUser(qInstance, userId);
   }



   /*******************************************************************************
    ** Look up access_token from session UUID
    **
    *******************************************************************************/
   private String getAccessTokenFromSessionUUID(String sessionUUID) throws QAuthenticationException
   {
      if(mayMemoize)
      {
         return getAccessTokenFromSessionUUIDMemoization.getResultThrowing(sessionUUID, (String x) ->
            doGetAccessTokenFromSessionUUID(sessionUUID)
         ).orElse(null);
      }
      else
      {
         return (doGetAccessTokenFromSessionUUID(sessionUUID));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String doGetAccessTokenFromSessionUUID(String sessionUUID) throws QAuthenticationException
   {
      String   accessToken   = null;
      QSession beforeSession = QContext.getQSession();

      try
      {
         QContext.setQSession(new QSystemUserSession());

         ///////////////////////////////////////
         // query for the user session record //
         ///////////////////////////////////////
         QRecord userSessionRecord = new GetAction().executeForRecord(new GetInput(UserSession.TABLE_NAME)
            .withUniqueKey(Map.of("uuid", sessionUUID))
            .withShouldMaskPasswords(false)
            .withShouldOmitHiddenFields(false));

         if(userSessionRecord != null)
         {
            accessToken = userSessionRecord.getValueString("accessToken");

            ////////////////////////////////////////////////////////////
            // decode the accessToken and make sure it is not expired //
            ////////////////////////////////////////////////////////////
            if(accessToken != null)
            {
               DecodedJWT jwt = JWT.decode(accessToken);
               if(jwt.getExpiresAtAsInstant().isBefore(Instant.now()))
               {
                  throw (new QAuthenticationException("accessToken is expired"));
               }
            }
         }
      }
      catch(QAuthenticationException qae)
      {
         throw (qae);
      }
      catch(Exception e)
      {
         LOG.warn("Error looking up userSession by sessionUUID", e);
         throw (new QAuthenticationException("Error looking up userSession by sessionUUID", e));
      }
      finally
      {
         QContext.setQSession(beforeSession);
      }

      return (accessToken);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean usesSessionIdCookie()
   {
      return (true);
   }

}
