/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.state;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractStateKey
{

   /*******************************************************************************
    ** Require all state keys to implement the equals method
    *
    *******************************************************************************/
   @Override
   public abstract boolean equals(Object that);

   /*******************************************************************************
    ** Require all state keys to implement the hashCode method
    *
    *******************************************************************************/
   @Override
   public abstract int hashCode();

   /*******************************************************************************
    ** Require all state keys to implement the toString method
    *
    *******************************************************************************/
   @Override
   public abstract String toString();

}
