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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import com.kingsrook.qqq.backend.module.api.actions.QHttpResponse;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;


/*******************************************************************************
 **
 *******************************************************************************/
public class MockHttpResponse implements CloseableHttpResponse
{
   private final MockApiUtilsHelper mockApiUtilsHelper;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public MockHttpResponse(MockApiUtilsHelper mockApiUtilsHelper)
   {
      this.mockApiUtilsHelper = mockApiUtilsHelper;
   }



   @Override
   public void close() throws IOException
   {

   }



   @Override
   public StatusLine getStatusLine()
   {
      ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);

      if(!mockApiUtilsHelper.getMockResponseQueue().isEmpty())
      {
         QHttpResponse qHttpResponse = mockApiUtilsHelper.getMockResponseQueue().peekFirst();
         return (new BasicStatusLine(protocolVersion, qHttpResponse.getStatusCode(), qHttpResponse.getStatusReasonPhrase()));
      }
      else
      {
         return (new BasicStatusLine(protocolVersion, 200, "OK"));
      }
   }



   @Override
   public void setStatusLine(StatusLine statusLine)
   {

   }



   @Override
   public void setStatusLine(ProtocolVersion protocolVersion, int i)
   {

   }



   @Override
   public void setStatusLine(ProtocolVersion protocolVersion, int i, String s)
   {

   }



   @Override
   public void setStatusCode(int i) throws IllegalStateException
   {

   }



   @Override
   public void setReasonPhrase(String s) throws IllegalStateException
   {

   }



   @Override
   public HttpEntity getEntity()
   {
      BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

      if(!mockApiUtilsHelper.getMockResponseQueue().isEmpty())
      {
         QHttpResponse qHttpResponse = mockApiUtilsHelper.getMockResponseQueue().removeFirst();
         basicHttpEntity.setContent(new ByteArrayInputStream(qHttpResponse.getContent().getBytes()));
      }
      else
      {
         basicHttpEntity.setContent(new ByteArrayInputStream("".getBytes()));
      }
      return (basicHttpEntity);
   }



   @Override
   public void setEntity(HttpEntity httpEntity)
   {

   }



   @Override
   public Locale getLocale()
   {
      return null;
   }



   @Override
   public void setLocale(Locale locale)
   {

   }



   @Override
   public ProtocolVersion getProtocolVersion()
   {
      return null;
   }



   @Override
   public boolean containsHeader(String s)
   {
      return false;
   }



   @Override
   public Header[] getHeaders(String s)
   {
      return new Header[0];
   }



   @Override
   public Header getFirstHeader(String s)
   {
      return null;
   }



   @Override
   public Header getLastHeader(String s)
   {
      return null;
   }



   @Override
   public Header[] getAllHeaders()
   {
      return new Header[0];
   }



   @Override
   public void addHeader(Header header)
   {

   }



   @Override
   public void addHeader(String s, String s1)
   {

   }



   @Override
   public void setHeader(Header header)
   {

   }



   @Override
   public void setHeader(String s, String s1)
   {

   }



   @Override
   public void setHeaders(Header[] headers)
   {

   }



   @Override
   public void removeHeader(Header header)
   {

   }



   @Override
   public void removeHeaders(String s)
   {

   }



   @Override
   public HeaderIterator headerIterator()
   {
      return null;
   }



   @Override
   public HeaderIterator headerIterator(String s)
   {
      return null;
   }



   @Override
   public HttpParams getParams()
   {
      return null;
   }



   @Override
   public void setParams(HttpParams httpParams)
   {

   }
}
