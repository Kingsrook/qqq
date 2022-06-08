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


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import io.javalin.Javalin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class SampleJavalinServer
{
   private static final Logger LOG = LogManager.getLogger(SampleJavalinServer.class);

   private static final int PORT = 8000;

   private QInstance qInstance;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args)
   {
      new SampleJavalinServer().startJavalinServer();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void startJavalinServer()
   {
      qInstance = SampleMetaDataProvider.defineInstance();

      QJavalinImplementation qJavalinImplementation = new QJavalinImplementation(qInstance);
      Javalin service = Javalin.create(config ->
      {
         // todo - not all!!
         config.enableCorsForAllOrigins();
      }).start(PORT);
      service.routes(qJavalinImplementation.getRoutes());
      service.after(ctx ->
         ctx.res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"));
   }

}
