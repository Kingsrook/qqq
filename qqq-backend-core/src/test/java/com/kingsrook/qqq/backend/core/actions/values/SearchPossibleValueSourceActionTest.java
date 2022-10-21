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

package com.kingsrook.qqq.backend.core.actions.values;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for SearchPossibleValueSourceAction
 *******************************************************************************/
class SearchPossibleValueSourceActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach() throws QException
   {
      MemoryRecordStore.getInstance().reset();
      TestUtils.insertDefaultShapes(TestUtils.defineInstance());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @ParameterizedTest
   @NullAndEmptySource
   @ValueSource(strings = { " " })
   void testSearchPvsAction_enumNullAndEmptySearchTerms(String searchTerm) throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutput(searchTerm, TestUtils.POSSIBLE_VALUE_SOURCE_STATE);
      assertEquals(2, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(1) && pv.getLabel().equals("IL"));
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(2) && pv.getLabel().equals("MO"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @ParameterizedTest
   @ValueSource(strings = { "I", "IL", "1", "i", "iL", "il" })
   void testSearchPvsAction_enumMatchesForIL(String searchTerm) throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutput(searchTerm, TestUtils.POSSIBLE_VALUE_SOURCE_STATE);
      assertEquals(1, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(1) && pv.getLabel().equals("IL"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @ParameterizedTest
   @ValueSource(strings = { "3", "ILL" })
   void testSearchPvsAction_enumMatchesNothing(String searchTerm) throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutput(searchTerm, TestUtils.POSSIBLE_VALUE_SOURCE_STATE);
      assertEquals(0, output.getResults().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPvsAction_enumById() throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutputById(2, TestUtils.POSSIBLE_VALUE_SOURCE_STATE);
      assertEquals(1, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(2) && pv.getLabel().equals("MO"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPvsAction_enumByIdNotFound() throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutputById(-1, TestUtils.POSSIBLE_VALUE_SOURCE_STATE);
      assertEquals(0, output.getResults().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @ParameterizedTest
   @NullAndEmptySource
   @ValueSource(strings = { " " })
   void testSearchPvsAction_tableNullAndEmptySearchTerms(String searchTerm) throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutput(searchTerm, TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE);
      assertEquals(3, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(1) && pv.getLabel().equals("Triangle"));
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(2) && pv.getLabel().equals("Square"));
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(3) && pv.getLabel().equals("Circle"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @ParameterizedTest
   @ValueSource(strings = { "1", "Triangle" })
   void testSearchPvsAction_tableMatchesOne(String searchTerm) throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutput(searchTerm, TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE);
      assertEquals(1, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(1) && pv.getLabel().equals("Triangle"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPvsAction_tableById() throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutputById(2, TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE);
      assertEquals(1, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(2) && pv.getLabel().equals("Square"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPvsAction_tableByIds() throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutputByIds(List.of(2, 3), TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE);
      assertEquals(2, output.getResults().size());
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(2) && pv.getLabel().equals("Square"));
      assertThat(output.getResults()).anyMatch(pv -> pv.getId().equals(3) && pv.getLabel().equals("Circle"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPvsAction_tableByIdNotFound() throws QException
   {
      SearchPossibleValueSourceOutput output = getSearchPossibleValueSourceOutputById(-1, TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE);
      assertEquals(0, output.getResults().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput getSearchPossibleValueSourceOutput(String searchTerm, String possibleValueSourceName) throws QException
   {
      SearchPossibleValueSourceInput input = new SearchPossibleValueSourceInput(TestUtils.defineInstance());
      input.setSession(new QSession());
      input.setSearchTerm(searchTerm);
      input.setPossibleValueSourceName(possibleValueSourceName);
      SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceAction().execute(input);
      return output;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput getSearchPossibleValueSourceOutputById(Serializable id, String possibleValueSourceName) throws QException
   {
      SearchPossibleValueSourceInput input = new SearchPossibleValueSourceInput(TestUtils.defineInstance());
      input.setSession(new QSession());
      input.setIdList(List.of(id));
      input.setPossibleValueSourceName(possibleValueSourceName);
      SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceAction().execute(input);
      return output;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput getSearchPossibleValueSourceOutputByIds(List<Serializable> ids, String possibleValueSourceName) throws QException
   {
      SearchPossibleValueSourceInput input = new SearchPossibleValueSourceInput(TestUtils.defineInstance());
      input.setSession(new QSession());
      input.setIdList(ids);
      input.setPossibleValueSourceName(possibleValueSourceName);
      SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceAction().execute(input);
      return output;
   }

}