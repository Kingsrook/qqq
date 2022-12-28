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
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule;


/*******************************************************************************
 ** Meta-data to provide details of an Auth0 Authentication module
 *******************************************************************************/
public class Auth0AuthenticationMetaData extends QAuthenticationMetaData
{
   private String baseUrl;
   private String clientId;

   ////////////////////////////////////////////////////////////////////////////////////////
   // keep this secret, on the server - don't let it be serialized and sent to a client! //
   ////////////////////////////////////////////////////////////////////////////////////////
   @JsonIgnore
   private String clientSecret;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public Auth0AuthenticationMetaData()
   {
      super();
      setType(QAuthenticationType.AUTH_0);

      //////////////////////////////////////////////////////////
      // ensure this module is registered with the dispatcher //
      //////////////////////////////////////////////////////////
      QAuthenticationModuleDispatcher.registerModule(QAuthenticationType.AUTH_0.getName(), Auth0AuthenticationModule.class.getName());
   }



   /*******************************************************************************
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   public Auth0AuthenticationMetaData withBaseUrl(String baseUrl)
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
   public Auth0AuthenticationMetaData withClientId(String clientId)
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
   public Auth0AuthenticationMetaData withClientSecret(String clientSecret)
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
}
