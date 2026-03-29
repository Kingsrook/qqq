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


/***************************************************************************
 * Marker interface for objects that uniquely identify a {@link FieldFunctionType}.
 *
 * <p>Implementations are typically anonymous lambda expressions or enum constants
 * that return a stable, unique name string.  The name is used as the registry key
 * in {@link FieldFunctionIdentifierRegistry} and for JSON serialization.</p>
 ***************************************************************************/
public interface FieldFunctionTypeIdentifier
{
   /***************************************************************************
    * Returns the unique string name for this function type identifier.
    ***************************************************************************/
   String getName();
}
