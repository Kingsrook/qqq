/*
 * Copyright © 2022-2023. ColdTrack <contact@coldtrack.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions.dashboard.widgets;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.QWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;


/*******************************************************************************
 ** AssertJ assert class for widget TableData
 *******************************************************************************/
public class TableDataAssert extends AbstractAssert<TableDataAssert, TableData>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected TableDataAssert(TableData actual, Class<?> selfType)
   {
      super(actual, selfType);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static TableDataAssert assertThat(RenderWidgetOutput widgetOutput)
   {
      Assertions.assertThat(widgetOutput).isNotNull();
      QWidgetData widgetData = widgetOutput.getWidgetData();
      Assertions.assertThat(widgetData).isNotNull();
      Assertions.assertThat(widgetData).isInstanceOf(TableData.class);
      return (new TableDataAssert((TableData) widgetData, TableDataAssert.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static TableDataAssert assertThat(TableData actual)
   {
      return (new TableDataAssert(actual, TableDataAssert.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasSize(int expectedSize)
   {
      Assertions.assertThat(actual.getRows()).hasSize(expectedSize);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasSizeAtLeast(int sizeAtLeast)
   {
      Assertions.assertThat(actual.getRows()).hasSizeGreaterThanOrEqualTo(sizeAtLeast);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert doesNotHaveRowWithColumnContaining(String columnName, String containingValue)
   {
      for(Map<String, Object> row : actual.getRows())
      {
         if(row.containsKey(columnName))
         {
            String value = String.valueOf(row.get(columnName));
            if(value != null && value.contains(containingValue))
            {
               failWithMessage("Failed because a row was found with a value in the [" + columnName + "] column containing [" + containingValue + "]"
                  + (containingValue.equals(value) ? "" : " (full value: [" + value + "])."));
            }
         }
      }

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasRowWithColumnContaining(String columnName, String containingValue)
   {
      hasRowWithColumnContaining(columnName, containingValue, (row) ->
      {
      });
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasRowWithColumnContaining(String columnName, String containingValue, Consumer<TableDataRowAssert> rowAsserter)
   {
      return hasRowWithColumnPredicate(columnName, value -> value != null && value.contains(containingValue), "containing [" + containingValue + "]", rowAsserter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasRowWithColumnMatching(String columnName, String matchingValue)
   {
      hasRowWithColumnMatching(columnName, matchingValue, (row) ->
      {
      });
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasRowWithColumnMatching(String columnName, String matchingValue, Consumer<TableDataRowAssert> rowAsserter)
   {
      return hasRowWithColumnPredicate(columnName, value -> value != null && value.matches(matchingValue), "matching [" + matchingValue + "]", rowAsserter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasRowWithColumnEqualTo(String columnName, String equalToValue)
   {
      hasRowWithColumnEqualTo(columnName, equalToValue, (row) ->
      {
      });
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataAssert hasRowWithColumnEqualTo(String columnName, String equalToValue, Consumer<TableDataRowAssert> rowAsserter)
   {
      return hasRowWithColumnPredicate(columnName, value -> Objects.equals(value, equalToValue), "equalTo [" + equalToValue + "]", rowAsserter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private TableDataAssert hasRowWithColumnPredicate(String columnName, Predicate<String> predicate, String predicateDescription, Consumer<TableDataRowAssert> rowAsserter)
   {
      List<String> foundValuesInColumn = new ArrayList<>();
      for(Map<String, Object> row : actual.getRows())
      {
         if(row.containsKey(columnName))
         {
            String value = String.valueOf(row.get(columnName));
            foundValuesInColumn.add(value);

            if(predicate.test(value))
            {
               TableDataRowAssert tableDataRowAssert = TableDataRowAssert.assertThat(row);
               rowAsserter.accept(tableDataRowAssert);

               return (this);
            }
         }
      }

      if(actual.getRows().isEmpty())
      {
         failWithMessage("Failed because there are no rows in the table.");
      }
      else if(foundValuesInColumn.isEmpty())
      {
         failWithMessage("Failed to find any rows with a column named: [" + columnName + "]");
      }
      else
      {
         failWithMessage("Failed to find a row with column [" + columnName + "] " + predicateDescription
            + ".\nFound values were:\n" + StringUtils.join("\n", foundValuesInColumn));
      }
      return (null);
   }

}
