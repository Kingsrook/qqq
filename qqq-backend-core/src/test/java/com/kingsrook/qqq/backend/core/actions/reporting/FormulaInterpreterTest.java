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

package com.kingsrook.qqq.backend.core.actions.reporting;


import java.math.BigDecimal;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QFormulaException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.actions.reporting.FormulaInterpreter.interpretFormula;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for FormulaInterpreter
 *******************************************************************************/
class FormulaInterpreterTest
{
   public static final Offset<BigDecimal> ZERO_OFFSET = Offset.offset(BigDecimal.ZERO);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretFormulaSimpleSuccess() throws QFormulaException
   {
      QMetaDataVariableInterpreter vi = new QMetaDataVariableInterpreter();

      assertEquals(new BigDecimal("7"), interpretFormula(vi, "7"));
      assertEquals(new BigDecimal("8"), interpretFormula(vi, "ADD(3,5)"));
      assertEquals(new BigDecimal("9"), interpretFormula(vi, "ADD(2,ADD(3,4))"));
      assertEquals(new BigDecimal("10"), interpretFormula(vi, "ADD(ADD(1,5),4)"));
      assertEquals(new BigDecimal("11"), interpretFormula(vi, "ADD(ADD(1,5),ADD(2,3))"));
      assertEquals(new BigDecimal("15"), interpretFormula(vi, "ADD(1,ADD(2,ADD(3,ADD(4,5))))"));
      assertEquals(new BigDecimal("15"), interpretFormula(vi, "ADD(1,ADD(ADD(2,ADD(3,4)),5))"));
      assertEquals(new BigDecimal("15"), interpretFormula(vi, "ADD(ADD(ADD(ADD(1,2),3),4),5)"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretFormulaWithVariables() throws QFormulaException
   {
      QMetaDataVariableInterpreter vi = new QMetaDataVariableInterpreter();
      vi.addValueMap("input", Map.of("i", 5, "j", 6, "f", new BigDecimal("0.1")));

      assertEquals("5", interpretFormula(vi, "${input.i}"));
      assertEquals(new BigDecimal("8"), interpretFormula(vi, "ADD(3,${input.i})"));
      assertEquals(new BigDecimal("11"), interpretFormula(vi, "ADD(${input.i},${input.j})"));
      assertEquals(new BigDecimal("11.1"), interpretFormula(vi, "ADD(${input.f},ADD(${input.i},${input.j}))"));
      assertEquals(new BigDecimal("11.2"), interpretFormula(vi, "ADD(ADD(${input.f},ADD(${input.i},${input.j})),${input.f})"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretFormulaRecursiveExceptions()
   {
      QMetaDataVariableInterpreter vi = new QMetaDataVariableInterpreter();
      vi.addValueMap("input", Map.of("i", 5, "c", 'c'));

      assertThatThrownBy(() -> interpretFormula(vi, "")).hasMessageContaining("No results");
      assertThatThrownBy(() -> interpretFormula(vi, "NOT-A-FUN(1,2)")).hasMessageContaining("unrecognized expression");
      assertThatThrownBy(() -> interpretFormula(vi, "ADD(1)")).hasMessageContaining("Wrong number of arguments");
      assertThatThrownBy(() -> interpretFormula(vi, "ADD(1,2,3)")).hasMessageContaining("Wrong number of arguments");
      assertThatThrownBy(() -> interpretFormula(vi, "ADD(1,A)")).hasMessageContaining("[A] as a number");
      assertThatThrownBy(() -> interpretFormula(vi, "ADD(1,${input.c})")).hasMessageContaining("[c] as a number");
      // todo - bad syntax (e.g., missing ')'
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFunctions() throws QFormulaException
   {
      QMetaDataVariableInterpreter vi = new QMetaDataVariableInterpreter();

      assertEquals(new BigDecimal("3"), interpretFormula(vi, "ADD(1,2)"));
      assertEquals(new BigDecimal("2"), interpretFormula(vi, "MINUS(4,2)"));
      assertEquals(new BigDecimal("34.500"), interpretFormula(vi, "MULTIPLY(100,0.345)"));

      assertThat((BigDecimal) interpretFormula(vi, "DIVIDE(1,2)")).isCloseTo(new BigDecimal("0.5"), ZERO_OFFSET);
      assertNull(interpretFormula(vi, "DIVIDE(1,0)"));

      assertEquals(new BigDecimal("0.5"), interpretFormula(vi, "ROUND(0.510,1)"));
      assertEquals(new BigDecimal("5.0"), interpretFormula(vi, "ROUND(5.010,2)"));
      assertEquals(new BigDecimal("5"), interpretFormula(vi, "ROUND(5.010,1)"));

      assertEquals(new BigDecimal("0.5100"), interpretFormula(vi, "SCALE(0.510,4)"));
      assertEquals(new BigDecimal("5.01"), interpretFormula(vi, "SCALE(5.010,2)"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QFormulaException
   {
      QMetaDataVariableInterpreter vi = new QMetaDataVariableInterpreter();
      vi.addValueMap("pivot", Map.of("sum.noOfShoes", 5));
      vi.addValueMap("total", Map.of("sum.noOfShoes", 18));

      assertEquals(new BigDecimal("27.78"), interpretFormula(vi, "SCALE(MULTIPLY(100,DIVIDE_SCALE(${pivot.sum.noOfShoes},${total.sum.noOfShoes},6)),2)"));
   }

}