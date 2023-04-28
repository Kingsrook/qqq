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

package com.kingsrook.qqq.backend.core.model.actions.scripts;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunAdHocRecordScriptInput extends AbstractRunScriptInput<AdHocScriptCodeReference>
{
   private List<Serializable> recordPrimaryKeyList; // can either supply recordList, or recordPrimaryKeyList
   private List<QRecord>      recordList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunAdHocRecordScriptInput()
   {
   }



   /*******************************************************************************
    ** Getter for recordList
    *******************************************************************************/
   public List<QRecord> getRecordList()
   {
      return (this.recordList);
   }



   /*******************************************************************************
    ** Setter for recordList
    *******************************************************************************/
   public void setRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
   }



   /*******************************************************************************
    ** Fluent setter for recordList
    *******************************************************************************/
   public RunAdHocRecordScriptInput withRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordPrimaryKeyList
    *******************************************************************************/
   public List<Serializable> getRecordPrimaryKeyList()
   {
      return (this.recordPrimaryKeyList);
   }



   /*******************************************************************************
    ** Setter for recordPrimaryKeyList
    *******************************************************************************/
   public void setRecordPrimaryKeyList(List<Serializable> recordPrimaryKeyList)
   {
      this.recordPrimaryKeyList = recordPrimaryKeyList;
   }



   /*******************************************************************************
    ** Fluent setter for recordPrimaryKeyList
    *******************************************************************************/
   public RunAdHocRecordScriptInput withRecordPrimaryKeyList(List<Serializable> recordPrimaryKeyList)
   {
      this.recordPrimaryKeyList = recordPrimaryKeyList;
      return (this);
   }

}
