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

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** Meta-Data to define a process in a QQQ instance.
 **
 *******************************************************************************/
public class QProcessMetaData
{
   private String name;
   private String tableName;
   private List<QFunctionMetaData> functionList;



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public QProcessMetaData withName(String name)
   {
      this.name = name;
      return (this);
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
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public QProcessMetaData withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for functionList
    **
    *******************************************************************************/
   public List<QFunctionMetaData> getFunctionList()
   {
      return functionList;
   }



   /*******************************************************************************
    ** Setter for functionList
    **
    *******************************************************************************/
   public QProcessMetaData withFunctionList(List<QFunctionMetaData> functionList)
   {
      this.functionList = functionList;
      return (this);
   }



   /*******************************************************************************
    ** Setter for functionList
    **
    *******************************************************************************/
   public QProcessMetaData addFunction(QFunctionMetaData function)
   {
      if(this.functionList == null)
      {
         this.functionList = new ArrayList<>();
      }
      this.functionList.add(function);
      return (this);
   }



   /*******************************************************************************
    ** Setter for functionList
    **
    *******************************************************************************/
   public void setFunctionList(List<QFunctionMetaData> functionList)
   {
      this.functionList = functionList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFunctionMetaData getFunction(String functionName)
   {
      for(QFunctionMetaData function : functionList)
      {
         if(function.getName().equals(functionName))
         {
            return (function);
         }
      }

      return (null);
   }
}
