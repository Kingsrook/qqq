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

package com.kingsrook.sampleapp.metadata;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.OAuth2AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.metadata.RedirectStateMetaDataProducer;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.model.UserSession;


/*******************************************************************************
 ** Provides all OAuth2 authentication related metadata to the QQQ engine
 *
 *******************************************************************************/
public class OAuth2MetaDataProvider implements MetaDataProducerInterface<QAuthenticationMetaData>
{
   public static final String NAME = "OAuth2";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QAuthenticationMetaData produce(QInstance qInstance) throws QException
   {
      QMetaDataVariableInterpreter qMetaDataVariableInterpreter = new QMetaDataVariableInterpreter();

      String oauth2BaseUrl      = qMetaDataVariableInterpreter.interpret("${env.OAUTH2_BASE_URL}");
      String oauth2ClientId     = qMetaDataVariableInterpreter.interpret("${env.OAUTH2_CLIENT_ID}");
      String oauth2ClientSecret = qMetaDataVariableInterpreter.interpret("${env.OAUTH2_CLIENT_SECRET}");
      String oauth2Scopes       = qMetaDataVariableInterpreter.interpret("${env.OAUTH2_SCOPES}");

      return (new OAuth2AuthenticationMetaData()
         .withBaseUrl(oauth2BaseUrl)
         .withClientId(oauth2ClientId)
         .withClientSecret(oauth2ClientSecret)
         .withScopes(oauth2Scopes)
         .withUserSessionTableName(UserSession.TABLE_NAME)
         .withRedirectStateTableName(RedirectStateMetaDataProducer.TABLE_NAME)
         .withName(NAME));
   }
}
