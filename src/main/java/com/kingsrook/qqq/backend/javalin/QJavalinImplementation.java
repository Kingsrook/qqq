package com.kingsrook.qqq.backend.javalin;


import com.kingsrook.qqq.backend.core.actions.MetaDataAction;
import com.kingsrook.qqq.backend.core.actions.QueryAction;
import com.kingsrook.qqq.backend.core.actions.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.MetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.MetaDataResult;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;
import com.kingsrook.qqq.backend.core.model.actions.TableMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.TableMetaDataResult;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;


/*******************************************************************************
 **
 *******************************************************************************/
public class QJavalinImplementation
{
   private static final Logger LOG = LogManager.getLogger(QJavalinImplementation.class);

   private static QInstance qInstance;

   private static int PORT = 8001;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args)
   {
      QInstance qInstance = new QInstance();
      // todo - parse args to look up metaData and prime instance
      // qInstance.addBackend(QMetaDataProvider.getQBackend());

      new QJavalinImplementation(qInstance).startJavalinServer(PORT);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJavalinImplementation(QInstance qInstance)
   {
      QJavalinImplementation.qInstance = qInstance;
   }



   /*******************************************************************************
    ** Setter for qInstance
    **
    *******************************************************************************/
   public static void setQInstance(QInstance qInstance)
   {
      QJavalinImplementation.qInstance = qInstance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void startJavalinServer(int port)
   {
      // todo port from arg
      // todo base path from arg?
      Javalin service = Javalin.create().start(port);
      service.routes(getRoutes());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public EndpointGroup getRoutes()
   {
      return (() ->
      {
         path("/metaData", () ->
         {
            get("/", QJavalinImplementation::metaData);
            path("/:table", () ->
            {
               get("", QJavalinImplementation::tableMetaData);
            });
         });
         path("/data", () ->
         {
            path("/:table", () ->
            {
               get("/", QJavalinImplementation::dataQuery);
               post("/", QJavalinImplementation::dataInsert);
               path("/:id", () ->
               {
                  get("", QJavalinImplementation::dataGet);
                  patch("", QJavalinImplementation::dataUpdate);
                  delete("", QJavalinImplementation::dataDelete);
               });
            });
         });
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataDelete(Context context)
   {
      context.result("{\"deleteResult\":{}}");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataUpdate(Context context)
   {
      context.result("{\"updateResult\":{}}");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataInsert(Context context)
   {
      context.result("{\"insertResult\":{}}");
   }



   /*******************************************************************************
    **
    ********************************************************************************/
   private static void dataGet(Context context)
   {
      context.result("{\"getResult\":{}}");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void dataQuery(Context context)
   {
      try
      {
         QueryRequest queryRequest = new QueryRequest(qInstance);
         queryRequest.setTableName(context.pathParam("table"));
         queryRequest.setSkip(integerQueryParam(context, "skip"));
         queryRequest.setLimit(integerQueryParam(context, "limit"));

         QueryAction queryAction = new QueryAction();
         QueryResult queryResult = queryAction.execute(queryRequest);

         context.result(JsonUtils.toJson(queryResult));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void metaData(Context context)
   {
      try
      {
         MetaDataRequest metaDataRequest = new MetaDataRequest(qInstance);
         MetaDataAction metaDataAction = new MetaDataAction();
         MetaDataResult metaDataResult = metaDataAction.execute(metaDataRequest);

         context.result(JsonUtils.toJson(metaDataResult));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void tableMetaData(Context context)
   {
      try
      {
         TableMetaDataRequest tableMetaDataRequest = new TableMetaDataRequest(qInstance);
         tableMetaDataRequest.setTableName(context.pathParam("table"));
         TableMetaDataAction tableMetaDataAction = new TableMetaDataAction();
         TableMetaDataResult tableMetaDataResult = tableMetaDataAction.execute(tableMetaDataRequest);

         context.result(JsonUtils.toJson(tableMetaDataResult));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void handleException(Context context, Exception e)
   {
      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(e, QUserFacingException.class);
      if(userFacingException != null)
      {
         LOG.info("User-facing exception", e);
         context.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            .result("{\"error\":\"" + userFacingException.getMessage() + "\"}");
      }
      else
      {
         LOG.warn("Exception in javalin request", e);
         e.printStackTrace();
         context.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            .result("{\"error\":\"" + e.getClass().getSimpleName() + "\"}");
      }
   }



   /*******************************************************************************
    ** Returns Integer if context has a valid int query parameter by the given name,
    *  Returns null if no param (or empty value).
    *  Throws NumberFormatException for malformed numbers.
    *******************************************************************************/
   private static Integer integerQueryParam(Context context, String name) throws NumberFormatException
   {
      String value = context.queryParam(name);
      if(StringUtils.hasContent(value))
      {
         return (Integer.parseInt(value));
      }

      return (null);
   }
}
