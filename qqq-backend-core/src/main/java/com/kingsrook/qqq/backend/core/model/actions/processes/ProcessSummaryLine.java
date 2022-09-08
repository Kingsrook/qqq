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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** For processes that may show a review & result screen, this class provides a
 ** standard way to summarize information about the records in the process.
 **
 *******************************************************************************/
public class ProcessSummaryLine implements Serializable
{
   private Status  status;
   private Integer count = 0;
   private String  message;

   //////////////////////////////////////////////////////////////////////////
   // using ArrayList, because we need to be Serializable, and List is not //
   //////////////////////////////////////////////////////////////////////////
   private ArrayList<Serializable> primaryKeys;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLine(Status status, Integer count, String message, ArrayList<Serializable> primaryKeys)
   {
      this.status = status;
      this.count = count;
      this.message = message;
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLine(Status status, Integer count, String message)
   {
      this.status = status;
      this.count = count;
      this.message = message;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLine(Status status, String message)
   {
      this.status = status;
      this.message = message;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLine(Status status)
   {
      this.status = status;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "ProcessSummaryLine{status=" + status + ", count=" + count + ", message='" + message + '\'' + '}';
   }



   /*******************************************************************************
    ** Getter for status
    **
    *******************************************************************************/
   public Status getStatus()
   {
      return status;
   }



   /*******************************************************************************
    ** Setter for status
    **
    *******************************************************************************/
   public void setStatus(Status status)
   {
      this.status = status;
   }



   /*******************************************************************************
    ** Getter for primaryKeys
    **
    *******************************************************************************/
   public List<Serializable> getPrimaryKeys()
   {
      return primaryKeys;
   }



   /*******************************************************************************
    ** Setter for primaryKeys
    **
    *******************************************************************************/
   public void setPrimaryKeys(ArrayList<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    ** Getter for count
    **
    *******************************************************************************/
   public Integer getCount()
   {
      return count;
   }



   /*******************************************************************************
    ** Setter for count
    **
    *******************************************************************************/
   public void setCount(Integer count)
   {
      this.count = count;
   }



   /*******************************************************************************
    ** Getter for message
    **
    *******************************************************************************/
   public String getMessage()
   {
      return message;
   }



   /*******************************************************************************
    ** Setter for message
    **
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void incrementCount()
   {
      incrementCount(1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void incrementCount(int amount)
   {
      if(count == null)
      {
         count = 0;
      }
      count += amount;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void incrementCountAndAddPrimaryKey(Serializable primaryKey)
   {
      incrementCount();

      if(primaryKeys == null)
      {
         primaryKeys = new ArrayList<>();
      }
      primaryKeys.add(primaryKey);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addSelfToListIfAnyCount(ArrayList<ProcessSummaryLine> rs)
   {
      if(count != null && count > 0)
      {
         rs.add(this);
      }
   }
}
