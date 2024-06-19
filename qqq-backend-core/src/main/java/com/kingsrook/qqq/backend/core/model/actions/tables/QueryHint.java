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

package com.kingsrook.qqq.backend.core.model.actions.tables;


/*******************************************************************************
 ** Information about the query that an application (or qqq service) may know and
 ** want to tell the backend, that can help influence how the backend processes
 ** query.
 **
 ** For example, a query with potentially a large result set, for MySQL backend,
 ** we may want to configure the result set to stream results rather than do its
 ** default in-memory thing.  See RDBMSQueryAction for usage.
 *******************************************************************************/
public enum QueryHint
{
   POTENTIALLY_LARGE_NUMBER_OF_RESULTS,
   MAY_USE_READ_ONLY_BACKEND
}
