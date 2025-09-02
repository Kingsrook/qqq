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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Definition of a Unique Key (or "Constraint", if you wanna use fancy words)
 ** on a QTable.
 *******************************************************************************/
public class UniqueKey implements QMetaDataObject, Cloneable
{
   private List<String> fieldNames;
   private String       label;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public UniqueKey()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public UniqueKey(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public UniqueKey(String... fieldNames)
   {
      this.fieldNames = Arrays.stream(fieldNames).toList();
   }



   /*******************************************************************************
    ** Getter for fieldNames
    **
    *******************************************************************************/
   public List<String> getFieldNames()
   {
      return fieldNames;
   }



   /*******************************************************************************
    ** Setter for fieldNames
    **
    *******************************************************************************/
   public void setFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
   }



   /*******************************************************************************
    ** Fluent setter for fieldNames
    **
    *******************************************************************************/
   public UniqueKey withFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public UniqueKey withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public UniqueKey withFieldName(String fieldName)
   {
      if(this.fieldNames == null)
      {
         this.fieldNames = new ArrayList<>();
      }
      this.fieldNames.add(fieldName);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getDescription(QTableMetaData table)
   {
      List<String> fieldLabels = new ArrayList<>();

      for(String fieldName : getFieldNames())
      {
         fieldLabels.add(table.getField(fieldName).getLabel());
      }

      return (StringUtils.joinWithCommasAndAnd(fieldLabels));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public UniqueKey clone()
   {
      try
      {
         UniqueKey clone = (UniqueKey) super.clone();
         if(fieldNames != null)
         {
            clone.fieldNames = new ArrayList<>(fieldNames);
         }
         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }
}
