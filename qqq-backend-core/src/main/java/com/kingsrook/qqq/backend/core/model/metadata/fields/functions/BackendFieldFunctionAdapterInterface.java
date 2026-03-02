/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.fields.functions;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 * interface for an "adapter" class that implements a {@link FieldFunction} for a
 * particular backend ({@link QBackendModuleInterface}).
 *
 * <p>For example, RDBMS - needs to wrap column names in function calls.</p>
 *
 * <p>It is expected that individual modules would define their own subinterfaces
 * which would define the actual methods appropriate for that backend.</p>
 *
 * <p>This interface just provides a common type to be exposed in the
 * {@link BackendFieldFunctionAdapterRegistry}</p>
 *******************************************************************************/
public interface BackendFieldFunctionAdapterInterface extends Serializable
{
}
