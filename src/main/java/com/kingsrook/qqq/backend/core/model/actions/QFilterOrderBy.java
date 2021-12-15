/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions;


/*******************************************************************************
 ** Bean representing an element of a query order-by clause.
 **
 *******************************************************************************/
public class QFilterOrderBy
{
   private String fieldName;
   private boolean isAscending = true;



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
    ** Fluent Setter for fieldName
    **
    *******************************************************************************/
   public QFilterOrderBy withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isAscending
    **
    *******************************************************************************/
   public boolean getIsAscending()
   {
      return isAscending;
   }



   /*******************************************************************************
    ** Setter for isAscending
    **
    *******************************************************************************/
   public void setIsAscending(boolean ascending)
   {
      isAscending = ascending;
   }



   /*******************************************************************************
    ** Fluent Setter for isAscending
    **
    *******************************************************************************/
   public QFilterOrderBy withIsAscending(boolean ascending)
   {
      this.isAscending = ascending;
      return (this);
   }

}
