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

package com.kingsrook.qqq.backend.core.instances;


import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;


/*******************************************************************************
 * interface that can be added to a QSupplementalInstanceMetaData, to receive
 * QHelpContent records during instance boot or upon updates in the help content
 * table.
 *******************************************************************************/
public interface QHelpContentPlugin
{
   /***************************************************************************
    * accept a single helpContent record, and apply its data to some data in the
    * qInstance
    *
    * @param qInstance the active qInstance, that the content should be applied to
    * @param helpContent entity with values from HelpContent table
    * @param nameValuePairs parsed string -> string map from the help content key.
    ***************************************************************************/
   void acceptHelpContent(QInstance qInstance, QHelpContent helpContent, Map<String, String> nameValuePairs);
}
