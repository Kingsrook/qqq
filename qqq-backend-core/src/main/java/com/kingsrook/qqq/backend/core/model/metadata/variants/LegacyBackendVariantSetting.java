/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.variants;


/*******************************************************************************
 ** temporary class, while we migrate from original way that variants were set up
 ** e.g., by calling 'variantOptionsTableUsernameField', to the new way, using
 ** the BackendVariantsConfig which uses a map of enum constants.
 **
 ** so when those deprecated setters are removed, this enum can be too.
 *****************************************************************************/
public enum LegacyBackendVariantSetting implements BackendVariantSetting
{
   USERNAME,
   PASSWORD,
   API_KEY,
   CLIENT_ID,
   CLIENT_SECRET
}
