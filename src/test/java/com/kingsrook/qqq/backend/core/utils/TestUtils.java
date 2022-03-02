/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.utils;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QOutputView;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListView;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.mock.MockAuthenticationModule;


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
      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTablePerson());
      qInstance.addPossibleValueSource(defineStatesPossibleValueSource());
      qInstance.addProcess(defineProcessGreetPeople());
      return (qInstance);
   }



   /*******************************************************************************
    ** Define the "states" possible value source used in standard tests
    **
    *******************************************************************************/
   private static QPossibleValueSource<String> defineStatesPossibleValueSource()
   {
      return new QPossibleValueSource<String>()
         .withName("state")
         .withType(QPossibleValueSourceType.ENUM)
         .withEnumValues(List.of("IL", "MO"));
   }



   /*******************************************************************************
    ** Define the authentication used in standard tests - using 'mock' type.
    **
    *******************************************************************************/
   public static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("mock")
         .withType("mock");
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
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("homeState", QFieldType.STRING).withPossibleValueSourceName("state"));
   }



   /*******************************************************************************
    ** Define the 'greet people' process
    *******************************************************************************/
   private static QProcessMetaData defineProcessGreetPeople()
   {
      return new QProcessMetaData()
         .withName("greet")
         .withTableName("person")
         .addFunction(new QFunctionMetaData()
            .withName("prepare")
            .withCode(new QCodeReference()
               .withName("com.kingsrook.qqq.backend.core.interfaces.mock.MockFunctionBody")
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.FUNCTION)) // todo - needed, or implied in this context?
            .withInputData(new QFunctionInputMetaData()
               .withRecordListMetaData(new QRecordListMetaData().withTableName("person"))
               .withFieldList(List.of(
                  new QFieldMetaData("greetingPrefix", QFieldType.STRING),
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING)
               )))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName("person")
                  .addField(new QFieldMetaData("fullGreeting", QFieldType.STRING))
               )
               .withFieldList(List.of(new QFieldMetaData("outputMessage", QFieldType.STRING))))
            .withOutputView(new QOutputView()
               .withMessageField("outputMessage")
               .withRecordListView(new QRecordListView().withFieldNames(List.of("id", "firstName", "lastName", "fullGreeting"))))
         );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QSession getMockSession()
   {
      MockAuthenticationModule mockAuthenticationModule = new MockAuthenticationModule();
      return (mockAuthenticationModule.createSession(null));
   }
}
