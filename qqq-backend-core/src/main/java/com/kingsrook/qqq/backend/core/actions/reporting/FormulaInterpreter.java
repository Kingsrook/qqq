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


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.exceptions.QFormulaException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class FormulaInterpreter
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public static Serializable interpretFormula(QMetaDataVariableInterpreter variableInterpreter, String formula) throws QFormulaException
   {
      List<Serializable> results = interpretFormula(variableInterpreter, formula, new AtomicInteger(0));
      if(results.size() == 1)
      {
         return (results.get(0));
      }
      else if(results.isEmpty())
      {
         throw (new QFormulaException("No results from formula"));
      }
      else
      {
         throw (new QFormulaException("More than 1 result from formula"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<Serializable> interpretFormula(QMetaDataVariableInterpreter variableInterpreter, String formula, AtomicInteger i) throws QFormulaException
   {
      StringBuilder      functionName = new StringBuilder();
      List<Serializable> result       = new ArrayList<>();

      char previousChar = 0;
      while(i.get() < formula.length())
      {
         if(i.get() > 0)
         {
            previousChar = formula.charAt(i.get() - 1);
         }
         char c = formula.charAt(i.getAndIncrement());
         if(c == '(' && i.get() < formula.length() - 1)
         {
            //////////////////////////////////////////////////////////////////////////////////////////////////
            // open paren means:  go into a sub-parse - to get a list of arguments for the current function //
            //////////////////////////////////////////////////////////////////////////////////////////////////
            List<Serializable> args     = interpretFormula(variableInterpreter, formula, i);
            Serializable       evaluate = evaluate(functionName.toString(), args, variableInterpreter);
            result.add(evaluate);
         }
         else if(c == ')')
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // close paren means:  end this sub-parse.  evaluate the current function, add it to the result list, and return the result list. //
            // unless we just closed a paren.                                                                                                 //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(previousChar != ')')
            {
               Serializable evaluate = evaluate(functionName.toString(), Collections.emptyList(), variableInterpreter);
               result.add(evaluate);
            }
            return (result);
         }
         else if(c == ',')
         {
            /////////////////////////////////////////////////////////////////////////
            // comma means:  evaluate the current thing; add it to the result list //
            // unless we just closed a paren.                                      //
            /////////////////////////////////////////////////////////////////////////
            if(previousChar != ')')
            {
               Serializable evaluate = evaluate(functionName.toString(), Collections.emptyList(), variableInterpreter);
               result.add(evaluate);
            }
            functionName = new StringBuilder();
         }
         else
         {
            ////////////////////////////////////////////////
            // else, we add this char to the current name //
            ////////////////////////////////////////////////
            functionName.append(c);
         }
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if we haven't found a result yet, assume we have just a literal, not a function call, and evaluate as such //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(result.isEmpty())
      {
         if(!functionName.isEmpty())
         {
            Serializable evaluate = evaluate(functionName.toString(), Collections.emptyList(), variableInterpreter);
            result.add(evaluate);
         }
      }

      return (result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Serializable evaluate(String functionName, List<Serializable> args, QMetaDataVariableInterpreter variableInterpreter) throws QFormulaException
   {
      // System.out.format("== Evaluating [%s](%s) ==\n", functionName, args);
      switch(functionName)
      {
         case "ADD":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).add(numbers.get(1)));
         }
         case "MINUS":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).subtract(numbers.get(1)));
         }
         case "MULTIPLY":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).multiply(numbers.get(1)));
         }
         case "DIVIDE":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            if(numbers.get(1) == null || numbers.get(1).equals(BigDecimal.ZERO))
            {
               return null;
            }
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).divide(numbers.get(1), 4, RoundingMode.HALF_UP));
         }
         case "DIVIDE_SCALE":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 3, variableInterpreter);
            if(numbers.get(1) == null || numbers.get(1).equals(BigDecimal.ZERO))
            {
               return null;
            }
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).divide(numbers.get(1), numbers.get(2).intValue(), RoundingMode.HALF_UP));
         }
         case "ROUND":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).round(new MathContext(numbers.get(1).intValue())));
         }
         case "SCALE":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBigDecimal(numbers, () -> numbers.get(0).setScale(numbers.get(1).intValue(), RoundingMode.HALF_UP));
         }
         case "NVL":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return Objects.requireNonNullElse(numbers.get(0), numbers.get(1));
         }
         case "IF":
         {
            // IF(CONDITION,TRUE,ELSE)
            List<Serializable> actualArgs = getArgumentList(args, 3, variableInterpreter);
            Serializable       condition  = actualArgs.get(0);
            boolean            conditionBoolean;
            if(condition == null)
            {
               conditionBoolean = false;
            }
            else if(condition instanceof Boolean b)
            {
               conditionBoolean = b;
            }
            else if(condition instanceof BigDecimal bd)
            {
               conditionBoolean = (bd.compareTo(BigDecimal.ZERO) != 0);
            }
            else if(condition instanceof String s)
            {
               if("true".equalsIgnoreCase(s))
               {
                  conditionBoolean = true;
               }
               else if("false".equalsIgnoreCase(s))
               {
                  conditionBoolean = false;
               }
               else
               {
                  conditionBoolean = StringUtils.hasContent(s);
               }
            }
            else
            {
               conditionBoolean = false;
            }

            return conditionBoolean ? actualArgs.get(1) : actualArgs.get(2);
         }
         case "LT":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBoolean(numbers, () -> numbers.get(0).compareTo(numbers.get(1)) < 0);
         }
         case "LTE":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBoolean(numbers, () -> numbers.get(0).compareTo(numbers.get(1)) <= 0);
         }
         case "GT":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBoolean(numbers, () -> numbers.get(0).compareTo(numbers.get(1)) > 0);
         }
         case "GTE":
         {
            List<BigDecimal> numbers = getNumberArgumentList(args, 2, variableInterpreter);
            return nullIfAnyNullArgsElseBoolean(numbers, () -> numbers.get(0).compareTo(numbers.get(1)) >= 0);
         }
         default:
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // if there aren't arguments, then we can try to evaluate the thing not as a function //
            ////////////////////////////////////////////////////////////////////////////////////////
            if(CollectionUtils.nullSafeIsEmpty(args))
            {
               try
               {
                  return (ValueUtils.getValueAsBigDecimal(functionName));
               }
               catch(Exception e)
               {
                  // continue
               }

               try
               {
                  return (variableInterpreter.interpret(functionName));
               }
               catch(Exception e)
               {
                  // continue
               }
            }
         }
      }

      throw (new QFormulaException("Unable to evaluate unrecognized expression: " + functionName + ""));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Serializable nullIfAnyNullArgsElseBigDecimal(List<BigDecimal> numbers, Supplier<BigDecimal> supplier)
   {
      if(numbers.stream().anyMatch(Objects::isNull))
      {
         return (null);
      }
      return supplier.get();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Serializable nullIfAnyNullArgsElseBoolean(List<BigDecimal> numbers, Supplier<Boolean> supplier)
   {
      if(numbers.stream().anyMatch(Objects::isNull))
      {
         return (null);
      }
      return supplier.get();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<BigDecimal> getNumberArgumentList(List<Serializable> originalArgs, Integer howMany, QMetaDataVariableInterpreter variableInterpreter) throws QFormulaException
   {
      if(howMany != null)
      {
         if(!howMany.equals(originalArgs.size()))
         {
            throw (new QFormulaException("Wrong number of arguments (required: " + howMany + ", received: " + originalArgs.size() + ")"));
         }
      }

      List<BigDecimal> rs = new ArrayList<>();
      for(Serializable originalArg : originalArgs)
      {
         try
         {
            Serializable interpretedArg = variableInterpreter.interpretForObject(ValueUtils.getValueAsString(originalArg), null);
            rs.add(ValueUtils.getValueAsBigDecimal(interpretedArg));
         }
         catch(QValueException e)
         {
            throw (new QFormulaException("Could not process [" + originalArg + "] as a number"));
         }
      }
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<Serializable> getArgumentList(List<Serializable> originalArgs, Integer howMany, QMetaDataVariableInterpreter variableInterpreter) throws QFormulaException
   {
      if(howMany != null)
      {
         if(!howMany.equals(originalArgs.size()))
         {
            throw (new QFormulaException("Wrong number of arguments (required: " + howMany + ", received: " + originalArgs.size() + ")"));
         }
      }

      List<Serializable> rs = new ArrayList<>();
      for(Serializable originalArg : originalArgs)
      {
         try
         {
            Serializable interpretedArg = variableInterpreter.interpretForObject(ValueUtils.getValueAsString(originalArg), null);
            rs.add(interpretedArg);
         }
         catch(QValueException e)
         {
            throw (new QFormulaException("Could not process [" + originalArg + "] as a number"));
         }
      }
      return (rs);
   }

}
