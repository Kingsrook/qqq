/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.sampleapp;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.frontend.picocli.QPicoCliImplementation;


/*******************************************************************************
 **
 *******************************************************************************/
public class SampleCli
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args)
   {
      new SampleCli().run(args);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void run(String[] args)
   {
      QInstance qInstance = SampleMetaDataProvider.defineInstance();
      QPicoCliImplementation qPicoCliImplementation = new QPicoCliImplementation(qInstance);
      int exitCode = qPicoCliImplementation.runCli("my-sample-cli", args);
      System.exit(exitCode);
   }
}
