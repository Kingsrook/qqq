/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
