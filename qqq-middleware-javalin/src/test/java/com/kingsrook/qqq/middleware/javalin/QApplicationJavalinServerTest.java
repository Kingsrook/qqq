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

package com.kingsrook.qqq.middleware.javalin;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.AbstractQQQApplication;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.javalin.TestUtils;
import com.kingsrook.qqq.middleware.javalin.specs.v1.MiddlewareVersionV1;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QApplicationJavalinServer 
 *******************************************************************************/
class QApplicationJavalinServerTest
{
   private static final int PORT = 6265;

   private QApplicationJavalinServer javalinServer;



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      javalinServer.stop();
      TestApplication.callCount = 0;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithLegacyImplementation() throws QException
   {
      javalinServer = new QApplicationJavalinServer(getQqqApplication())
         .withPort(PORT)
         .withServeFrontendMaterialDashboard(false);
      javalinServer.start();

      HttpResponse<String> response = Unirest.get("http://localhost:" + PORT + "/metaData").asString();
      assertEquals(200, response.getStatus());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithoutLegacyImplementation() throws QException
   {
      javalinServer = new QApplicationJavalinServer(getQqqApplication())
         .withPort(PORT)
         .withServeLegacyUnversionedMiddlewareAPI(false)
         .withServeFrontendMaterialDashboard(false);
      javalinServer.start();

      HttpResponse<String> response = Unirest.get("http://localhost:" + PORT + "/metaData").asString();
      assertEquals(404, response.getStatus());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithVersionedImplementation() throws QException
   {
      javalinServer = new QApplicationJavalinServer(getQqqApplication())
         .withPort(PORT)
         .withMiddlewareVersionList(List.of(new MiddlewareVersionV1()))
         .withServeFrontendMaterialDashboard(false);
      javalinServer.start();

      HttpResponse<String> response = Unirest.get("http://localhost:" + PORT + "/qqq/v1/metaData").asString();
      assertEquals(200, response.getStatus());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithoutHotSwap() throws QException
   {
      testWithOrWithoutHotSwap(false);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithHotSwap() throws QException
   {
      testWithOrWithoutHotSwap(true);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void testWithOrWithoutHotSwap(boolean withHotSwap) throws QException
   {
      System.setProperty("qqq.javalin.hotSwapInstance", String.valueOf(withHotSwap));
      javalinServer = new QApplicationJavalinServer(getQqqApplication())
         .withPort(PORT)
         .withMiddlewareVersionList(List.of(new MiddlewareVersionV1()))
         .withMillisBetweenHotSwaps(0)
         .withServeFrontendMaterialDashboard(false);
      javalinServer.start();
      System.clearProperty("qqq.javalin.hotSwapInstance");
      assertThat(TestApplication.callCount).isEqualTo(1);

      HttpResponse<String> response = Unirest.get("http://localhost:" + PORT + "/qqq/v1/metaData").asString();
      assertEquals(200, response.getStatus());

      response = Unirest.get("http://localhost:" + PORT + "/qqq/v1/metaData").asString();
      assertEquals(200, response.getStatus());
      JSONObject metaData   = new JSONObject(response.getBody());
      JSONObject tables     = metaData.getJSONObject("tables");
      String     aTableName = tables.keySet().iterator().next();
      JSONObject aTable     = tables.getJSONObject(aTableName);

      if(withHotSwap)
      {
         assertThat(aTable.getString("label")).doesNotEndWith("1");
         assertThat(TestApplication.callCount).isGreaterThanOrEqualTo(1);
      }
      else
      {
         assertThat(aTable.getString("label")).endsWith("1");
         assertThat(TestApplication.callCount).isEqualTo(1);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static AbstractQQQApplication getQqqApplication()
   {
      return new TestApplication();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TestApplication extends AbstractQQQApplication
   {
      static int callCount = 0;



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QInstance defineQInstance() throws QException
      {
         callCount++;
         QInstance qInstance = TestUtils.defineInstance();

         qInstance.getTables().values().forEach(t -> t.setLabel(t.getLabel() + callCount));

         return (qInstance);
      }
   }

}