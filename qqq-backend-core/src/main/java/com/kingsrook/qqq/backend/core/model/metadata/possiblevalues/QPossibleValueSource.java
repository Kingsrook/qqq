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

package com.kingsrook.qqq.backend.core.model.metadata.possiblevalues;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;


/*******************************************************************************
 ** Meta-data to represent a single field in a table.
 **
 *******************************************************************************/
public class QPossibleValueSource
{
   private String                   name;
   private QPossibleValueSourceType type;
   private QFieldType               idType = QFieldType.INTEGER;

   private String       valueFormat           = ValueFormat.DEFAULT;
   private List<String> valueFields           = ValueFields.DEFAULT;
   private String       valueFormatIfNotFound = null;
   private List<String> valueFieldsIfNotFound = null;



   public interface ValueFormat
   {
      String DEFAULT         = "%s";
      String LABEL_ONLY      = "%s";
      String LABEL_PARENS_ID = "%s (%s)";
      String ID_COLON_LABEL  = "%s: %s";
   }



   public interface ValueFields
   {
      List<String> DEFAULT         = List.of("label");
      List<String> LABEL_ONLY      = List.of("label");
      List<String> LABEL_PARENS_ID = List.of("label", "id");
      List<String> ID_COLON_LABEL  = List.of("id", "label");
   }

   // todo - optimization hints, such as "table is static, fully cache" or "table is small, so we can pull the whole thing into memory?"

   //////////////////////
   // for type = TABLE //
   //////////////////////
   private String tableName;
   // todo - override labelFormat & labelFields?

   /////////////////////
   // for type = ENUM //
   /////////////////////
   private List<QPossibleValue<?>> enumValues;

   ///////////////////////
   // for type = CUSTOM //
   ///////////////////////
   private QCodeReference customCodeReference;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource()
   {
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
   public QPossibleValueSource withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSourceType getType()
   {
      return type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setType(QPossibleValueSourceType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource withType(QPossibleValueSourceType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for idType
    **
    *******************************************************************************/
   public QFieldType getIdType()
   {
      return idType;
   }



   /*******************************************************************************
    ** Setter for idType
    **
    *******************************************************************************/
   public void setIdType(QFieldType idType)
   {
      this.idType = idType;
   }



   /*******************************************************************************
    ** Fluent setter for idType
    **
    *******************************************************************************/
   public QPossibleValueSource withIdType(QFieldType idType)
   {
      this.idType = idType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for valueFormat
    **
    *******************************************************************************/
   public String getValueFormat()
   {
      return valueFormat;
   }



   /*******************************************************************************
    ** Setter for valueFormat
    **
    *******************************************************************************/
   public void setValueFormat(String valueFormat)
   {
      this.valueFormat = valueFormat;
   }



   /*******************************************************************************
    ** Fluent setter for valueFormat
    **
    *******************************************************************************/
   public QPossibleValueSource withValueFormat(String valueFormat)
   {
      this.valueFormat = valueFormat;
      return (this);
   }



   /*******************************************************************************
    ** Getter for valueFields
    **
    *******************************************************************************/
   public List<String> getValueFields()
   {
      return valueFields;
   }



   /*******************************************************************************
    ** Setter for valueFields
    **
    *******************************************************************************/
   public void setValueFields(List<String> valueFields)
   {
      this.valueFields = valueFields;
   }



   /*******************************************************************************
    ** Fluent setter for valueFields
    **
    *******************************************************************************/
   public QPossibleValueSource withValueFields(List<String> valueFields)
   {
      this.valueFields = valueFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for valueFormatIfNotFound
    **
    *******************************************************************************/
   public String getValueFormatIfNotFound()
   {
      return valueFormatIfNotFound;
   }



   /*******************************************************************************
    ** Setter for valueFormatIfNotFound
    **
    *******************************************************************************/
   public void setValueFormatIfNotFound(String valueFormatIfNotFound)
   {
      this.valueFormatIfNotFound = valueFormatIfNotFound;
   }



   /*******************************************************************************
    ** Fluent setter for valueFormatIfNotFound
    **
    *******************************************************************************/
   public QPossibleValueSource withValueFormatIfNotFound(String valueFormatIfNotFound)
   {
      this.valueFormatIfNotFound = valueFormatIfNotFound;
      return (this);
   }



   /*******************************************************************************
    ** Getter for valueFieldsIfNotFound
    **
    *******************************************************************************/
   public List<String> getValueFieldsIfNotFound()
   {
      return valueFieldsIfNotFound;
   }



   /*******************************************************************************
    ** Setter for valueFieldsIfNotFound
    **
    *******************************************************************************/
   public void setValueFieldsIfNotFound(List<String> valueFieldsIfNotFound)
   {
      this.valueFieldsIfNotFound = valueFieldsIfNotFound;
   }



   /*******************************************************************************
    ** Fluent setter for valueFieldsIfNotFound
    **
    *******************************************************************************/
   public QPossibleValueSource withValueFieldsIfNotFound(List<String> valueFieldsIfNotFound)
   {
      this.valueFieldsIfNotFound = valueFieldsIfNotFound;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    **
    *******************************************************************************/
   public QPossibleValueSource withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for enumValues
    **
    *******************************************************************************/
   public List<QPossibleValue<?>> getEnumValues()
   {
      return enumValues;
   }



   /*******************************************************************************
    ** Setter for enumValues
    **
    *******************************************************************************/
   public void setEnumValues(List<QPossibleValue<?>> enumValues)
   {
      this.enumValues = enumValues;
   }



   /*******************************************************************************
    ** Fluent setter for enumValues
    **
    *******************************************************************************/
   public QPossibleValueSource withEnumValues(List<QPossibleValue<?>> enumValues)
   {
      this.enumValues = enumValues;
      return this;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addEnumValue(QPossibleValue<?> possibleValue)
   {
      if(this.enumValues == null)
      {
         this.enumValues = new ArrayList<>();
      }
      this.enumValues.add(possibleValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public <T extends PossibleValueEnum<?>> QPossibleValueSource withValuesFromEnum(T[] values)
   {
      for(T t : values)
      {
         addEnumValue(new QPossibleValue<>(t.getPossibleValueId(), t.getPossibleValueLabel()));
      }

      return (this);
   }



   /*******************************************************************************
    ** Getter for customCodeReference
    **
    *******************************************************************************/
   public QCodeReference getCustomCodeReference()
   {
      return customCodeReference;
   }



   /*******************************************************************************
    ** Setter for customCodeReference
    **
    *******************************************************************************/
   public void setCustomCodeReference(QCodeReference customCodeReference)
   {
      this.customCodeReference = customCodeReference;
   }



   /*******************************************************************************
    ** Fluent setter for customCodeReference
    **
    *******************************************************************************/
   public QPossibleValueSource withCustomCodeReference(QCodeReference customCodeReference)
   {
      this.customCodeReference = customCodeReference;
      return (this);
   }

}
