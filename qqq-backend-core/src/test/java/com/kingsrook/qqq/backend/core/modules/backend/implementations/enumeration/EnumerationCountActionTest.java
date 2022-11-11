/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.enumeration;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecordEnum;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for EnumerationCountAction
 *******************************************************************************/
class EnumerationCountActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUnfilteredCount() throws QException
   {
      QInstance instance = defineQInstance();

      CountInput countInput = new CountInput(instance);
      countInput.setSession(new QSession());
      countInput.setTableName("statesEnum");
      CountOutput countOutput = new CountAction().execute(countInput);
      assertEquals(2, countOutput.getCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFilteredCount() throws QException
   {
      QInstance instance = defineQInstance();

      CountInput countInput = new CountInput(instance);
      countInput.setSession(new QSession());
      countInput.setTableName("statesEnum");
      countInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria("population", QCriteriaOperator.GREATER_THAN, List.of(20_000_000))));
      CountOutput countOutput = new CountAction().execute(countInput);
      assertEquals(1, countOutput.getCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QInstance defineQInstance()
   {
      QInstance instance = TestUtils.defineInstance();
      instance.addBackend(new QBackendMetaData()
         .withName("enum")
         .withBackendType("enum")
      );

      instance.addTable(new QTableMetaData()
         .withName("statesEnum")
         .withBackendName("enum")
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("postalCode", QFieldType.STRING))
         .withField(new QFieldMetaData("population", QFieldType.INTEGER))
         .withBackendDetails(new EnumerationTableBackendDetails().withEnumClass(States.class))
      );
      return instance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static enum States implements QRecordEnum
   {
      MO(1, "Missouri", "MO", 15_000_000),
      IL(2, "Illinois", "IL", 25_000_000);


      private final Integer id;
      private final String  name;
      private final String  postalCode;
      private final Integer population;



      /*******************************************************************************
       **
       *******************************************************************************/
      States(int id, String name, String postalCode, int population)
      {
         this.id = id;
         this.name = name;
         this.postalCode = postalCode;
         this.population = population;
      }



      /*******************************************************************************
       ** Getter for id
       **
       *******************************************************************************/
      public Integer getId()
      {
         return id;
      }



      /*******************************************************************************
       ** Getter for name
       **
       *******************************************************************************/
      public String getName()
      {
         return name;
      }



      /*******************************************************************************
       ** Getter for postalCode
       **
       *******************************************************************************/
      public String getPostalCode()
      {
         return postalCode;
      }



      /*******************************************************************************
       ** Getter for population
       **
       *******************************************************************************/
      public Integer getPopulation()
      {
         return population;
      }
   }

}