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

package com.kingsrook.qqq.backend.module.mongodb.model.metadata;


import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.module.mongodb.MongoDBBackendModule;


/*******************************************************************************
 ** Meta-data to provide details of a MongoDB backend (e.g., connection params)
 *******************************************************************************/
public class MongoDBBackendMetaData extends QBackendMetaData
{
   private String  host;
   private Integer port;
   private String  databaseName;
   private String  username;
   private String  password;
   private String  authSourceDatabase;
   private String  urlSuffix;

   private boolean transactionsSupported = true;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public MongoDBBackendMetaData()
   {
      super();
      setBackendType(MongoDBBackendModule.class);
   }



   /*******************************************************************************
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   @Override
   public MongoDBBackendMetaData withName(String name)
   {
      setName(name);
      return this;
   }



   /*******************************************************************************
    ** Getter for host
    **
    *******************************************************************************/
   public String getHost()
   {
      return host;
   }



   /*******************************************************************************
    ** Setter for host
    **
    *******************************************************************************/
   public void setHost(String host)
   {
      this.host = host;
   }



   /*******************************************************************************
    ** Fluent Setter for host
    **
    *******************************************************************************/
   public MongoDBBackendMetaData withHost(String host)
   {
      this.host = host;
      return (this);
   }



   /*******************************************************************************
    ** Getter for port
    **
    *******************************************************************************/
   public Integer getPort()
   {
      return port;
   }



   /*******************************************************************************
    ** Setter for port
    **
    *******************************************************************************/
   public void setPort(Integer port)
   {
      this.port = port;
   }



   /*******************************************************************************
    ** Fluent Setter for port
    **
    *******************************************************************************/
   public MongoDBBackendMetaData withPort(Integer port)
   {
      this.port = port;
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
    ** Fluent Setter for username
    **
    *******************************************************************************/
   public MongoDBBackendMetaData withUsername(String username)
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
    ** Fluent Setter for password
    **
    *******************************************************************************/
   public MongoDBBackendMetaData withPassword(String password)
   {
      this.password = password;
      return (this);
   }



   /*******************************************************************************
    ** Called by the QInstanceEnricher - to do backend-type-specific enrichments.
    ** Original use case is:  reading secrets into fields (e.g., passwords).
    *******************************************************************************/
   @Override
   public void enrich()
   {
      super.enrich();
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      username = interpreter.interpret(username);
      password = interpreter.interpret(password);
   }



   /*******************************************************************************
    ** Getter for urlSuffix
    *******************************************************************************/
   public String getUrlSuffix()
   {
      return (this.urlSuffix);
   }



   /*******************************************************************************
    ** Setter for urlSuffix
    *******************************************************************************/
   public void setUrlSuffix(String urlSuffix)
   {
      this.urlSuffix = urlSuffix;
   }



   /*******************************************************************************
    ** Fluent setter for urlSuffix
    *******************************************************************************/
   public MongoDBBackendMetaData withUrlSuffix(String urlSuffix)
   {
      this.urlSuffix = urlSuffix;
      return (this);
   }



   /*******************************************************************************
    ** Getter for databaseName
    *******************************************************************************/
   public String getDatabaseName()
   {
      return (this.databaseName);
   }



   /*******************************************************************************
    ** Setter for databaseName
    *******************************************************************************/
   public void setDatabaseName(String databaseName)
   {
      this.databaseName = databaseName;
   }



   /*******************************************************************************
    ** Fluent setter for databaseName
    *******************************************************************************/
   public MongoDBBackendMetaData withDatabaseName(String databaseName)
   {
      this.databaseName = databaseName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for transactionsSupported
    *******************************************************************************/
   public boolean getTransactionsSupported()
   {
      return (this.transactionsSupported);
   }



   /*******************************************************************************
    ** Setter for transactionsSupported
    *******************************************************************************/
   public void setTransactionsSupported(boolean transactionsSupported)
   {
      this.transactionsSupported = transactionsSupported;
   }



   /*******************************************************************************
    ** Fluent setter for transactionsSupported
    *******************************************************************************/
   public MongoDBBackendMetaData withTransactionsSupported(boolean transactionsSupported)
   {
      this.transactionsSupported = transactionsSupported;
      return (this);
   }



   /*******************************************************************************
    ** Getter for authSourceDatabase
    *******************************************************************************/
   public String getAuthSourceDatabase()
   {
      return (this.authSourceDatabase);
   }



   /*******************************************************************************
    ** Setter for authSourceDatabase
    *******************************************************************************/
   public void setAuthSourceDatabase(String authSourceDatabase)
   {
      this.authSourceDatabase = authSourceDatabase;
   }



   /*******************************************************************************
    ** Fluent setter for authSourceDatabase
    *******************************************************************************/
   public MongoDBBackendMetaData withAuthSourceDatabase(String authSourceDatabase)
   {
      this.authSourceDatabase = authSourceDatabase;
      return (this);
   }

}
