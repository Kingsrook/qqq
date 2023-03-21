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

package com.kingsrook.qqq.api.javalin;


import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for QJavalinApiHandler
 *******************************************************************************/
class QJavalinApiHandlerTest
{
   private static final   int    PORT     = 6263;
   protected static final String BASE_URL = "http://localhost:" + PORT;

   private static final String VERSION = "2023.Q1";

   protected static QJavalinImplementation qJavalinImplementation;



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeAll
   static void beforeAll() throws QInstanceValidationException
   {
      QInstance qInstance = TestUtils.defineInstance();
      qJavalinImplementation = new QJavalinImplementation(qInstance);
      qJavalinImplementation.startJavalinServer(PORT);
      qJavalinImplementation.getJavalinService().routes(new QJavalinApiHandler(qInstance).getRoutes());
   }



   /*******************************************************************************
    ** Before the class (all) runs, start a javalin server.
    **
    *******************************************************************************/
   @AfterAll
   public static void afterAll()
   {
      qJavalinImplementation.stopJavalinServer();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSpec()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/openapi.yaml").asString();
      System.out.println(response.getBody());
      assertThat(response.getBody())
         .contains("""
            title: "QQQ API"
            """)
         .contains("""
            /person/query:
            """)
      ;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query").asString();
      System.out.println(response.getBody());
   }

}