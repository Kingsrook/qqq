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

package com.kingsrook.qqq.backend.core.processes.implementations.sharing;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class SharingMetaDataProvider
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, Consumer<QProcessMetaData> processEnricher) throws QException
   {
      List<QProcessMetaData> processes = new ArrayList<>();
      processes.add(new GetSharedRecordsProcess().produce(instance));
      processes.add(new InsertSharedRecordProcess().produce(instance));
      processes.add(new EditSharedRecordProcess().produce(instance));
      processes.add(new DeleteSharedRecordProcess().produce(instance));

      for(QProcessMetaData process : processes)
      {
         if(processEnricher != null)
         {
            processEnricher.accept(process);
         }

         instance.addProcess(process);
      }
   }

}
