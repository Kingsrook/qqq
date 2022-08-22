package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for StreamedETLWithFrontendProcess
 *******************************************************************************/
class StreamedETLWithFrontendProcessTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QProcessMetaData process = new StreamedETLWithFrontendProcess().defineProcessMetaData();
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);

      for(QFieldMetaData inputField : process.getInputFields())
      {
         if(StreamedETLWithFrontendProcess.FIELD_EXTRACT_CODE.equals(inputField.getName()))
         {
            inputField.setDefaultValue(new QCodeReference(TestExtractStep.class));
         }
         else if(StreamedETLWithFrontendProcess.FIELD_TRANSFORM_CODE.equals(inputField.getName()))
         {
            inputField.setDefaultValue(new QCodeReference(TestTransformStep.class));
         }
         else if(StreamedETLWithFrontendProcess.FIELD_LOAD_CODE.equals(inputField.getName()))
         {
            inputField.setDefaultValue(new QCodeReference(TestLoadStep.class));
         }
      }

      QInstance instance = TestUtils.defineInstance();
      instance.addProcess(process);

      InsertInput insertInput = new InsertInput(instance);
      insertInput.setSession(TestUtils.getMockSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
      insertInput.setRecords(List.of(
         new QRecord().withTableName(TestUtils.TABLE_NAME_SHAPE).withValue("id", 1).withValue("name", "Circle"),
         new QRecord().withTableName(TestUtils.TABLE_NAME_SHAPE).withValue("id", 2).withValue("name", "Triangle"),
         new QRecord().withTableName(TestUtils.TABLE_NAME_SHAPE).withValue("id", 3).withValue("name", "Square")
      ));
      new InsertAction().execute(insertInput);

      List<QRecord> preList = TestUtils.queryTable(TestUtils.TABLE_NAME_SHAPE);

      RunProcessInput request = new RunProcessInput(instance);
      request.setSession(TestUtils.getMockSession());
      request.setProcessName(process.getName());
      request.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);

      assertTrue(result.getException().isEmpty());

      List<QRecord> postList = TestUtils.queryTable(TestUtils.TABLE_NAME_SHAPE);
      assertEquals(6, postList.size());
      assertThat(postList)
         .anyMatch(qr -> qr.getValue("name").equals("Circle"))
         .anyMatch(qr -> qr.getValue("name").equals("Triangle"))
         .anyMatch(qr -> qr.getValue("name").equals("Square"))
         .anyMatch(qr -> qr.getValue("name").equals("Transformed: Circle"))
         .anyMatch(qr -> qr.getValue("name").equals("Transformed: Triangle"))
         .anyMatch(qr -> qr.getValue("name").equals("Transformed: Square"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestExtractStep extends AbstractExtractFunction
   {

      /*******************************************************************************
       ** Execute the backend step - using the request as input, and the result as output.
       **
       *******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         QueryInput queryInput = new QueryInput(runBackendStepInput.getInstance());
         queryInput.setSession(runBackendStepInput.getSession());
         queryInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
         queryInput.setRecordPipe(getRecordPipe());
         new QueryAction().execute(queryInput);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestTransformStep extends AbstractTransformFunction
   {

      /*******************************************************************************
       ** Execute the backend step - using the request as input, and the result as output.
       **
       *******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         for(QRecord qRecord : getInputRecordPage())
         {
            QRecord newQRecord = new QRecord();
            newQRecord.setValue("id", null);
            newQRecord.setValue("name", "Transformed: " + qRecord.getValueString("name"));
            getOutputRecordPage().add(newQRecord);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestLoadStep extends AbstractLoadFunction
   {

      /*******************************************************************************
       ** Execute the backend step - using the request as input, and the result as output.
       **
       *******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         InsertInput insertInput = new InsertInput(runBackendStepInput.getInstance());
         insertInput.setSession(runBackendStepInput.getSession());
         insertInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
         insertInput.setTransaction(transaction);
         insertInput.setRecords(getInputRecordPage());
         new InsertAction().execute(insertInput);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      protected QBackendTransaction doOpenTransaction(RunBackendStepInput runBackendStepInput) throws QException
      {
         return null;
      }
   }

}