package com.kingsrook.qqq.backend.core.model.actions;


/*******************************************************************************
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
