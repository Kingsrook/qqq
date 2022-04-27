/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.etl;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public interface QDataSource

{
   /*******************************************************************************
    ** listAvailableBatches
    **
    *******************************************************************************/
   List<String> listAvailableBatches();

   /*******************************************************************************
    ** getBatch
    **
    *******************************************************************************/
   QDataBatch getBatch(String identity, QTableMetaData destination) throws QException;

   /*******************************************************************************
    ** discardBatch
    **
    *******************************************************************************/
   void discardBatch(QDataBatch batch);
}
