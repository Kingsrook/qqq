/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.middleware.javalin.routeproviders;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.middleware.javalin.QJavalinRouteProviderInterface;
import com.kingsrook.qqq.middleware.javalin.metadata.JavalinRouteProviderMetaData;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.staticfiles.StaticFileConfig;


/*******************************************************************************
 **
 *******************************************************************************/
public class SimpleFileSystemDirectoryRouter implements QJavalinRouteProviderInterface
{
   private final String    hostedPath;
   private final String    fileSystemPath;
   private       QInstance qInstance;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SimpleFileSystemDirectoryRouter(String hostedPath, String fileSystemPath)
   {
      this.hostedPath = hostedPath;
      this.fileSystemPath = fileSystemPath;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public SimpleFileSystemDirectoryRouter(JavalinRouteProviderMetaData routeProvider)
   {
      this(routeProvider.getHostedPath(), routeProvider.getFileSystemPath());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void setQInstance(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void acceptJavalinConfig(JavalinConfig config)
   {
      config.staticFiles.add((StaticFileConfig userConfig) ->
      {
         userConfig.hostedPath = hostedPath;
         userConfig.directory = fileSystemPath;
         userConfig.location = Location.EXTERNAL;
      });
   }
}
