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


import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class StoreAssociatedScriptOutput extends AbstractActionOutput
{
   private Integer scriptId;
   private String  scriptName;
   private Integer scriptRevisionId;
   private Integer scriptRevisionSequenceNo;



   /*******************************************************************************
    ** Getter for scriptId
    **
    *******************************************************************************/
   public Integer getScriptId()
   {
      return scriptId;
   }



   /*******************************************************************************
    ** Setter for scriptId
    **
    *******************************************************************************/
   public void setScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
   }



   /*******************************************************************************
    ** Getter for scriptName
    **
    *******************************************************************************/
   public String getScriptName()
   {
      return scriptName;
   }



   /*******************************************************************************
    ** Setter for scriptName
    **
    *******************************************************************************/
   public void setScriptName(String scriptName)
   {
      this.scriptName = scriptName;
   }



   /*******************************************************************************
    ** Getter for scriptRevisionId
    **
    *******************************************************************************/
   public Integer getScriptRevisionId()
   {
      return scriptRevisionId;
   }



   /*******************************************************************************
    ** Setter for scriptRevisionId
    **
    *******************************************************************************/
   public void setScriptRevisionId(Integer scriptRevisionId)
   {
      this.scriptRevisionId = scriptRevisionId;
   }



   /*******************************************************************************
    ** Getter for scriptRevisionSequenceNo
    **
    *******************************************************************************/
   public Integer getScriptRevisionSequenceNo()
   {
      return scriptRevisionSequenceNo;
   }



   /*******************************************************************************
    ** Setter for scriptRevisionSequenceNo
    **
    *******************************************************************************/
   public void setScriptRevisionSequenceNo(Integer scriptRevisionSequenceNo)
   {
      this.scriptRevisionSequenceNo = scriptRevisionSequenceNo;
   }

}
