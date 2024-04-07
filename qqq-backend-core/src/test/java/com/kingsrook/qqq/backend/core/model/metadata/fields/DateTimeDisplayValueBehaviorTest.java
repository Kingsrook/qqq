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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for DateTimeDisplayValueBehavior 
 *******************************************************************************/
class DateTimeDisplayValueBehaviorTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testZoneIdFromFieldName()
   {
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);

      table.withField(new QFieldMetaData("timeZone", QFieldType.STRING));
      table.getField("createDate").withBehavior(new DateTimeDisplayValueBehavior().withZoneIdFromFieldName("timeZone"));

      QRecord record = new QRecord().withValue("createDate", Instant.parse("2024-04-04T19:12:00Z")).withValue("timeZone", "America/Chicago");
      ValueBehaviorApplier.applyFieldBehaviors(ValueBehaviorApplier.Action.FORMATTING, qInstance, table, List.of(record), null);
      assertEquals("2024-04-04 02:12:00 PM CDT", record.getDisplayValue("createDate"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testZoneIdFromFieldNameWithFallback()
   {
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);

      table.withField(new QFieldMetaData("timeZone", QFieldType.STRING));
      table.getField("createDate").withBehavior(new DateTimeDisplayValueBehavior().withZoneIdFromFieldName("timeZone").withFallbackZoneId("America/Denver"));

      QRecord record = new QRecord().withValue("createDate", Instant.parse("2024-04-04T19:12:00Z")).withValue("timeZone", "whodis");
      ValueBehaviorApplier.applyFieldBehaviors(ValueBehaviorApplier.Action.FORMATTING, qInstance, table, List.of(record), null);
      assertEquals("2024-04-04 01:12:00 PM MDT", record.getDisplayValue("createDate"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDefaultZoneId()
   {
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);

      table.withField(new QFieldMetaData("timeZone", QFieldType.STRING));
      table.getField("createDate").withBehavior(new DateTimeDisplayValueBehavior().withDefaultZoneId("America/Los_Angeles"));

      QRecord record = new QRecord().withValue("createDate", Instant.parse("2024-04-04T19:12:00Z"));
      ValueBehaviorApplier.applyFieldBehaviors(ValueBehaviorApplier.Action.FORMATTING, qInstance, table, List.of(record), null);
      assertEquals("2024-04-04 12:12:00 PM PDT", record.getDisplayValue("createDate"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidation()
   {
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      QFieldMetaData field = table.getField("createDate");
      table.withField(new QFieldMetaData("timeZone", QFieldType.STRING));

      Function<Consumer<DateTimeDisplayValueBehavior>, List<String>> testOne = setup ->
      {
         DateTimeDisplayValueBehavior dateTimeDisplayValueBehavior = new DateTimeDisplayValueBehavior();
         setup.accept(dateTimeDisplayValueBehavior);
         return (dateTimeDisplayValueBehavior.validateBehaviorConfiguration(table, field));
      };

      ///////////////////
      // valid configs //
      ///////////////////
      assertThat(testOne.apply(b -> b.toString())).isEmpty(); // default setup (noop use-case) is valid
      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("timeZone"))).isEmpty();
      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("timeZone").withFallbackZoneId("UTC"))).isEmpty();
      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("timeZone").withFallbackZoneId("America/Chicago"))).isEmpty();
      assertThat(testOne.apply(b -> b.withDefaultZoneId("UTC"))).isEmpty();
      assertThat(testOne.apply(b -> b.withDefaultZoneId("America/Chicago"))).isEmpty();

      /////////////////////
      // invalid configs //
      /////////////////////
      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("notAField")))
         .hasSize(1).first().asString()
         .contains("Unrecognized field name");

      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("id")))
         .hasSize(1).first().asString()
         .contains("A non-STRING type [INTEGER] was specified as the zoneIdFromFieldName field");

      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("timeZone").withDefaultZoneId("UTC")))
         .hasSize(1).first().asString()
         .contains("You may not specify both zoneIdFromFieldName and defaultZoneId");

      assertThat(testOne.apply(b -> b.withDefaultZoneId("UTC").withFallbackZoneId("UTC")))
         .hasSize(2)
         .anyMatch(s -> s.contains("You may not specify both defaultZoneId and fallbackZoneId"))
         .anyMatch(s -> s.contains("You may only set fallbackZoneId if using zoneIdFromFieldName"));

      assertThat(testOne.apply(b -> b.withFallbackZoneId("UTC")))
         .hasSize(1).first().asString()
         .contains("You may only set fallbackZoneId if using zoneIdFromFieldName");

      assertThat(testOne.apply(b -> b.withDefaultZoneId("notAZone")))
         .hasSize(1).first().asString()
         .contains("Invalid ZoneId [notAZone] for [defaultZoneId]");

      assertThat(testOne.apply(b -> b.withZoneIdFromFieldName("timeZone").withFallbackZoneId("notAZone")))
         .hasSize(1).first().asString()
         .contains("Invalid ZoneId [notAZone] for [fallbackZoneId]");

      assertThat(new DateTimeDisplayValueBehavior().validateBehaviorConfiguration(table, table.getField("firstName")))
         .hasSize(1).first().asString()
         .contains("non-DATE_TIME field [firstName]");
   }

}