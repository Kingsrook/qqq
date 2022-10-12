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

package com.kingsrook.qqq.backend.module.api.actions;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class APIInsertAction extends AbstractAPIAction implements InsertInterface
{
   private static final Logger LOG = LogManager.getLogger(APIInsertAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      InsertOutput insertOutput = new InsertOutput();
      insertOutput.setRecords(new ArrayList<>());

      if(CollectionUtils.nullSafeIsEmpty(insertInput.getRecords()))
      {
         LOG.info("Insert request called with 0 records.  Returning with no-op");
         return (insertOutput);
      }

      QTableMetaData table = insertInput.getTable();

      preAction(insertInput);

      try
      {
         HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
         HttpClient        client            = httpClientBuilder.build();

         String   url     = apiActionUtil.buildTableUrl(table);
         HttpPost request = new HttpPost(url);
         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         // todo - supports bulk post?

         for(QRecord record : insertInput.getRecords())
         {
            try
            {
               request.setEntity(apiActionUtil.recordToEntity(table, record));

               HttpResponse response = client.execute(request);

               QRecord outputRecord = apiActionUtil.processPostResponse(table, record, response);
               insertOutput.addRecord(outputRecord);
            }
            catch(Exception e)
            {
               record.addError("Error: " + e.getMessage());
            }
         }

         return (insertOutput);
      }
      catch(Exception e)
      {
         LOG.warn("Error in API Insert", e);
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
   }

}
