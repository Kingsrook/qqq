/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.api.mocks;


import java.io.IOException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.api.actions.BaseAPIActionUtil;
import com.kingsrook.qqq.backend.module.api.actions.QHttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;


/*******************************************************************************
 **
 *******************************************************************************/
public class MockApiActionUtils extends BaseAPIActionUtil
{
   public static MockApiUtilsHelper mockApiUtilsHelper;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QHttpResponse makeRequest(QTableMetaData table, HttpRequestBase request) throws QException
   {
      return (mockApiUtilsHelper.defaultMockMakeRequest(mockApiUtilsHelper, table, request, () -> super.makeRequest(table, request)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected CloseableHttpResponse executeHttpRequest(HttpRequestBase request, CloseableHttpClient httpClient) throws IOException
   {
      runMockAsserter(request);
      return new MockHttpResponse(mockApiUtilsHelper);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void runMockAsserter(HttpRequestBase request)
   {
      if(mockApiUtilsHelper.getMockRequestAsserter() != null)
      {
         try
         {
            mockApiUtilsHelper.getMockRequestAsserter().run(request);
         }
         catch(Exception e)
         {
            throw (new RuntimeException("Error running mock request asserter", e));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected CloseableHttpResponse executeOAuthTokenRequest(CloseableHttpClient client, HttpRequestBase request) throws IOException
   {
      runMockAsserter(request);
      return new MockHttpResponse(mockApiUtilsHelper);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected int getInitialRateLimitBackoffMillis()
   {
      return (1);
   }

}
