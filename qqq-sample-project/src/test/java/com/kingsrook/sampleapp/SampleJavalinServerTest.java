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

package com.kingsrook.sampleapp;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.sampleapp.metadata.SampleMetaDataProvider;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Verifies that the sample starts a real HTTP server and serves its dashboard.
 *******************************************************************************/
class SampleJavalinServerTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStartStop() throws Exception
   {
      String originalMockAuthentication = System.getProperty("qqq.sample.mockAuthentication");
      SampleJavalinServer sampleJavalinServer = new SampleJavalinServer();
      AtomicReference<Javalin> service = new AtomicReference<>();
      sampleJavalinServer.setPort(0);
      sampleJavalinServer.withJavalinConfigurationCustomizer(service::set);

      try
      {
         System.setProperty("qqq.sample.mockAuthentication", "true");
         assertEquals(QAuthenticationType.MOCK, new SampleMetaDataProvider().defineQInstance().getAuthentication().getType());
         sampleJavalinServer.start();

         try(HttpClient client = HttpClient.newHttpClient())
         {
            HttpResponse<String> response = client.send(
               HttpRequest.newBuilder(URI.create("http://localhost:" + service.get().port() + "/")).build(),
               HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("<html"));
         }

         System.clearProperty("qqq.sample.mockAuthentication");
         assertEquals(QAuthenticationType.OAUTH2, new SampleMetaDataProvider().defineQInstance().getAuthentication().getType());
      }
      finally
      {
         sampleJavalinServer.stop();
         QContext.clear();
         if(originalMockAuthentication == null)
         {
            System.clearProperty("qqq.sample.mockAuthentication");
         }
         else
         {
            System.setProperty("qqq.sample.mockAuthentication", originalMockAuthentication);
         }
      }
   }
}
