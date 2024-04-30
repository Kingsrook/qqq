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

package com.kingsrook.qqq.backend.core.model.metadata.sharing;


import java.io.Serializable;


/*******************************************************************************
 ** As a component of a ShareableTableMetaData instance, define details about
 ** one particular audience type.
 **
 ** e.g., if a table can be shared to users and groups, there'd be 2 instances of
 ** this object - one like:
 ** - name: user
 ** - fieldName: userId
 ** - sourceTableName: User.TABLE_NAME
 ** - sourceTableKeyFieldName: email (e.g., can be a UK, not just the PKey)
 **
 ** and another similar, w/ the group-type details.
 *******************************************************************************/
public class ShareableAudienceType implements Serializable
{
   private String name;
   private String fieldName;
   private String sourceTableName;

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   // maybe normally the primary key in the source table, but could be a unique-key instead sometimes //
   /////////////////////////////////////////////////////////////////////////////////////////////////////
   private String sourceTableKeyFieldName;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ShareableAudienceType()
   {
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public ShareableAudienceType withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldName
    *******************************************************************************/
   public String getFieldName()
   {
      return (this.fieldName);
   }



   /*******************************************************************************
    ** Setter for fieldName
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    *******************************************************************************/
   public ShareableAudienceType withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceTableName
    *******************************************************************************/
   public String getSourceTableName()
   {
      return (this.sourceTableName);
   }



   /*******************************************************************************
    ** Setter for sourceTableName
    *******************************************************************************/
   public void setSourceTableName(String sourceTableName)
   {
      this.sourceTableName = sourceTableName;
   }



   /*******************************************************************************
    ** Fluent setter for sourceTableName
    *******************************************************************************/
   public ShareableAudienceType withSourceTableName(String sourceTableName)
   {
      this.sourceTableName = sourceTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceTableKeyFieldName
    *******************************************************************************/
   public String getSourceTableKeyFieldName()
   {
      return (this.sourceTableKeyFieldName);
   }



   /*******************************************************************************
    ** Setter for sourceTableKeyFieldName
    *******************************************************************************/
   public void setSourceTableKeyFieldName(String sourceTableKeyFieldName)
   {
      this.sourceTableKeyFieldName = sourceTableKeyFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for sourceTableKeyFieldName
    *******************************************************************************/
   public ShareableAudienceType withSourceTableKeyFieldName(String sourceTableKeyFieldName)
   {
      this.sourceTableKeyFieldName = sourceTableKeyFieldName;
      return (this);
   }

}
