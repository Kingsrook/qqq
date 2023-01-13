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

package com.kingsrook.qqq.backend.core.model.metadata.reporting;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Meta-data definition of a source of data for a report (e.g., a table and query
 ** filter or custom-code reference).
 *******************************************************************************/
public class QReportDataSource
{
   private String name;

   private String          sourceTable;
   private QQueryFilter    queryFilter;
   private List<QueryJoin> queryJoins = null;


   private QCodeReference queryInputCustomizer;
   private QCodeReference staticDataSupplier;



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
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QReportDataSource withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceTable
    **
    *******************************************************************************/
   public String getSourceTable()
   {
      return sourceTable;
   }



   /*******************************************************************************
    ** Setter for sourceTable
    **
    *******************************************************************************/
   public void setSourceTable(String sourceTable)
   {
      this.sourceTable = sourceTable;
   }



   /*******************************************************************************
    ** Fluent setter for sourceTable
    **
    *******************************************************************************/
   public QReportDataSource withSourceTable(String sourceTable)
   {
      this.sourceTable = sourceTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryFilter
    **
    *******************************************************************************/
   public QQueryFilter getQueryFilter()
   {
      return queryFilter;
   }



   /*******************************************************************************
    ** Setter for queryFilter
    **
    *******************************************************************************/
   public void setQueryFilter(QQueryFilter queryFilter)
   {
      this.queryFilter = queryFilter;
   }



   /*******************************************************************************
    ** Fluent setter for queryFilter
    **
    *******************************************************************************/
   public QReportDataSource withQueryFilter(QQueryFilter queryFilter)
   {
      this.queryFilter = queryFilter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for staticDataSupplier
    **
    *******************************************************************************/
   public QCodeReference getStaticDataSupplier()
   {
      return staticDataSupplier;
   }



   /*******************************************************************************
    ** Setter for staticDataSupplier
    **
    *******************************************************************************/
   public void setStaticDataSupplier(QCodeReference staticDataSupplier)
   {
      this.staticDataSupplier = staticDataSupplier;
   }



   /*******************************************************************************
    ** Fluent setter for staticDataSupplier
    **
    *******************************************************************************/
   public QReportDataSource withStaticDataSupplier(QCodeReference staticDataSupplier)
   {
      this.staticDataSupplier = staticDataSupplier;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryJoins
    **
    *******************************************************************************/
   public List<QueryJoin> getQueryJoins()
   {
      return queryJoins;
   }



   /*******************************************************************************
    ** Setter for queryJoins
    **
    *******************************************************************************/
   public void setQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    **
    *******************************************************************************/
   public QReportDataSource withQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    **
    *******************************************************************************/
   public QReportDataSource withQueryJoin(QueryJoin queryJoin)
   {
      if(this.queryJoins == null)
      {
         this.queryJoins = new ArrayList<>();
      }
      this.queryJoins.add(queryJoin);
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryInputCustomizer
    **
    *******************************************************************************/
   public QCodeReference getQueryInputCustomizer()
   {
      return queryInputCustomizer;
   }



   /*******************************************************************************
    ** Setter for queryInputCustomizer
    **
    *******************************************************************************/
   public void setQueryInputCustomizer(QCodeReference queryInputCustomizer)
   {
      this.queryInputCustomizer = queryInputCustomizer;
   }



   /*******************************************************************************
    ** Fluent setter for queryInputCustomizer
    **
    *******************************************************************************/
   public QReportDataSource withQueryInputCustomizer(QCodeReference queryInputCustomizer)
   {
      this.queryInputCustomizer = queryInputCustomizer;
      return (this);
   }

}
