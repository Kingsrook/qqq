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


import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class WidgetAdHocValue extends AbstractWidgetValueSource
{
   private QCodeReference codeReference;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetAdHocValue()
   {
      setType(getClass().getSimpleName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object evaluate(Map<String, Object> context, RenderWidgetInput input) throws QException
   {
      if(inputValues != null)
      {
         context.putAll(inputValues);
      }

      Function<Object, Object> function = QCodeLoader.getFunction(codeReference);
      Object                   result   = function.apply(context);
      return (result);
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   @Override
   public WidgetAdHocValue withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeReference
    *******************************************************************************/
   public QCodeReference getCodeReference()
   {
      return (this.codeReference);
   }



   /*******************************************************************************
    ** Setter for codeReference
    *******************************************************************************/
   public void setCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
   }



   /*******************************************************************************
    ** Fluent setter for codeReference
    *******************************************************************************/
   public WidgetAdHocValue withCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for inputValues
    *******************************************************************************/
   @Override
   public WidgetAdHocValue withInputValues(Map<String, Serializable> inputValues)
   {
      this.inputValues = inputValues;
      return (this);
   }

}
