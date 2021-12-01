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
