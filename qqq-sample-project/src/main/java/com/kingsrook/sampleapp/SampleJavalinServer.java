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


import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.javalin.QJavalinMetaData;
import com.kingsrook.qqq.middleware.javalin.QApplicationJavalinServer;
import com.kingsrook.sampleapp.metadata.SampleMetaDataProvider;


/*******************************************************************************
 **
 *******************************************************************************/
public class SampleJavalinServer
{
   private static final QLogger LOG = QLogger.getLogger(SampleJavalinServer.class);

   private static final int PORT = 8000;

   private QApplicationJavalinServer qApplicationJavalinServer;



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
      startJavalinServer(PORT);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void startJavalinServer(int port)
   {
      try
      {
         SampleMetaDataProvider.primeTestDatabase("prime-test-database.sql");

         qApplicationJavalinServer = new QApplicationJavalinServer(new SampleMetaDataProvider());
         qApplicationJavalinServer.setServeFrontendMaterialDashboard(true);
         qApplicationJavalinServer.setServeLegacyUnversionedMiddlewareAPI(true);
         qApplicationJavalinServer.setPort(port);
         qApplicationJavalinServer.setJavalinMetaData(new QJavalinMetaData()
            .withUploadedFileArchiveTableName(SampleMetaDataProvider.UPLOAD_FILE_ARCHIVE_TABLE_NAME));
         qApplicationJavalinServer.start();
      }
      catch(Exception e)
      {
         LOG.error("Failed to start javalin server.  See stack trace for details.", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void stopJavalinServer()
   {
      if(qApplicationJavalinServer != null)
      {
         qApplicationJavalinServer.stop();
      }
   }
}
