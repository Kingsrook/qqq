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

package com.kingsrook.qqq.backend.core.model.actions.tables.replace;


import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class ReplaceOutput extends AbstractActionOutput
{
   private InsertOutput insertOutput;
   private UpdateOutput updateOutput;
   private DeleteOutput deleteOutput;



   /*******************************************************************************
    ** Getter for insertOutput
    *******************************************************************************/
   public InsertOutput getInsertOutput()
   {
      return (this.insertOutput);
   }



   /*******************************************************************************
    ** Setter for insertOutput
    *******************************************************************************/
   public void setInsertOutput(InsertOutput insertOutput)
   {
      this.insertOutput = insertOutput;
   }



   /*******************************************************************************
    ** Fluent setter for insertOutput
    *******************************************************************************/
   public ReplaceOutput withInsertOutput(InsertOutput insertOutput)
   {
      this.insertOutput = insertOutput;
      return (this);
   }



   /*******************************************************************************
    ** Getter for updateOutput
    *******************************************************************************/
   public UpdateOutput getUpdateOutput()
   {
      return (this.updateOutput);
   }



   /*******************************************************************************
    ** Setter for updateOutput
    *******************************************************************************/
   public void setUpdateOutput(UpdateOutput updateOutput)
   {
      this.updateOutput = updateOutput;
   }



   /*******************************************************************************
    ** Fluent setter for updateOutput
    *******************************************************************************/
   public ReplaceOutput withUpdateOutput(UpdateOutput updateOutput)
   {
      this.updateOutput = updateOutput;
      return (this);
   }



   /*******************************************************************************
    ** Getter for deleteOutput
    *******************************************************************************/
   public DeleteOutput getDeleteOutput()
   {
      return (this.deleteOutput);
   }



   /*******************************************************************************
    ** Setter for deleteOutput
    *******************************************************************************/
   public void setDeleteOutput(DeleteOutput deleteOutput)
   {
      this.deleteOutput = deleteOutput;
   }



   /*******************************************************************************
    ** Fluent setter for deleteOutput
    *******************************************************************************/
   public ReplaceOutput withDeleteOutput(DeleteOutput deleteOutput)
   {
      this.deleteOutput = deleteOutput;
      return (this);
   }

}
