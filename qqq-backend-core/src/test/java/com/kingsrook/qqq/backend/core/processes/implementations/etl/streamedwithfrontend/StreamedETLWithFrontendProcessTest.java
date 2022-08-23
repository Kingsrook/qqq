package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallback;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;
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
   void testSimpleSmallQueryTransformInsert() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      ////////////////////////////////////////////////////////
      // define the process - an ELT from Shapes to Persons //
      ////////////////////////////////////////////////////////
      QProcessMetaData process = new StreamedETLWithFrontendProcess().defineProcessMetaData(TestUtils.TABLE_NAME_SHAPE, TestUtils.TABLE_NAME_PERSON, ExtractViaQueryStep.class, TestTransformStep.class, LoadViaInsertStep.class);
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      ///////////////////////////////////////////////////////
      // switch the person table to use the memory backend //
      ///////////////////////////////////////////////////////
      instance.getTable(TestUtils.TABLE_NAME_PERSON).setBackendName(TestUtils.MEMORY_BACKEND_NAME);

      TestUtils.insertDefaultShapes(instance);

      /////////////////////
      // run the process //
      /////////////////////
      RunProcessInput request = new RunProcessInput(instance);
      request.setSession(TestUtils.getMockSession());
      request.setProcessName(process.getName());
      request.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      request.setCallback(new Callback());

      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getException().isEmpty());

      List<QRecord> postList = TestUtils.queryTable(instance, TestUtils.TABLE_NAME_PERSON);
      assertThat(postList)
         .as("Should have inserted Circle").anyMatch(qr -> qr.getValue("lastName").equals("Circle"))
         .as("Should have inserted Triangle").anyMatch(qr -> qr.getValue("lastName").equals("Triangle"))
         .as("Should have inserted Square").anyMatch(qr -> qr.getValue("lastName").equals("Square"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBig() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // define the process - an ELT from Persons to Persons - using the mock backend, and set to do many many records //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QProcessMetaData process = new StreamedETLWithFrontendProcess().defineProcessMetaData(TestUtils.TABLE_NAME_PERSON, TestUtils.TABLE_NAME_PERSON, ExtractViaQueryWithCustomLimitStep.class, TestTransformStep.class, LoadViaInsertStep.class);
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      /////////////////////
      // run the process //
      /////////////////////
      RunProcessInput request = new RunProcessInput(instance);
      request.setSession(TestUtils.getMockSession());
      request.setProcessName(process.getName());
      request.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      request.setCallback(new Callback());

      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getException().isEmpty());

      assertEquals(new ExtractViaQueryWithCustomLimitStep().getLimit(), result.getValues().get(StreamedETLProcess.FIELD_RECORD_COUNT));

      // todo what can we assert?
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestTransformStep extends AbstractTransformStep
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
            newQRecord.setValue("firstName", "Johnny");
            newQRecord.setValue("lastName", qRecord.getValueString("name"));
            getOutputRecordPage().add(newQRecord);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Callback implements QProcessCallback
   {
      /*******************************************************************************
       ** Get the filter query for this callback.
       *******************************************************************************/
      @Override
      public QQueryFilter getQueryFilter()
      {
         return (new QQueryFilter());
      }



      /*******************************************************************************
       ** Get the field values for this callback.
       *******************************************************************************/
      @Override
      public Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
      {
         return (null);
      }
   }



   /*******************************************************************************
    ** The Mock backend - its query action will return as many rows as the limit -
    ** so let's make sure to give it a big limit.
    *******************************************************************************/
   public static class ExtractViaQueryWithCustomLimitStep extends ExtractViaQueryStep
   {
      @Override
      public Integer getLimit()
      {
         return (10_000);
      }
   }
}