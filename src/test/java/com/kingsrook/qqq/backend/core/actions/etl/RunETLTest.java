/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions.etl;


import com.kingsrook.qqq.backend.core.model.etl.QDataSource;
import com.kingsrook.qqq.backend.core.model.etl.QFileSystemDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
class RunETLTest
{
   @Test
   public void testRun() throws Exception
   {
      RunETL runETL = new RunETL();

      QDataSource dataSource = new QFileSystemDataSource()
         .withPath("/tmp/etl-source")
         .withGlob("*.csv");

      QInstance qInstance = TestUtils.defineInstance();
      runETL.run(qInstance, TestUtils.getMockSession(), dataSource, qInstance.getTable("person"));
   }
}