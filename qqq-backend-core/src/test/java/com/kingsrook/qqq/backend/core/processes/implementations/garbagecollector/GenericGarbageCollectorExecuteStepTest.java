/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.processes.implementations.garbagecollector;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for GenericGarbageCollectorExecuteStep 
 *******************************************************************************/
class GenericGarbageCollectorExecuteStepTest extends BaseTest
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
   void testErrors() throws Exception
   {
      QContext.getQInstance().addProcess(new GenericGarbageCollectorProcessMetaDataProducer().produce(QContext.getQInstance()));

      RunProcessInput input = new RunProcessInput();
      input.setProcessName(GenericGarbageCollectorProcessMetaDataProducer.NAME);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("Unrecognized table: null");

      input.addValue("table", "notATable");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("Unrecognized table: notATable");

      input.addValue("table", TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("Unrecognized field: null");

      input.addValue("field", "notAField");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("Unrecognized field: notAField");

      input.addValue("field", "firstName");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("not a date");

      input.addValue("field", "timestamp");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("daysBack: null");

      input.addValue("daysBack", "-1");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("daysBack: -1");

      input.addValue("daysBack", "1");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("maxPageSize: null");

      input.addValue("maxPageSize", "-1");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("maxPageSize: -1");

      input.addValue("maxPageSize", "1");
      assertThatThrownBy(() -> runAndThrow(input)).isInstanceOf(QUserFacingException.class).hasMessageContaining("Could not find min date value in table");
   }


   /***************************************************************************
    **
    ***************************************************************************/
   private void runAndThrow(RunProcessInput input) throws Exception
   {
      input.setStartAfterStep(null);
      input.setProcessUUID(null);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      if(runProcessOutput.getException().isPresent())
      {
         throw (runProcessOutput.getException().get());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test30days() throws QException
   {
      insertAndRunGC(30);
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withFilter(new QQueryFilter()));
      assertEquals(2, queryOutput.getRecords().size());
      assertEquals(Set.of(4, 5), queryOutput.getRecords().stream().map(r -> r.getValueInteger("id")).collect(Collectors.toSet()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled("memory aggregator is failing to return an aggregate when no rows found, which is throwing an error...")
   void test100days() throws QException
   {
      insertAndRunGC(100);
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withFilter(new QQueryFilter()));
      assertEquals(5, queryOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test10days() throws QException
   {
      insertAndRunGC(10);
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withFilter(new QQueryFilter()));
      assertEquals(1, queryOutput.getRecords().size());
      assertEquals(Set.of(5), queryOutput.getRecords().stream().map(r -> r.getValueInteger("id")).collect(Collectors.toSet()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test1day() throws QException
   {
      insertAndRunGC(1);
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withFilter(new QQueryFilter()));
      assertEquals(0, queryOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test1dayPartitioned() throws QException
   {
      insertAndRunGC(1, 2);
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withFilter(new QQueryFilter()));
      assertEquals(0, queryOutput.getRecords().size());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void insertAndRunGC(Integer daysBack) throws QException
   {
      insertAndRunGC(daysBack, 1000);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void insertAndRunGC(Integer daysBack, Integer maxPageSize) throws QException
   {
      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withRecords(getPersonRecords()));
      QContext.getQInstance().addProcess(new GenericGarbageCollectorProcessMetaDataProducer().produce(QContext.getQInstance()));

      RunProcessInput input = new RunProcessInput();
      input.setProcessName(GenericGarbageCollectorProcessMetaDataProducer.NAME);
      input.addValue("table", TestUtils.TABLE_NAME_PERSON_MEMORY);
      input.addValue("field", "timestamp");
      input.addValue("daysBack", daysBack);
      input.addValue("maxPageSize", maxPageSize);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      new RunProcessAction().execute(input);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QRecord> getPersonRecords()
   {
      List<QRecord> records = List.of(
         new QRecord().withValue("id", 1).withValue("timestamp", Instant.now().minus(90, ChronoUnit.DAYS)),
         new QRecord().withValue("id", 2).withValue("timestamp", Instant.now().minus(31, ChronoUnit.DAYS)),
         new QRecord().withValue("id", 3).withValue("timestamp", Instant.now().minus(30, ChronoUnit.DAYS).minus(5, ChronoUnit.MINUTES)),
         new QRecord().withValue("id", 4).withValue("timestamp", Instant.now().minus(29, ChronoUnit.DAYS).minus(23, ChronoUnit.HOURS)),
         new QRecord().withValue("id", 5).withValue("timestamp", Instant.now().minus(5, ChronoUnit.DAYS)));
      return records;
   }

}