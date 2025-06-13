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


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.instances.assessment.Assessable;
import com.kingsrook.qqq.backend.core.instances.assessment.QInstanceAssessor;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendModule;
import com.kingsrook.qqq.backend.module.rdbms.strategy.BaseRDBMSActionStrategy;
import com.kingsrook.qqq.backend.module.rdbms.strategy.RDBMSActionStrategyInterface;


/*******************************************************************************
 ** Meta-data to provide details of an RDBMS backend (e.g., connection params)
 *******************************************************************************/
public class RDBMSBackendMetaData extends QBackendMetaData implements Assessable
{
   private String  vendor;
   private String  hostName;
   private Integer port;
   private String  databaseName;
   private String  username;
   private String  password;

   private String jdbcUrl;
   private String jdbcDriverClassName;

   private QCodeReference connectionProvider;

   private ConnectionPoolSettings connectionPoolSettings;

   private RDBMSBackendMetaData readOnlyBackendMetaData;

   private QCodeReference               actionStrategyCodeReference;
   private RDBMSActionStrategyInterface actionStrategy;

   private List<String> queriesForNewConnections = null;

   ///////////////////////////////////////////////////////////
   // define well-known (and fully supported) vendor values //
   ///////////////////////////////////////////////////////////
   public static final String VENDOR_MYSQL        = "mysql";
   public static final String VENDOR_H2           = "h2";
   public static final String VENDOR_AURORA_MYSQL = "aurora-mysql";



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public RDBMSBackendMetaData()
   {
      super();
      setBackendType(RDBMSBackendModule.class);
   }



   /*******************************************************************************
    ** Fluent setter, override to help fluent flows
    *******************************************************************************/
   @Override
   public RDBMSBackendMetaData withName(String name)
   {
      setName(name);
      return this;
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
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      username = interpreter.interpret(username);
      password = interpreter.interpret(password);
   }



   /*******************************************************************************
    ** Getter for jdbcUrl
    *******************************************************************************/
   public String getJdbcUrl()
   {
      return (this.jdbcUrl);
   }



   /*******************************************************************************
    ** Setter for jdbcUrl
    *******************************************************************************/
   public void setJdbcUrl(String jdbcUrl)
   {
      this.jdbcUrl = jdbcUrl;
   }



   /*******************************************************************************
    ** Fluent setter for jdbcUrl
    *******************************************************************************/
   public RDBMSBackendMetaData withJdbcUrl(String jdbcUrl)
   {
      this.jdbcUrl = jdbcUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for jdbcDriverClassName
    *******************************************************************************/
   public String getJdbcDriverClassName()
   {
      return (this.jdbcDriverClassName);
   }



   /*******************************************************************************
    ** Setter for jdbcDriverClassName
    *******************************************************************************/
   public void setJdbcDriverClassName(String jdbcDriverClassName)
   {
      this.jdbcDriverClassName = jdbcDriverClassName;
   }



   /*******************************************************************************
    ** Fluent setter for jdbcDriverClassName
    *******************************************************************************/
   public RDBMSBackendMetaData withJdbcDriverClassName(String jdbcDriverClassName)
   {
      this.jdbcDriverClassName = jdbcDriverClassName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for connectionProvider
    *******************************************************************************/
   public QCodeReference getConnectionProvider()
   {
      return (this.connectionProvider);
   }



   /*******************************************************************************
    ** Setter for connectionProvider
    *******************************************************************************/
   public void setConnectionProvider(QCodeReference connectionProvider)
   {
      this.connectionProvider = connectionProvider;
   }



   /*******************************************************************************
    ** Fluent setter for connectionProvider
    *******************************************************************************/
   public RDBMSBackendMetaData withConnectionProvider(QCodeReference connectionProvider)
   {
      this.connectionProvider = connectionProvider;
      return (this);
   }



   /*******************************************************************************
    ** Getter for readOnlyBackendMetaData
    *******************************************************************************/
   public RDBMSBackendMetaData getReadOnlyBackendMetaData()
   {
      return (this.readOnlyBackendMetaData);
   }



   /*******************************************************************************
    ** Setter for readOnlyBackendMetaData
    *******************************************************************************/
   public void setReadOnlyBackendMetaData(RDBMSBackendMetaData readOnlyBackendMetaData)
   {
      this.readOnlyBackendMetaData = readOnlyBackendMetaData;
   }



   /*******************************************************************************
    ** Fluent setter for readOnlyBackendMetaData
    *******************************************************************************/
   public RDBMSBackendMetaData withReadOnlyBackendMetaData(RDBMSBackendMetaData readOnlyBackendMetaData)
   {
      this.readOnlyBackendMetaData = readOnlyBackendMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for connectionPoolSettings
    *******************************************************************************/
   public ConnectionPoolSettings getConnectionPoolSettings()
   {
      return (this.connectionPoolSettings);
   }



   /*******************************************************************************
    ** Setter for connectionPoolSettings
    *******************************************************************************/
   public void setConnectionPoolSettings(ConnectionPoolSettings connectionPoolSettings)
   {
      this.connectionPoolSettings = connectionPoolSettings;
   }



   /*******************************************************************************
    ** Fluent setter for connectionPoolSettings
    *******************************************************************************/
   public RDBMSBackendMetaData withConnectionPoolSettings(ConnectionPoolSettings connectionPoolSettings)
   {
      this.connectionPoolSettings = connectionPoolSettings;
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String buildConnectionString()
   {
      return null;
   }



   /*******************************************************************************
    ** Getter for actionStrategyCodeReference
    *******************************************************************************/
   public QCodeReference getActionStrategyCodeReference()
   {
      return (this.actionStrategyCodeReference);
   }



   /*******************************************************************************
    ** Setter for actionStrategyCodeReference
    *******************************************************************************/
   public void setActionStrategyCodeReference(QCodeReference actionStrategyCodeReference)
   {
      this.actionStrategyCodeReference = actionStrategyCodeReference;
   }



   /*******************************************************************************
    ** Fluent setter for actionStrategyCodeReference
    *******************************************************************************/
   public RDBMSBackendMetaData withActionStrategyCodeReference(QCodeReference actionStrategyCodeReference)
   {
      this.actionStrategyCodeReference = actionStrategyCodeReference;
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @JsonIgnore
   public RDBMSActionStrategyInterface getActionStrategy()
   {
      if(actionStrategy == null)
      {
         if(actionStrategyCodeReference != null)
         {
            actionStrategy = QCodeLoader.getAdHoc(RDBMSActionStrategyInterface.class, actionStrategyCodeReference);
         }
         else
         {
            actionStrategy = new BaseRDBMSActionStrategy();
         }
      }

      return (actionStrategy);
   }



   /***************************************************************************
    * note - protected - meant for sub-classes to use in their implementation of
    * getActionStrategy, but not for public use.
    ***************************************************************************/
   protected RDBMSActionStrategyInterface getActionStrategyField()
   {
      return (actionStrategy);
   }



   /***************************************************************************
    * note - protected - meant for sub-classes to use in their implementation of
    * getActionStrategy, but not for public use.
    ***************************************************************************/
   protected void setActionStrategyField(RDBMSActionStrategyInterface actionStrategy)
   {
      this.actionStrategy = actionStrategy;
   }


   /*******************************************************************************
    ** Getter for queriesForNewConnections
    *******************************************************************************/
   public List<String> getQueriesForNewConnections()
   {
      return (this.queriesForNewConnections);
   }



   /*******************************************************************************
    ** Setter for queriesForNewConnections
    *******************************************************************************/
   public void setQueriesForNewConnections(List<String> queriesForNewConnections)
   {
      this.queriesForNewConnections = queriesForNewConnections;
   }



   /*******************************************************************************
    ** Fluent setter for queriesForNewConnections
    *******************************************************************************/
   public RDBMSBackendMetaData withQueriesForNewConnections(List<String> queriesForNewConnections)
   {
      this.queriesForNewConnections = queriesForNewConnections;
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void assess(QInstanceAssessor qInstanceAssessor, QInstance qInstance)
   {
      List<QTableMetaData> tables = new ArrayList<>();
      for(QTableMetaData table : qInstance.getTables().values())
      {
         if(Objects.equals(getName(), table.getBackendName()))
         {
            tables.add(table);
         }
      }

      if(!tables.isEmpty())
      {
         new RDBMSBackendAssessor(qInstanceAssessor, this, tables).assess();
      }
   }
}
