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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;


/*******************************************************************************
 ** A row of values, e.g., from a file, for bulk-load
 *******************************************************************************/
public class BulkLoadFileRow implements Serializable
{
   private int            rowNo;
   private Serializable[] values;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BulkLoadFileRow(Serializable[] values, int rowNo)
   {
      this.values = values;
      this.rowNo = rowNo;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public int size()
   {
      if(values == null)
      {
         return (0);
      }

      return (values.length);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public boolean hasIndex(int i)
   {
      if(values == null)
      {
         return (false);
      }

      if(i >= values.length || i < 0)
      {
         return (false);
      }

      return (true);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public Serializable getValue(int i)
   {
      if(values == null)
      {
         throw new IllegalStateException("Row has no values");
      }

      if(i >= values.length || i < 0)
      {
         throw new IllegalArgumentException("Index out of bounds:  Requested index " + i + "; values.length: " + values.length);
      }

      return (values[i]);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public Serializable getValueElseNull(int i)
   {
      if(!hasIndex(i))
      {
         return (null);
      }

      return (values[i]);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String toString()
   {
      if(values == null)
      {
         return ("null");
      }

      return Arrays.stream(values).map(String::valueOf).collect(Collectors.joining(","));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }

      if(o == null || getClass() != o.getClass())
      {
         return false;
      }

      BulkLoadFileRow that = (BulkLoadFileRow) o;
      return rowNo == that.rowNo && Objects.deepEquals(values, that.values);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(rowNo, Arrays.hashCode(values));
   }



   /*******************************************************************************
    ** Getter for rowNo
    *******************************************************************************/
   public int getRowNo()
   {
      return (this.rowNo);
   }



   /*******************************************************************************
    ** Setter for rowNo
    *******************************************************************************/
   public void setRowNo(int rowNo)
   {
      this.rowNo = rowNo;
   }



   /*******************************************************************************
    ** Fluent setter for rowNo
    *******************************************************************************/
   public BulkLoadFileRow withRowNo(int rowNo)
   {
      this.rowNo = rowNo;
      return (this);
   }

}
