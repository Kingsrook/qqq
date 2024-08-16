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

package com.kingsrook.qqq.backend.core.actions.dashboard.widgets;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** AssertJ assert class for a row of data from a widget TableData
 *******************************************************************************/
public class TableDataRowAssert extends AbstractAssert<TableDataRowAssert, Map<String, Object>>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected TableDataRowAssert(Map<String, Object> actual, Class<?> selfType)
   {
      super(actual, selfType);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static TableDataRowAssert assertThat(Map<String, Object> actual)
   {
      return (new TableDataRowAssert(actual, TableDataRowAssert.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataRowAssert hasColumnContaining(String columnName, String containingValue)
   {
      String value = String.valueOf(actual.get(columnName));
      Assertions.assertThat(value)
         .withFailMessage("Expected column [" + columnName + "] in row [" + actual + "] to contain [" + containingValue + "], but it didn't")
         .contains(containingValue);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataRowAssert hasNoSubRows()
   {
      Object subRowsObject = actual.get("subRows");
      if(subRowsObject != null)
      {
         @SuppressWarnings("unchecked")
         List<Map<String, Object>> subRowsList = (List<Map<String, Object>>) subRowsObject;
         if(!subRowsList.isEmpty())
         {
            fail("Row [" + actual + "] should not have had any subRows, but it did.");
         }
      }

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataRowAssert hasSubRowWithColumnContaining(String columnName, String containingValue)
   {
      hasSubRowWithColumnContaining(columnName, containingValue, (row) ->
      {
      });

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private TableDataRowAssert hasSubRowWithColumnPredicate(String columnName, Function<Object, Boolean> predicate, String predicateDescription, Consumer<TableDataRowAssert> rowAsserter)
   {
      Object subRowsObject = actual.get("subRows");
      Assertions.assertThat(subRowsObject)
         .withFailMessage("subRows should not be null").isNotNull()
         .withFailMessage("subRows should be a List").isInstanceOf(List.class);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> subRowsList = (List<Map<String, Object>>) subRowsObject;

      List<String> foundValuesInColumn = new ArrayList<>();
      for(Map<String, Object> row : subRowsList)
      {
         if(row.containsKey(columnName))
         {
            String value = String.valueOf(row.get(columnName));
            foundValuesInColumn.add(value);

            if(value != null && predicate.apply(value))
            {
               TableDataRowAssert tableDataRowAssert = TableDataRowAssert.assertThat(row);
               rowAsserter.accept(tableDataRowAssert);

               return (this);
            }
         }
      }

      if(foundValuesInColumn.isEmpty())
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



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataRowAssert hasSubRowWithColumnMatching(String columnName, String matchesValue, Consumer<TableDataRowAssert> rowAsserter)
   {
      Function<Object, Boolean> predicate = (value) -> ValueUtils.getValueAsString(value).matches(matchesValue);
      return hasSubRowWithColumnPredicate(columnName, predicate, " matching [" + matchesValue + "]", rowAsserter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataRowAssert hasSubRowWithColumnContaining(String columnName, String containingValue, Consumer<TableDataRowAssert> rowAsserter)
   {
      Function<Object, Boolean> predicate = (value) -> ValueUtils.getValueAsString(value).contains(containingValue);
      return hasSubRowWithColumnPredicate(columnName, predicate, " containing [" + containingValue + "]", rowAsserter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableDataRowAssert doesNotHaveSubRowWithColumnContaining(String columnName, String containingValue)
   {
      Object subRowsObject = actual.get("subRows");
      if(subRowsObject != null)
      {
         Assertions.assertThat(subRowsObject).withFailMessage("subRows should be a List").isInstanceOf(List.class);

         @SuppressWarnings("unchecked")
         List<Map<String, Object>> subRowsList = (List<Map<String, Object>>) subRowsObject;

         for(Map<String, Object> row : subRowsList)
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
      }

      return (this);
   }

}
