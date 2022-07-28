package com.kingsrook.sampleapp;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for com.kingsrook.sampleapp.SampleJavalinServer
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