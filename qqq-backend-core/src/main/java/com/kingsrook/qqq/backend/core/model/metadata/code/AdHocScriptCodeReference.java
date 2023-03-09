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

package com.kingsrook.qqq.backend.core.model.metadata.code;


import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class AdHocScriptCodeReference extends QCodeReference
{
   ////////////////////////////////////////////////////////////////////////////////
   // can supply scriptId (in which case, current revisionId will be looked up), //
   // or revisionId (in which case, record will be looked up)                    //
   // or, the record.                                                            //
   ////////////////////////////////////////////////////////////////////////////////
   private Integer scriptId;
   private Integer scriptRevisionId;
   private QRecord scriptRevisionRecord;



   /*******************************************************************************
    ** Getter for scriptId
    *******************************************************************************/
   public Integer getScriptId()
   {
      return (this.scriptId);
   }



   /*******************************************************************************
    ** Setter for scriptId
    *******************************************************************************/
   public void setScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptId
    *******************************************************************************/
   public AdHocScriptCodeReference withScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptRevisionId
    *******************************************************************************/
   public Integer getScriptRevisionId()
   {
      return (this.scriptRevisionId);
   }



   /*******************************************************************************
    ** Setter for scriptRevisionId
    *******************************************************************************/
   public void setScriptRevisionId(Integer scriptRevisionId)
   {
      this.scriptRevisionId = scriptRevisionId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptRevisionId
    *******************************************************************************/
   public AdHocScriptCodeReference withScriptRevisionId(Integer scriptRevisionId)
   {
      this.scriptRevisionId = scriptRevisionId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptRevisionRecord
    *******************************************************************************/
   public QRecord getScriptRevisionRecord()
   {
      return (this.scriptRevisionRecord);
   }



   /*******************************************************************************
    ** Setter for scriptRevisionRecord
    *******************************************************************************/
   public void setScriptRevisionRecord(QRecord scriptRevisionRecord)
   {
      this.scriptRevisionRecord = scriptRevisionRecord;
   }



   /*******************************************************************************
    ** Fluent setter for scriptRevisionRecord
    *******************************************************************************/
   public AdHocScriptCodeReference withScriptRevisionRecord(QRecord scriptRevisionRecord)
   {
      this.scriptRevisionRecord = scriptRevisionRecord;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "AdHocScriptCodeReference{" +
         "scriptId=" + scriptId +
         ", scriptRevisionId=" + scriptRevisionId +
         ", scriptRevisionRecord=" + scriptRevisionRecord +
         '}';
   }
}
