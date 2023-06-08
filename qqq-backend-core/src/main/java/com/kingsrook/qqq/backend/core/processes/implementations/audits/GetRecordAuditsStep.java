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

package com.kingsrook.qqq.backend.core.processes.implementations.audits;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qqq.backend.core.actions.metadata.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** This is a single-step process used to look up audits for a record.
 *******************************************************************************/
public class GetRecordAuditsStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(GetRecordAuditsStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         String  tableName     = runBackendStepInput.getValueString("tableName");
         Integer recordId      = runBackendStepInput.getValueInteger("recordId");
         String  sortDirection = runBackendStepInput.getValueString("sortDirection");

         Integer limit = 1000;

         /////////////////////////////////////////
         // make sure user may query this table //
         /////////////////////////////////////////
         PermissionsHelper.checkTablePermissionThrowing(new QueryInput().withTableName(tableName), TablePermissionSubType.READ);
         PermissionsHelper.checkTablePermissionThrowing(new QueryInput().withTableName(AuditsMetaDataProvider.TABLE_NAME_AUDIT), TablePermissionSubType.READ);

         /////////////////////////////////////////////////////////////////////////
         // set up filter for audits - always start with the record in question //
         // possibly add in other pairs of table/recordId based on the tree...  //
         // combine all of those options in a filter of OR's                    //
         /////////////////////////////////////////////////////////////////////////
         QQueryFilter auditRecordFilter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR);

         auditRecordFilter.addSubFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria(AuditsMetaDataProvider.TABLE_NAME_AUDIT_TABLE + ".name", QCriteriaOperator.EQUALS, tableName))
            .withCriteria(new QFilterCriteria("recordId", QCriteriaOperator.EQUALS, recordId)));

         Set<Integer> otherAuditTableIds = new HashSet<>();

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if the table is part of an audit tree, then find other table/recordId pairs to look for in the audits query //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         QTableMetaData table      = QContext.getQInstance().getTable(tableName);
         QAuditRules    auditRules = Objects.requireNonNullElseGet(table.getAuditRules(), QAuditRules::new);
         if(auditRules.getIsAuditTreeRoot())
         {
            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(AuditsMetaDataProvider.TABLE_NAME_AUDIT_TREE);
            queryInput.setFilter(new QQueryFilter(new QFilterCriteria("rootTable.name", QCriteriaOperator.EQUALS, tableName)));

            queryInput.withQueryJoin(new QueryJoin(AuditsMetaDataProvider.TABLE_NAME_AUDIT_TABLE)
               .withJoinMetaData(QContext.getQInstance().getJoin(AuditsMetaDataProvider.AUDIT_TREE_JOIN_AUDIT_TABLE_FOR_ROOT))
               .withAlias("rootTable"));

            for(QRecord auditTreeRecord : new QueryAction().execute(queryInput).getRecords())
            {
               otherAuditTableIds.add(auditTreeRecord.getValueInteger("nodeAuditTableId"));
               auditRecordFilter.addSubFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("auditTableId", QCriteriaOperator.EQUALS, auditTreeRecord.getValue("nodeAuditTableId")))
                  .withCriteria(new QFilterCriteria("recordId", QCriteriaOperator.EQUALS, auditTreeRecord.getValue("nodeRecordId"))));
            }
         }
         ////////////////////////////////////////////////
         // todo - reverse (e.g., for child, non-root) //
         ////////////////////////////////////////////////

         ///////////////////////
         // select the audits //
         ///////////////////////
         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(AuditsMetaDataProvider.TABLE_NAME_AUDIT);
         queryInput.setShouldGenerateDisplayValues(true);
         queryInput.setShouldTranslatePossibleValues(true);
         queryInput.setIncludeAssociations(true);
         queryInput.setFilter(new QQueryFilter()
            .withLimit(limit)
            .withSubFilters(List.of(auditRecordFilter))
            .withOrderBy(new QFilterOrderBy("timestamp", "asc".equals(sortDirection)))
         );
         QueryOutput        queryOutput = new QueryAction().execute(queryInput);
         ArrayList<QRecord> audits      = CollectionUtils.useOrWrap(queryOutput.getRecords(), new TypeToken<>() {}); // todo - share simple-class format change, verify in checkstyle
         runBackendStepOutput.addValue("audits", audits);

         ///////////////////////////////////////////////////////
         // look up count if needed, else use audit list size //
         ///////////////////////////////////////////////////////
         if(audits.size() >= limit)
         {
            CountInput countInput = new CountInput();
            countInput.setTableName(AuditsMetaDataProvider.TABLE_NAME_AUDIT);
            countInput.setFilter(new QQueryFilter().withSubFilters(List.of(auditRecordFilter)));
            CountOutput countOutput = new CountAction().execute(countInput);
            runBackendStepOutput.addValue("count", countOutput.getCount());
         }
         else
         {
            runBackendStepOutput.addValue("count", audits.size());
         }

         ////////////////////////////////////////////////////////////////////////////////////////////////
         // put map of auditTableId to table names, and table name to (full) table meta data in result //
         ////////////////////////////////////////////////////////////////////////////////////////////////
         if(CollectionUtils.nullSafeHasContents(otherAuditTableIds))
         {
            QueryInput auditTableQueryInput = new QueryInput();
            auditTableQueryInput.setTableName(AuditsMetaDataProvider.TABLE_NAME_AUDIT_TABLE);
            auditTableQueryInput.setFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, new ArrayList<>(otherAuditTableIds))));
            QueryOutput auditTableQueryOutput = new QueryAction().execute(auditTableQueryInput);

            HashMap<Integer, QRecord> auditTableMap = auditTableQueryOutput.getRecords().stream().collect(Collectors.toMap(r -> r.getValueInteger("id"), r -> r, (a, b) -> a, HashMap::new));
            runBackendStepOutput.addValue("auditTableMap", auditTableMap);

            HashMap<String, QFrontendTableMetaData> tableMetaDataMap = new HashMap<>();
            for(QRecord auditTable : auditTableMap.values())
            {
               String subTableName = auditTable.getValueString("name");

               TableMetaDataInput tableMetaDataInput = new TableMetaDataInput();
               tableMetaDataInput.setTableName(subTableName);
               TableMetaDataOutput tableMetaDataOutput = new TableMetaDataAction().execute(tableMetaDataInput);

               tableMetaDataMap.put(subTableName, tableMetaDataOutput.getTable());
            }

            runBackendStepOutput.addValue("tableMetaDataMap", tableMetaDataMap);
         }
      }
      catch(Exception e)
      {
         throw new QException("Error getting record audits", e);
      }
   }

}
