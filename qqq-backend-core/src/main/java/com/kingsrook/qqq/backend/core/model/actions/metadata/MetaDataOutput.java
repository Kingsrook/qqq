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

package com.kingsrook.qqq.backend.core.model.actions.metadata;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.metadata.branding.QBrandingMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;


/*******************************************************************************
 * Output for a metaData action
 *
 *******************************************************************************/
public class MetaDataOutput extends AbstractActionOutput
{
   private Map<String, QFrontendTableMetaData>   tables;
   private Map<String, QFrontendProcessMetaData> processes;
   private Map<String, QFrontendReportMetaData>  reports;
   private Map<String, QFrontendAppMetaData>     apps;
   private Map<String, QFrontendWidgetMetaData>  widgets;
   private Map<String, String>                   environmentValues;

   private List<AppTreeNode>               appTree;
   private QBrandingMetaData               branding;
   private Map<String, List<QHelpContent>> helpContents;



   /*******************************************************************************
    ** Getter for tables
    **
    *******************************************************************************/
   public Map<String, QFrontendTableMetaData> getTables()
   {
      return tables;
   }



   /*******************************************************************************
    ** Setter for tables
    **
    *******************************************************************************/
   public void setTables(Map<String, QFrontendTableMetaData> tables)
   {
      this.tables = tables;
   }



   /*******************************************************************************
    ** Getter for processes
    **
    *******************************************************************************/
   public Map<String, QFrontendProcessMetaData> getProcesses()
   {
      return processes;
   }



   /*******************************************************************************
    ** Setter for processes
    **
    *******************************************************************************/
   public void setProcesses(Map<String, QFrontendProcessMetaData> processes)
   {
      this.processes = processes;
   }



   /*******************************************************************************
    ** Getter for reports
    **
    *******************************************************************************/
   public Map<String, QFrontendReportMetaData> getReports()
   {
      return reports;
   }



   /*******************************************************************************
    ** Setter for reports
    **
    *******************************************************************************/
   public void setReports(Map<String, QFrontendReportMetaData> reports)
   {
      this.reports = reports;
   }



   /*******************************************************************************
    ** Getter for appTree
    **
    *******************************************************************************/
   public List<AppTreeNode> getAppTree()
   {
      return appTree;
   }



   /*******************************************************************************
    ** Setter for appTree
    **
    *******************************************************************************/
   public void setAppTree(List<AppTreeNode> appTree)
   {
      this.appTree = appTree;
   }



   /*******************************************************************************
    ** Getter for apps
    **
    *******************************************************************************/
   public Map<String, QFrontendAppMetaData> getApps()
   {
      return apps;
   }



   /*******************************************************************************
    ** Setter for apps
    **
    *******************************************************************************/
   public void setApps(Map<String, QFrontendAppMetaData> apps)
   {
      this.apps = apps;
   }



   /*******************************************************************************
    ** Getter for widgets
    **
    *******************************************************************************/
   public Map<String, QFrontendWidgetMetaData> getWidgets()
   {
      return widgets;
   }



   /*******************************************************************************
    ** Setter for widgets
    **
    *******************************************************************************/
   public void setWidgets(Map<String, QFrontendWidgetMetaData> widgets)
   {
      this.widgets = widgets;
   }



   /*******************************************************************************
    ** Getter for branding
    **
    *******************************************************************************/
   public QBrandingMetaData getBranding()
   {
      return branding;
   }



   /*******************************************************************************
    ** Setter for branding
    **
    *******************************************************************************/
   public void setBranding(QBrandingMetaData branding)
   {
      this.branding = branding;
   }



   /*******************************************************************************
    ** Getter for environmentValues
    **
    *******************************************************************************/
   public Map<String, String> getEnvironmentValues()
   {
      return environmentValues;
   }



   /*******************************************************************************
    ** Setter for environmentValues
    **
    *******************************************************************************/
   public void setEnvironmentValues(Map<String, String> environmentValues)
   {
      this.environmentValues = environmentValues;
   }



   /*******************************************************************************
    ** Setter for helpContents
    **
    *******************************************************************************/
   public void setHelpContents(Map<String, List<QHelpContent>> helpContents)
   {
      this.helpContents = helpContents;
   }



   /*******************************************************************************
    ** Getter for helpContents
    **
    *******************************************************************************/
   public Map<String, List<QHelpContent>> getHelpContents()
   {
      return helpContents;
   }
}
