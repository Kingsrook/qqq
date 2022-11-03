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
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.module.api.exceptions.RateLimitException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class APIUpdateAction extends AbstractAPIAction implements UpdateInterface
{
   private static final Logger LOG = LogManager.getLogger(APIUpdateAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public UpdateOutput execute(UpdateInput updateInput) throws QException
   {
      UpdateOutput updateOutput = new UpdateOutput();
      updateOutput.setRecords(new ArrayList<>());

      if(CollectionUtils.nullSafeIsEmpty(updateInput.getRecords()))
      {
         LOG.info("Update request called with 0 records.  Returning with no-op");
         return (updateOutput);
      }

      QTableMetaData table = updateInput.getTable();
      preAction(updateInput);

      HttpClientConnectionManager connectionManager = null;
      try
      {
         connectionManager = new PoolingHttpClientConnectionManager();

         // todo - supports bulk put?

         for(QRecord record : updateInput.getRecords())
         {
            putRecords(updateOutput, table, connectionManager, record);

            if(updateInput.getRecords().size() > 1 && apiActionUtil.getMillisToSleepAfterEveryCall() > 0)
            {
               SleepUtils.sleep(apiActionUtil.getMillisToSleepAfterEveryCall(), TimeUnit.MILLISECONDS);
            }
         }

         return (updateOutput);
      }
      catch(Exception e)
      {
         LOG.warn("Error in API Insert for [" + table.getName() + "]", e);
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
      finally
      {
         if(connectionManager != null)
         {
            connectionManager.shutdown();
         }
      }

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void putRecords(UpdateOutput updateOutput, QTableMetaData table, HttpClientConnectionManager connectionManager, QRecord record) throws RateLimitException
   {
      int sleepMillis      = apiActionUtil.getInitialRateLimitBackoffMillis();
      int rateLimitsCaught = 0;
      while(true)
      {
         try
         {
            putOneTime(updateOutput, table, connectionManager, record);
            return;
         }
         catch(RateLimitException rle)
         {
            rateLimitsCaught++;
            if(rateLimitsCaught > apiActionUtil.getMaxAllowedRateLimitErrors())
            {
               LOG.warn("Giving up PUT to [" + table.getName() + "] after too many rate-limit errors (" + apiActionUtil.getMaxAllowedRateLimitErrors() + ")");
               record.addError("Error: " + rle.getMessage());
               updateOutput.addRecord(record);
               return;
            }

            LOG.info("Caught RateLimitException [#" + rateLimitsCaught + "] PUT'ing to [" + table.getName() + "] - sleeping [" + sleepMillis + "]...");
            SleepUtils.sleep(sleepMillis, TimeUnit.MILLISECONDS);
            sleepMillis *= 2;
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void putOneTime(UpdateOutput insertOutput, QTableMetaData table, HttpClientConnectionManager connectionManager, QRecord record) throws RateLimitException
   {
      try
      {
         CloseableHttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build();

         String url = buildTableUrl(table);
         url += record.getValueString("number");
         HttpPut request = new HttpPut(url);
         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         request.setEntity(apiActionUtil.recordToEntity(table, record));

         HttpResponse response   = client.execute(request);
         int          statusCode = response.getStatusLine().getStatusCode();
         if(statusCode == 429)
         {
            throw (new RateLimitException(EntityUtils.toString(response.getEntity())));
         }

         QRecord outputRecord = apiActionUtil.processPostResponse(table, record, response);
         insertOutput.addRecord(outputRecord);
      }
      catch(RateLimitException rle)
      {
         throw (rle);
      }
      catch(Exception e)
      {
         LOG.warn("Error posting to [" + table.getName() + "]", e);
         record.addError("Error: " + e.getMessage());
         insertOutput.addRecord(record);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String buildTableUrl(QTableMetaData table)
   {
      return (backendMetaData.getBaseUrl() + "/orders/SalesOrder/");
   }

}
