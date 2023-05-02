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
import java.util.ArrayDeque;
import java.util.Deque;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeConsumer;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeSupplier;
import com.kingsrook.qqq.backend.module.api.actions.QHttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 **
 *******************************************************************************/
public class MockApiUtilsHelper
{
   private static final QLogger LOG = QLogger.getLogger(MockApiUtilsHelper.class);

   private boolean                                              useMock             = true;
   private Deque<QHttpResponse>                                 mockResponseQueue   = new ArrayDeque<>();
   private UnsafeConsumer<HttpRequestBase, ? extends Throwable> mockRequestAsserter = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void enqueueMockResponse(String json)
   {
      mockResponseQueue.addLast(new QHttpResponse()
         .withStatusCode(200)
         .withContent(json)
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void enqueueMockResponse(QHttpResponse qHttpResponse)
   {
      mockResponseQueue.addLast(qHttpResponse);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QHttpResponse returnMockResponseFromQueue(HttpRequestBase request) throws QException
   {
      if(getMockRequestAsserter() != null)
      {
         try
         {
            getMockRequestAsserter().run(request);
         }
         catch(Exception e)
         {
            throw (new QException("Error running mock request asserter", e));
         }
      }

      if(mockResponseQueue.isEmpty())
      {
         fail("No mock response is in the queue for " + request.getMethod() + " " + request.getURI());
      }

      LOG.info("Returning mock http response for " + request.getMethod() + " " + request.getURI());
      return (mockResponseQueue.removeFirst());
   }



   /*******************************************************************************
    ** Getter for useMock
    *******************************************************************************/
   public boolean getUseMock()
   {
      return (this.useMock);
   }



   /*******************************************************************************
    ** Setter for useMock
    *******************************************************************************/
   public void setUseMock(boolean useMock)
   {
      this.useMock = useMock;
   }



   /*******************************************************************************
    ** Fluent setter for useMock
    *******************************************************************************/
   public MockApiUtilsHelper withUseMock(boolean useMock)
   {
      this.useMock = useMock;
      return (this);
   }



   /*******************************************************************************
    ** Getter for mockResponseQueue
    *******************************************************************************/
   public Deque<QHttpResponse> getMockResponseQueue()
   {
      return (this.mockResponseQueue);
   }



   /*******************************************************************************
    ** Setter for mockResponseQueue
    *******************************************************************************/
   public void setMockResponseQueue(Deque<QHttpResponse> mockResponseQueue)
   {
      this.mockResponseQueue = mockResponseQueue;
   }



   /*******************************************************************************
    ** Fluent setter for mockResponseQueue
    *******************************************************************************/
   public MockApiUtilsHelper withMockResponseQueue(Deque<QHttpResponse> mockResponseQueue)
   {
      this.mockResponseQueue = mockResponseQueue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for mockRequestAsserter
    *******************************************************************************/
   public UnsafeConsumer<HttpRequestBase, ? extends Throwable> getMockRequestAsserter()
   {
      return (this.mockRequestAsserter);
   }



   /*******************************************************************************
    ** Setter for mockRequestAsserter
    *******************************************************************************/
   public void setMockRequestAsserter(UnsafeConsumer<HttpRequestBase, ? extends Throwable> mockRequestAsserter)
   {
      this.mockRequestAsserter = mockRequestAsserter;
   }



   /*******************************************************************************
    ** Fluent setter for mockRequestAsserter
    *******************************************************************************/
   public MockApiUtilsHelper withMockRequestAsserter(UnsafeConsumer<HttpRequestBase, ? extends Throwable> mockRequestAsserter)
   {
      this.mockRequestAsserter = mockRequestAsserter;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QHttpResponse defaultMockMakeRequest(MockApiUtilsHelper mockApiUtilsHelper, QTableMetaData table, HttpRequestBase request, UnsafeSupplier<QHttpResponse, QException> superMethod) throws QException
   {
      if(!mockApiUtilsHelper.getUseMock())
      {
         QHttpResponse superResponse = superMethod.get();
         System.out.println("== non-mock response content: ==");
         System.out.println("Code: " + superResponse.getStatusCode());
         System.out.println(superResponse.getContent());
         System.out.println("== ==");
         return (superResponse);
      }

      return mockApiUtilsHelper.returnMockResponseFromQueue(request);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static String readRequestBody(HttpRequestBase request) throws IOException
   {
      return (StringUtils.join("\n", IOUtils.readLines(((HttpPost) request).getEntity().getContent())));
   }
}
