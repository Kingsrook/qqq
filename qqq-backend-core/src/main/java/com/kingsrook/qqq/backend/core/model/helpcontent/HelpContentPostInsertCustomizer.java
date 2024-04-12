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

package com.kingsrook.qqq.backend.core.model.helpcontent;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPostInsertCustomizer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceHelpContentManager;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** after records are inserted, put their help content in meta-data
 *******************************************************************************/
public class HelpContentPostInsertCustomizer extends AbstractPostInsertCustomizer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> apply(List<QRecord> records) throws QException
   {
      return insertRecordsIntoMetaData(records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static List<QRecord> insertRecordsIntoMetaData(List<QRecord> records)
   {
      if(records != null)
      {
         for(QRecord record : records)
         {
            QInstanceHelpContentManager.processHelpContentRecord(QContext.getQInstance(), record);
         }
      }

      return records;
   }

}
