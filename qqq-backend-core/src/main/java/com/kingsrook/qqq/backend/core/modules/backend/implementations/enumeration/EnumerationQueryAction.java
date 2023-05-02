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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.enumeration;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEnum;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.utils.BackendQueryFilterUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class EnumerationQueryAction implements QueryInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      try
      {
         QTableMetaData                 table          = queryInput.getTable();
         EnumerationTableBackendDetails backendDetails = (EnumerationTableBackendDetails) table.getBackendDetails();
         Class<? extends QRecordEnum>   enumClass      = backendDetails.getEnumClass();
         QRecordEnum[]                  values         = (QRecordEnum[]) enumClass.getMethod("values").invoke(null);

         //////////////////////////////////////////////
         // note - not good streaming behavior here. //
         //////////////////////////////////////////////

         List<QRecord> recordList = new ArrayList<>();
         for(QRecordEnum value : values)
         {
            QRecord record        = value.toQRecord();
            boolean recordMatches = BackendQueryFilterUtils.doesRecordMatch(queryInput.getFilter(), record);
            if(recordMatches)
            {
               recordList.add(record);
            }
         }

         BackendQueryFilterUtils.sortRecordList(queryInput.getFilter(), recordList);
         recordList = BackendQueryFilterUtils.applySkipAndLimit(queryInput.getFilter(), recordList);

         QueryOutput queryOutput = new QueryOutput(queryInput);
         queryOutput.addRecords(recordList);
         return queryOutput;
      }
      catch(Exception e)
      {
         throw (new QException("Error executing query", e));
      }
   }

}
