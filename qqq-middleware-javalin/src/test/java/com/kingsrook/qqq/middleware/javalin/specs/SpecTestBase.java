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


import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
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
   protected static int PORT = 6273;

   protected static Javalin service;



   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract AbstractEndpointSpec<?, ?, ?> getSpec();



   /***************************************************************************
    **
    ***************************************************************************/
   protected List<AbstractEndpointSpec<?, ?, ?>> getAdditionalSpecs()
   {
      return (Collections.emptyList());
   }



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
               QInstance qInstance;
               try
               {
                  qInstance = defineQInstance();
                  primeTestData(qInstance);
               }
               catch(Exception e)
               {
                  throw new RuntimeException(e);
               }

               AtomicReference<AbstractMiddlewareVersion> middlewareVersionRef = new AtomicReference<>();
               QContext.withTemporaryContext(new CapturedContext(qInstance, new QSystemUserSession()), () ->
                  middlewareVersionRef.set(getMiddlewareVersion()));
               AbstractMiddlewareVersion middlewareVersion = middlewareVersionRef.get();

               AbstractEndpointSpec<?, ?, ?> spec = getSpec();
               spec.setQInstance(qInstance);

               String versionBasePath = middlewareVersion.getVersionBasePath();
               config.router.apiBuilder(() -> spec.defineRoute(middlewareVersion, versionBasePath));

               for(AbstractEndpointSpec<?, ?, ?> additionalSpec : getAdditionalSpecs())
               {
                  additionalSpec.setQInstance(qInstance);
                  config.router.apiBuilder(() -> additionalSpec.defineRoute(middlewareVersion, versionBasePath));
               }
            }
         ).start(PORT);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void primeTestData(QInstance qInstance) throws Exception
   {
      TestUtils.primeTestDatabase();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected AbstractMiddlewareVersion getMiddlewareVersion()
   {
      return new TestMiddlewareVersion();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected QInstance defineQInstance() throws QException
   {
      return (TestUtils.defineInstance());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static class TestMiddlewareVersion extends AbstractMiddlewareVersion
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public String getVersion()
      {
         return "test";
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public List<AbstractEndpointSpec<?, ?, ?>> getEndpointSpecs()
      {
         return List.of();
      }
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
