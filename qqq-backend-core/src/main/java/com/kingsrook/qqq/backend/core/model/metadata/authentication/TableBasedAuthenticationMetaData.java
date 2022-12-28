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


import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.TableBasedAuthenticationModule;


/*******************************************************************************
 ** Meta-data to provide details of an Auth0 Authentication module
 *******************************************************************************/
public class TableBasedAuthenticationMetaData extends QAuthenticationMetaData
{
   private String userTableName              = "user";
   private String userTablePrimaryKeyField   = "id";
   private String userTableUsernameField     = "username";
   private String userTablePasswordHashField = "passwordHash";
   private String userTableFullNameField     = "fullName";

   private String sessionTableName                 = "session";
   private String sessionTablePrimaryKeyField      = "id";
   private String sessionTableUuidField            = "id";
   private String sessionTableUserIdField          = "userId";
   private String sessionTableAccessTimestampField = "accessTimestamp";

   private Integer inactivityTimeoutSeconds = 14_400; // 4 hours



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public TableBasedAuthenticationMetaData()
   {
      super();
      setType(QAuthenticationType.TABLE_BASED);

      //////////////////////////////////////////////////////////
      // ensure this module is registered with the dispatcher //
      //////////////////////////////////////////////////////////
      QAuthenticationModuleDispatcher.registerModule(QAuthenticationType.TABLE_BASED.getName(), TableBasedAuthenticationModule.class.getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData defineStandardUserTable(String backendName)
   {
      return (new QTableMetaData()
         .withName(getUserTableName())
         .withBackendName(backendName)
         .withPrimaryKeyField(getUserTablePrimaryKeyField())
         .withRecordLabelFormat("%s")
         .withRecordLabelFields(getUserTableUsernameField())
         .withUniqueKey(new UniqueKey(getUserTableUsernameField()))
         .withField(new QFieldMetaData(getUserTablePrimaryKeyField(), QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData(getUserTablePrimaryKeyField(), QFieldType.INTEGER))
         .withField(new QFieldMetaData(getUserTableUsernameField(), QFieldType.STRING))
         .withField(new QFieldMetaData(getUserTablePasswordHashField(), QFieldType.STRING))
         .withField(new QFieldMetaData(getUserTableFullNameField(), QFieldType.STRING)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData defineStandardSessionTable(String backendName)
   {
      return (new QTableMetaData()
         .withName(getSessionTableName())
         .withBackendName(backendName)
         .withPrimaryKeyField(getSessionTablePrimaryKeyField())
         .withRecordLabelFormat("%s")
         .withRecordLabelFields(getSessionTableUuidField())
         .withUniqueKey(new UniqueKey(getSessionTableUuidField()))
         .withField(new QFieldMetaData(getSessionTableUuidField(), QFieldType.STRING))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData(getSessionTableUserIdField(), QFieldType.INTEGER))
         .withField(new QFieldMetaData(getSessionTableAccessTimestampField(), QFieldType.DATE_TIME)));
   }



   /*******************************************************************************
    ** Getter for userTableName
    *******************************************************************************/
   public String getUserTableName()
   {
      return (this.userTableName);
   }



   /*******************************************************************************
    ** Setter for userTableName
    *******************************************************************************/
   public void setUserTableName(String userTableName)
   {
      this.userTableName = userTableName;
   }



   /*******************************************************************************
    ** Fluent setter for userTableName
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withUserTableName(String userTableName)
   {
      this.userTableName = userTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sessionTableName
    *******************************************************************************/
   public String getSessionTableName()
   {
      return (this.sessionTableName);
   }



   /*******************************************************************************
    ** Setter for sessionTableName
    *******************************************************************************/
   public void setSessionTableName(String sessionTableName)
   {
      this.sessionTableName = sessionTableName;
   }



   /*******************************************************************************
    ** Fluent setter for sessionTableName
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withSessionTableName(String sessionTableName)
   {
      this.sessionTableName = sessionTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for userTablePrimaryKeyField
    *******************************************************************************/
   public String getUserTablePrimaryKeyField()
   {
      return (this.userTablePrimaryKeyField);
   }



   /*******************************************************************************
    ** Setter for userTablePrimaryKeyField
    *******************************************************************************/
   public void setUserTablePrimaryKeyField(String userTablePrimaryKeyField)
   {
      this.userTablePrimaryKeyField = userTablePrimaryKeyField;
   }



   /*******************************************************************************
    ** Fluent setter for userTablePrimaryKeyField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withUserTablePrimaryKeyField(String userTablePrimaryKeyField)
   {
      this.userTablePrimaryKeyField = userTablePrimaryKeyField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for userTableUsernameField
    *******************************************************************************/
   public String getUserTableUsernameField()
   {
      return (this.userTableUsernameField);
   }



   /*******************************************************************************
    ** Setter for userTableUsernameField
    *******************************************************************************/
   public void setUserTableUsernameField(String userTableUsernameField)
   {
      this.userTableUsernameField = userTableUsernameField;
   }



   /*******************************************************************************
    ** Fluent setter for userTableUsernameField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withUserTableUsernameField(String userTableUsernameField)
   {
      this.userTableUsernameField = userTableUsernameField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sessionTablePrimaryKeyField
    *******************************************************************************/
   public String getSessionTablePrimaryKeyField()
   {
      return (this.sessionTablePrimaryKeyField);
   }



   /*******************************************************************************
    ** Setter for sessionTablePrimaryKeyField
    *******************************************************************************/
   public void setSessionTablePrimaryKeyField(String sessionTablePrimaryKeyField)
   {
      this.sessionTablePrimaryKeyField = sessionTablePrimaryKeyField;
   }



   /*******************************************************************************
    ** Fluent setter for sessionTablePrimaryKeyField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withSessionTablePrimaryKeyField(String sessionTablePrimaryKeyField)
   {
      this.sessionTablePrimaryKeyField = sessionTablePrimaryKeyField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sessionTableUserIdField
    *******************************************************************************/
   public String getSessionTableUserIdField()
   {
      return (this.sessionTableUserIdField);
   }



   /*******************************************************************************
    ** Setter for sessionTableUserIdField
    *******************************************************************************/
   public void setSessionTableUserIdField(String sessionTableUserIdField)
   {
      this.sessionTableUserIdField = sessionTableUserIdField;
   }



   /*******************************************************************************
    ** Fluent setter for sessionTableUserIdField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withSessionTableUserIdField(String sessionTableUserIdField)
   {
      this.sessionTableUserIdField = sessionTableUserIdField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sessionTableUuidField
    *******************************************************************************/
   public String getSessionTableUuidField()
   {
      return (this.sessionTableUuidField);
   }



   /*******************************************************************************
    ** Setter for sessionTableUuidField
    *******************************************************************************/
   public void setSessionTableUuidField(String sessionTableUuidField)
   {
      this.sessionTableUuidField = sessionTableUuidField;
   }



   /*******************************************************************************
    ** Fluent setter for sessionTableUuidField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withSessionTableUuidField(String sessionTableUuidField)
   {
      this.sessionTableUuidField = sessionTableUuidField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for userTableFullNameField
    *******************************************************************************/
   public String getUserTableFullNameField()
   {
      return (this.userTableFullNameField);
   }



   /*******************************************************************************
    ** Setter for userTableFullNameField
    *******************************************************************************/
   public void setUserTableFullNameField(String userTableFullNameField)
   {
      this.userTableFullNameField = userTableFullNameField;
   }



   /*******************************************************************************
    ** Fluent setter for userTableFullNameField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withUserTableFullNameField(String userTableFullNameField)
   {
      this.userTableFullNameField = userTableFullNameField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for userTablePasswordHashField
    *******************************************************************************/
   public String getUserTablePasswordHashField()
   {
      return (this.userTablePasswordHashField);
   }



   /*******************************************************************************
    ** Setter for userTablePasswordHashField
    *******************************************************************************/
   public void setUserTablePasswordHashField(String userTablePasswordHashField)
   {
      this.userTablePasswordHashField = userTablePasswordHashField;
   }



   /*******************************************************************************
    ** Fluent setter for userTablePasswordHashField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withUserTablePasswordHashField(String userTablePasswordHashField)
   {
      this.userTablePasswordHashField = userTablePasswordHashField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sessionTableAccessTimestampField
    *******************************************************************************/
   public String getSessionTableAccessTimestampField()
   {
      return (this.sessionTableAccessTimestampField);
   }



   /*******************************************************************************
    ** Setter for sessionTableAccessTimestampField
    *******************************************************************************/
   public void setSessionTableAccessTimestampField(String sessionTableAccessTimestampField)
   {
      this.sessionTableAccessTimestampField = sessionTableAccessTimestampField;
   }



   /*******************************************************************************
    ** Fluent setter for sessionTableAccessTimestampField
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withSessionTableAccessTimestampField(String sessionTableAccessTimestampField)
   {
      this.sessionTableAccessTimestampField = sessionTableAccessTimestampField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inactivityTimeoutSeconds
    *******************************************************************************/
   public Integer getInactivityTimeoutSeconds()
   {
      return (this.inactivityTimeoutSeconds);
   }



   /*******************************************************************************
    ** Setter for inactivityTimeoutSeconds
    *******************************************************************************/
   public void setInactivityTimeoutSeconds(Integer inactivityTimeoutSeconds)
   {
      this.inactivityTimeoutSeconds = inactivityTimeoutSeconds;
   }



   /*******************************************************************************
    ** Fluent setter for inactivityTimeoutSeconds
    *******************************************************************************/
   public TableBasedAuthenticationMetaData withInactivityTimeoutSeconds(Integer inactivityTimeoutSeconds)
   {
      this.inactivityTimeoutSeconds = inactivityTimeoutSeconds;
      return (this);
   }

}
