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

package com.kingsrook.qqq.backend.module.api.model.metadata;


import java.io.Serializable;
import java.util.HashMap;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.module.api.APIBackendModule;
import com.kingsrook.qqq.backend.module.api.model.AuthorizationType;


/*******************************************************************************
 ** Meta-data to provide details of an API backend (e.g., connection params)
 *******************************************************************************/
public class APIBackendMetaData extends QBackendMetaData
{
   private String baseUrl;
   private String apiKey;
   private String username;
   private String password;

   private AuthorizationType authorizationType;
   private String            contentType; // todo enum?

   private QCodeReference actionUtil;

   private HashMap<String, Serializable> customValues = new HashMap<>();



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public APIBackendMetaData()
   {
      super();
      setBackendType(APIBackendModule.class);
   }



   /*******************************************************************************
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   @Override
   public APIBackendMetaData withName(String name)
   {
      setName(name);
      return this;
   }

   // todo?
   // /*******************************************************************************
   //  ** Called by the QInstanceEnricher - to do backend-type-specific enrichments.
   //  ** Original use case is:  reading secrets into fields (e.g., passwords).
   //  *******************************************************************************/
   // @Override
   // public void enrich()
   // {
   //    super.enrich();
   //    QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
   //    username = interpreter.interpret(username);
   //    password = interpreter.interpret(password);
   // }



   /*******************************************************************************
    ** Getter for baseUrl
    **
    *******************************************************************************/
   public String getBaseUrl()
   {
      return baseUrl;
   }



   /*******************************************************************************
    ** Setter for baseUrl
    **
    *******************************************************************************/
   public void setBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
   }



   /*******************************************************************************
    ** Fluent setter for baseUrl
    **
    *******************************************************************************/
   public APIBackendMetaData withBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiKey
    **
    *******************************************************************************/
   public String getApiKey()
   {
      return apiKey;
   }



   /*******************************************************************************
    ** Setter for apiKey
    **
    *******************************************************************************/
   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }



   /*******************************************************************************
    ** Fluent setter for apiKey
    **
    *******************************************************************************/
   public APIBackendMetaData withApiKey(String apiKey)
   {
      this.apiKey = apiKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for username
    **
    *******************************************************************************/
   public String getUsername()
   {
      return username;
   }



   /*******************************************************************************
    ** Setter for username
    **
    *******************************************************************************/
   public void setUsername(String username)
   {
      this.username = username;
   }



   /*******************************************************************************
    ** Fluent setter for username
    **
    *******************************************************************************/
   public APIBackendMetaData withUsername(String username)
   {
      this.username = username;
      return (this);
   }



   /*******************************************************************************
    ** Getter for password
    **
    *******************************************************************************/
   public String getPassword()
   {
      return password;
   }



   /*******************************************************************************
    ** Setter for password
    **
    *******************************************************************************/
   public void setPassword(String password)
   {
      this.password = password;
   }



   /*******************************************************************************
    ** Fluent setter for password
    **
    *******************************************************************************/
   public APIBackendMetaData withPassword(String password)
   {
      this.password = password;
      return (this);
   }



   /*******************************************************************************
    ** Getter for authorizationType
    **
    *******************************************************************************/
   public AuthorizationType getAuthorizationType()
   {
      return authorizationType;
   }



   /*******************************************************************************
    ** Setter for authorizationType
    **
    *******************************************************************************/
   public void setAuthorizationType(AuthorizationType authorizationType)
   {
      this.authorizationType = authorizationType;
   }



   /*******************************************************************************
    ** Fluent setter for authorizationType
    **
    *******************************************************************************/
   public APIBackendMetaData withAuthorizationType(AuthorizationType authorizationType)
   {
      this.authorizationType = authorizationType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contentType
    **
    *******************************************************************************/
   public String getContentType()
   {
      return contentType;
   }



   /*******************************************************************************
    ** Setter for contentType
    **
    *******************************************************************************/
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }



   /*******************************************************************************
    ** Fluent setter for contentType
    **
    *******************************************************************************/
   public APIBackendMetaData withContentType(String contentType)
   {
      this.contentType = contentType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for actionUtil
    **
    *******************************************************************************/
   public QCodeReference getActionUtil()
   {
      return actionUtil;
   }



   /*******************************************************************************
    ** Setter for actionUtil
    **
    *******************************************************************************/
   public void setActionUtil(QCodeReference actionUtil)
   {
      this.actionUtil = actionUtil;
   }



   /*******************************************************************************
    ** Fluent setter for actionUtil
    **
    *******************************************************************************/
   public APIBackendMetaData withActionUtil(QCodeReference actionUtil)
   {
      this.actionUtil = actionUtil;
      return (this);
   }



   /*******************************************************************************
    ** Getter for customValues
    **
    *******************************************************************************/
   public HashMap<String, Serializable> getCustomValues()
   {
      return customValues;
   }



   /*******************************************************************************
    ** Getter for a single customValue
    **
    *******************************************************************************/
   public Serializable getCustomValue(String key)
   {
      return customValues.get(key);
   }



   /*******************************************************************************
    ** Setter for customValues
    **
    *******************************************************************************/
   public void setCustomValues(HashMap<String, Serializable> customValues)
   {
      this.customValues = customValues;
   }



   /*******************************************************************************
    ** Fluent setter for customValues
    **
    *******************************************************************************/
   public APIBackendMetaData withCustomValues(HashMap<String, Serializable> customValues)
   {
      this.customValues = customValues;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single customValue
    **
    *******************************************************************************/
   public APIBackendMetaData withCustomValue(String key, Serializable value)
   {
      this.customValues.put(key, value);
      return (this);
   }

}
