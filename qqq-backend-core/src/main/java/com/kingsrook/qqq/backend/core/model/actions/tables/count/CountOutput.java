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

package com.kingsrook.qqq.backend.core.model.actions.tables.count;


import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 ** Output for a count action
 **
 *******************************************************************************/
public class CountOutput extends AbstractActionOutput
{
   private Integer count;
   private Integer distinctCount;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer getCount()
   {
      return count;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setCount(Integer count)
   {
      this.count = count;
   }



   /*******************************************************************************
    ** Getter for distinctCount
    *******************************************************************************/
   public Integer getDistinctCount()
   {
      return (this.distinctCount);
   }



   /*******************************************************************************
    ** Setter for distinctCount
    *******************************************************************************/
   public void setDistinctCount(Integer distinctCount)
   {
      this.distinctCount = distinctCount;
   }



   /*******************************************************************************
    ** Fluent setter for distinctCount
    *******************************************************************************/
   public CountOutput withDistinctCount(Integer distinctCount)
   {
      this.distinctCount = distinctCount;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for count
    *******************************************************************************/
   public CountOutput withCount(Integer count)
   {
      this.count = count;
      return (this);
   }

}
