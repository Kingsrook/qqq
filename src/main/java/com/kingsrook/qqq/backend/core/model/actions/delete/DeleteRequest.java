/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.actions.delete;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQTableRequest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Request for a Delete action.
 **
 *******************************************************************************/
public class DeleteRequest extends AbstractQTableRequest
{
   private List<Serializable> primaryKeys;



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteRequest(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    ** Getter for ids
    **
    *******************************************************************************/
   public List<Serializable> getPrimaryKeys()
   {
      return primaryKeys;
   }



   /*******************************************************************************
    ** Setter for ids
    **
    *******************************************************************************/
   public void setPrimaryKeys(List<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    ** Setter for ids
    **
    *******************************************************************************/
   public DeleteRequest withPrimaryKeys(List<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
      return (this);
   }
}
