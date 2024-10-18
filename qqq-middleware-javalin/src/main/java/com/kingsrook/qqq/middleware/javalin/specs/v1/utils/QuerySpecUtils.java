/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.middleware.javalin.specs.v1.utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qqq.middleware.javalin.executors.io.QueryMiddlewareInput;
import com.kingsrook.qqq.openapi.model.Schema;
import com.kingsrook.qqq.openapi.model.Type;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuerySpecUtils
{

   /***************************************************************************
    **
    ***************************************************************************/
   public static Schema defineQueryJoinsSchema()
   {
      Schema queryJoinsSchema = new Schema()
         .withType(Type.ARRAY)
         .withItems(new Schema()
            .withProperties(MapBuilder.of(
               "joinTable", new Schema()
                  .withType(Type.STRING),
               "select", new Schema()
                  .withType(Type.BOOLEAN),
               "type", new Schema()
                  .withType(Type.STRING)
                  .withEnumValues(Arrays.stream(QueryJoin.Type.values()).map(o -> o.name()).toList()),
               "alias", new Schema()
                  .withType(Type.STRING),
               "baseTableOrAlias", new Schema()
                  .withType(Type.STRING)
            ))
         );
      return queryJoinsSchema;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static Schema defineQQueryFilterSchema()
   {
      Schema qQueryFilterSchema = new Schema()
         .withType(Type.OBJECT)
         .withExample(List.of(
            JsonUtils.toJson(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.LESS_THAN, 5)))
         ))
         .withProperties(MapBuilder.of(
            "criteria", new Schema()
               .withType(Type.ARRAY)
               .withItems(new Schema()
                  .withProperties(MapBuilder.of(
                     "fieldName", new Schema()
                        .withType(Type.STRING),
                     "operator", new Schema()
                        .withType(Type.STRING)
                        .withEnumValues(Arrays.stream(QCriteriaOperator.values()).map(o -> o.name()).toList()),
                     "values", new Schema()
                        .withType(Type.ARRAY)
                        .withItems(new Schema().withOneOf(List.of(
                           new Schema().withType(Type.INTEGER),
                           new Schema().withType(Type.STRING)
                        )))
                  ))
               ),
            "orderBys", new Schema()
               .withType(Type.ARRAY)
               .withItems(new Schema()
                  .withProperties(MapBuilder.of(
                     "fieldName", new Schema()
                        .withType(Type.STRING),
                     "isAscending", new Schema()
                        .withType(Type.BOOLEAN)))
               ),
            "booleanOperator", new Schema().withType(Type.STRING).withEnumValues(Arrays.stream(QQueryFilter.BooleanOperator.values()).map(o -> o.name()).toList()),
            "skip", new Schema().withType(Type.INTEGER),
            "limit", new Schema().withType(Type.INTEGER)
            // todo - subfilters??
         ));
      return qQueryFilterSchema;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static Schema getQueryResponseSchema()
   {
      Schema schema = new Schema()
         .withDescription("Records found by the query.  May be empty.")
         .withType(Type.OBJECT)
         .withProperties(MapBuilder.of(
            "records", new Schema()
               .withType(Type.ARRAY)
               .withItems(new Schema()
                  .withType(Type.OBJECT)
                  .withProperties(MapBuilder.of(
                     "recordLabel", new Schema().withType(Type.STRING),
                     "tableName", new Schema().withType(Type.STRING),
                     "values", new Schema().withType(Type.OBJECT).withDescription("Keys for each field in the table"),
                     "displayValues", new Schema().withType(Type.OBJECT)
                  ))
               )
         ));
      return schema;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static QueryMiddlewareInput buildInput(Map<String, String> paramMap) throws IOException
   {

      QQueryFilter filter      = null;
      String       filterParam = paramMap.get("filter");
      if(StringUtils.hasContent(filterParam))
      {
         filter = JsonUtils.toObject(filterParam, QQueryFilter.class);
      }

      List<QueryJoin> queryJoins      = null;
      String          queryJoinsParam = paramMap.get("queryJoins");
      if(StringUtils.hasContent(queryJoinsParam))
      {
         queryJoins = new ArrayList<>();

         JSONArray queryJoinsJSON = new JSONArray(queryJoinsParam);
         for(int i = 0; i < queryJoinsJSON.length(); i++)
         {
            QueryJoin queryJoin = new QueryJoin();
            queryJoins.add(queryJoin);

            JSONObject jsonObject = queryJoinsJSON.getJSONObject(i);
            queryJoin.setJoinTable(jsonObject.optString("joinTable"));

            if(jsonObject.has("baseTableOrAlias") && !jsonObject.isNull("baseTableOrAlias"))
            {
               queryJoin.setBaseTableOrAlias(jsonObject.optString("baseTableOrAlias"));
            }

            if(jsonObject.has("alias") && !jsonObject.isNull("alias"))
            {
               queryJoin.setAlias(jsonObject.optString("alias"));
            }

            queryJoin.setSelect(jsonObject.optBoolean("select"));

            if(jsonObject.has("type") && !jsonObject.isNull("type"))
            {
               queryJoin.setType(QueryJoin.Type.valueOf(jsonObject.getString("type")));
            }

            if(jsonObject.has("joinName") && !jsonObject.isNull("joinName"))
            {
               queryJoin.setJoinMetaData(QContext.getQInstance().getJoin(jsonObject.getString("joinName")));
            }
         }
      }

      return new QueryMiddlewareInput()
         .withTable(paramMap.get("table"))
         .withFilter(filter)
         .withQueryJoins(queryJoins);
   }
}
