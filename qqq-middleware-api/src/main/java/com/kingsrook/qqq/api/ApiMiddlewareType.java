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

package com.kingsrook.qqq.api;


import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public interface ApiMiddlewareType
{
   String NAME = "api";

   /*******************************************************************************
    **
    *******************************************************************************/
   static ApiInstanceMetaData getApiInstanceMetaData(QInstance instance)
   {
      return ((ApiInstanceMetaData) instance.getMiddlewareMetaData(NAME));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   static ApiTableMetaData getApiTableMetaData(QTableMetaData table)
   {
      return ((ApiTableMetaData) table.getMiddlewareMetaData(NAME));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   static ApiFieldMetaData getApiFieldMetaData(QFieldMetaData field)
   {
      return ((ApiFieldMetaData) field.getMiddlewareMetaData(NAME));
   }

}
