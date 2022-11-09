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

package com.kingsrook.qqq.backend.module.api;


import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.authentication.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.module.api.model.AuthorizationType;
import com.kingsrook.qqq.backend.module.api.model.metadata.APIBackendMetaData;
import com.kingsrook.qqq.backend.module.api.model.metadata.APITableBackendDetails;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String EASYPOST_BACKEND_NAME = "easypost";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTableEasypostTracker());
      qInstance.setAuthentication(defineAuthentication());
      return (qInstance);
   }



   /*******************************************************************************
    ** Define the authentication used in standard tests - using 'mock' type.
    **
    *******************************************************************************/
   public static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("mock")
         .withType(QAuthenticationType.MOCK);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendMetaData defineBackend()
   {
      String apiKey = new QMetaDataVariableInterpreter().interpret("${env.EASYPOST_API_KEY}");

      return (new APIBackendMetaData()
         .withName("easypost")
         .withApiKey(apiKey)
         .withAuthorizationType(AuthorizationType.BASIC_AUTH_API_KEY)
         .withBaseUrl("https://api.easypost.com/v2/")
         .withContentType("application/json")
         .withActionUtil(new QCodeReference(EasyPostUtils.class, QCodeUsage.CUSTOMIZER))
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData defineTableEasypostTracker()
   {
      return (new QTableMetaData()
         .withName("easypostTracker")
         .withBackendName("easypost")
         .withField(new QFieldMetaData("id", QFieldType.STRING))
         .withField(new QFieldMetaData("trackingNo", QFieldType.STRING).withBackendName("tracking_code"))
         .withField(new QFieldMetaData("carrier", QFieldType.STRING).withBackendName("carrier"))
         .withPrimaryKeyField("id")
         .withBackendDetails(new APITableBackendDetails()
            .withTablePath("trackers")
            .withTableWrapperObjectName("tracker")
         )
      );
   }
}
