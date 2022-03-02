/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.adapters;


import java.io.File;
import java.io.IOException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
class QInstanceAdapterTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void qInstanceToJson()
   {
      QInstance qInstance = TestUtils.defineInstance();
      String json = new QInstanceAdapter().qInstanceToJson(qInstance);
      System.out.println(json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void qInstanceToJsonIncludingBackend()
   {
      QInstance qInstance = TestUtils.defineInstance();
      String json = new QInstanceAdapter().qInstanceToJsonIncludingBackend(qInstance);
      System.out.println(json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void jsonToQInstance() throws IOException
   {
      String json = FileUtils.readFileToString(new File("src/test/resources/personQInstance.json"));
      QInstance qInstance = new QInstanceAdapter().jsonToQInstance(json);
      System.out.println(qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void jsonToQInstanceIncludingBackend() throws IOException
   {
      String json = FileUtils.readFileToString(new File("src/test/resources/personQInstanceIncludingBackend.json"));
      QInstance qInstance = new QInstanceAdapter().jsonToQInstanceIncludingBackends(json);
      System.out.println(qInstance);
      assertNotNull(qInstance.getBackends());
      assertTrue(qInstance.getBackends().size() > 0);
   }
}