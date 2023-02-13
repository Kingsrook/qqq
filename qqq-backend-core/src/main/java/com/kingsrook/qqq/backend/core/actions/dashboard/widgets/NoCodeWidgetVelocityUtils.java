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


import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.AbstractHTMLWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetCount;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class NoCodeWidgetVelocityUtils
{
   private static final QLogger LOG = QLogger.getLogger(NoCodeWidgetVelocityUtils.class);


   /*******************************************************************************
    **
    *******************************************************************************/
   private final Map<String, Object> context;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public NoCodeWidgetVelocityUtils(Map<String, Object> context)
   {
      this.context = context;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public final String errorIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: red; position: relative; top: 6px;" aria-hidden="true">error_outline</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public final String checkIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: green; position: relative; top: 6px;" aria-hidden="true">check</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String plural(Integer size, String ifOne, String ifNotOne)
   {
      return StringUtils.plural(size, ifOne, ifNotOne);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String tableCountFilterLink(String countVariableName, String singular, String plural) throws QException
   {
      try
      {
         WidgetCount  widgetCount = (WidgetCount) context.get(countVariableName + ".source");
         Integer      count       = ValueUtils.getValueAsInteger(context.get(countVariableName));
         QQueryFilter filter      = widgetCount.getFilter();
         return (AbstractHTMLWidgetRenderer.aHrefTableFilterNoOfRecords(null, widgetCount.getTableName(), filter, count, singular, plural));
      }
      catch(Exception e)
      {
         LOG.warn("Error rendering widget link", e);
         return ("");
      }
   }
}
