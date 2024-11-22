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


import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.AbstractQQQApplication;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.ClassPathUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import com.kingsrook.qqq.middleware.javalin.specs.AbstractMiddlewareVersion;
import com.kingsrook.qqq.middleware.javalin.specs.v1.MiddlewareVersionV1;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 ** Second-generation qqq javalin server.
 **
 ** An evolution over the original QJavalinImplementation, which both managed
 ** the javalin instance itself, but also provided all of the endpoint handlers...
 ** This class instead just configures & starts the server.
 **
 ** Makes several setters available, to let application-developer choose what
 ** standard qqq endpoints are served (e.g., frontend-material-dashboard, the
 ** legacy-unversioned middleware, newer versioned-middleware, and additional qqq
 ** modules or application-defined services (both provided as instances of
 ** QJavalinRouteProviderInterface).
 **
 ** System property `qqq.javalin.hotSwapInstance` (defaults to false), causes the
 ** QInstance to be re-loaded every X millis, to avoid some server restarts while
 ** doing dev.
 *******************************************************************************/
public class QApplicationJavalinServer
{
   private static final QLogger LOG = QLogger.getLogger(QApplicationJavalinServer.class);

   private final AbstractQQQApplication application;

   private Integer                              port                                = 8000;
   private boolean                              serveFrontendMaterialDashboard      = true;
   private boolean                              serveLegacyUnversionedMiddlewareAPI = true;
   private List<AbstractMiddlewareVersion>      middlewareVersionList               = List.of(new MiddlewareVersionV1());
   private List<QJavalinRouteProviderInterface> additionalRouteProviders            = null;
   private Consumer<Javalin>                    javalinConfigurationCustomizer      = null;

   private long                lastQInstanceHotSwapMillis;
   private long                millisBetweenHotSwaps = 2500;
   private Consumer<QInstance> hotSwapCustomizer     = null;

   private Javalin service;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QApplicationJavalinServer(AbstractQQQApplication application)
   {
      this.application = application;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void start() throws QException
   {
      QInstance qInstance = application.defineValidatedQInstance();

      service = Javalin.create(config ->
      {
         if(serveFrontendMaterialDashboard)
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // If you have any assets to add to the web server (e.g., logos, icons) place them at //
            // src/main/resources/material-dashboard-overlay (or a directory of your choice       //
            // under src/main/resources) and use this line of code to tell javalin about it.      //
            // Make sure to add your app-specific directory to the javalin config before the core //
            // material-dashboard directory, so in case the same file exists in both (e.g.,       //
            // favicon.png), the app-specific one will be used.                                   //
            ////////////////////////////////////////////////////////////////////////////////////////
            config.staticFiles.add("/material-dashboard-overlay");

            /////////////////////////////////////////////////////////////////////
            // tell javalin where to find material-dashboard static web assets //
            /////////////////////////////////////////////////////////////////////
            config.staticFiles.add("/material-dashboard");

            ////////////////////////////////////////////////////////////
            // set the index page for the SPA from material dashboard //
            ////////////////////////////////////////////////////////////
            config.spaRoot.addFile("/", "material-dashboard/index.html");
         }

         ///////////////////////////////////////////
         // add qqq routes to the javalin service //
         ///////////////////////////////////////////
         if(serveLegacyUnversionedMiddlewareAPI)
         {
            try
            {
               QJavalinImplementation qJavalinImplementation = new QJavalinImplementation(qInstance);
               config.router.apiBuilder(qJavalinImplementation.getRoutes());
            }
            catch(QInstanceValidationException e)
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // we should be pretty comfortable that this won't happen, because we've pre-validated the instance above... //
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
               throw new RuntimeException(e);
            }
         }

         /////////////////////////////////////
         // versioned qqq middleware routes //
         /////////////////////////////////////
         if(CollectionUtils.nullSafeHasContents(middlewareVersionList))
         {
            config.router.apiBuilder(new QMiddlewareApiSpecHandler(middlewareVersionList).defineJavalinEndpointGroup());
            for(AbstractMiddlewareVersion version : middlewareVersionList)
            {
               version.setQInstance(qInstance);
               config.router.apiBuilder(version.getJavalinEndpointGroup(qInstance));
            }
         }

         ////////////////////////////////////////////////////////////////////////////
         // additional route providers (e.g., application-apis, other middlewares) //
         ////////////////////////////////////////////////////////////////////////////
         for(QJavalinRouteProviderInterface routeProvider : CollectionUtils.nonNullList(additionalRouteProviders))
         {
            routeProvider.setQInstance(qInstance);
            config.router.apiBuilder(routeProvider.getJavalinEndpointGroup());
         }
      });

      //////////////////////////////////////////////////////////////////////////////////////
      // per system property, set the server to hot-swap the q instance before all routes //
      //////////////////////////////////////////////////////////////////////////////////////
      String hotSwapPropertyValue = System.getProperty("qqq.javalin.hotSwapInstance", "false");
      if(BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(hotSwapPropertyValue)))
      {
         LOG.info("Server will hotSwap QInstance before requests every [" + millisBetweenHotSwaps + "] millis.");
         service.before(context -> hotSwapQInstance());
      }

      service.before((Context context) -> context.header("Content-Type", "application/json"));
      service.after(QJavalinImplementation::clearQContext);

      ////////////////////////////////////////////////
      // allow a configuration-customizer to be run //
      ////////////////////////////////////////////////
      if(javalinConfigurationCustomizer != null)
      {
         javalinConfigurationCustomizer.accept(service);
      }

      service.start(port);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void stop()
   {
      if(this.service == null)
      {
         LOG.info("Stop called, but there is no javalin service, so noop.");
         return;
      }

      this.service.stop();
   }



   /*******************************************************************************
    ** If there's a qInstanceHotSwapSupplier, and its been a little while, replace
    ** the qInstance with a new one from the supplier.  Meant to be used while doing
    ** development.
    *******************************************************************************/
   public void hotSwapQInstance()
   {
      long now = System.currentTimeMillis();
      if(now - lastQInstanceHotSwapMillis < millisBetweenHotSwaps)
      {
         return;
      }

      lastQInstanceHotSwapMillis = now;

      try
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // clear the cache of classes in this class, so that new classes can be found if a meta-data-producer is being used //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         ClassPathUtils.clearTopLevelClassCache();

         ///////////////////////////////////////////////////////////////
         // try to get a new, validated instance from the application //
         ///////////////////////////////////////////////////////////////
         QInstance newQInstance = application.defineValidatedQInstance();
         if(newQInstance == null)
         {
            LOG.warn("Got a null qInstance from the application.defineQInstance().  Not hot-swapping.");
            return;
         }

         ////////////////////////////////////////
         // allow a hot-swap customizer to run //
         ////////////////////////////////////////
         if(hotSwapCustomizer != null)
         {
            hotSwapCustomizer.accept(newQInstance);
         }

         ///////////////////////////////////////////////////////////////////////
         // pass the new qInstance into all of the objects serving qqq routes //
         ///////////////////////////////////////////////////////////////////////
         if(serveLegacyUnversionedMiddlewareAPI)
         {
            QJavalinImplementation.setQInstance(newQInstance);
         }

         if(CollectionUtils.nullSafeHasContents(middlewareVersionList))
         {
            for(AbstractMiddlewareVersion spec : CollectionUtils.nonNullList(middlewareVersionList))
            {
               spec.setQInstance(newQInstance);
            }
         }

         for(QJavalinRouteProviderInterface routeProvider : CollectionUtils.nonNullList(additionalRouteProviders))
         {
            routeProvider.setQInstance(newQInstance);
         }

         LOG.info("Swapped qInstance");
      }
      catch(QInstanceValidationException e)
      {
         LOG.error("Validation Error while hot-swapping QInstance", e);
      }
      catch(Exception e)
      {
         LOG.error("Error hot-swapping QInstance", e);
      }
   }



   /*******************************************************************************
    ** Getter for port
    *******************************************************************************/
   public Integer getPort()
   {
      return (this.port);
   }



   /*******************************************************************************
    ** Setter for port
    *******************************************************************************/
   public void setPort(Integer port)
   {
      this.port = port;
   }



   /*******************************************************************************
    ** Fluent setter for port
    *******************************************************************************/
   public QApplicationJavalinServer withPort(Integer port)
   {
      this.port = port;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setMillisBetweenHotSwaps(long millisBetweenHotSwaps)
   {
      this.millisBetweenHotSwaps = millisBetweenHotSwaps;
   }



   /*******************************************************************************
    ** Getter for serveFrontendMaterialDashboard
    *******************************************************************************/
   public boolean getServeFrontendMaterialDashboard()
   {
      return (this.serveFrontendMaterialDashboard);
   }



   /*******************************************************************************
    ** Setter for serveFrontendMaterialDashboard
    *******************************************************************************/
   public void setServeFrontendMaterialDashboard(boolean serveFrontendMaterialDashboard)
   {
      this.serveFrontendMaterialDashboard = serveFrontendMaterialDashboard;
   }



   /*******************************************************************************
    ** Fluent setter for serveFrontendMaterialDashboard
    *******************************************************************************/
   public QApplicationJavalinServer withServeFrontendMaterialDashboard(boolean serveFrontendMaterialDashboard)
   {
      this.serveFrontendMaterialDashboard = serveFrontendMaterialDashboard;
      return (this);
   }



   /*******************************************************************************
    ** Getter for serveLegacyUnversionedMiddlewareAPI
    *******************************************************************************/
   public boolean getServeLegacyUnversionedMiddlewareAPI()
   {
      return (this.serveLegacyUnversionedMiddlewareAPI);
   }



   /*******************************************************************************
    ** Setter for serveLegacyUnversionedMiddlewareAPI
    *******************************************************************************/
   public void setServeLegacyUnversionedMiddlewareAPI(boolean serveLegacyUnversionedMiddlewareAPI)
   {
      this.serveLegacyUnversionedMiddlewareAPI = serveLegacyUnversionedMiddlewareAPI;
   }



   /*******************************************************************************
    ** Fluent setter for serveLegacyUnversionedMiddlewareAPI
    *******************************************************************************/
   public QApplicationJavalinServer withServeLegacyUnversionedMiddlewareAPI(boolean serveLegacyUnversionedMiddlewareAPI)
   {
      this.serveLegacyUnversionedMiddlewareAPI = serveLegacyUnversionedMiddlewareAPI;
      return (this);
   }



   /*******************************************************************************
    ** Getter for middlewareVersionList
    *******************************************************************************/
   public List<AbstractMiddlewareVersion> getMiddlewareVersionList()
   {
      return (this.middlewareVersionList);
   }



   /*******************************************************************************
    ** Setter for middlewareVersionList
    *******************************************************************************/
   public void setMiddlewareVersionList(List<AbstractMiddlewareVersion> middlewareVersionList)
   {
      this.middlewareVersionList = middlewareVersionList;
   }



   /*******************************************************************************
    ** Fluent setter for middlewareVersionList
    *******************************************************************************/
   public QApplicationJavalinServer withMiddlewareVersionList(List<AbstractMiddlewareVersion> middlewareVersionList)
   {
      this.middlewareVersionList = middlewareVersionList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for additionalRouteProviders
    *******************************************************************************/
   public List<QJavalinRouteProviderInterface> getAdditionalRouteProviders()
   {
      return (this.additionalRouteProviders);
   }



   /*******************************************************************************
    ** Setter for additionalRouteProviders
    *******************************************************************************/
   public void setAdditionalRouteProviders(List<QJavalinRouteProviderInterface> additionalRouteProviders)
   {
      this.additionalRouteProviders = additionalRouteProviders;
   }



   /*******************************************************************************
    ** Fluent setter for additionalRouteProviders
    *******************************************************************************/
   public QApplicationJavalinServer withAdditionalRouteProviders(List<QJavalinRouteProviderInterface> additionalRouteProviders)
   {
      this.additionalRouteProviders = additionalRouteProviders;
      return (this);
   }



   /*******************************************************************************
    ** Getter for MILLIS_BETWEEN_HOT_SWAPS
    *******************************************************************************/
   public long getMillisBetweenHotSwaps()
   {
      return (millisBetweenHotSwaps);
   }



   /*******************************************************************************
    ** Fluent setter for MILLIS_BETWEEN_HOT_SWAPS
    *******************************************************************************/
   public QApplicationJavalinServer withMillisBetweenHotSwaps(long millisBetweenHotSwaps)
   {
      this.millisBetweenHotSwaps = millisBetweenHotSwaps;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hotSwapCustomizer
    *******************************************************************************/
   public Consumer<QInstance> getHotSwapCustomizer()
   {
      return (this.hotSwapCustomizer);
   }



   /*******************************************************************************
    ** Setter for hotSwapCustomizer
    *******************************************************************************/
   public void setHotSwapCustomizer(Consumer<QInstance> hotSwapCustomizer)
   {
      this.hotSwapCustomizer = hotSwapCustomizer;
   }



   /*******************************************************************************
    ** Fluent setter for hotSwapCustomizer
    *******************************************************************************/
   public QApplicationJavalinServer withHotSwapCustomizer(Consumer<QInstance> hotSwapCustomizer)
   {
      this.hotSwapCustomizer = hotSwapCustomizer;
      return (this);
   }



   /*******************************************************************************
    ** Getter for javalinConfigurationCustomizer
    *******************************************************************************/
   public Consumer<Javalin> getJavalinConfigurationCustomizer()
   {
      return (this.javalinConfigurationCustomizer);
   }



   /*******************************************************************************
    ** Setter for javalinConfigurationCustomizer
    *******************************************************************************/
   public void setJavalinConfigurationCustomizer(Consumer<Javalin> javalinConfigurationCustomizer)
   {
      this.javalinConfigurationCustomizer = javalinConfigurationCustomizer;
   }



   /*******************************************************************************
    ** Fluent setter for javalinConfigurationCustomizer
    *******************************************************************************/
   public QApplicationJavalinServer withJavalinConfigurationCustomizer(Consumer<Javalin> javalinConfigurationCustomizer)
   {
      this.javalinConfigurationCustomizer = javalinConfigurationCustomizer;
      return (this);
   }

}
