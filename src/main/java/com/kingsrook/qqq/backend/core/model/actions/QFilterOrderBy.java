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
}
