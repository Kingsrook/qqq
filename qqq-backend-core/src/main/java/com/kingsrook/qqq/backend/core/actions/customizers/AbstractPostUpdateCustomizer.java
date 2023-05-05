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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractPostUpdateCustomizer
{
   protected UpdateInput   updateInput;
   protected List<QRecord> oldRecordList;

   private Map<Serializable, QRecord> oldRecordMap = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract List<QRecord> apply(List<QRecord> records);



   /*******************************************************************************
    ** Getter for updateInput
    **
    *******************************************************************************/
   public UpdateInput getUpdateInput()
   {
      return updateInput;
   }



   /*******************************************************************************
    ** Setter for updateInput
    **
    *******************************************************************************/
   public void setUpdateInput(UpdateInput updateInput)
   {
      this.updateInput = updateInput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setOldRecordList(List<QRecord> oldRecordList)
   {
      this.oldRecordList = oldRecordList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getOldRecordList()
   {
      return oldRecordList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Map<Serializable, QRecord> getOldRecordMap()
   {
      if(oldRecordMap == null)
      {
         oldRecordMap = new HashMap<>();
         for(QRecord qRecord : oldRecordList)
         {
            oldRecordMap.put(qRecord.getValue(updateInput.getTable().getPrimaryKeyField()), qRecord);
         }
      }

      return (oldRecordMap);
   }

}
