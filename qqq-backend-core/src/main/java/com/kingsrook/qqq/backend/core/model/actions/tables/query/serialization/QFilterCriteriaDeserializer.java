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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.serialization;


import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.AbstractFilterExpression;


/*******************************************************************************
 ** Custom jackson deserializer, to deal w/ abstract expression field
 *******************************************************************************/
public class QFilterCriteriaDeserializer extends StdDeserializer<QFilterCriteria>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteriaDeserializer()
   {
      this(null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteriaDeserializer(Class<?> vc)
   {
      super(vc);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QFilterCriteria deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException
   {
      JsonNode     node         = jsonParser.getCodec().readTree(jsonParser);
      ObjectMapper objectMapper = new ObjectMapper();

      /////////////////////////////////
      // get values out of json node //
      /////////////////////////////////
      List              values         = objectMapper.treeToValue(node.get("values"), List.class);
      String            fieldName      = objectMapper.treeToValue(node.get("fieldName"), String.class);
      QCriteriaOperator operator       = objectMapper.treeToValue(node.get("operator"), QCriteriaOperator.class);
      String            otherFieldName = objectMapper.treeToValue(node.get("otherFieldName"), String.class);

      AbstractFilterExpression<?> expression = null;
      if(node.has("expression"))
      {
         JsonNode expressionNode = node.get("expression");
         String   expressionType = objectMapper.treeToValue(expressionNode.get("type"), String.class);

         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // right now, we'll assume that all expression subclasses are in the same package as AbstractFilterExpression //
         // so, we can just do a Class.forName on that name, and treeToValue like that.                                //
         // if we ever had to, we could instead switch(expressionType), and do like so...                              //
         // case "NowWithOffset" -> objectMapper.treeToValue(expressionNode, NowWithOffset.class);                     //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         try
         {
            String className = AbstractFilterExpression.class.getName().replace(AbstractFilterExpression.class.getSimpleName(), expressionType);
            expression = (AbstractFilterExpression<?>) objectMapper.treeToValue(expressionNode, Class.forName(className));
         }
         catch(Exception e)
         {
            throw (new IOException("Error deserializing expression of type [" + expressionType + "] inside QFilterCriteria", e));
         }
      }

      ///////////////////////////////////
      // put fields into return object //
      ///////////////////////////////////
      QFilterCriteria criteria = new QFilterCriteria();
      criteria.setFieldName(fieldName);
      criteria.setOperator(operator);
      criteria.setValues(values);
      criteria.setOtherFieldName(otherFieldName);
      criteria.setExpression(expression);

      return (criteria);

      /*
      int      id       = (Integer) (node.get("id")).numberValue();
      String   itemName = node.get("itemName").asText();
      int      userId   = (Integer) (node.get("createdBy")).numberValue();

      return null;
       */
   }
}
