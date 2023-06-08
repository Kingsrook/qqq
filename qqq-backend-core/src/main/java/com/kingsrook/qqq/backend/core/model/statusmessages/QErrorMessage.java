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

package com.kingsrook.qqq.backend.core.model.statusmessages;


/*******************************************************************************
 ** Abstract base class for all errors to be attached to QRecords.
 **
 ** Framework is aware of a few specific sultypes here, that drive things like
 ** returning http 400 vs 500.
 **
 ** Applications can define further subtypes (recommended to subclass this class's
 ** children, rather than this class), to help give (type-based) definition to errors.
 *******************************************************************************/
public abstract class QErrorMessage extends QStatusMessage
{

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QErrorMessage(String message)
   {
      super(message);
   }

}
