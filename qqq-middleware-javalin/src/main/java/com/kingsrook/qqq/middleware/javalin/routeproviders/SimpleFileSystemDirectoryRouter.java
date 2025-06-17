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


import java.net.URL;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import com.kingsrook.qqq.middleware.javalin.QJavalinRouteProviderInterface;
import com.kingsrook.qqq.middleware.javalin.metadata.JavalinRouteProviderMetaData;
import com.kingsrook.qqq.middleware.javalin.routeproviders.authentication.RouteAuthenticatorInterface;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.staticfiles.StaticFileConfig;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** javalin route provider that hosts a path in the http server via a path on
 ** the file system
 *******************************************************************************/
public class SimpleFileSystemDirectoryRouter implements QJavalinRouteProviderInterface
{
   private QCodeReference routeAuthenticator;
   private QInstance qInstance;
   private final String fileSystemPath;
   private final String hostedPath;

   private static final QLogger LOG = QLogger.getLogger(SimpleFileSystemDirectoryRouter.class);

   public static final String loadStaticFilesFromJarProperty = "qqq.javalin.enableStaticFilesFromJar";
   public static boolean loadStaticFilesFromJar = false;

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SimpleFileSystemDirectoryRouter(String hostedPath, String fileSystemPath)
   {
      this.hostedPath = hostedPath;
      this.fileSystemPath = fileSystemPath;

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // read the property to see if we should load static files from the jar file or from the file system //
      // Javan only supports loading via one method per path, so its a choice of one or the other...       //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      try
      {
         String propertyValue = System.getProperty(SimpleFileSystemDirectoryRouter.loadStaticFilesFromJarProperty, "");
         if(propertyValue.equals("true"))
         {
            loadStaticFilesFromJar = true;
         }
      }
      catch(Exception e)
      {
         loadStaticFilesFromJar = false;
         LOG.warn("Exception attempting to read system property, defaulting to false. ", logPair("system property", SimpleFileSystemDirectoryRouter.loadStaticFilesFromJarProperty));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public SimpleFileSystemDirectoryRouter(JavalinRouteProviderMetaData routeProvider)
   {
      this(routeProvider.getHostedPath(), routeProvider.getFileSystemPath());
      setRouteAuthenticator(routeProvider.getRouteAuthenticator());
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
   private void handleJavalinStaticFileConfig(StaticFileConfig staticFileConfig)
   {

      if(!hostedPath.startsWith("/"))
      {
         LOG.warn("hostedPath should probably start with a leading slash...", logPair("hostedPath", hostedPath));
      }

      /////////////////////////////////////////////////////////////////////////////////////////
      // Handle loading static files from the jar OR the filesystem based on system property //
      /////////////////////////////////////////////////////////////////////////////////////////
      if(SimpleFileSystemDirectoryRouter.loadStaticFilesFromJar)
      {
         staticFileConfig.directory = fileSystemPath;
         staticFileConfig.hostedPath = hostedPath;
         staticFileConfig.location = Location.CLASSPATH;
      }
      else
      {
         URL resource = getClass().getClassLoader().getResource(fileSystemPath);
         if(resource == null)
         {
            String message = "Could not find file system path: " + fileSystemPath;
            if(fileSystemPath.startsWith("/") && getClass().getClassLoader().getResource(fileSystemPath.replaceFirst("^/+", "")) != null)
            {
               message += ".  For non-absolute paths, do not prefix with a leading slash.";
            }
            throw new RuntimeException(message);
         }

         staticFileConfig.directory = resource.getFile();
         staticFileConfig.hostedPath = hostedPath;
         staticFileConfig.location = Location.EXTERNAL;
      }

      LOG.info("Static File Config", logPair("hostedPath", hostedPath), logPair("directory", staticFileConfig.directory), logPair("location", staticFileConfig.location));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void before(Context context) throws QException
   {
      LOG.debug("In before handler for simpleFileSystemRouter", logPair("hostedPath", hostedPath));
      QContext.init(qInstance, new QSystemUserSession());

      if(routeAuthenticator != null)
      {
         try
         {
            RouteAuthenticatorInterface routeAuthenticator = QCodeLoader.getAdHoc(RouteAuthenticatorInterface.class, this.routeAuthenticator);
            boolean                     isAuthenticated    = routeAuthenticator.authenticateRequest(context);
            if(!isAuthenticated)
            {
               LOG.info("Static file request is not authenticated, so telling javalin to skip remaining handlers", logPair("path", context.path()));
               context.skipRemainingHandlers();
            }
         }
         catch(Exception e)
         {
            context.skipRemainingHandlers();
            QJavalinImplementation.handleException(context, e);
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void after(Context context)
   {
      LOG.debug("In after handler for simpleFileSystemRouter", logPair("hostedPath", hostedPath));
      QContext.clear();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void acceptJavalinConfig(JavalinConfig config)
   {
      config.staticFiles.add(this::handleJavalinStaticFileConfig);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void acceptJavalinService(Javalin service)
   {
      String javalinPath = hostedPath;
      if(!javalinPath.endsWith("/"))
      {
         javalinPath += "/";
      }
      javalinPath += "<subPath>";

      service.before(javalinPath, this::before);
      service.before(javalinPath, this::after);
   }



   /*******************************************************************************
    ** Getter for routeAuthenticator
    *******************************************************************************/
   public QCodeReference getRouteAuthenticator()
   {
      return (this.routeAuthenticator);
   }



   /*******************************************************************************
    ** Setter for routeAuthenticator
    *******************************************************************************/
   public void setRouteAuthenticator(QCodeReference routeAuthenticator)
   {
      this.routeAuthenticator = routeAuthenticator;
   }



   /*******************************************************************************
    ** Fluent setter for routeAuthenticator
    *******************************************************************************/
   public SimpleFileSystemDirectoryRouter withRouteAuthenticator(QCodeReference routeAuthenticator)
   {
      this.routeAuthenticator = routeAuthenticator;
      return (this);
   }

}
