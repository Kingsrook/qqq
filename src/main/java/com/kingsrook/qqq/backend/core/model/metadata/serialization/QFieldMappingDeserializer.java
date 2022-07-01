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
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;


/*******************************************************************************
 ** Jackson custom deserialization class, to return an appropriate sub-type of
 ** A QBackendMetaData, based on the backendType specified within.
 *******************************************************************************/
public class QFieldMappingDeserializer extends JsonDeserializer<AbstractQFieldMapping>
{
   @Override
   public AbstractQFieldMapping deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException
   {
      TreeNode treeNode = jsonParser.readValueAsTree();

      TreeNode sourceTypeTreeNode = treeNode.get("sourceType");
      if(sourceTypeTreeNode == null || sourceTypeTreeNode instanceof NullNode)
      {
         throw new IOException("Missing sourceType in serializedMapping");
      }

      if(!(sourceTypeTreeNode instanceof TextNode textNode))
      {
         throw new IOException("sourceType is not a string value (is: " + sourceTypeTreeNode.getClass().getSimpleName() + ")");
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // get the value of the backendType json node, and use it to look up the qBackendModule object //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      String backendType = textNode.asText();

      QBackendModuleInterface backendModule = DeserializerUtils.getBackendModule(treeNode);

      return null;
   }

}
