/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.qbits;


/***************************************************************************
 ** Common (maybe)? qbit config pattern, where the qbit may be able to provide
 ** a particular table, or, the application may supply it itself.
 **
 ** If the qbit provides it, then we need to be told (by the application)
 ** what backendName to use for the table.
 **
 ** Else if the application supplies it, it needs to tell the qBit what the
 ** tableName is.
 ***************************************************************************/
public class ProvidedOrSuppliedTableConfig
{
   private boolean doProvideTable;
   private String  backendName;
   private String  tableName;



   /***************************************************************************
    **
    ***************************************************************************/
   public ProvidedOrSuppliedTableConfig(boolean doProvideTable, String backendName, String tableName)
   {
      this.doProvideTable = doProvideTable;
      this.backendName = backendName;
      this.tableName = tableName;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static ProvidedOrSuppliedTableConfig provideTableUsingBackendNamed(String backendName)
   {
      return (new ProvidedOrSuppliedTableConfig(true, backendName, null));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static ProvidedOrSuppliedTableConfig useSuppliedTaleNamed(String tableName)
   {
      return (new ProvidedOrSuppliedTableConfig(false, null, tableName));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String getEffectiveTableName(String tableNameIfProviding)
   {
      if (getDoProvideTable())
      {
         return tableNameIfProviding;
      }
      else
      {
         return getTableName();
      }
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Getter for doProvideTable
    **
    *******************************************************************************/
   public boolean getDoProvideTable()
   {
      return doProvideTable;
   }



   /*******************************************************************************
    ** Getter for backendName
    **
    *******************************************************************************/
   public String getBackendName()
   {
      return backendName;
   }
}
