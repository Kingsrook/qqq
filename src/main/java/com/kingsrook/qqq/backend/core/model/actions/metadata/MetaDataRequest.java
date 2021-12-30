/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.metadata;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQRequest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Request for the meta-data action
 **
 *******************************************************************************/
public class MetaDataRequest extends AbstractQRequest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public MetaDataRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public MetaDataRequest(QInstance instance)
   {
      super(instance);
   }

}
