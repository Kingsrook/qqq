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
   List<String> listAvailableBatches();

   QDataBatch getBatch(String identity, QTableMetaData destination) throws QException;

   void discardBatch(QDataBatch batch);
}
