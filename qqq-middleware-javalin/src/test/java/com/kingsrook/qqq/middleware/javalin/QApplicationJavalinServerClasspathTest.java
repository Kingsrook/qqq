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


import com.kingsrook.qqq.middleware.javalin.routeproviders.SimpleFileSystemDirectoryRouter;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QApplicationJavalinServer with static files served from classpath
 *******************************************************************************/
class QApplicationJavalinServerClasspathTest
{
   private static final int PORT = 6265;
   private QApplicationJavalinServer javalinServer;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStaticRouterFilesFromClasspath() throws Exception
   {
      System.setProperty("qqq.javalin.enableStaticFilesFromJar", "true");

      javalinServer = new QApplicationJavalinServer(new QApplicationJavalinServerTest.TestApplication())
         .withServeFrontendMaterialDashboard(false)
         .withPort(PORT)
         .withAdditionalRouteProvider(new SimpleFileSystemDirectoryRouter("/statically-served-from-jar", "static-site-from-jar/"));

      javalinServer.start();

      Unirest.config().setDefaultResponseEncoding("UTF-8");
      HttpResponse<String> response = Unirest.get("http://localhost:" + PORT + "/statically-served-from-jar/foo-in-jar.html").asString();
      assertEquals("Foo in a Jar!\n", response.getBody());
   }
}