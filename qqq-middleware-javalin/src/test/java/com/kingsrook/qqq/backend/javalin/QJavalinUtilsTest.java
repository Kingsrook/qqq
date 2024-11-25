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

package com.kingsrook.qqq.backend.javalin;


import java.io.InputStream;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QJavalinUtils 
 *******************************************************************************/
class QJavalinUtilsTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////
      // demonstrate that calling formParam or queryParam can throw (e.g., on our lame MockContext) //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      assertThatThrownBy(() -> new MockContext(false, false).queryParam("foo"));
      assertThatThrownBy(() -> new MockContext(false, false).formParam("foo"));
      assertEquals("query:foo", new MockContext(true, false).queryParam("foo"));
      assertEquals("form:foo", new MockContext(false, true).formParam("foo"));

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now demonstrate that calling these wrapping util methods avoid such exceptions (which was their intent.) //
      // and, that when the context can return values, that the right ones are used                               //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertNull(QJavalinUtils.getQueryParamOrFormParam(new MockContext(false, false), "foo"));
      assertEquals("query:foo", QJavalinUtils.getQueryParamOrFormParam(new MockContext(true, false), "foo"));
      assertEquals("form:foo", QJavalinUtils.getQueryParamOrFormParam(new MockContext(false, true), "foo"));
      assertEquals("query:foo", QJavalinUtils.getQueryParamOrFormParam(new MockContext(true, true), "foo"));

      assertNull(QJavalinUtils.getFormParamOrQueryParam(new MockContext(false, false), "foo"));
      assertEquals("form:foo", QJavalinUtils.getFormParamOrQueryParam(new MockContext(false, true), "foo"));
      assertEquals("query:foo", QJavalinUtils.getFormParamOrQueryParam(new MockContext(true, false), "foo"));
      assertEquals("form:foo", QJavalinUtils.getFormParamOrQueryParam(new MockContext(true, true), "foo"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static class MockContext implements Context
   {
      boolean returnsQueryParams;
      boolean returnsFormParams;



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public MockContext(boolean returnsQueryParams, boolean returnsFormParams)
      {
         this.returnsQueryParams = returnsQueryParams;
         this.returnsFormParams = returnsFormParams;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Nullable
      @Override
      public String queryParam(@NotNull String key)
      {
         if(this.returnsQueryParams)
         {
            return ("query:" + key);
         }

         return Context.super.queryParam(key);
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Nullable
      @Override
      public String formParam(@NotNull String key)
      {
         if(this.returnsFormParams)
         {
            return ("form:" + key);
         }

         return Context.super.formParam(key);
      }



      @Override
      public boolean strictContentTypes()
      {
         return false;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public HttpServletRequest req()
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public HttpServletResponse res()
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public HandlerType handlerType()
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public String matchedPath()
      {
         return "";
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public String endpointHandlerPath()
      {
         return "";
      }



      @Override
      public <T> T appData(@NotNull Key<T> key)
      {
         return null;
      }



      @Override
      public @NotNull JsonMapper jsonMapper()
      {
         return null;
      }



      @Override
      public <T> T with(@NotNull Class<? extends ContextPlugin<?, T>> aClass)
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public String pathParam(@NotNull String s)
      {
         return "";
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public Map<String, String> pathParamMap()
      {
         return Map.of();
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public ServletOutputStream outputStream()
      {
         return null;
      }



      @Override
      public @NotNull Context minSizeForCompression(int i)
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @NotNull
      @Override
      public Context result(@NotNull InputStream inputStream)
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Nullable
      @Override
      public InputStream resultInputStream()
      {
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void future(@NotNull Supplier<? extends CompletableFuture<?>> supplier)
      {

      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void redirect(@NotNull String s, @NotNull HttpStatus httpStatus)
      {

      }



      @Override
      public void writeJsonStream(@NotNull Stream<?> stream)
      {

      }



      @Override
      public @NotNull Context skipRemainingHandlers()
      {
         return null;
      }



      @Override
      public @NotNull Set<RouteRole> routeRoles()
      {
         return Set.of();
      }
   }
}