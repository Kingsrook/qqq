/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode;


import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class WidgetCalculation extends AbstractWidgetValueSource
{
   private Operator     operator;
   private List<String> values;



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum Operator
   {
      SUM_INTEGERS((List<String> valueNames, Map<String, Object> context) ->
      {
         Integer sum = 0;
         for(String valueName : valueNames)
         {
            try
            {
               Integer addend = ValueUtils.getValueAsInteger(context.get(valueName));
               sum += addend;
            }
            catch(Exception e)
            {
               ////////////////////////////////////////////////
               // assume value to be null or 0, don't add it //
               ////////////////////////////////////////////////
               e.printStackTrace();
            }
         }
         return (sum);
      }),

      AGE_MINUTES((List<String> valueNames, Map<String, Object> context) ->
      {
         Instant now  = Instant.now();
         Instant then = ValueUtils.getValueAsInstant(context.get(valueNames.get(0)));
         return (then.until(now, ChronoUnit.MINUTES));
      }),

      AGE_SECONDS((List<String> valueNames, Map<String, Object> context) ->
      {
         Instant now  = Instant.now();
         Instant then = ValueUtils.getValueAsInstant(context.get(valueNames.get(0)));
         return (then.until(now, ChronoUnit.SECONDS));
      }),

      PERCENT_CHANGE((List<String> valueNames, Map<String, Object> context) ->
      {
         BigDecimal current  = ValueUtils.getValueAsBigDecimal(context.get(valueNames.get(0)));
         BigDecimal previous = ValueUtils.getValueAsBigDecimal(context.get(valueNames.get(1)));

         ///////////////////////////////////////////////
         // 100 * ( (current - previous) / previous ) //
         ///////////////////////////////////////////////
         BigDecimal difference = current.subtract(previous);
         if(BigDecimal.ZERO.equals(previous))
         {
            return (null);
         }
         BigDecimal quotient = difference.divide(previous, MathContext.DECIMAL32);
         return new BigDecimal("100").multiply(quotient);
      });


      private final BiFunction<List<String>, Map<String, Object>, Object> function;



      /*******************************************************************************
       **
       *******************************************************************************/
      Operator(BiFunction<List<String>, Map<String, Object>, Object> function)
      {
         this.function = function;
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public Object execute(List<String> values, Map<String, Object> context)
      {
         return (function.apply(values, context));
      }
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetCalculation()
   {
      setType(getClass().getSimpleName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object evaluate(Map<String, Object> context, RenderWidgetInput input) throws QException
   {
      return (operator.execute(values, context));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public WidgetCalculation withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for operator
    *******************************************************************************/
   public Operator getOperator()
   {
      return (this.operator);
   }



   /*******************************************************************************
    ** Setter for operator
    *******************************************************************************/
   public void setOperator(Operator operator)
   {
      this.operator = operator;
   }



   /*******************************************************************************
    ** Fluent setter for operator
    *******************************************************************************/
   public WidgetCalculation withOperator(Operator operator)
   {
      this.operator = operator;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public List<String> getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(List<String> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public WidgetCalculation withValues(List<String> values)
   {
      this.values = values;
      return (this);
   }

}
