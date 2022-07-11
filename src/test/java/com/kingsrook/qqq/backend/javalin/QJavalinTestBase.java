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

package com.kingsrook.qqq.backend.javalin;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 ** base class for javalin implementation tests.
 *******************************************************************************/
public class QJavalinTestBase
{
   private static final   int    PORT     = 6262;
   protected static final String BASE_URL = "http://localhost:" + PORT;

   private static QJavalinImplementation qJavalinImplementation;



   /*******************************************************************************
    ** Before the class (all) runs, start a javalin server.
    **
    *******************************************************************************/
   @BeforeAll
   public static void beforeAll()
   {
      qJavalinImplementation = new QJavalinImplementation(TestUtils.defineInstance());
      QJavalinProcessHandler.setAsyncStepTimeoutMillis(500);
      qJavalinImplementation.startJavalinServer(PORT);
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
    ** Fully rebuild the test-database before each test runs, for completely known state.
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      TestUtils.primeTestDatabase();
   }

}
