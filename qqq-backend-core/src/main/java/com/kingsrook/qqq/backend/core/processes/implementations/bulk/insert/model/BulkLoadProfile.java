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


/***************************************************************************
 * this is the model of a saved bulk load profile - which is what passes back
 * and forth with the frontend.
 ****************************************************************************/
public class BulkLoadProfile implements Serializable
{
   private ArrayList<BulkLoadProfileField> fieldList;

   private Boolean hasHeaderRow;
   private String  layout;
   private String  version;



   /*******************************************************************************
    ** Getter for fieldList
    *******************************************************************************/
   public ArrayList<BulkLoadProfileField> getFieldList()
   {
      return (this.fieldList);
   }



   /*******************************************************************************
    ** Getter for hasHeaderRow
    *******************************************************************************/
   public Boolean getHasHeaderRow()
   {
      return (this.hasHeaderRow);
   }



   /*******************************************************************************
    ** Setter for hasHeaderRow
    *******************************************************************************/
   public void setHasHeaderRow(Boolean hasHeaderRow)
   {
      this.hasHeaderRow = hasHeaderRow;
   }



   /*******************************************************************************
    ** Fluent setter for hasHeaderRow
    *******************************************************************************/
   public BulkLoadProfile withHasHeaderRow(Boolean hasHeaderRow)
   {
      this.hasHeaderRow = hasHeaderRow;
      return (this);
   }



   /*******************************************************************************
    ** Getter for layout
    *******************************************************************************/
   public String getLayout()
   {
      return (this.layout);
   }



   /*******************************************************************************
    ** Setter for layout
    *******************************************************************************/
   public void setLayout(String layout)
   {
      this.layout = layout;
   }



   /*******************************************************************************
    ** Fluent setter for layout
    *******************************************************************************/
   public BulkLoadProfile withLayout(String layout)
   {
      this.layout = layout;
      return (this);
   }



   /*******************************************************************************
    ** Setter for fieldList
    *******************************************************************************/
   public void setFieldList(ArrayList<BulkLoadProfileField> fieldList)
   {
      this.fieldList = fieldList;
   }



   /*******************************************************************************
    ** Fluent setter for fieldList
    *******************************************************************************/
   public BulkLoadProfile withFieldList(ArrayList<BulkLoadProfileField> fieldList)
   {
      this.fieldList = fieldList;
      return (this);
   }


   /*******************************************************************************
    ** Getter for version
    *******************************************************************************/
   public String getVersion()
   {
      return (this.version);
   }



   /*******************************************************************************
    ** Setter for version
    *******************************************************************************/
   public void setVersion(String version)
   {
      this.version = version;
   }



   /*******************************************************************************
    ** Fluent setter for version
    *******************************************************************************/
   public BulkLoadProfile withVersion(String version)
   {
      this.version = version;
      return (this);
   }


}
