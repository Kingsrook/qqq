package com.kingsrook.qqq.backend.core.processes.implementations.columnstats;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for ColumnStatsStep 
 *******************************************************************************/
class ColumnStatsStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEmptyStringAndNullRollUpTogether() throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("noOfShoes", 1).withValue("lastName", "Simpson"),
         new QRecord().withValue("noOfShoes", 2).withValue("lastName", "Simpson"),
         new QRecord().withValue("noOfShoes", 2).withValue("lastName", "Simpson"),
         new QRecord().withValue("noOfShoes", 2).withValue("lastName", ""), // this record and the next one -
         new QRecord().withValue("noOfShoes", 3).withValue("lastName", null), // this record and the previous - should both come out as null below
         new QRecord().withValue("noOfShoes", null).withValue("lastName", "Flanders")
      ));
      new InsertAction().execute(insertInput);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY);
      input.addValue("fieldName", "lastName");
      input.addValue("orderBy", "count.desc");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ColumnStatsStep().run(input, output);

      Map<String, Serializable> values = output.getValues();

      @SuppressWarnings("unchecked")
      List<QRecord> valueCounts = (List<QRecord>) values.get("valueCounts");

      assertThat(valueCounts.get(0).getValues()).hasFieldOrPropertyWithValue("lastName", "Simpson").hasFieldOrPropertyWithValue("count", 3);
      assertThat(valueCounts.get(1).getValues()).hasFieldOrPropertyWithValue("lastName", null).hasFieldOrPropertyWithValue("count", 2); // here's the assert for the "" and null record above.
      assertThat(valueCounts.get(2).getValues()).hasFieldOrPropertyWithValue("lastName", "Flanders").hasFieldOrPropertyWithValue("count", 1);
   }

}