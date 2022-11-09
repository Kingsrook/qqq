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


import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class APICountAction extends AbstractAPIAction implements CountInterface
{
   private static final Logger LOG = LogManager.getLogger(APICountAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public CountOutput execute(CountInput countInput) throws QException
   {
      QTableMetaData table = countInput.getTable();
      preAction(countInput);

      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         QQueryFilter filter      = countInput.getFilter();
         String       paramString = apiActionUtil.buildQueryStringForGet(filter, null, null, table.getFields());

         String url = apiActionUtil.buildTableUrl(table) + paramString;
         LOG.info("API URL: " + url);
         HttpGet request = new HttpGet(url);

         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            Integer count = apiActionUtil.processGetResponseForCount(table, response);

            CountOutput rs = new CountOutput();
            rs.setCount(count);
            return rs;
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error in API count", e);
         throw new QException("Error executing count: " + e.getMessage(), e);
      }
   }
}
