/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata.possiblevalues;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** Meta-data to represent a single field in a table.
 **
 *******************************************************************************/
public class QPossibleValueSource<T>
{
   private String name;
   private QPossibleValueSourceType type;

   // should these be in sub-types??
   private List<T> enumValues;



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
   public QPossibleValueSource<T> withName(String name)
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
   public QPossibleValueSource<T> withType(QPossibleValueSourceType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for enumValues
    **
    *******************************************************************************/
   public List<T> getEnumValues()
   {
      return enumValues;
   }



   /*******************************************************************************
    ** Setter for enumValues
    **
    *******************************************************************************/
   public void setEnumValues(List<T> enumValues)
   {
      this.enumValues = enumValues;
   }



   /*******************************************************************************
    ** Fluent setter for enumValues
    **
    *******************************************************************************/
   public QPossibleValueSource<T> withEnumValues(List<T> enumValues)
   {
      this.enumValues = enumValues;
      return this;
   }



   /*******************************************************************************
    ** Fluent adder for enumValues
    **
    *******************************************************************************/
   public QPossibleValueSource<T> addEnumValue(T enumValue)
   {
      if(this.enumValues == null)
      {
         this.enumValues = new ArrayList<>();
      }
      this.enumValues.add(enumValue);
      return this;
   }
}
