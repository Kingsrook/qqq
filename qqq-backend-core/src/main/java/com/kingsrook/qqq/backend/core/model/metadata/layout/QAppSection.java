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

package com.kingsrook.qqq.backend.core.model.metadata.layout;


import java.util.List;


/*******************************************************************************
 ** A section of apps/tables/processes - a logical grouping.
 *******************************************************************************/
public class QAppSection
{
   private String name;
   private String label;
   private QIcon  icon;

   private List<String> tables;
   private List<String> processes;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAppSection()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAppSection(String name, String label, QIcon icon, List<String> tables, List<String> processes)
   {
      this.name = name;
      this.label = label;
      this.icon = icon;
      this.tables = tables;
      this.processes = processes;
   }



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
   public QAppSection withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public QAppSection withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tables
    **
    *******************************************************************************/
   public List<String> getTables()
   {
      return tables;
   }



   /*******************************************************************************
    ** Setter for tables
    **
    *******************************************************************************/
   public void setTables(List<String> tables)
   {
      this.tables = tables;
   }



   /*******************************************************************************
    ** Fluent setter for tables
    **
    *******************************************************************************/
   public QAppSection withTables(List<String> tables)
   {
      this.tables = tables;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processes
    **
    *******************************************************************************/
   public List<String> getProcesses()
   {
      return processes;
   }



   /*******************************************************************************
    ** Setter for processes
    **
    *******************************************************************************/
   public void setProcesses(List<String> processes)
   {
      this.processes = processes;
   }



   /*******************************************************************************
    ** Fluent setter for processes
    **
    *******************************************************************************/
   public QAppSection withProcesses(List<String> processes)
   {
      this.processes = processes;
      return (this);
   }



   /*******************************************************************************
    ** Getter for icon
    **
    *******************************************************************************/
   public QIcon getIcon()
   {
      return icon;
   }



   /*******************************************************************************
    ** Setter for icon
    **
    *******************************************************************************/
   public void setIcon(QIcon icon)
   {
      this.icon = icon;
   }



   /*******************************************************************************
    ** Fluent setter for icon
    **
    *******************************************************************************/
   public QAppSection withIcon(QIcon icon)
   {
      this.icon = icon;
      return (this);
   }

}
