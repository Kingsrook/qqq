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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class QNoCodeWidgetMetaData extends QWidgetMetaData
{
   private List<AbstractWidgetValueSource> values  = new ArrayList<>();
   private List<AbstractWidgetOutput>      outputs = new ArrayList<>();



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public List<AbstractWidgetValueSource> getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(List<AbstractWidgetValueSource> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter to add a single value
    *******************************************************************************/
   public QNoCodeWidgetMetaData withValue(AbstractWidgetValueSource value)
   {
      if(this.values == null)
      {
         this.values = new ArrayList<>();
      }
      this.values.add(value);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public QNoCodeWidgetMetaData withValues(List<AbstractWidgetValueSource> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputs
    *******************************************************************************/
   public List<AbstractWidgetOutput> getOutputs()
   {
      return (this.outputs);
   }



   /*******************************************************************************
    ** Setter for outputs
    *******************************************************************************/
   public void setOutputs(List<AbstractWidgetOutput> outputs)
   {
      this.outputs = outputs;
   }



   /*******************************************************************************
    ** Fluent setter to add a single output
    *******************************************************************************/
   public QNoCodeWidgetMetaData withOutput(AbstractWidgetOutput output)
   {
      if(this.outputs == null)
      {
         this.outputs = new ArrayList<>();
      }
      this.outputs.add(output);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for outputs
    *******************************************************************************/
   public QNoCodeWidgetMetaData withOutputs(List<AbstractWidgetOutput> outputs)
   {
      this.outputs = outputs;
      return (this);
   }

}
