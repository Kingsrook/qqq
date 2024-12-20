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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model;


import java.io.Serializable;
import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkLoadTableStructure implements Serializable
{
   private boolean isMain;
   private boolean isMany;

   private String tableName;
   private String label;
   private String associationPath; // null/empty for main table, then associationName for a child, associationName.associationName for a grandchild

   private ArrayList<QFieldMetaData>         fields; // mmm, not marked as serializable (at this time) - is okay?
   private ArrayList<BulkLoadTableStructure> associations;



   /*******************************************************************************
    ** Getter for isMain
    *******************************************************************************/
   public boolean getIsMain()
   {
      return (this.isMain);
   }



   /*******************************************************************************
    ** Setter for isMain
    *******************************************************************************/
   public void setIsMain(boolean isMain)
   {
      this.isMain = isMain;
   }



   /*******************************************************************************
    ** Fluent setter for isMain
    *******************************************************************************/
   public BulkLoadTableStructure withIsMain(boolean isMain)
   {
      this.isMain = isMain;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isMany
    *******************************************************************************/
   public boolean getIsMany()
   {
      return (this.isMany);
   }



   /*******************************************************************************
    ** Setter for isMany
    *******************************************************************************/
   public void setIsMany(boolean isMany)
   {
      this.isMany = isMany;
   }



   /*******************************************************************************
    ** Fluent setter for isMany
    *******************************************************************************/
   public BulkLoadTableStructure withIsMany(boolean isMany)
   {
      this.isMany = isMany;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public BulkLoadTableStructure withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public BulkLoadTableStructure withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fields
    *******************************************************************************/
   public ArrayList<QFieldMetaData> getFields()
   {
      return (this.fields);
   }



   /*******************************************************************************
    ** Setter for fields
    *******************************************************************************/
   public void setFields(ArrayList<QFieldMetaData> fields)
   {
      this.fields = fields;
   }



   /*******************************************************************************
    ** Fluent setter for fields
    *******************************************************************************/
   public BulkLoadTableStructure withFields(ArrayList<QFieldMetaData> fields)
   {
      this.fields = fields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for associationPath
    *******************************************************************************/
   public String getAssociationPath()
   {
      return (this.associationPath);
   }



   /*******************************************************************************
    ** Setter for associationPath
    *******************************************************************************/
   public void setAssociationPath(String associationPath)
   {
      this.associationPath = associationPath;
   }



   /*******************************************************************************
    ** Fluent setter for associationPath
    *******************************************************************************/
   public BulkLoadTableStructure withAssociationPath(String associationPath)
   {
      this.associationPath = associationPath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for associations
    *******************************************************************************/
   public ArrayList<BulkLoadTableStructure> getAssociations()
   {
      return (this.associations);
   }



   /*******************************************************************************
    ** Setter for associations
    *******************************************************************************/
   public void setAssociations(ArrayList<BulkLoadTableStructure> associations)
   {
      this.associations = associations;
   }



   /*******************************************************************************
    ** Fluent setter for associations
    *******************************************************************************/
   public BulkLoadTableStructure withAssociations(ArrayList<BulkLoadTableStructure> associations)
   {
      this.associations = associations;
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void addAssociation(BulkLoadTableStructure association)
   {
      if(this.associations == null)
      {
         this.associations = new ArrayList<>();
      }
      this.associations.add(association);
   }
}
