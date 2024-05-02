/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.messaging.ses;


import com.kingsrook.qqq.backend.core.model.metadata.messaging.QMessagingProviderMetaData;
import com.kingsrook.qqq.backend.core.modules.messaging.QMessagingProviderDispatcher;


/*******************************************************************************
 **
 *******************************************************************************/
public class SESMessagingProviderMetaData extends QMessagingProviderMetaData
{
   private String accessKey;
   private String secretKey;
   private String region;

   public static final String TYPE = "SES";

   static
   {
      QMessagingProviderDispatcher.registerMessagingProvider(new SESMessagingProvider());
   }

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SESMessagingProviderMetaData()
   {
      super();
      setType(TYPE);
   }



   /*******************************************************************************
    ** Getter for accessKey
    *******************************************************************************/
   public String getAccessKey()
   {
      return (this.accessKey);
   }



   /*******************************************************************************
    ** Setter for accessKey
    *******************************************************************************/
   public void setAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
   }



   /*******************************************************************************
    ** Fluent setter for accessKey
    *******************************************************************************/
   public SESMessagingProviderMetaData withAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for secretKey
    *******************************************************************************/
   public String getSecretKey()
   {
      return (this.secretKey);
   }



   /*******************************************************************************
    ** Setter for secretKey
    *******************************************************************************/
   public void setSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
   }



   /*******************************************************************************
    ** Fluent setter for secretKey
    *******************************************************************************/
   public SESMessagingProviderMetaData withSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for region
    *******************************************************************************/
   public String getRegion()
   {
      return (this.region);
   }



   /*******************************************************************************
    ** Setter for region
    *******************************************************************************/
   public void setRegion(String region)
   {
      this.region = region;
   }



   /*******************************************************************************
    ** Fluent setter for region
    *******************************************************************************/
   public SESMessagingProviderMetaData withRegion(String region)
   {
      this.region = region;
      return (this);
   }

}
