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

package com.kingsrook.qqq.backend.module.rdbms.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QSecretReader;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendModule;


/*******************************************************************************
 ** Meta-data to provide details of an RDBMS backend (e.g., connection params)
 *******************************************************************************/
public class RDBMSBackendMetaData extends QBackendMetaData
{
   private String  vendor;
   private String  hostName;
   private Integer port;
   private String  databaseName;
   private String  username;
   private String  password;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public RDBMSBackendMetaData()
   {
      super();
      setBackendType(RDBMSBackendModule.class);
   }



   /*******************************************************************************
    ** Getter for vendor
    **
    *******************************************************************************/
   public String getVendor()
   {
      return vendor;
   }



   /*******************************************************************************
    ** Setter for vendor
    **
    *******************************************************************************/
   public void setVendor(String vendor)
   {
      this.vendor = vendor;
   }



   /*******************************************************************************
    ** Fluent Setter for vendor
    **
    *******************************************************************************/
   public RDBMSBackendMetaData withVendor(String vendor)
   {
      this.vendor = vendor;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hostName
    **
    *******************************************************************************/
   public String getHostName()
   {
      return hostName;
   }



   /*******************************************************************************
    ** Setter for hostName
    **
    *******************************************************************************/
   public void setHostName(String hostName)
   {
      this.hostName = hostName;
   }



   /*******************************************************************************
    ** Fluent Setter for hostName
    **
    *******************************************************************************/
   public RDBMSBackendMetaData withHostName(String hostName)
   {
      this.hostName = hostName;
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
   public RDBMSBackendMetaData withPort(Integer port)
   {
      this.port = port;
      return (this);
   }



   /*******************************************************************************
    ** Getter for databaseName
    **
    *******************************************************************************/
   public String getDatabaseName()
   {
      return databaseName;
   }



   /*******************************************************************************
    ** Setter for databaseName
    **
    *******************************************************************************/
   public void setDatabaseName(String databaseName)
   {
      this.databaseName = databaseName;
   }



   /*******************************************************************************
    ** Fluent Setter for databaseName
    **
    *******************************************************************************/
   public RDBMSBackendMetaData withDatabaseName(String databaseName)
   {
      this.databaseName = databaseName;
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
   public RDBMSBackendMetaData withUsername(String username)
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
   public RDBMSBackendMetaData withPassword(String password)
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
      QSecretReader secretReader = new QSecretReader();
      username = secretReader.readSecret(username);
      password = secretReader.readSecret(password);
   }

}
