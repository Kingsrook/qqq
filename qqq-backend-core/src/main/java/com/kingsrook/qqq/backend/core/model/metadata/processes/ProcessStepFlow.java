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

package com.kingsrook.qqq.backend.core.model.metadata.processes;


/*******************************************************************************
 ** Possible ways the steps of a process can flow.
 **
 ** LINEAR - (the default) - the list of steps in the process are executed in-order
 **
 ** STATE_MACHINE - concept of "states", each which has a backend & frontend step;
 ** a backend step can (must?) set the field "stepState" (or "nextStepName") to
 ** say what the next (frontend) step is.
 *******************************************************************************/
public enum ProcessStepFlow
{
   LINEAR,
   STATE_MACHINE
}
