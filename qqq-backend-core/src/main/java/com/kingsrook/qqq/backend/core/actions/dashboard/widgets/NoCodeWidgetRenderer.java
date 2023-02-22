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

package com.kingsrook.qqq.backend.core.actions.dashboard.widgets;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.RawHTML;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.AbstractWidgetOutput;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.AbstractWidgetValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.QNoCodeWidgetMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.utils.BackendQueryFilterUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class NoCodeWidgetRenderer extends AbstractWidgetRenderer
{
   private static final QLogger LOG = QLogger.getLogger(NoCodeWidgetRenderer.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      QNoCodeWidgetMetaData widgetMetaData = (QNoCodeWidgetMetaData) input.getWidgetMetaData();

      Map<String, Object> context = initContext(input);
      context.putAll(input.getQueryParams());

      ///////////////////////////////////////////////
      // populate context by evaluating all values //
      ///////////////////////////////////////////////
      for(AbstractWidgetValueSource valueSource : widgetMetaData.getValues())
      {
         try
         {
            LOG.trace("Computing: " + valueSource.getType() + " named " + valueSource.getName() + "...");
            Object value = valueSource.evaluate(context, input);
            LOG.trace("Computed: " + valueSource.getName() + " = " + value);
            context.put(valueSource.getName(), value);
            context.put(valueSource.getName() + ".source", valueSource);
         }
         catch(Exception e)
         {
            LOG.warn("Error evaluating widget value source", e, logPair("widgetName", input.getWidgetMetaData().getName()), logPair("valueSourceName", valueSource.getName()));
         }
      }

      /////////////////////////////////////////////
      // build content by evaluating all outputs //
      /////////////////////////////////////////////
      List<AbstractWidgetOutput> outputs = widgetMetaData.getOutputs();
      String                     content = renderOutputs(context, outputs);

      return (new RenderWidgetOutput(new RawHTML(widgetMetaData.getLabel(), content)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, Object> initContext(RenderWidgetInput input)
   {
      Map<String, Object> context = new HashMap<>();
      context.put("utils", new NoCodeWidgetVelocityUtils(context, input));
      context.put("input", input);
      return context;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String renderOutputs(Map<String, Object> context, List<AbstractWidgetOutput> outputs) throws QException
   {
      StringBuilder content = new StringBuilder();
      for(AbstractWidgetOutput output : CollectionUtils.nonNullList(outputs))
      {
         boolean conditionPassed = true;
         if(output.getCondition() != null)
         {
            conditionPassed = evaluateCondition(output.getCondition(), context);
         }

         if(conditionPassed)
         {
            String render = output.render(context);
            content.append(render);
            LOG.trace("Condition passed, rendered: " + render);
         }
         else
         {
            LOG.trace("Condition failed - not rendering this output.");
         }
      }
      return (content.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean evaluateCondition(QFilterCriteria condition, Map<String, Object> context)
   {
      try
      {
         Object value = context.get(condition.getFieldName());
         return (BackendQueryFilterUtils.doesCriteriaMatch(condition, condition.getFieldName(), (Serializable) value));
      }
      catch(Exception e)
      {
         LOG.warn("Error evaluating condition: " + condition, e);
         return (false);
      }
   }

}
