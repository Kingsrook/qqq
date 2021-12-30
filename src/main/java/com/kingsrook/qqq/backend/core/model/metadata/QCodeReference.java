/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata;


/*******************************************************************************
 **
 *******************************************************************************/
public class QCodeReference
{
   private String name;
   private QCodeType codeType;
   private QCodeUsage codeUsage;



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public QCodeReference withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeType
    **
    *******************************************************************************/
   public QCodeType getCodeType()
   {
      return codeType;
   }



   /*******************************************************************************
    ** Setter for codeType
    **
    *******************************************************************************/
   public void setCodeType(QCodeType codeType)
   {
      this.codeType = codeType;
   }



   /*******************************************************************************
    ** Setter for codeType
    **
    *******************************************************************************/
   public QCodeReference withCodeType(QCodeType codeType)
   {
      this.codeType = codeType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeUsage
    **
    *******************************************************************************/
   public QCodeUsage getCodeUsage()
   {
      return codeUsage;
   }



   /*******************************************************************************
    ** Setter for codeUsage
    **
    *******************************************************************************/
   public void setCodeUsage(QCodeUsage codeUsage)
   {
      this.codeUsage = codeUsage;
   }



   /*******************************************************************************
    ** Setter for codeUsage
    **
    *******************************************************************************/
   public QCodeReference withCodeUsage(QCodeUsage codeUsage)
   {
      this.codeUsage = codeUsage;
      return (this);
   }

}
