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

package com.kingsrook.qqq.backend.core.model.actions.audits;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Object to accumulate multiple audit-details to be recorded under a single
 ** audit per-record, within a process step.  Especially useful if/when the
 ** process step spreads its work out through multiple classes.
 **
 ** Pattern of usage looks like:
 **
 ** <pre>
 ** // declare as a field (or local) w/ message for the audit headers
 ** private AuditDetailAccumulator auditDetailAccumulator = new AuditDetailAccumulator("Audit header message");
 **
 **  // put into thread context
 **  AuditDetailAccumulator.setInContext(auditDetailAccumulator);
 **
 **  // add a detail message for a record
 **  auditDetailAccumulator.addAuditDetail(tableName, record, "Detail message");
 **
 **  // in another class, get the accumulator from context and safely add a detail message
 **  AuditDetailAccumulator.getFromContext().ifPresent(ada -> ada.addAuditDetail(tableName, record, "More Details"));
 **
 **  // at the end of a step run/runOnePage method, add the accumulated audit details to step output
 **  auditDetailAccumulator.getAccumulatedAuditSingleInputs().forEach(runBackendStepOutput::addAuditSingleInput);
 **  auditDetailAccumulator.clear();
 ** </pre>
 *******************************************************************************/
public class AuditDetailAccumulator implements Serializable
{
   private static final QLogger LOG = QLogger.getLogger(AuditDetailAccumulator.class);

   private static final String objectKey = AuditDetailAccumulator.class.getSimpleName();

   private String header;

   private Map<TableNameAndPrimaryKey, AuditSingleInput> recordAuditInputMap = new HashMap<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AuditDetailAccumulator(String header)
   {
      this.header = header;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setInContext()
   {
      QContext.setObject(objectKey, this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<AuditDetailAccumulator> getFromContext()
   {
      return QContext.getObject(objectKey, AuditDetailAccumulator.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addAuditDetail(String tableName, QRecordEntity entity, String message)
   {
      if(entity != null)
      {
         addAuditDetail(tableName, entity.toQRecord(), message);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addAuditDetail(String tableName, QRecord record, String message)
   {
      QTableMetaData table      = QContext.getQInstance().getTable(tableName);
      Serializable   primaryKey = record.getValue(table.getPrimaryKeyField());
      if(primaryKey == null)
      {
         LOG.info("Missing primary key in input record - audit detail message will not be recorded.", logPair("message", message));
         return;
      }

      AuditSingleInput auditSingleInput = recordAuditInputMap.computeIfAbsent(new TableNameAndPrimaryKey(tableName, primaryKey), (key) -> new AuditSingleInput(table, record, header));
      auditSingleInput.addDetail(message);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public Collection<AuditSingleInput> getAccumulatedAuditSingleInputs()
   {
      return (recordAuditInputMap.values());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public void clear()
   {
      recordAuditInputMap.clear();
   }


   private record TableNameAndPrimaryKey(String tableName, Serializable primaryKey) {}
}
