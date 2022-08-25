package com.kingsrook.sampleapp;


import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for SampleJavalinServer
 *******************************************************************************/
class SampleJavalinServerTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStartStop()
   {
      SampleJavalinServer sampleJavalinServer = new SampleJavalinServer();
      sampleJavalinServer.startJavalinServer();
      sampleJavalinServer.stopJavalinServer();
   }

}