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


import java.util.Arrays;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;


/*******************************************************************************
 ** class to contain httpResponse data from closable responses
 **
 *******************************************************************************/
public class QHttpResponse
{
   private Integer      statusCode;
   private String       statusProtocolVersion;
   private String       statusReasonPhrase;
   private List<Header> headerList;
   private String       content;



   /*******************************************************************************
    ** Default Constructor for qHttpResponse
    **
    *******************************************************************************/
   public QHttpResponse()
   {
   }



   /*******************************************************************************
    ** Constructor for qHttpResponse
    **
    *******************************************************************************/
   public QHttpResponse(HttpResponse httpResponse) throws Exception
   {
      this.headerList = Arrays.asList(httpResponse.getAllHeaders());
      if(httpResponse.getStatusLine() != null)
      {
         this.statusCode = httpResponse.getStatusLine().getStatusCode();
         this.statusReasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
         if(httpResponse.getStatusLine().getProtocolVersion() != null)
         {
            this.statusProtocolVersion = httpResponse.getStatusLine().getProtocolVersion().toString();
         }
      }
      this.content = EntityUtils.toString(httpResponse.getEntity());
   }



   /*******************************************************************************
    ** Getter for statusCode
    **
    *******************************************************************************/
   public Integer getStatusCode()
   {
      return statusCode;
   }



   /*******************************************************************************
    ** Setter for statusCode
    **
    *******************************************************************************/
   public void setStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
   }



   /*******************************************************************************
    ** Getter for statusProtocolVersion
    **
    *******************************************************************************/
   public String getStatusProtocolVersion()
   {
      return statusProtocolVersion;
   }



   /*******************************************************************************
    ** Setter for statusProtocolVersion
    **
    *******************************************************************************/
   public void setStatusProtocolVersion(String statusProtocolVersion)
   {
      this.statusProtocolVersion = statusProtocolVersion;
   }



   /*******************************************************************************
    ** Getter for statusReasonPhrase
    **
    *******************************************************************************/
   public String getStatusReasonPhrase()
   {
      return statusReasonPhrase;
   }



   /*******************************************************************************
    ** Setter for statusReasonPhrase
    **
    *******************************************************************************/
   public void setStatusReasonPhrase(String statusReasonPhrase)
   {
      this.statusReasonPhrase = statusReasonPhrase;
   }



   /*******************************************************************************
    ** Getter for headerList
    **
    *******************************************************************************/
   public List<Header> getHeaderList()
   {
      return headerList;
   }



   /*******************************************************************************
    ** Setter for headerList
    **
    *******************************************************************************/
   public void setHeaderList(List<Header> headerList)
   {
      this.headerList = headerList;
   }



   /*******************************************************************************
    ** Getter for content
    **
    *******************************************************************************/
   public String getContent()
   {
      return content;
   }



   /*******************************************************************************
    ** Setter for content
    **
    *******************************************************************************/
   public void setContent(String content)
   {
      this.content = content;
   }



   /*******************************************************************************
    ** Fluent setter for content
    **
    *******************************************************************************/
   public QHttpResponse withContent(String content)
   {
      this.content = content;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for statusCode
    *******************************************************************************/
   public QHttpResponse withStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for statusProtocolVersion
    *******************************************************************************/
   public QHttpResponse withStatusProtocolVersion(String statusProtocolVersion)
   {
      this.statusProtocolVersion = statusProtocolVersion;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for statusReasonPhrase
    *******************************************************************************/
   public QHttpResponse withStatusReasonPhrase(String statusReasonPhrase)
   {
      this.statusReasonPhrase = statusReasonPhrase;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for headerList
    *******************************************************************************/
   public QHttpResponse withHeaderList(List<Header> headerList)
   {
      this.headerList = headerList;
      return (this);
   }

}
