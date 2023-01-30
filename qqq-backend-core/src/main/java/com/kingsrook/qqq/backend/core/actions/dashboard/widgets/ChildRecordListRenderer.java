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

package com.kingsrook.qqq.backend.core.actions.dashboard.widgets;


import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChildRecordListData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.AbstractWidgetMetaDataBuilder;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 ** Generic widget for display a list of child records.
 *******************************************************************************/
public class ChildRecordListRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static Builder widgetMetaDataBuilder(QJoinMetaData join)
   {
      return (new Builder(new QWidgetMetaData()
         .withName(join.getName())
         .withIsCard(true)
         .withCodeReference(new QCodeReference(ChildRecordListRenderer.class, null))
         .withType(WidgetType.CHILD_RECORD_LIST.getType())
         .withDefaultValue("joinName", join.getName())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Builder extends AbstractWidgetMetaDataBuilder
   {

      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public Builder(QWidgetMetaData widgetMetaData)
      {
         super(widgetMetaData);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public Builder withName(String name)
      {
         widgetMetaData.setName(name);
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public Builder withLabel(String label)
      {
         widgetMetaData.setLabel(label);
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public Builder withCanAddChildRecord(boolean b)
      {
         widgetMetaData.withDefaultValue("canAddChildRecord", true);
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public Builder withDisabledFieldsForNewChildRecords(Set<String> disabledFieldsForNewChildRecords)
      {
         widgetMetaData.withDefaultValue("disabledFieldsForNewChildRecords", new HashSet<>(disabledFieldsForNewChildRecords));
         return (this);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String        widgetLabel = input.getQueryParams().get("widgetLabel");
      String        joinName    = input.getQueryParams().get("joinName");
      QJoinMetaData join        = input.getInstance().getJoin(joinName);
      String        id          = input.getQueryParams().get("id");

      ////////////////////////////////////////////////////////
      // fetch the record that we're getting children for.  //
      // e.g., the left-side of the join, with the input id //
      ////////////////////////////////////////////////////////
      GetInput getInput = new GetInput();
      getInput.setTableName(join.getLeftTable());
      getInput.setPrimaryKey(id);
      GetOutput getOutput = new GetAction().execute(getInput);
      QRecord   record    = getOutput.getRecord();

      if(record == null)
      {
         QTableMetaData table = input.getInstance().getTable(join.getLeftTable());
         throw (new QNotFoundException("Could not find " + (table == null ? "" : table.getLabel()) + " with primary key " + id));
      }

      ////////////////////////////////////////////////////////////////////
      // set up the query - for the table on the right side of the join //
      ////////////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter();
      for(JoinOn joinOn : join.getJoinOns())
      {
         filter.addCriteria(new QFilterCriteria(joinOn.getRightField(), QCriteriaOperator.EQUALS, List.of(record.getValue(joinOn.getLeftField()))));
      }
      filter.setOrderBys(join.getOrderBys());

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(join.getRightTable());
      queryInput.setShouldTranslatePossibleValues(true);
      queryInput.setShouldGenerateDisplayValues(true);
      queryInput.setFilter(filter);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      QTableMetaData table       = input.getInstance().getTable(join.getRightTable());
      String         tablePath   = input.getInstance().getTablePath(input, table.getName());
      String         viewAllLink = tablePath == null ? null : (tablePath + "?filter=" + URLEncoder.encode(JsonUtils.toJson(filter), Charset.defaultCharset()));

      ChildRecordListData widgetData = new ChildRecordListData(widgetLabel, queryOutput, table, tablePath, viewAllLink);

      if(BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(input.getQueryParams().get("canAddChildRecord"))))
      {
         widgetData.setCanAddChildRecord(true);

         //////////////////////////////////////////////////////////
         // new child records must have values from the join-ons //
         //////////////////////////////////////////////////////////
         Map<String, Serializable> defaultValuesForNewChildRecords = new HashMap<>();
         for(JoinOn joinOn : join.getJoinOns())
         {
            defaultValuesForNewChildRecords.put(joinOn.getRightField(), record.getValue(joinOn.getLeftField()));
         }
         widgetData.setDefaultValuesForNewChildRecords(defaultValuesForNewChildRecords);

         Map<String, Serializable> widgetValues = input.getWidgetMetaData().getDefaultValues();
         if(widgetValues.containsKey("disabledFieldsForNewChildRecords"))
         {
            widgetData.setDisabledFieldsForNewChildRecords((Set<String>) widgetValues.get("disabledFieldsForNewChildRecords"));
         }
      }

      return (new RenderWidgetOutput(widgetData));
   }

}
