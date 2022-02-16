/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.session;


/*******************************************************************************
 **
 *******************************************************************************/
public class QUser
{
   private String idReference;
   private String fullName;



   /*******************************************************************************
    ** Getter for idReference
    **
    *******************************************************************************/
   public String getIdReference()
   {
      return idReference;
   }



   /*******************************************************************************
    ** Setter for idReference
    **
    *******************************************************************************/
   public void setIdReference(String idReference)
   {
      this.idReference = idReference;
   }



   /*******************************************************************************
    ** Getter for fullName
    **
    *******************************************************************************/
   public String getFullName()
   {
      return fullName;
   }



   /*******************************************************************************
    ** Setter for fullName
    **
    *******************************************************************************/
   public void setFullName(String fullName)
   {
      this.fullName = fullName;
   }
}
