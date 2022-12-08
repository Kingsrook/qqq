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

package com.kingsrook.qqq.backend.core.actions.dashboard;


import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;


/*******************************************************************************
 ** Base class for rendering qqq HTML dashboard widgets
 **
 *******************************************************************************/
public abstract class AbstractHTMLWidgetRenderer extends AbstractWidgetRenderer
{


   /*******************************************************************************
    **
    *******************************************************************************/
   protected String openTopLevelBulletList()
   {
      return ("""
         <div style="padding-left: 2rem;">
            <ul>""");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String closeTopLevelBulletList()
   {
      return ("""
            </ul>
         </div>""");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String bulletItalics(String text)
   {
      return ("<li><i>" + text + "</i></li>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String bulletLink(String href, String text)
   {
      return ("<li><a href=\"" + href + "\">" + text + "</a></li>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String bulletNameLink(String name, String href, String text)
   {
      return (bulletNameValue(name, "<a href=\"" + href + "\">" + text + "</a>"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String bulletNameValue(String name, String value)
   {
      return ("<li><b>" + name + "</b> &nbsp; " + Objects.requireNonNullElse(value, "--") + "</li>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableBulkLoad(RenderWidgetInput input, String tableName) throws QException
   {
      String tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "/" + tableName + ".bulkInsert");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableBulkLoadChildren(String tableName) throws QException
   {
      return ("#/launchProcess=" + tableName + ".bulkInsert");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableCreate(RenderWidgetInput input, String tableName) throws QException
   {
      String tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "/create");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableCreateWithDefaultValues(RenderWidgetInput input, String tableName, Map<String, Serializable> defaultValues) throws QException
   {
      String tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "/create?defaultValues=" + URLEncoder.encode(JsonUtils.toJson(defaultValues), Charset.defaultCharset()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableFilter(RenderWidgetInput input, String tableName, QQueryFilter filter) throws QException
   {
      String tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "?filter=" + URLEncoder.encode(JsonUtils.toJson(filter), Charset.defaultCharset()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String aHrefTableFilterNoOfRecords(RenderWidgetInput input, String tableName, QQueryFilter filter, Integer noOfRecords, String singularLabel, String pluralLabel) throws QException
   {
      String href = linkTableFilter(input, tableName, filter);
      return ("<a href=\"" + href + "\">" + QValueFormatter.formatValue(DisplayFormat.COMMAS, noOfRecords) + " " + pluralize(noOfRecords, singularLabel, pluralLabel) + "</a>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkRecordEdit(AbstractActionInput input, String tableName, Serializable recordId) throws QException
   {
      String tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "/" + recordId + "/edit");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkRecordView(AbstractActionInput input, String tableName, Serializable recordId) throws QException
   {
      String tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "/" + recordId);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkProcessForRecord(AbstractActionInput input, String processName, Serializable recordId) throws QException
   {
      QProcessMetaData process   = input.getInstance().getProcess(processName);
      String           tableName = process.getTableName();
      String           tablePath = input.getInstance().getTablePath(input, tableName);
      return (tablePath + "/" + recordId + "/" + processName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableCreateChild(String childTableName, Map<String, Serializable> defaultValues)
   {
      return (linkTableCreateChild(childTableName, defaultValues, defaultValues.keySet()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String aHrefTableCreateChild(String childTableName, Map<String, Serializable> defaultValues)
   {
      return (aHrefTableCreateChild(childTableName, defaultValues, defaultValues.keySet()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String linkTableCreateChild(String childTableName, Map<String, Serializable> defaultValues, Set<String> disabledFields)
   {
      Map<String, Integer> disabledFieldsMap = disabledFields.stream().collect(Collectors.toMap(k -> k, k -> 1));

      return ("#/createChild=" + childTableName
         + "/defaultValues=" + URLEncoder.encode(JsonUtils.toJson(defaultValues), StandardCharsets.UTF_8).replaceAll("\\+", "%20")
         + "/disabledFields=" + URLEncoder.encode(JsonUtils.toJson(disabledFieldsMap), StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String aHrefTableCreateChild(String childTableName, Map<String, Serializable> defaultValues, Set<String> disabledFields)
   {
      return ("<a href=\"" + linkTableCreateChild(childTableName, defaultValues, defaultValues.keySet()) + "\">Create new</a>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String pluralize(Integer count, String singular, String plural)
   {
      if(count != null && count.equals(1))
      {
         return (singular);
      }

      return (plural);
   }

}
