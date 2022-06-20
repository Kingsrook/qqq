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

package com.kingsrook.qqq.backend.module.filesystem.local.actions;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.UpdateInterface;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilesystemUpdateAction implements UpdateInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateResult execute(UpdateRequest updateRequest) throws QException
   {
      try
      {
         UpdateResult rs = new UpdateResult();
         QTableMetaData table = updateRequest.getTable();

         List<QRecordWithStatus> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);

         throw new NotImplementedException("Filesystem update not implemented");

         // return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
   }

}
