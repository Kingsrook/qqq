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
import java.util.List;
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
import org.apache.http.client.methods.HttpPost;
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
         LOG.debug("Update request called with 0 records.  Returning with no-op");
         return (updateOutput);
      }

      QTableMetaData table = updateInput.getTable();
      preAction(updateInput);

      HttpClientConnectionManager connectionManager = null;
      try
      {
         connectionManager = new PoolingHttpClientConnectionManager();

         ///////////////////////////////////////////////////////////////
         // make post requests for groups of orders that need updated //
         ///////////////////////////////////////////////////////////////
         for(List<QRecord> recordList : CollectionUtils.getPages(updateInput.getRecords(), 20))
         {
            processRecords(table, connectionManager, recordList);

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
   private void processRecords(QTableMetaData table, HttpClientConnectionManager connectionManager, List<QRecord> recordList)
   {
      int sleepMillis      = apiActionUtil.getInitialRateLimitBackoffMillis();
      int rateLimitsCaught = 0;
      while(true)
      {
         try
         {
            doPost(table, connectionManager, recordList);
            return;
         }
         catch(RateLimitException rle)
         {
            rateLimitsCaught++;
            if(rateLimitsCaught > apiActionUtil.getMaxAllowedRateLimitErrors())
            {
               LOG.warn("Giving up PUT to [" + table.getName() + "] after too many rate-limit errors (" + apiActionUtil.getMaxAllowedRateLimitErrors() + ")");
               return;
            }

            LOG.info("Caught RateLimitException [#" + rateLimitsCaught + "] POSTing to [" + table.getName() + "] - sleeping [" + sleepMillis + "]...");
            SleepUtils.sleep(sleepMillis, TimeUnit.MILLISECONDS);
            sleepMillis *= 2;
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doPost(QTableMetaData table, HttpClientConnectionManager connectionManager, List<QRecord> recordList) throws RateLimitException
   {
      try(CloseableHttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build())
      {
         String   url     = apiActionUtil.buildTableUrl(table);
         HttpPost request = new HttpPost(url);
         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         request.setEntity(apiActionUtil.recordsToEntity(table, recordList));

         HttpResponse response   = client.execute(request);
         int          statusCode = response.getStatusLine().getStatusCode();
         if(statusCode == 429)
         {
            throw (new RateLimitException(EntityUtils.toString(response.getEntity())));
         }
         if(statusCode != 207)
         {
            LOG.warn("Did not receive response status code of 207: " + EntityUtils.toString(response.getEntity()));
         }
      }
      catch(RateLimitException rle)
      {
         throw (rle);
      }
      catch(Exception e)
      {
         LOG.warn("Error posting to [" + table.getName() + "]", e);
      }
   }

}
