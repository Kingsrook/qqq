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

package com.kingsrook.qqq.backend.core.model.metadata.authentication;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.OAuth2AuthenticationModule;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Meta-data to provide details of an OAuth2 Authentication module
 *******************************************************************************/
public class OAuth2AuthenticationMetaData extends QAuthenticationMetaData
{
   private String baseUrl;
   private String tokenUrl;
   private String clientId;

   private String userSessionTableName;
   private String redirectStateTableName;

   ////////////////////////////////////////////////////////////////////////////////////////
   // keep this secret, on the server - don't let it be serialized and sent to a client! //
   ////////////////////////////////////////////////////////////////////////////////////////
   @JsonIgnore
   private String clientSecret;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public OAuth2AuthenticationMetaData()
   {
      super();
      setType(QAuthenticationType.OAUTH2);

      //////////////////////////////////////////////////////////
      // ensure this module is registered with the dispatcher //
      //////////////////////////////////////////////////////////
      QAuthenticationModuleDispatcher.registerModule(QAuthenticationType.OAUTH2.getName(), OAuth2AuthenticationModule.class.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void validate(QInstance qInstance, QInstanceValidator qInstanceValidator)
   {
      super.validate(qInstance, qInstanceValidator);

      String prefix = "OAuth2AuthenticationMetaData (named '" + getName() + "'): ";

      qInstanceValidator.assertCondition(StringUtils.hasContent(baseUrl), prefix + "baseUrl must be set");
      qInstanceValidator.assertCondition(StringUtils.hasContent(clientId), prefix + "clientId must be set");
      qInstanceValidator.assertCondition(StringUtils.hasContent(clientSecret), prefix + "clientSecret must be set");

      if(qInstanceValidator.assertCondition(StringUtils.hasContent(userSessionTableName), prefix + "userSessionTableName must be set"))
      {
         qInstanceValidator.assertCondition(qInstance.getTable(userSessionTableName) != null, prefix + "userSessionTableName ('" + userSessionTableName + "') was not found in the instance");
      }

      if(qInstanceValidator.assertCondition(StringUtils.hasContent(redirectStateTableName), prefix + "redirectStateTableName must be set"))
      {
         qInstanceValidator.assertCondition(qInstance.getTable(redirectStateTableName) != null, prefix + "redirectStateTableName ('" + redirectStateTableName + "') was not found in the instance");
      }
   }



   /*******************************************************************************
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   public OAuth2AuthenticationMetaData withBaseUrl(String baseUrl)
   {
      setBaseUrl(baseUrl);
      return this;
   }



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
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   public OAuth2AuthenticationMetaData withClientId(String clientId)
   {
      setClientId(clientId);
      return this;
   }



   /*******************************************************************************
    ** Getter for clientId
    **
    *******************************************************************************/
   public String getClientId()
   {
      return clientId;
   }



   /*******************************************************************************
    ** Setter for clientId
    **
    *******************************************************************************/
   public void setClientId(String clientId)
   {
      this.clientId = clientId;
   }



   /*******************************************************************************
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   public OAuth2AuthenticationMetaData withClientSecret(String clientSecret)
   {
      setClientSecret(clientSecret);
      return this;
   }



   /*******************************************************************************
    ** Getter for clientSecret
    **
    *******************************************************************************/
   public String getClientSecret()
   {
      return clientSecret;
   }



   /*******************************************************************************
    ** Setter for clientSecret
    **
    *******************************************************************************/
   public void setClientSecret(String clientSecret)
   {
      this.clientSecret = clientSecret;
   }



   /*******************************************************************************
    ** Getter for tokenUrl
    *******************************************************************************/
   public String getTokenUrl()
   {
      return (this.tokenUrl);
   }



   /*******************************************************************************
    ** Setter for tokenUrl
    *******************************************************************************/
   public void setTokenUrl(String tokenUrl)
   {
      this.tokenUrl = tokenUrl;
   }



   /*******************************************************************************
    ** Fluent setter for tokenUrl
    *******************************************************************************/
   public OAuth2AuthenticationMetaData withTokenUrl(String tokenUrl)
   {
      this.tokenUrl = tokenUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for userSessionTableName
    *******************************************************************************/
   public String getUserSessionTableName()
   {
      return (this.userSessionTableName);
   }



   /*******************************************************************************
    ** Setter for userSessionTableName
    *******************************************************************************/
   public void setUserSessionTableName(String userSessionTableName)
   {
      this.userSessionTableName = userSessionTableName;
   }



   /*******************************************************************************
    ** Fluent setter for userSessionTableName
    *******************************************************************************/
   public OAuth2AuthenticationMetaData withUserSessionTableName(String userSessionTableName)
   {
      this.userSessionTableName = userSessionTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for redirectStateTableName
    *******************************************************************************/
   public String getRedirectStateTableName()
   {
      return (this.redirectStateTableName);
   }



   /*******************************************************************************
    ** Setter for redirectStateTableName
    *******************************************************************************/
   public void setRedirectStateTableName(String redirectStateTableName)
   {
      this.redirectStateTableName = redirectStateTableName;
   }



   /*******************************************************************************
    ** Fluent setter for redirectStateTableName
    *******************************************************************************/
   public OAuth2AuthenticationMetaData withRedirectStateTableName(String redirectStateTableName)
   {
      this.redirectStateTableName = redirectStateTableName;
      return (this);
   }

}
