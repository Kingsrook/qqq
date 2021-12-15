/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.utils;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;


/*******************************************************************************
 ** Utility class for backend-core test classes
 **
 *******************************************************************************/
public class TestUtils
{
   /*******************************************************************************
    ** Define the instance used in standard tests.
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTablePerson());
      return (qInstance);
   }



   /*******************************************************************************
    ** Define the backend used in standard tests - using 'mock' type.
    *******************************************************************************/
   public static QBackendMetaData defineBackend()
   {
      return new QBackendMetaData()
         .withName("default")
         .withType("mock");
   }



   /*******************************************************************************
    ** Define the 'person' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName("person")
         .withLabel("Person")
         .withBackendName(defineBackend().getName())
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE))
         .withField(new QFieldMetaData("email", QFieldType.STRING));
   }

}
