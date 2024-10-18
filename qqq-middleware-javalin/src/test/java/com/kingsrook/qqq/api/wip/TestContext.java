/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.wip;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;
import io.javalin.config.Key;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.ContextPlugin;
import io.javalin.security.RouteRole;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestContext implements Context
{
   private Map<String, String> queryParams = new LinkedHashMap<>();
   private Map<String, String> pathParams  = new LinkedHashMap<>();
   private Map<String, String> formParams  = new LinkedHashMap<>();

   private InputStream result;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TestContext()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public TestContext withQueryParam(String key, String value)
   {
      queryParams.put(key, value);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public TestContext withPathParam(String key, String value)
   {
      pathParams.put(key, value);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public TestContext withFormParam(String key, String value)
   {
      formParams.put(key, value);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String queryParam(String key)
   {
      return queryParams.get(key);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String formParam(String key)
   {
      return formParams.get(key);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean strictContentTypes()
   {
      return false;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public HttpServletRequest req()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public HttpServletResponse res()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public HandlerType handlerType()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String matchedPath()
   {
      return "";
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String endpointHandlerPath()
   {
      return "";
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public <T> T appData(Key<T> key)
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public JsonMapper jsonMapper()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public <T> T with(Class<? extends ContextPlugin<?, T>> aClass)
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String pathParam(String key)
   {
      return pathParams.get(key);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Map<String, String> pathParamMap()
   {
      return pathParams;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public ServletOutputStream outputStream()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Context minSizeForCompression(int i)
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Context result(InputStream inputStream)
   {
      this.result = inputStream;
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public InputStream resultInputStream()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void future(Supplier<? extends CompletableFuture<?>> supplier)
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void redirect(String s, HttpStatus httpStatus)
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void writeJsonStream(Stream<?> stream)
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Context skipRemainingHandlers()
   {
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Set<RouteRole> routeRoles()
   {
      return Set.of();
   }



   /*******************************************************************************
    ** Getter for response
    **
    *******************************************************************************/
   public String getResultAsString() throws IOException
   {
      byte[] bytes = IOUtils.readFully(result, result.available());
      return new String(bytes, StandardCharsets.UTF_8);
   }

}
