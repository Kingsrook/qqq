/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions;


import java.io.Serializable;
import java.util.List;


/*******************************************************************************
 * A single criteria Component of a Query
 *
 *******************************************************************************/
public class QFilterCriteria
{
   private String fieldName;
   private QCriteriaOperator operator;
   private List<Serializable> values;



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
    ** Setter for fieldName
    **
    *******************************************************************************/
   public QFilterCriteria withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return this;
   }



   /*******************************************************************************
    ** Getter for operator
    **
    *******************************************************************************/
   public QCriteriaOperator getOperator()
   {
      return operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public void setOperator(QCriteriaOperator operator)
   {
      this.operator = operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public QFilterCriteria withOperator(QCriteriaOperator operator)
   {
      this.operator = operator;
      return this;
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public List<Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(List<Serializable> values)
   {
      this.values = values;
   }


   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public QFilterCriteria withValues(List<Serializable> values)
   {
      this.values = values;
      return this;
   }
}
