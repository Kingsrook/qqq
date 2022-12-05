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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Standard/re-usable post-insert customizer, for the use case where, when we
 ** do an insert into table "parent", we want a record automatically inserted into
 ** table "child", and there's a foreign key in "parent", pointed at "child"
 ** e.g., named: "parent.childId".
 **
 ** A similar use-case would have the foreign key in the child table - in which case,
 ** we could add a "Type" enum, plus abstract method to get our "Type", then logic
 ** to switch behavior based on type.  See existing type enum, but w/ only 1 case :)
 *******************************************************************************/
public abstract class ChildInserterPostInsertCustomizer extends AbstractPostInsertCustomizer
{
   public enum RelationshipType
   {
      PARENT_POINTS_AT_CHILD
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract QRecord buildChildForRecord(QRecord parentRecord) throws QException;

   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract String getChildTableName();

   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract String getForeignKeyFieldName();

   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract RelationshipType getRelationshipType();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> apply(List<QRecord> records)
   {
      try
      {
         List<QRecord>  rs               = new ArrayList<>();
         List<QRecord>  childrenToInsert = new ArrayList<>();
         QTableMetaData table            = getInsertInput().getTable();
         QTableMetaData childTable       = getInsertInput().getInstance().getTable(getChildTableName());

         ////////////////////////////////////////////////////////////////////////////////
         // iterate over the inserted records, building a list child records to insert //
         // for ones missing a value in the foreign key field.                         //
         ////////////////////////////////////////////////////////////////////////////////
         for(QRecord record : records)
         {
            if(record.getValue(getForeignKeyFieldName()) == null)
            {
               childrenToInsert.add(buildChildForRecord(record));
            }
         }

         ///////////////////////////////////////////////////////////////////////////////////
         // if there are no children to insert, then just return the original record list //
         ///////////////////////////////////////////////////////////////////////////////////
         if(childrenToInsert.isEmpty())
         {
            return (records);
         }

         /////////////////////////
         // insert the children //
         /////////////////////////
         InsertInput insertInput = new InsertInput(getInsertInput().getInstance());
         insertInput.setSession(getInsertInput().getSession());
         insertInput.setTableName(getChildTableName());
         insertInput.setRecords(childrenToInsert);
         InsertOutput      insertOutput           = new InsertAction().execute(insertInput);
         Iterator<QRecord> insertedRecordIterator = insertOutput.getRecords().iterator();

         //////////////////////////////////////////////////////////////////////////////////////////////////////
         // iterate over the original list of records again - for any that need a child (e.g., are missing   //
         // foreign key), set their foreign key to a newly inserted child's key, and add them to be updated. //
         //////////////////////////////////////////////////////////////////////////////////////////////////////
         List<QRecord> recordsToUpdate = new ArrayList<>();
         for(QRecord record : records)
         {
            Serializable primaryKey = record.getValue(table.getPrimaryKeyField());
            if(record.getValue(getForeignKeyFieldName()) == null)
            {
               Serializable foreignKey = insertedRecordIterator.next().getValue(childTable.getPrimaryKeyField());
               recordsToUpdate.add(new QRecord().withValue(table.getPrimaryKeyField(), primaryKey).withValue(getForeignKeyFieldName(), foreignKey));
               record.setValue(getForeignKeyFieldName(), foreignKey);
               rs.add(record);
            }
            else
            {
               rs.add(record);
            }
         }

         ////////////////////////////////////////////////////////////////////////////
         // update the originally inserted records to reference their new children //
         ////////////////////////////////////////////////////////////////////////////
         UpdateInput updateInput = new UpdateInput(insertInput.getInstance());
         updateInput.setSession(getInsertInput().getSession());
         updateInput.setTableName(getInsertInput().getTableName());
         updateInput.setRecords(recordsToUpdate);
         new UpdateAction().execute(updateInput);

         return (rs);
      }
      catch(Exception e)
      {
         throw new RuntimeException("Error inserting new child records for new parent records", e);
      }
   }
}
