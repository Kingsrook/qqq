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

package com.kingsrook.qqq.backend.core.model.metadata.code;


import java.io.Serializable;
import java.util.Objects;


/*******************************************************************************
 **
 *******************************************************************************/
public class AssociatedScriptCodeReference extends QCodeReference
{
   private String       recordTable;
   private Serializable recordPrimaryKey;
   private String       fieldName;



   /*******************************************************************************
    ** Getter for recordTable
    **
    *******************************************************************************/
   public String getRecordTable()
   {
      return recordTable;
   }



   /*******************************************************************************
    ** Setter for recordTable
    **
    *******************************************************************************/
   public void setRecordTable(String recordTable)
   {
      this.recordTable = recordTable;
   }



   /*******************************************************************************
    ** Fluent setter for recordTable
    **
    *******************************************************************************/
   public AssociatedScriptCodeReference withRecordTable(String recordTable)
   {
      this.recordTable = recordTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordPrimaryKey
    **
    *******************************************************************************/
   public Serializable getRecordPrimaryKey()
   {
      return recordPrimaryKey;
   }



   /*******************************************************************************
    ** Setter for recordPrimaryKey
    **
    *******************************************************************************/
   public void setRecordPrimaryKey(Serializable recordPrimaryKey)
   {
      this.recordPrimaryKey = recordPrimaryKey;
   }



   /*******************************************************************************
    ** Fluent setter for recordPrimaryKey
    **
    *******************************************************************************/
   public AssociatedScriptCodeReference withRecordPrimaryKey(Serializable recordPrimaryKey)
   {
      this.recordPrimaryKey = recordPrimaryKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldName
    **
    *******************************************************************************/
   public String getFieldName()
   {
      return fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    **
    *******************************************************************************/
   public AssociatedScriptCodeReference withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(o == null || getClass() != o.getClass())
      {
         return false;
      }
      AssociatedScriptCodeReference that = (AssociatedScriptCodeReference) o;
      return Objects.equals(recordTable, that.recordTable) && Objects.equals(recordPrimaryKey, that.recordPrimaryKey) && Objects.equals(fieldName, that.fieldName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(recordTable, recordPrimaryKey, fieldName);
   }
}
