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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class APIQueryAction extends AbstractAPIAction implements QueryInterface
{
   private static final Logger LOG = LogManager.getLogger(APIQueryAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      QTableMetaData table = queryInput.getTable();
      preAction(queryInput);

      try
      {
         QQueryFilter filter      = queryInput.getFilter();
         String       paramString = apiActionUtil.buildQueryStringForGet(filter, queryInput.getLimit(), queryInput.getSkip(), table.getFields());

         HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
         HttpClient        client            = httpClientBuilder.build();

         String  url     = apiActionUtil.buildTableUrl(table) + paramString;
         HttpGet request = new HttpGet(url);

         LOG.info("API URL: " + url);

         apiActionUtil.setupAuthorizationInRequest(request);
         apiActionUtil.setupContentTypeInRequest(request);
         apiActionUtil.setupAdditionalHeaders(request);

         HttpResponse  response     = client.execute(request);
         List<QRecord> queryResults = apiActionUtil.processGetResponse(table, response);

         QueryOutput queryOutput = new QueryOutput(queryInput);
         queryOutput.addRecords(queryResults);
         return (queryOutput);
      }
      catch(Exception e)
      {
         LOG.warn("Error in API Query", e);
         throw new QException("Error executing query: " + e.getMessage(), e);
      }
   }

}
