package com.kingsrook.qqq.backend.core.instances.loaders.implementations;


import java.nio.charset.StandardCharsets;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.instances.loaders.QMetaDataLoaderException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for GenericMetaDataLoader - providing coverage for AbstractMetaDataLoader.
 *******************************************************************************/
class GenericMetaDataLoaderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testProcess() throws QMetaDataLoaderException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // trying to get some coverage of various types in here (for Abstract loader) //
      ////////////////////////////////////////////////////////////////////////////////
      QProcessMetaData process = new GenericMetaDataLoader<>(QProcessMetaData.class).fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
         class: QProcessMetaData
         version: 1
         name: myProcess
         tableName: someTable
         maxInputRecords: 1
         isHidden: true
         """, StandardCharsets.UTF_8), "myProcess.yaml");

      assertEquals("myProcess", process.getName());
      assertEquals("someTable", process.getTableName());
      assertEquals(1, process.getMaxInputRecords());
      assertTrue(process.getIsHidden());
   }



   /*******************************************************************************
    ** just here for coverage of this class, as we're failing to hit it otherwise.
    *******************************************************************************/
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Test
   void testNoValueException()
   {
      assertThatThrownBy(() -> new GenericMetaDataLoader(QBackendMetaData.class).reflectivelyMapValue(new QInstance(), null, GenericMetaDataLoaderTest.class, "rawValue"));
   }

}