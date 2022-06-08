/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.actions.etl;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.etl.QDataBatch;
import com.kingsrook.qqq.backend.core.model.etl.QDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunETL
{
   private static final Logger LOG = LogManager.getLogger(RunETL.class);

   /*******************************************************************************
    **
    *******************************************************************************/
   public void run(QInstance instance, QSession session, QDataSource source, QTableMetaData destination) throws QException
   {
      List<String> batchIdentifiers = source.listAvailableBatches();
      if(CollectionUtils.nullSafeHasContents(batchIdentifiers))
      {
         for(String identifier : batchIdentifiers)
         {
            QDataBatch batch = source.getBatch(identifier, destination);
            InsertRequest insertRequest = new InsertRequest(instance);
            insertRequest.setTableName(destination.getName());
            insertRequest.setSession(session);
            insertRequest.setRecords(batch.getRecords());

            InsertAction insertAction = new InsertAction();
            InsertResult insertResult = insertAction.execute(insertRequest);
            System.out.println("** Inserted [" + insertResult.getRecords().size() + "] records into table [" + destination.getName() + "].");
            for(QRecord record : insertRequest.getRecords())
            {
               System.out.println("   Inserted [" + record.getValueString("firstName") + "][" + record.getValueString("lastName") + "].");
            }
            source.discardBatch(batch);
         }
      }
   }
}
