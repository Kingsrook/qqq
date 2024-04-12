/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.scheduler.schedulable;


import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class SchedulableType
{
   private String         name;
   private QCodeReference runner;



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
   public SchedulableType withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for runner
    *******************************************************************************/
   public QCodeReference getRunner()
   {
      return (this.runner);
   }



   /*******************************************************************************
    ** Setter for runner
    *******************************************************************************/
   public void setRunner(QCodeReference runner)
   {
      this.runner = runner;
   }



   /*******************************************************************************
    ** Fluent setter for runner
    *******************************************************************************/
   public SchedulableType withRunner(QCodeReference runner)
   {
      this.runner = runner;
      return (this);
   }

}
