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
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunAssociatedScriptOutput extends AbstractActionOutput
{
   private Serializable output;
   private Integer      scriptRevisionId;



   /*******************************************************************************
    ** Getter for output
    **
    *******************************************************************************/
   public Serializable getOutput()
   {
      return output;
   }



   /*******************************************************************************
    ** Setter for output
    **
    *******************************************************************************/
   public void setOutput(Serializable output)
   {
      this.output = output;
   }



   /*******************************************************************************
    ** Fluent setter for output
    **
    *******************************************************************************/
   public RunAssociatedScriptOutput withOutput(Serializable output)
   {
      this.output = output;
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
   public RunAssociatedScriptOutput withScriptRevisionId(Integer scriptRevisionId)
   {
      this.scriptRevisionId = scriptRevisionId;
      return (this);
   }


}
