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

package com.kingsrook.qqq.middleware.javalin.specs;


import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.javalin.TestUtils;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class SpecTestBase
{
   private static int PORT = 6263;

   protected static Javalin service;



   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract AbstractEndpointSpec<?, ?, ?> getSpec();

   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract String getVersion();



   /***************************************************************************
    **
    ***************************************************************************/
   protected String getBaseUrlAndPath()
   {
      return "http://localhost:" + PORT + "/qqq/" + getVersion();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.fullReset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      //////////////////////////////////////////////////////////////////////////////////////
      // during initial dev here, we were having issues running multiple tests together,  //
      // where the second (but not first, and not any after second) would fail w/ javalin //
      // not responding... so, this "works" - to constantly change our port, and stop     //
      // and restart aggresively... could be optimized, but it works.                     //
      //////////////////////////////////////////////////////////////////////////////////////
      PORT++;
      if(service != null)
      {
         service.stop();
         service = null;
      }

      if(service == null)
      {
         service = Javalin.create(config ->
            {
               AbstractEndpointSpec<?, ?, ?> spec = getSpec();
               spec.setQInstance(TestUtils.defineInstance());
               config.router.apiBuilder(() -> spec.defineRoute(getVersion()));
            }
         ).start(PORT);
      }

      TestUtils.primeTestDatabase();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterAll
   static void afterAll()
   {
      if(service != null)
      {
         service.stop();
         service = null;
      }
   }

}
