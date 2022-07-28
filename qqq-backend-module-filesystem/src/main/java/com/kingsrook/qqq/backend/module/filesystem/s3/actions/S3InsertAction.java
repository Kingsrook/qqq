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

package com.kingsrook.qqq.backend.module.filesystem.s3.actions;


import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 **
 *******************************************************************************/
public class S3InsertAction implements InsertInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      throw new NotImplementedException("S3 insert not implemented");
      /*
      try
      {
         InsertResult rs = new InsertResult();
         QTableMetaData table = insertRequest.getTable();

         List<QRecord> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);


         // return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
      */
   }

}
