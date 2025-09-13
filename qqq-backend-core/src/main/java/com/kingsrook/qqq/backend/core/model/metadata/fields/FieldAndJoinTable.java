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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.util.Collection;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Wrapper (record) that holds a QFieldMetaData and a QTableMetaData -
 **
 ** With a factory method (`get()`) to go from the use-case of, a String that's
 ** "joinTable.fieldName" or "fieldName" to the pair.
 **
 ** Note that the "joinTable" member here - could be the "mainTable" passed in
 ** to that `get()` method.
 **
 *******************************************************************************/
public record FieldAndJoinTable(QFieldMetaData field, QTableMetaData joinTable)
{

   /***************************************************************************
    ** given a table, and a field-name string (which should either be the name
    ** of a field on that table, or another tableName + "." + fieldName (from
    ** that table) - get back the pair of table & field metaData that the
    ** input string is talking about.
    ***************************************************************************/
   public static FieldAndJoinTable get(QTableMetaData mainTable, String fieldName) throws QException
   {
      if(fieldName.indexOf('.') > -1)
      {
         String joinTableName = fieldName.replaceAll("\\..*", "");
         String joinFieldName = fieldName.replaceAll(".*\\.", "");

         QTableMetaData joinTable = QContext.getQInstance().getTable(joinTableName);
         if(joinTable == null)
         {
            throw (new QException("Unrecognized join table name: " + joinTableName));
         }

         return new FieldAndJoinTable(joinTable.getField(joinFieldName), joinTable);
      }
      else
      {
         return new FieldAndJoinTable(mainTable.getField(fieldName), mainTable);
      }
   }



   /***************************************************************************
    ** given a table, and a field-name string (which should either be the name
    ** of a field on that table, or another tableName + "." + fieldName (from
    ** that table - or an alias insteaad of tableName) - get back the pair of
    ** table & field metaData that the input string is talking about.
    ***************************************************************************/
   public static FieldAndJoinTable get(QTableMetaData mainTable, String fieldName, Collection<QueryJoin> queryJoins) throws QException
   {
      if(fieldName.indexOf('.') > -1)
      {
         String joinTableName = fieldName.replaceAll("\\..*", "");
         String joinFieldName = fieldName.replaceAll(".*\\.", "");

         QTableMetaData joinTable = QContext.getQInstance().getTable(joinTableName);
         if(joinTable == null)
         {
            ///////////////////////////////////////////////////////////////
            // check if the table name is an alias in a given query join //
            ///////////////////////////////////////////////////////////////
            for(QueryJoin queryJoin : CollectionUtils.nonNullCollection(queryJoins))
            {
               if(joinTableName.equals(queryJoin.getAlias()))
               {
                  joinTable = QContext.getQInstance().getTable(queryJoin.getJoinTable());
               }
            }
         }

         if(joinTable == null)
         {
            throw (new QException("Unrecognized join table name: " + joinTableName));
         }

         return new FieldAndJoinTable(joinTable.getField(joinFieldName), joinTable);
      }
      else
      {
         return new FieldAndJoinTable(mainTable.getField(fieldName), mainTable);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getLabel(QTableMetaData mainTable)
   {
      if(mainTable.getName().equals(joinTable.getName()))
      {
         return (field.getLabel());
      }
      else
      {
         return (joinTable.getLabel() + ": " + field.getLabel());
      }
   }
}
