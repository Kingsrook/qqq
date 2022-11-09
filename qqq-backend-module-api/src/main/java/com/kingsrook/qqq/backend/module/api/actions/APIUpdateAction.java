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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


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

      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         ///////////////////////////////////////////////////////////////
         // make post requests for groups of orders that need updated //
         ///////////////////////////////////////////////////////////////
         for(List<QRecord> recordList : CollectionUtils.getPages(updateInput.getRecords(), 20))
         {
            processRecords(table, httpClient, recordList);
            for(QRecord qRecord : recordList)
            {
               updateOutput.addRecord(qRecord);
            }
            if(recordList.size() == 20 && apiActionUtil.getMillisToSleepAfterEveryCall() > 0)
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
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void processRecords(QTableMetaData table, CloseableHttpClient httpClient, List<QRecord> recordList) throws QException
   {
      int sleepMillis      = apiActionUtil.getInitialRateLimitBackoffMillis();
      int rateLimitsCaught = 0;
      while(true)
      {
         try
         {
            doPost(table, httpClient, recordList);
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
   private void doPost(QTableMetaData table, CloseableHttpClient httpClient, List<QRecord> recordList) throws RateLimitException, QException
   {
      try
      {
         String   url     = apiActionUtil.buildTableUrl(table);
         HttpPost request = new HttpPost(url);
         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         request.setEntity(apiActionUtil.recordsToEntity(table, recordList));

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            int    statusCode     = response.getStatusLine().getStatusCode();
            String responseString = EntityUtils.toString(response.getEntity());
            if(statusCode == HttpStatus.SC_TOO_MANY_REQUESTS)
            {
               throw (new RateLimitException(responseString));
            }
            if(statusCode != HttpStatus.SC_MULTI_STATUS && statusCode != HttpStatus.SC_OK)
            {
               String errorMessage = "Did not receive response status code of 200 or 207: " + responseString;
               LOG.warn(errorMessage);
               throw (new QException(errorMessage));
            }
            if(statusCode == HttpStatus.SC_MULTI_STATUS)
            {
               JSONObject responseJSON = new JSONObject(responseString).getJSONObject("response");
               if(!responseJSON.optString("status").contains("200 OK"))
               {
                  String errorMessage = "Did not receive ok status response: " + responseJSON.optString("description");
                  LOG.warn(errorMessage);
                  throw (new QException(errorMessage));
               }
            }
         }
      }
      catch(RateLimitException | QException e)
      {
         throw (e);
      }
      catch(Exception e)
      {
         String errorMessage = "An unexpected error occurred updating entities.";
         LOG.warn(errorMessage, e);
         throw (new QException(errorMessage, e));
      }
   }

}
