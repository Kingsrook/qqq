/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.state;


import java.io.Serializable;


/*******************************************************************************
 **
 *******************************************************************************/
public interface StateProviderInterface
{

   /*******************************************************************************
    ** Put a block of data, under a key, into the state store.
    *******************************************************************************/
   <T extends Serializable> void put(AbstractStateKey key, T data);

   /*******************************************************************************
    ** Get a block of data, under a key, from the state store.
    *******************************************************************************/
   <T extends Serializable> T get(Class<? extends T> type, AbstractStateKey key);
}
