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

package com.kingsrook.qqq.backend.core.actions.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;


/*******************************************************************************
 ** Interface for the Delete action.
 **
 *******************************************************************************/
public interface DeleteInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   DeleteOutput execute(DeleteInput deleteInput) throws QException;

   /*******************************************************************************
    ** Specify whether this particular module's delete action natively supports
    ** receiving a queryFilter as input (e.g., SQL does).  If the module doesn't
    ** support a query filter, then the qqq framework (DeleteAction) will, if it
    ** receives a queryFilter in its input, it will execute the query, and pass
    ** the list of primary keys down into the module's delete implementation.
    *******************************************************************************/
   default boolean supportsQueryFilterInput()
   {
      return (false);
   }

}
