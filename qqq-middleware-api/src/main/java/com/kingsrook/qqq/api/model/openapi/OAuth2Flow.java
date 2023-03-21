/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.model.openapi;


import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class OAuth2Flow
{
   private String              authorizationUrl;
   private String              tokenUrl;
   private Map<String, String> scopes;



   /*******************************************************************************
    ** Getter for authorizationUrl
    *******************************************************************************/
   public String getAuthorizationUrl()
   {
      return (this.authorizationUrl);
   }



   /*******************************************************************************
    ** Setter for authorizationUrl
    *******************************************************************************/
   public void setAuthorizationUrl(String authorizationUrl)
   {
      this.authorizationUrl = authorizationUrl;
   }



   /*******************************************************************************
    ** Fluent setter for authorizationUrl
    *******************************************************************************/
   public OAuth2Flow withAuthorizationUrl(String authorizationUrl)
   {
      this.authorizationUrl = authorizationUrl;
      return (this);
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
   public OAuth2Flow withTokenUrl(String tokenUrl)
   {
      this.tokenUrl = tokenUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scopes
    *******************************************************************************/
   public Map<String, String> getScopes()
   {
      return (this.scopes);
   }



   /*******************************************************************************
    ** Setter for scopes
    *******************************************************************************/
   public void setScopes(Map<String, String> scopes)
   {
      this.scopes = scopes;
   }



   /*******************************************************************************
    ** Fluent setter for scopes
    *******************************************************************************/
   public OAuth2Flow withScopes(Map<String, String> scopes)
   {
      this.scopes = scopes;
      return (this);
   }

}
