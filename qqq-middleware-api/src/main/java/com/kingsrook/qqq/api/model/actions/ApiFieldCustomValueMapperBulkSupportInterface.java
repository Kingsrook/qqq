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

package com.kingsrook.qqq.api.model.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** optional interface that can be added to an ApiFieldCustomValueMapper to
 ** signal that the customizer knows how to work in bulk.
 **
 ** e.g., given a list of records, do a bulk query for data; memoize that data;
 ** then use it in multiple calls to produceApiValue, without, e.g., going back to
 ** a backend to fetch data.
 *******************************************************************************/
public interface ApiFieldCustomValueMapperBulkSupportInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   void prepareToProduceApiValues(List<QRecord> records);

}
