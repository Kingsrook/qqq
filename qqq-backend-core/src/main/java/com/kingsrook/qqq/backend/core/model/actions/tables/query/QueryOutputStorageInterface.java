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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Interface used within QueryOutput, to handle diffrent ways we may store records
 ** (e.g., in a list (that holds them all), or a pipe, that streams them to a consumer thread))
 *******************************************************************************/
interface QueryOutputStorageInterface
{

   /*******************************************************************************
    ** add a records to this output
    *******************************************************************************/
   void addRecord(QRecord record);


   /*******************************************************************************
    ** add a list of records to this output
    *******************************************************************************/
   void addRecords(List<QRecord> records);

   /*******************************************************************************
    ** Get all stored records
    *******************************************************************************/
   List<QRecord> getRecords();
}
