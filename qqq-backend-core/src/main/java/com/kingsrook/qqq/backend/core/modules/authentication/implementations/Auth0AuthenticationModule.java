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
import java.util.Map;
import java.util.Optional;
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
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.SimpleStateKey;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class Auth0AuthenticationModule implements QAuthenticationModuleInterface
{
   private static final Logger LOG = LogManager.getLogger(Auth0AuthenticationModule.class);

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // 30 minutes - ideally this would be lower, but right now we've been dealing with re-validation issues... //
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public static final int ID_TOKEN_VALIDATION_INTERVAL_SECONDS = 1800;

   public static final String AUTH0_ID_TOKEN_KEY = "sessionId";
   public static final String BASIC_AUTH_KEY     = "basicAuthString";

   public static final String TOKEN_NOT_PROVIDED_ERROR = "Id Token was not provided";
   public static final String COULD_NOT_DECODE_ERROR   = "Unable to decode id token";
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
            String idToken           = getIdTokenFromBase64BasicAuthCredentials(auth, base64Credentials);
            context.put(AUTH0_ID_TOKEN_KEY, idToken);
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

      //////////////////////////////////////////////////
      // get the jwt id token from the context object //
      //////////////////////////////////////////////////
      String idToken = context.get(AUTH0_ID_TOKEN_KEY);
      if(idToken == null)
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
         QSession qSession = buildQSessionFromToken(idToken);
         if(isSessionValid(qInstance, qSession))
         {
            return (qSession);
         }

         ///////////////////////////////////////////////////////////////////////////////////////
         // if we make it here it means we have never validated this token or its been a long //
         // enough duration so we need to re-verify the token                                 //
         ///////////////////////////////////////////////////////////////////////////////////////
         qSession = revalidateToken(qInstance, idToken);

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
   private String getIdTokenFromBase64BasicAuthCredentials(AuthAPI auth, String base64Credentials) throws Auth0Exception
   {
      ////////////////////////////////////////////////////////////////////////////////
      // look for a fresh idToken in the state provider for this set of credentials //
      ////////////////////////////////////////////////////////////////////////////////
      SimpleStateKey<String> idTokenStateKey   = new SimpleStateKey<>(base64Credentials + ":idToken");
      SimpleStateKey<String> timestampStateKey = new SimpleStateKey<>(base64Credentials + ":timestamp");
      StateProviderInterface stateProvider     = getStateProvider();
      Optional<String>       cachedIdToken     = stateProvider.get(String.class, idTokenStateKey);
      Optional<Instant>      cachedTimestamp   = stateProvider.get(Instant.class, timestampStateKey);
      if(cachedIdToken.isPresent() && cachedTimestamp.isPresent())
      {
         if(cachedTimestamp.get().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)))
         {
            return cachedIdToken.get();
         }
      }

      //////////////////////////////////////////////////////////////////////////////
      // not found in cache, make request to auth0 and cache the returned idToken //
      //////////////////////////////////////////////////////////////////////////////
      byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
      String credentials = new String(credDecoded, StandardCharsets.UTF_8);

      String idToken = getIdTokenFromAuth0(auth, credentials);
      stateProvider.put(idTokenStateKey, idToken);
      stateProvider.put(timestampStateKey, Instant.now());
      return (idToken);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getIdTokenFromAuth0(AuthAPI auth, String credentials) throws Auth0Exception
   {
      /////////////////////////////////////
      // call auth0 with a login request //
      /////////////////////////////////////
      TokenHolder result = auth.login(credentials.split(":")[0], credentials.split(":")[1].toCharArray())
         .setScope("openid email nickname")
         .execute();

      return (result.getIdToken());
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
   private QSession revalidateToken(QInstance qInstance, String idToken) throws JwkException
   {
      Auth0AuthenticationMetaData metaData = (Auth0AuthenticationMetaData) qInstance.getAuthentication();

      DecodedJWT  jwt       = JWT.decode(idToken);
      JwkProvider provider  = new UrlJwkProvider(metaData.getBaseUrl());
      Jwk         jwk       = provider.get(jwt.getKeyId());
      Algorithm   algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
      JWTVerifier verifier = JWT.require(algorithm)
         .withIssuer(metaData.getBaseUrl())
         .build();

      ///////////////////////////////////
      // make call to verify the token //
      ///////////////////////////////////
      verifier.verify(idToken);

      return (buildQSessionFromToken(idToken));
   }



   /*******************************************************************************
    ** extracts info from token creating a QSession
    **
    *******************************************************************************/
   private QSession buildQSessionFromToken(String idToken) throws JwkException
   {
      ////////////////////////////////////
      // decode and extract the payload //
      ////////////////////////////////////
      DecodedJWT     jwt           = JWT.decode(idToken);
      Base64.Decoder decoder       = Base64.getUrlDecoder();
      String         payloadString = new String(decoder.decode(jwt.getPayload()));
      JSONObject     payload       = new JSONObject(payloadString);

      QUser qUser = new QUser();
      if(payload.has("name"))
      {
         qUser.setFullName(payload.getString("name"));
      }
      else
      {
         qUser.setFullName("Unknown");
      }

      if(payload.has("email"))
      {
         qUser.setIdReference(payload.getString("email"));
      }
      else
      {
         if(payload.has("sub"))
         {
            qUser.setIdReference(payload.getString("sub"));
         }
      }

      QSession qSession = new QSession();
      qSession.setIdReference(idToken);
      qSession.setUser(qUser);

      return (qSession);
   }



   /*******************************************************************************
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   public static StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      return (InMemoryStateProvider.getInstance());
   }

}
