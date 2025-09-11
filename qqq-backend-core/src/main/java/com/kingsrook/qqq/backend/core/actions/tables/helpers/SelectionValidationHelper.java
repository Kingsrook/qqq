/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.metadata.personalization.TableMetaDataPersonalizerAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.personalization.TableMetaDataPersonalizerInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrCountInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;


/*******************************************************************************
 * Utility to help query and aggregate actions validate the fieldNames being
 * selected, aggregated, grouped by.
 *******************************************************************************/
public class SelectionValidationHelper
{

   /***************************************************************************
    * For the given set of field names, and a query (or aggregate) input -
    * figure out if any field names (which may be joinTable (or alias) dot field
    * name) - figure out if any of those aren't recognized fields in the input
    * table or any tables that'll e joined.
    *
    * joins may either be explicit, via queryJoins in the input, else, they can
    * be "inferred" through a {@link JoinsContext}, which will look at the
    * filter and security fields.
    *
    * Note that table personalization is applied in here to the join tables
    * but it is assumed that input object has already has the table ran
    * through the personalize action.
    *
    * @param input assumed to be a QueryInput or AggregateInput - where the
    *              table in it should have already gone through
    *              {@link TableMetaDataPersonalizerAction}.
    * @param fieldNames assumed to have come from
    * {@link com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate}
    * or {@link com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy}
    * objects, or a {@link com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput#withFieldNamesToInclude(Set)}
    * @return list of field names that were unrecognized.  empty if none.
    ***************************************************************************/
   public static List<String> getUnrecognizedFieldNames(QueryOrCountInputInterface input, Set<String> fieldNames) throws QException
   {
      List<String>                unrecognizedFieldNames  = new ArrayList<>();
      Map<String, QTableMetaData> queryJoinsByNameOrAlias = null;
      for(String fieldName : fieldNames)
      {
         if(fieldName.contains("."))
         {
            ////////////////////////////////////////////////
            // handle names with dots - fields from joins //
            ////////////////////////////////////////////////
            String[] parts = fieldName.split("\\.");
            if(parts.length != 2)
            {
               unrecognizedFieldNames.add(fieldName);
            }
            else
            {
               String tableOrAlias  = parts[0];
               String fieldNamePart = parts[1];

               //////////////////////////////////////////////
               // build map of queryJoins by name or alias //
               //////////////////////////////////////////////
               if(queryJoinsByNameOrAlias == null)
               {
                  queryJoinsByNameOrAlias = new HashMap<>();

                  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // build a joinsContext, since it knows how to infer join tables that the user may not have specified in their input.  //
                  // but, since it can manipulate the query filter (e.g., adding security clauses), pass it a clone (or a new blank one) //
                  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  QQueryFilter    filterForJoinsContext = ObjectUtils.tryElse(() -> input.getFilter().clone(), new QQueryFilter());
                  JoinsContext    joinsContext = new JoinsContext(QContext.getQInstance(), input.getTableName(), input.getQueryJoins(), filterForJoinsContext);
                  List<QueryJoin> queryJoins   = joinsContext.getQueryJoins();

                  for(QueryJoin queryJoin : CollectionUtils.nonNullList(queryJoins))
                  {
                     String         joinTableOrAlias = queryJoin.getJoinTableOrItsAlias();
                     QTableMetaData joinTable        = QContext.getQInstance().getTable(queryJoin.getJoinTable());

                     /////////////////////////////////
                     // personalize the join table! //
                     /////////////////////////////////
                     joinTable = TableMetaDataPersonalizerAction.execute(new TableMetaDataPersonalizerInput().withTableMetaData(joinTable).withInputSource(input.getInputSource()));

                     if(joinTable != null)
                     {
                        queryJoinsByNameOrAlias.put(joinTableOrAlias, joinTable);
                     }
                  }
               }

               if(!queryJoinsByNameOrAlias.containsKey(tableOrAlias))
               {
                  ///////////////////////////////////////////
                  // unrecognized tableOrAlias is an error //
                  ///////////////////////////////////////////
                  unrecognizedFieldNames.add(fieldName);
               }
               else
               {
                  QTableMetaData joinTable = queryJoinsByNameOrAlias.get(tableOrAlias);
                  if(!joinTable.getFields().containsKey(fieldNamePart))
                  {
                     //////////////////////////////////////////////////////////
                     // unrecognized field within the join table is an error //
                     //////////////////////////////////////////////////////////
                     unrecognizedFieldNames.add(fieldName);
                  }
               }
            }
         }
         else
         {
            ///////////////////////////////////////////////////////////////////////
            // non-join fields - just ensure field name is in table's fields map //
            ///////////////////////////////////////////////////////////////////////
            if(!input.getTable().getFields().containsKey(fieldName))
            {
               unrecognizedFieldNames.add(fieldName);
            }
         }
      }
      return unrecognizedFieldNames;
   }

}
