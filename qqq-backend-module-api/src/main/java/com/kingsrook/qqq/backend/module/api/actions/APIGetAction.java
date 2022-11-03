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


import com.kingsrook.qqq.backend.core.actions.interfaces.GetInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class APIGetAction extends AbstractAPIAction implements GetInterface
{
   private static final Logger LOG = LogManager.getLogger(APIGetAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetOutput execute(GetInput getInput) throws QException
   {
      QTableMetaData table = getInput.getTable();
      preAction(getInput);

      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
      try(CloseableHttpClient client = httpClientBuilder.build())
      {
         String urlSuffix = apiActionUtil.buildUrlSuffixForSingleRecordGet(getInput.getPrimaryKey());

         String  url     = apiActionUtil.buildTableUrl(table);
         HttpGet request = new HttpGet(url + urlSuffix);

         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         HttpResponse response = client.execute(request);
         QRecord      record   = apiActionUtil.processSingleRecordGetResponse(table, response);

         GetOutput rs = new GetOutput();
         rs.setRecord(record);
         return rs;
      }
      catch(Exception e)
      {
         LOG.warn("Error in API get", e);
         throw new QException("Error executing get: " + e.getMessage(), e);
      }
   }
}
