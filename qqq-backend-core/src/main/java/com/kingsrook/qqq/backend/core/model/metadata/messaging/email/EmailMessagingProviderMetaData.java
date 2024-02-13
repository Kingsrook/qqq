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

package com.kingsrook.qqq.backend.core.model.metadata.messaging.email;


import com.kingsrook.qqq.backend.core.model.metadata.messaging.QMessagingProviderMetaData;
import com.kingsrook.qqq.backend.core.modules.messaging.QMessagingProviderDispatcher;


/*******************************************************************************
 **
 *******************************************************************************/
public class EmailMessagingProviderMetaData extends QMessagingProviderMetaData
{
   private String smtpServer;
   private String smtpPort;

   public static final String TYPE = "EMAIL";

   static
   {
      QMessagingProviderDispatcher.registerMessagingProvider(new EmailMessagingProvider());
   }

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public EmailMessagingProviderMetaData()
   {
      super();
      setType(TYPE);
   }



   /*******************************************************************************
    ** Getter for smtpServer
    *******************************************************************************/
   public String getSmtpServer()
   {
      return (this.smtpServer);
   }



   /*******************************************************************************
    ** Setter for smtpServer
    *******************************************************************************/
   public void setSmtpServer(String smtpServer)
   {
      this.smtpServer = smtpServer;
   }



   /*******************************************************************************
    ** Fluent setter for smtpServer
    *******************************************************************************/
   public EmailMessagingProviderMetaData withSmtpServer(String smtpServer)
   {
      this.smtpServer = smtpServer;
      return (this);
   }



   /*******************************************************************************
    ** Getter for smtpPort
    *******************************************************************************/
   public String getSmtpPort()
   {
      return (this.smtpPort);
   }



   /*******************************************************************************
    ** Setter for smtpPort
    *******************************************************************************/
   public void setSmtpPort(String smtpPort)
   {
      this.smtpPort = smtpPort;
   }



   /*******************************************************************************
    ** Fluent setter for smtpPort
    *******************************************************************************/
   public EmailMessagingProviderMetaData withSmtpPort(String smtpPort)
   {
      this.smtpPort = smtpPort;
      return (this);
   }

}
