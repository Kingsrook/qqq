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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.util.List;


/*******************************************************************************
 **
 *******************************************************************************/
public class ExposedJoin
{
   private String       label;
   private String       joinTable;
   private List<String> joinPath;



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public ExposedJoin withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinTable
    *******************************************************************************/
   public String getJoinTable()
   {
      return (this.joinTable);
   }



   /*******************************************************************************
    ** Setter for joinTable
    *******************************************************************************/
   public void setJoinTable(String joinTable)
   {
      this.joinTable = joinTable;
   }



   /*******************************************************************************
    ** Fluent setter for joinTable
    *******************************************************************************/
   public ExposedJoin withJoinTable(String joinTable)
   {
      this.joinTable = joinTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinPath
    *******************************************************************************/
   public List<String> getJoinPath()
   {
      return (this.joinPath);
   }



   /*******************************************************************************
    ** Setter for joinPath
    *******************************************************************************/
   public void setJoinPath(List<String> joinPath)
   {
      this.joinPath = joinPath;
   }



   /*******************************************************************************
    ** Fluent setter for joinPath
    *******************************************************************************/
   public ExposedJoin withJoinPath(List<String> joinPath)
   {
      this.joinPath = joinPath;
      return (this);
   }

}
