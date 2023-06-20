/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.model.openapi;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonGetter;


/*******************************************************************************
 **
 *******************************************************************************/
public class Schema
{
   private String              type;
   private String              format;
   private String              description;
   private List<String>        enumValues;
   private Schema              items;
   private Map<String, Schema> properties;
   private Object              example;
   private String              ref;
   private List<Schema>        allOf;
   private Boolean             readOnly;
   private Boolean             nullable;
   private Integer             maxLength;



   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   public String getType()
   {
      return (this.type);
   }



   /*******************************************************************************
    ** Setter for type
    *******************************************************************************/
   public void setType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    *******************************************************************************/
   public Schema withType(String type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for format
    *******************************************************************************/
   public String getFormat()
   {
      return (this.format);
   }



   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(String format)
   {
      this.format = format;
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public Schema withFormat(String format)
   {
      this.format = format;
      return (this);
   }



   /*******************************************************************************
    ** Getter for items
    *******************************************************************************/
   public Schema getItems()
   {
      return (this.items);
   }



   /*******************************************************************************
    ** Setter for items
    *******************************************************************************/
   public void setItems(Schema items)
   {
      this.items = items;
   }



   /*******************************************************************************
    ** Fluent setter for items
    *******************************************************************************/
   public Schema withItems(Schema items)
   {
      this.items = items;
      return (this);
   }



   /*******************************************************************************
    ** Getter for properties
    *******************************************************************************/
   public Map<String, Schema> getProperties()
   {
      return (this.properties);
   }



   /*******************************************************************************
    ** Setter for properties
    *******************************************************************************/
   public void setProperties(Map<String, Schema> properties)
   {
      this.properties = properties;
   }



   /*******************************************************************************
    ** Fluent setter for properties
    *******************************************************************************/
   public Schema withProperties(Map<String, Schema> properties)
   {
      this.properties = properties;
      return (this);
   }



   /*******************************************************************************
    ** Getter for example
    *******************************************************************************/
   public Object getExample()
   {
      return (this.example);
   }



   /*******************************************************************************
    ** Setter for example
    *******************************************************************************/
   public void setExample(String example)
   {
      this.example = example;
   }



   /*******************************************************************************
    ** Setter for example
    *******************************************************************************/
   public void setExample(BigDecimal example)
   {
      this.example = example;
   }



   /*******************************************************************************
    ** Fluent setter for example
    *******************************************************************************/
   public Schema withExample(Object example)
   {
      this.example = example;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for example
    *******************************************************************************/
   public Schema withExample(String example)
   {
      this.example = example;
      return (this);
   }



   /*******************************************************************************
    ** Setter for example
    *******************************************************************************/
   public void setExample(List<?> example)
   {
      this.example = example;
   }



   /*******************************************************************************
    ** Fluent setter for example
    *******************************************************************************/
   public Schema withExample(List<?> example)
   {
      this.example = example;
      return (this);
   }



   /*******************************************************************************
    ** Getter for ref
    *******************************************************************************/
   @JsonGetter("$ref")
   public String getRef()
   {
      return (this.ref);
   }



   /*******************************************************************************
    ** Setter for ref
    *******************************************************************************/
   public void setRef(String ref)
   {
      this.ref = ref;
   }



   /*******************************************************************************
    ** Fluent setter for ref
    *******************************************************************************/
   public Schema withRef(String ref)
   {
      this.ref = ref;
      return (this);
   }



   /*******************************************************************************
    ** Getter for allOf
    *******************************************************************************/
   public List<Schema> getAllOf()
   {
      return (this.allOf);
   }



   /*******************************************************************************
    ** Setter for allOf
    *******************************************************************************/
   public void setAllOf(List<Schema> allOf)
   {
      this.allOf = allOf;
   }



   /*******************************************************************************
    ** Fluent setter for allOf
    *******************************************************************************/
   public Schema withAllOf(List<Schema> allOf)
   {
      this.allOf = allOf;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public Schema withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for enumValues
    *******************************************************************************/
   @JsonGetter("enum")
   public List<String> getEnumValues()
   {
      return (this.enumValues);
   }



   /*******************************************************************************
    ** Setter for enumValues
    *******************************************************************************/
   public void setEnumValues(List<String> enumValues)
   {
      this.enumValues = enumValues;
   }



   /*******************************************************************************
    ** Fluent setter for enumValues
    *******************************************************************************/
   public Schema withEnumValues(List<String> enumValues)
   {
      this.enumValues = enumValues;
      return (this);
   }



   /*******************************************************************************
    ** Getter for readOnly
    *******************************************************************************/
   public Boolean getReadOnly()
   {
      return (this.readOnly);
   }



   /*******************************************************************************
    ** Setter for readOnly
    *******************************************************************************/
   public void setReadOnly(Boolean readOnly)
   {
      this.readOnly = readOnly;
   }



   /*******************************************************************************
    ** Fluent setter for readOnly
    *******************************************************************************/
   public Schema withReadOnly(Boolean readOnly)
   {
      this.readOnly = readOnly;
      return (this);
   }



   /*******************************************************************************
    ** Getter for nullable
    *******************************************************************************/
   public Boolean getNullable()
   {
      return (this.nullable);
   }



   /*******************************************************************************
    ** Setter for nullable
    *******************************************************************************/
   public void setNullable(Boolean nullable)
   {
      this.nullable = nullable;
   }



   /*******************************************************************************
    ** Fluent setter for nullable
    *******************************************************************************/
   public Schema withNullable(Boolean nullable)
   {
      this.nullable = nullable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for maxLength
    *******************************************************************************/
   public Integer getMaxLength()
   {
      return (this.maxLength);
   }



   /*******************************************************************************
    ** Setter for maxLength
    *******************************************************************************/
   public void setMaxLength(Integer maxLength)
   {
      this.maxLength = maxLength;
   }



   /*******************************************************************************
    ** Fluent setter for maxLength
    *******************************************************************************/
   public Schema withMaxLength(Integer maxLength)
   {
      this.maxLength = maxLength;
      return (this);
   }

}
