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

package com.kingsrook.qqq.backend.core.model.metadata.serialization;


import java.io.IOException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;


/*******************************************************************************
 ** Jackson custom deserialization class, to return an appropriate sub-type of
 ** QTableBackendDetails, based on the backendType of the containing table.
 *******************************************************************************/
public class QStepMetaDataDeserializer extends JsonDeserializer<QStepMetaData>
{
   @Override
   @SuppressWarnings("checkstyle:Indentation")
   public QStepMetaData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException
   {
      TreeNode treeNode = jsonParser.readValueAsTree();
      String   stepType = DeserializerUtils.readTextValue(treeNode, "stepType");
      Class<? extends QStepMetaData> targetClass = switch(stepType)
         {
            case "backend" -> QBackendStepMetaData.class;
            case "frontend" -> QFrontendStepMetaData.class;
            default -> throw new IllegalArgumentException("Unsupported StepType " + stepType + " for deserialization");
         };
      return (DeserializerUtils.reflectivelyDeserialize(targetClass, treeNode));
   }

}
