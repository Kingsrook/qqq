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


/*******************************************************************************
 ** definition of a qqq table that is "associated" with another table, e.g.,
 ** managed along with it - such as child-records under a parent record.
 *******************************************************************************/
public class Association
{
   private String name;
   private String associatedTableName;
   private String joinName;



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public Association withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for associatedTableName
    *******************************************************************************/
   public String getAssociatedTableName()
   {
      return (this.associatedTableName);
   }



   /*******************************************************************************
    ** Setter for associatedTableName
    *******************************************************************************/
   public void setAssociatedTableName(String associatedTableName)
   {
      this.associatedTableName = associatedTableName;
   }



   /*******************************************************************************
    ** Fluent setter for associatedTableName
    *******************************************************************************/
   public Association withAssociatedTableName(String associatedTableName)
   {
      this.associatedTableName = associatedTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinName
    *******************************************************************************/
   public String getJoinName()
   {
      return (this.joinName);
   }



   /*******************************************************************************
    ** Setter for joinName
    *******************************************************************************/
   public void setJoinName(String joinName)
   {
      this.joinName = joinName;
   }



   /*******************************************************************************
    ** Fluent setter for joinName
    *******************************************************************************/
   public Association withJoinName(String joinName)
   {
      this.joinName = joinName;
      return (this);
   }

}