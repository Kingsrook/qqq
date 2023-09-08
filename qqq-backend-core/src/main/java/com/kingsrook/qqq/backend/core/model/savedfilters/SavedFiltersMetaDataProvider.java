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

package com.kingsrook.qqq.backend.core.model.savedfilters;


import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.savedfilters.DeleteSavedFilterProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.savedfilters.QuerySavedFilterProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.savedfilters.StoreSavedFilterProcess;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedFiltersMetaDataProvider
{


   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      instance.addTable(defineSavedFilterTable(backendName, backendDetailEnricher));
      instance.addPossibleValueSource(defineSavedFilterPossibleValueSource());
      instance.addProcess(QuerySavedFilterProcess.getProcessMetaData());
      instance.addProcess(StoreSavedFilterProcess.getProcessMetaData());
      instance.addProcess(DeleteSavedFilterProcess.getProcessMetaData());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineSavedFilterTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData table = new QTableMetaData()
         .withName(SavedFilter.TABLE_NAME)
         .withLabel("Saved Filter")
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("label")
         .withBackendName(backendName)
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(SavedFilter.class);

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(table);
      }

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QPossibleValueSource defineSavedFilterPossibleValueSource()
   {
      return new QPossibleValueSource()
         .withName(SavedFilter.TABLE_NAME)
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(SavedFilter.TABLE_NAME)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY)
         .withOrderByField("label");
   }

}
