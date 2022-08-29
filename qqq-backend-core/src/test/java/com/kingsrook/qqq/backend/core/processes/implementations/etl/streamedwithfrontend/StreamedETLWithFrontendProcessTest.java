package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallback;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



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
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_PERSON,
         ExtractViaQueryStep.class,
         TestTransformShapeToPersonStep.class,
         LoadViaInsertStep.class);
      process.setName("test");
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
      runProcess(instance, process);

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
   void testSimpleSmallQueryTransformUpdate() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      ////////////////////////////////////////////////////////
      // define the process - an ELT from Shapes to Shapes //
      ////////////////////////////////////////////////////////
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_SHAPE,
         ExtractViaQueryStep.class,
         TestTransformUpdateShapeStep.class,
         LoadViaUpdateStep.class);
      process.setName("test");
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      TestUtils.insertDefaultShapes(instance);

      runProcess(instance, process);

      List<QRecord> postList = TestUtils.queryTable(instance, TestUtils.TABLE_NAME_SHAPE);
      for(String name : new String[] { "Circle", "Triangle", "Square" })
      {
         assertThat(postList).as("Should have transformed and updated " + name).anyMatch(qr -> qr.getValue("name").equals("Transformed:" + name));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithDefaultQueryFilter() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      ////////////////////////////////////////////////////////
      // define the process - an ELT from Shapes to Shapes //
      ////////////////////////////////////////////////////////
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_SHAPE,
         ExtractViaQueryStep.class,
         TestTransformUpdateShapeStep.class,
         LoadViaUpdateStep.class);
      process.setName("test");
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      TestUtils.insertDefaultShapes(instance);

      Map<String, Serializable> values = new HashMap<>();
      values.put(StreamedETLWithFrontendProcess.FIELD_DEFAULT_QUERY_FILTER, new QQueryFilter().withCriteria(new QFilterCriteria("name", QCriteriaOperator.EQUALS, List.of("Square"))));
      RunProcessOutput output = runProcess(instance, process, values, null);
      assertEquals(1, output.getValues().get(StreamedETLProcess.FIELD_RECORD_COUNT));

      List<QRecord> postList = TestUtils.queryTable(instance, TestUtils.TABLE_NAME_SHAPE);
      for(String name : new String[] { "Square" })
      {
         assertThat(postList).as("Should have transformed and updated " + name).anyMatch(qr -> qr.getValue("name").equals("Transformed:" + name));
      }

      for(String name : new String[] { "Circle", "Triangle" })
      {
         assertThat(postList).as("Should not have transformed and updated " + name).anyMatch(qr -> qr.getValue("name").equals(name));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBig() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // define the process - an ELT from Persons to Persons - using the mock backend, and set to do very many records //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_PERSON,
         TestUtils.TABLE_NAME_PERSON,
         ExtractViaQueryWithCustomLimitStep.class,
         TestTransformShapeToPersonStep.class,
         LoadViaInsertStep.class);
      process.setName("test");
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      /////////////////////
      // run the process //
      /////////////////////
      RunProcessOutput output = runProcess(instance, process);
      assertEquals(new ExtractViaQueryWithCustomLimitStep().getLimit(), output.getValues().get(StreamedETLProcess.FIELD_RECORD_COUNT));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithValidationStep() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      ////////////////////////////////////////////////////////
      // define the process - an ELT from Shapes to Persons //
      ////////////////////////////////////////////////////////
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_PERSON,
         ExtractViaQueryStep.class,
         TestTransformShapeToPersonWithValidationStep.class,
         LoadViaInsertStep.class);
      process.setName("test");
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      ///////////////////////////////////////////////////////
      // switch the person table to use the memory backend //
      ///////////////////////////////////////////////////////
      instance.getTable(TestUtils.TABLE_NAME_PERSON).setBackendName(TestUtils.MEMORY_BACKEND_NAME);

      TestUtils.insertDefaultShapes(instance);

      ///////////////////////////////////////////////////////////////////////////
      // run the process - breaking on the first instance of the Review screen //
      ///////////////////////////////////////////////////////////////////////////
      RunProcessOutput runProcessOutput = runProcess(instance, process, Map.of(StreamedETLWithFrontendProcess.FIELD_SUPPORTS_FULL_VALIDATION, true), new Callback(), RunProcessInput.FrontendStepBehavior.BREAK);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).hasValue("review");

      ////////////////////////////////////////////////////////
      // continue the process - telling it to do validation //
      ////////////////////////////////////////////////////////
      RunProcessInput runProcessInput = new RunProcessInput(instance);
      runProcessInput.setSession(TestUtils.getMockSession());
      runProcessInput.setProcessName(process.getName());
      runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);
      runProcessInput.setProcessUUID(runProcessOutput.getProcessUUID());
      runProcessInput.setStartAfterStep(runProcessOutput.getProcessState().getNextStepName().get());
      runProcessInput.setValues(Map.of(StreamedETLWithFrontendProcess.FIELD_DO_FULL_VALIDATION, true));
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertNotNull(runProcessOutput);
      assertTrue(runProcessOutput.getException().isEmpty());
      assertThat(runProcessOutput.getProcessState().getNextStepName()).hasValue("review");

      List<ProcessSummaryLine> validationSummaryLines = (List<ProcessSummaryLine>) runProcessOutput.getValues().get(StreamedETLWithFrontendProcess.FIELD_VALIDATION_SUMMARY);
      assertThat(validationSummaryLines)
         .usingRecursiveFieldByFieldElementComparatorOnFields("status", "count")
         .contains(new ProcessSummaryLine(Status.OK, 2, null))
         .contains(new ProcessSummaryLine(Status.ERROR, 1, null));

      ///////////////////////////////////////////////////////
      // continue the process - going to the result screen //
      ///////////////////////////////////////////////////////
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertNotNull(runProcessOutput);
      assertTrue(runProcessOutput.getException().isEmpty());
      assertThat(runProcessOutput.getProcessState().getNextStepName()).hasValue("result");

      ////////////////////////////////////
      // query for the inserted records //
      ////////////////////////////////////
      List<QRecord> postList = TestUtils.queryTable(instance, TestUtils.TABLE_NAME_PERSON);
      assertThat(postList)
         .as("Should not have inserted Circle").noneMatch(qr -> qr.getValue("lastName").equals("Circle"))
         .as("Should have inserted Triangle").anyMatch(qr -> qr.getValue("lastName").equals("Triangle"))
         .as("Should have inserted Square").anyMatch(qr -> qr.getValue("lastName").equals("Square"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunProcessOutput runProcess(QInstance instance, QProcessMetaData process) throws QException
   {
      return (runProcess(instance, process, new HashMap<>(), new Callback()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunProcessOutput runProcess(QInstance instance, QProcessMetaData process, Map<String, Serializable> values, QProcessCallback callback) throws QException
   {
      return (runProcess(instance, process, values, callback, RunProcessInput.FrontendStepBehavior.SKIP));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunProcessOutput runProcess(QInstance instance, QProcessMetaData process, Map<String, Serializable> values, QProcessCallback callback, RunProcessInput.FrontendStepBehavior frontendStepBehavior) throws QException
   {
      RunProcessInput request = new RunProcessInput(instance);
      request.setSession(TestUtils.getMockSession());
      request.setProcessName(process.getName());
      //////////////////////////////////////////////////////////////
      // wrap the map here, in case it was given as un-modifiable //
      //////////////////////////////////////////////////////////////
      request.setValues(new HashMap<>(values));
      request.setFrontendStepBehavior(frontendStepBehavior);
      request.setCallback(callback);

      RunProcessOutput output = new RunProcessAction().execute(request);
      assertNotNull(output);
      assertTrue(output.getException().isEmpty());
      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestTransformShapeToPersonStep extends AbstractTransformStep
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
   public static class TestTransformShapeToPersonWithValidationStep extends AbstractTransformStep implements ProcessSummaryProviderInterface
   {
      private ProcessSummaryLine okSummary          = new ProcessSummaryLine(Status.OK, 0, "can be transformed into a Person");
      private ProcessSummaryLine notAPolygonSummary = new ProcessSummaryLine(Status.ERROR, 0, "cannot be transformed, because they are not a Polygon");



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public ArrayList<ProcessSummaryLine> getProcessSummary(boolean isForResultScreen)
      {
         if(isForResultScreen)
         {
            okSummary.setMessage("were transformed into a Person");
         }

         ArrayList<ProcessSummaryLine> summaryList = new ArrayList<>();
         summaryList.add(okSummary);
         summaryList.add(notAPolygonSummary);
         return (summaryList);
      }



      /*******************************************************************************
       ** Execute the backend step - using the request as input, and the result as output.
       **
       *******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         for(QRecord qRecord : getInputRecordPage())
         {
            if(qRecord.getValueString("name").equals("Circle"))
            {
               notAPolygonSummary.incrementCountAndAddPrimaryKey(qRecord.getValue("id"));
            }
            else
            {
               QRecord newQRecord = new QRecord();
               newQRecord.setValue("firstName", "Johnny");
               newQRecord.setValue("lastName", qRecord.getValueString("name"));
               getOutputRecordPage().add(newQRecord);

               okSummary.incrementCountAndAddPrimaryKey(qRecord.getValue("id"));
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestTransformUpdateShapeStep extends AbstractTransformStep
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
            QRecord updatedQRecord = new QRecord();
            updatedQRecord.setValue("id", qRecord.getValue("id"));
            updatedQRecord.setValue("name", "Transformed:" + qRecord.getValueString("name"));
            getOutputRecordPage().add(updatedQRecord);
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