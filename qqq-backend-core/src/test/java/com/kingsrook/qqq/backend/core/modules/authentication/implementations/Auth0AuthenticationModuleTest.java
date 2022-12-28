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


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule.AUTH0_ID_TOKEN_KEY;
import static com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule.COULD_NOT_DECODE_ERROR;
import static com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule.EXPIRED_TOKEN_ERROR;
import static com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule.INVALID_TOKEN_ERROR;
import static com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule.TOKEN_NOT_PROVIDED_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for the FullyAnonymousAuthenticationModule
 *******************************************************************************/
public class Auth0AuthenticationModuleTest
{
   private static final String VALID_TOKEN       = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IllrY2FkWTA0Q3RFVUFxQUdLNTk3ayJ9.eyJnaXZlbl9uYW1lIjoiVGltIiwiZmFtaWx5X25hbWUiOiJDaGFtYmVybGFpbiIsIm5pY2tuYW1lIjoidGltLmNoYW1iZXJsYWluIiwibmFtZSI6IlRpbSBDaGFtYmVybGFpbiIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS0vQUZkWnVjcXVSaUFvTzk1RG9URklnbUtseVA1akVBVnZmWXFnS0lHTkVubzE9czk2LWMiLCJsb2NhbGUiOiJlbiIsInVwZGF0ZWRfYXQiOiIyMDIyLTA3LTE5VDE2OjI0OjQ1LjgyMloiLCJlbWFpbCI6InRpbS5jaGFtYmVybGFpbkBraW5nc3Jvb2suY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImlzcyI6Imh0dHBzOi8va2luZ3Nyb29rLnVzLmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEwODk2NDEyNjE3MjY1NzAzNDg2NyIsImF1ZCI6InNwQ1NtczAzcHpVZGRYN1BocHN4ZDlUd2FLMDlZZmNxIiwiaWF0IjoxNjU4MjQ3OTAyLCJleHAiOjE2NTgyODM5MDIsIm5vbmNlIjoiZUhOdFMxbEtUR2N5ZG5KS1VVY3RkRTFVT0ZKNmJFNUxVVkEwZEdsRGVXOXZkVkl4UW41eVRrUlJlZz09In0.hib7JR8NDU2kx8Fj1bnzo3IUuabE6Hb-Z7HHZAJPQuF_Zdg3L1KDypn6SY7HAd_dsz2N8RkXfvQto-Y2g2ukuz7FxzNFgcVL99cyEO3YqmyCa6JTOTCrxdeaIE8QZpCEKvC28oeJBv0wO1Dwc--OVJMsK2vSzyxj1WNok64YYjWKLL4c0dFf-nj0KWFr1IU-tMiyWLDDiJw2Sa8M4YxXZYqdlkgNmrBPExgcm9l9SiT2l3Ts3Sgc_IyMVyMrnV8XX50EWdsm6vuCOSUcqf0XhjDQ7urZveoVwVLnYq3GcLhVBcy1Hr9RL8zPdPynOzsbX6uCww2Esrv6iwWrgQ5zBA";
   private static final String INVALID_TOKEN     = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IllrY2FkWTA0Q3RFVUFxQUdLNTk3ayJ9.eyJnaXZlbl9uYW1lIjoiVGltIiwiZmFtaWx5X25hbWUiOiJDaGFtYmVybGFpbiIsIm5pY2tuYW1lIjoidGltLmNoYW1iZXJsYWluIiwibmFtZSI6IlRpbSBDaGFtYmVybGFpbiIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS0vQUZkWnVjcXVSaUFvTzk1RG9URklnbUtseVA1akVBVnZmWXFnS0lHTkVubzE9czk2LWMiLCJsb2NhbGUiOiJlbiIsInVwZGF0ZWRfYXQiOiIyMDIyLTA3LTE5VDE2OjI0OjQ1LjgyMloiLCJlbWFpbCI6InRpbS5jaGFtYmVybGFpbkBraW5nc3Jvb2suY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImlzcyI6Imh0dHBzOi8va2luZ3Nyb29rLnVzLmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEwODk2NDEyNjE3MjY1NzAzNDg2NyIsImF1ZCI6InNwQ1NtczAzcHpVZGRYN1BocHN4ZDlUd2FLMDlZZmNxIiwiaWF0IjoxNjU4MjQ3OTAyLCJleHAiOjE2NTgyODM5MDIsIm5vbmNlIjoiZUhOdFMxbEtUR2N5ZG5KS1VVY3RkRTFVT0ZKNmJFNUxVVkEwZEdsRGVXOXZkVkl4UW41eVRrUlJlZz09In0.hib7JR8NDU2kx8Fj1bnzo3IUuabE6Hb-Z7HHZAJPQuF_Zdg3L1KDypn6SY7HAd_dsz2N8RkXfvQto-Y2g2ukuz7FxzNFgcVL99cyEO3YqmyCa6JTOTCrxdeaIE8QZpCEKvC28oeJBv0wO1Dwc--OVJMsK2vSzyxj1WNok64YYjWKLL4c0dFf-nj0KWFr1IU-tMiyWLDDiJw2Sa8M4YxXZYqdlkgNmrBPExgcm9l9SiT2l3Ts3Sgc_IyMVyMrnV8XX50EWdsm6vuCOSUcqf0XhjDQ7urZveoVwVLnYq3GcLhVBcy1Hr9RL8zPdPynOzsbX6uCww2Esrv6iwWrgQ5zBA-thismakesinvalid";
   private static final String EXPIRED_TOKEN     = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IllrY2FkWTA0Q3RFVUFxQUdLNTk3ayJ9.eyJnaXZlbl9uYW1lIjoiVGltIiwiZmFtaWx5X25hbWUiOiJDaGFtYmVybGFpbiIsIm5pY2tuYW1lIjoidGltLmNoYW1iZXJsYWluIiwibmFtZSI6IlRpbSBDaGFtYmVybGFpbiIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS0vQUZkWnVjcXVSaUFvTzk1RG9URklnbUtseVA1akVBVnZmWXFnS0lHTkVubzE9czk2LWMiLCJsb2NhbGUiOiJlbiIsInVwZGF0ZWRfYXQiOiIyMDIyLTA3LTE4VDIxOjM4OjE1LjM4NloiLCJlbWFpbCI6InRpbS5jaGFtYmVybGFpbkBraW5nc3Jvb2suY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImlzcyI6Imh0dHBzOi8va2luZ3Nyb29rLnVzLmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEwODk2NDEyNjE3MjY1NzAzNDg2NyIsImF1ZCI6InNwQ1NtczAzcHpVZGRYN1BocHN4ZDlUd2FLMDlZZmNxIiwiaWF0IjoxNjU4MTgwNDc3LCJleHAiOjE2NTgyMTY0NzcsIm5vbmNlIjoiVkZkQlYzWmplR2hvY1cwMk9WZEtabHBLU0c1K1ZXbElhMEV3VkZaeFpVdEJVMDErZUZaT1RtMTNiZz09In0.fU7EwUgNrupOPz_PX_aQKON2xG1-LWD85xVo1Bn41WNEek-iMyJoch8l6NUihi7Bou14BoOfeWIG_sMqsLHqI2Pk7el7l1kigsjURx0wpiXadBt8piMxdIlxdToZEMuZCBzg7eJvXh4sM8tlV5cm0gPa6FT9Ih3VGJajNlXi5BcYS_JRpIvFvHn8-Bxj4KiAlZ5XPPkopjnDgP8kFfc4cMn_nxDkqWYlhj-5TaGW2xCLC9Qr_9UNxX0fm-CkKjYs3Z5ezbiXNkc-bxrCYvxeBeDPf8-T3EqrxCRVqCZSJ85BHdOc_E7UZC_g8bNj0umoplGwlCbzO4XIuOO-KlIaOg";
   private static final String UNDECODABLE_TOKEN = "UNDECODABLE";

   public static final String AUTH0_BASE_URL = "https://kingsrook.us.auth0.com/";



   /*******************************************************************************
    ** Test a token where last-checked is set to a time that would not require it to be
    ** re-checked, so it'll show as valid no matter what the token is.
    **
    *******************************************************************************/
   @Test
   public void testLastTimeCheckedJustUnderThreshold()
   {
      Instant underThreshold = Instant.now().minus(Auth0AuthenticationModule.ID_TOKEN_VALIDATION_INTERVAL_SECONDS - 60, ChronoUnit.SECONDS);
      assertTrue(testLastTimeChecked(underThreshold, INVALID_TOKEN), "A session checked under threshold should be valid");
   }



   /*******************************************************************************
    ** Test a token where last-checked is set to a time that would require it to be
    ** re-checked, so it'll show as invalid.
    **
    *******************************************************************************/
   @Test
   public void testLastTimeCheckedJustOverThreshold()
   {
      Instant overThreshold = Instant.now().minus(Auth0AuthenticationModule.ID_TOKEN_VALIDATION_INTERVAL_SECONDS + 60, ChronoUnit.SECONDS);
      assertFalse(testLastTimeChecked(overThreshold, INVALID_TOKEN), "A session checked over threshold should be re-validated, and in this case, not be valid.");
   }



   /*******************************************************************************
    ** Test a token where last-checked is past the threshold, so it'll get re-checked,
    ** and will fail.
    **
    *******************************************************************************/
   @Test
   public void testLastTimeCheckedOverThresholdAndUndecodable()
   {
      Instant overThreshold = Instant.now().minus(Auth0AuthenticationModule.ID_TOKEN_VALIDATION_INTERVAL_SECONDS + 60, ChronoUnit.SECONDS);
      assertFalse(testLastTimeChecked(overThreshold, UNDECODABLE_TOKEN), "A session checked over threshold should be re-validated, and in this case, not be valid.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean testLastTimeChecked(Instant lastTimeChecked, String token)
   {
      /////////////////////////////////////////////////////////////
      // put the input last-time-checked into the state provider //
      /////////////////////////////////////////////////////////////
      Auth0AuthenticationModule.Auth0StateKey key = new Auth0AuthenticationModule.Auth0StateKey(token);
      InMemoryStateProvider.getInstance().put(key, lastTimeChecked);

      //////////////////////
      // build up session //
      //////////////////////
      QSession session = new QSession();
      session.setIdReference(token);

      Auth0AuthenticationModule auth0AuthenticationModule = new Auth0AuthenticationModule();
      return (auth0AuthenticationModule.isSessionValid(getQInstance(), session));
   }



   /*******************************************************************************
    ** Test failure case, token is invalid
    **
    *******************************************************************************/
   @Test
   public void testInvalidToken()
   {
      Map<String, String> context = new HashMap<>();
      context.put(AUTH0_ID_TOKEN_KEY, INVALID_TOKEN);

      try
      {
         Auth0AuthenticationModule auth0AuthenticationModule = new Auth0AuthenticationModule();
         auth0AuthenticationModule.createSession(getQInstance(), context);
         fail("Should never get here");
      }
      catch(QAuthenticationException qae)
      {
         assertThat(qae.getMessage()).contains(INVALID_TOKEN_ERROR);
      }
   }



   /*******************************************************************************
    ** Test failure case, token can't be decoded
    **
    *******************************************************************************/
   @Test
   public void testUndecodableToken()
   {
      Map<String, String> context = new HashMap<>();
      context.put(AUTH0_ID_TOKEN_KEY, UNDECODABLE_TOKEN);

      try
      {
         Auth0AuthenticationModule auth0AuthenticationModule = new Auth0AuthenticationModule();
         auth0AuthenticationModule.createSession(getQInstance(), context);
         fail("Should never get here");
      }
      catch(QAuthenticationException qae)
      {
         assertThat(qae.getMessage()).contains(COULD_NOT_DECODE_ERROR);
      }
   }



   /*******************************************************************************
    ** Test failure case, token is expired
    **
    *******************************************************************************/
   @Test
   public void testProperlyFormattedButExpiredToken()
   {
      Map<String, String> context = new HashMap<>();
      context.put(AUTH0_ID_TOKEN_KEY, EXPIRED_TOKEN);

      try
      {
         Auth0AuthenticationModule auth0AuthenticationModule = new Auth0AuthenticationModule();
         auth0AuthenticationModule.createSession(getQInstance(), context);
         fail("Should never get here");
      }
      catch(QAuthenticationException qae)
      {
         assertThat(qae.getMessage()).contains(EXPIRED_TOKEN_ERROR);
      }
   }



   /*******************************************************************************
    ** Test failure case, empty context
    **
    *******************************************************************************/
   @Test
   public void testEmptyContext()
   {
      try
      {
         Auth0AuthenticationModule auth0AuthenticationModule = new Auth0AuthenticationModule();
         auth0AuthenticationModule.createSession(getQInstance(), new HashMap<>());
         fail("Should never get here");
      }
      catch(QAuthenticationException qae)
      {
         assertThat(qae.getMessage()).contains(TOKEN_NOT_PROVIDED_ERROR);
      }
   }



   /*******************************************************************************
    ** Test failure case, null token
    **
    *******************************************************************************/
   @Test
   public void testNullToken()
   {
      Map<String, String> context = new HashMap<>();
      context.put(AUTH0_ID_TOKEN_KEY, null);

      try
      {
         Auth0AuthenticationModule auth0AuthenticationModule = new Auth0AuthenticationModule();
         auth0AuthenticationModule.createSession(getQInstance(), context);
         fail("Should never get here");
      }
      catch(QAuthenticationException qae)
      {
         assertThat(qae.getMessage()).contains(TOKEN_NOT_PROVIDED_ERROR);
      }
   }



   /*******************************************************************************
    ** utility method to prime a qInstance for auth0 tests
    **
    *******************************************************************************/
   private QInstance getQInstance()
   {
      QAuthenticationMetaData authenticationMetaData = new Auth0AuthenticationMetaData()
         .withBaseUrl(AUTH0_BASE_URL)
         .withName("auth0");

      QInstance qInstance = TestUtils.defineInstance();
      qInstance.setAuthentication(authenticationMetaData);
      return (qInstance);
   }

}
