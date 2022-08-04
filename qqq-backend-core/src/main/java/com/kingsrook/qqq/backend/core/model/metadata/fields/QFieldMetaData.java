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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.io.Serializable;
import java.lang.reflect.Method;
import com.github.hervian.reflection.Fun;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Meta-data to represent a single field in a table.
 **
 *******************************************************************************/
public class QFieldMetaData
{
   private String     name;
   private String     label;
   private String     backendName;
   private QFieldType type;
   private boolean    isRequired = false;
   private boolean    isEditable = true;

   ///////////////////////////////////////////////////////////////////////////////////
   // if we need "only edit on insert" or "only edit on update" in the future,      //
   // propose doing that in a secondary field, e.g., "onlyEditableOn=insert|update" //
   ///////////////////////////////////////////////////////////////////////////////////

   private String       displayFormat = "%s";
   private Serializable defaultValue;
   private String       possibleValueSourceName;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData(String name, QFieldType type)
   {
      this.name = name;
      this.type = type;
   }



   /*******************************************************************************
    ** Initialize a fieldMetaData from a reference to a getter on an entity.
    ** e.g., new QFieldMetaData(Order::getOrderNo).
    *******************************************************************************/
   public <T> QFieldMetaData(Fun.With1ParamAndVoid<T> getterRef) throws QException
   {
      try
      {
         Method getter = Fun.toMethod(getterRef);
         this.name = QRecordEntity.getFieldNameFromGetter(getter);
         this.type = QFieldType.fromClass(getter.getReturnType());
      }
      catch(QException qe)
      {
         throw (qe);
      }
      catch(Exception e)
      {
         throw (new QException("Error constructing field from getterRef: " + getterRef, e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public QFieldType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(QFieldType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withType(QFieldType type)
   {
      this.type = type;
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
    **
    *******************************************************************************/
   public QFieldMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendName
    **
    *******************************************************************************/
   public String getBackendName()
   {
      return backendName;
   }



   /*******************************************************************************
    ** Setter for backendName
    **
    *******************************************************************************/
   public void setBackendName(String backendName)
   {
      this.backendName = backendName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getPossibleValueSourceName()
   {
      return possibleValueSourceName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultValue
    **
    *******************************************************************************/
   public Serializable getDefaultValue()
   {
      return defaultValue;
   }



   /*******************************************************************************
    ** Setter for defaultValue
    **
    *******************************************************************************/
   public void setDefaultValue(Serializable defaultValue)
   {
      this.defaultValue = defaultValue;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withDefaultValue(Serializable defaultValue)
   {
      this.defaultValue = defaultValue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isRequired
    **
    *******************************************************************************/
   public boolean getIsRequired()
   {
      return isRequired;
   }



   /*******************************************************************************
    ** Setter for isRequired
    **
    *******************************************************************************/
   public void setIsRequired(boolean isRequired)
   {
      this.isRequired = isRequired;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withIsRequired(boolean isRequired)
   {
      this.isRequired = isRequired;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isEditable
    **
    *******************************************************************************/
   public boolean getIsEditable()
   {
      return isEditable;
   }



   /*******************************************************************************
    ** Setter for isEditable
    **
    *******************************************************************************/
   public void setIsEditable(boolean isEditable)
   {
      this.isEditable = isEditable;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withIsEditable(boolean isEditable)
   {
      this.isEditable = isEditable;
      return (this);
   }


   /*******************************************************************************
    ** Getter for displayFormat
    **
    *******************************************************************************/
   public String getDisplayFormat()
   {
      return displayFormat;
   }



   /*******************************************************************************
    ** Setter for displayFormat
    **
    *******************************************************************************/
   public void setDisplayFormat(String displayFormat)
   {
      this.displayFormat = displayFormat;
   }


   /*******************************************************************************
    ** Fluent setter for displayFormat
    **
    *******************************************************************************/
   public QFieldMetaData withDisplayFormat(String displayFormat)
   {
      this.displayFormat = displayFormat;
      return (this);
   }

}
