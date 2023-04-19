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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.AbstractHTMLWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetCount;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class NoCodeWidgetVelocityUtils
{
   private static final QLogger LOG = QLogger.getLogger(NoCodeWidgetVelocityUtils.class);

   private Map<String, Object> context;
   private RenderWidgetInput   input;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public NoCodeWidgetVelocityUtils(Map<String, Object> context, RenderWidgetInput input)
   {
      this.context = context;
      this.input = input;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String helpIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: blue; position: relative; top: 6px;" aria-hidden="true">help_outline</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String errorIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: red; position: relative; top: 6px;" aria-hidden="true">error_outline</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String warningIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: orange; position: relative; top: 6px;" aria-hidden="true">warning</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String checkIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: green; position: relative; top: 6px;" aria-hidden="true">check</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String pendingIcon()
   {
      return ("""
         <span class="material-icons-round notranslate MuiIcon-root MuiIcon-fontSizeInherit" style="color: #0062ff; position: relative; top: 6px;" aria-hidden="true">pending</span>
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String spanColorGreen()
   {
      return ("""
         <span style="color: green;">
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String spanColorOrange()
   {
      return ("""
         <span style="color: orange;">
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String spanColorRed()
   {
      return ("""
         <span style="color: red;">
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
   public String formatDateTime(Instant i)
   {
      if(i == null)
      {
         return ("");
      }
      return QValueFormatter.formatDateTimeWithZone(i.atZone(ZoneId.of(QContext.getQInstance().getDefaultTimeZoneId())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String formatSecondsAsDuration(Integer seconds)
   {
      StringBuilder rs = new StringBuilder();

      if(seconds == null)
      {
         return ("");
      }

      int secondsPerDay = 24 * 60 * 60;
      if(seconds >= secondsPerDay)
      {
         int days = seconds / (secondsPerDay);
         seconds = seconds % secondsPerDay;
         rs.append(days).append(StringUtils.plural(days, " day", " days")).append(" ");
      }

      int secondsPerHour = 60 * 60;
      if(seconds >= secondsPerHour)
      {
         int hours = seconds / (secondsPerHour);
         seconds = seconds % secondsPerHour;
         rs.append(hours).append(StringUtils.plural(hours, " hour", " hours")).append(" ");
      }

      int secondsPerMinute = 60;
      if(seconds >= secondsPerMinute)
      {
         int minutes = seconds / (secondsPerMinute);
         seconds = seconds % secondsPerMinute;
         rs.append(minutes).append(StringUtils.plural(minutes, " minute", " minutes")).append(" ");
      }

      if(seconds > 0 || rs.length() == 0)
      {
         rs.append(seconds).append(StringUtils.plural(seconds, " second", " seconds")).append(" ");
      }

      if(rs.length() > 0)
      {
         rs.deleteCharAt(rs.length() - 1);
      }

      return (rs.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String formatSecondsAsRoundedDuration(Integer seconds)
   {
      StringBuilder rs = new StringBuilder();

      if(seconds == null)
      {
         return ("");
      }

      int secondsPerDay = 24 * 60 * 60;
      if(seconds >= secondsPerDay)
      {
         int days = seconds / (secondsPerDay);
         return (days + StringUtils.plural(days, " day", " days"));
      }

      int secondsPerHour = 60 * 60;
      if(seconds >= secondsPerHour)
      {
         int hours = seconds / (secondsPerHour);
         return (hours + StringUtils.plural(hours, " hour", " hours"));
      }

      int secondsPerMinute = 60;
      if(seconds >= secondsPerMinute)
      {
         int minutes = seconds / (secondsPerMinute);
         return (minutes + StringUtils.plural(minutes, " minute", " minutes"));
      }

      if(seconds > 0 || rs.length() == 0)
      {
         return (seconds + StringUtils.plural(seconds, " second", " seconds"));
      }

      return ("");
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
         QQueryFilter filter      = widgetCount.getEffectiveFilter(input);
         return (AbstractHTMLWidgetRenderer.aHrefTableFilterNoOfRecords(null, widgetCount.getTableName(), filter, count, singular, plural));
      }
      catch(Exception e)
      {
         LOG.warn("Error rendering widget link", e);
         return ("");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String format(String displayFormat, Serializable value)
   {
      return (QValueFormatter.formatValue(displayFormat, value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String round(BigDecimal input, int digits)
   {
      return String.valueOf(input.setScale(digits, RoundingMode.HALF_UP));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Object ifElse(Object ifObject, Object elseObject)
   {
      if(StringUtils.hasContent(ValueUtils.getValueAsString(ifObject)))
      {
         return (ifObject);
      }
      else if(StringUtils.hasContent(ValueUtils.getValueAsString(elseObject)))
      {
         return (elseObject);
      }

      return ("");
   }

}
