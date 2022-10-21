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

package com.kingsrook.qqq.backend.core.actions.metadata;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Action to fetch top-level meta-data in a qqq instance.
 **
 *******************************************************************************/
public class MetaDataAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public MetaDataOutput execute(MetaDataInput metaDataInput) throws QException
   {
      ActionHelper.validateSession(metaDataInput);

      // todo pre-customization - just get to modify the request?
      MetaDataOutput metaDataOutput = new MetaDataOutput();

      Map<String, AppTreeNode> treeNodes = new LinkedHashMap<>();

      /////////////////////////////////////
      // map tables to frontend metadata //
      /////////////////////////////////////
      Map<String, QFrontendTableMetaData> tables = new LinkedHashMap<>();
      for(Map.Entry<String, QTableMetaData> entry : metaDataInput.getInstance().getTables().entrySet())
      {
         tables.put(entry.getKey(), new QFrontendTableMetaData(entry.getValue(), false));
         treeNodes.put(entry.getKey(), new AppTreeNode(entry.getValue()));
      }
      metaDataOutput.setTables(tables);

      ////////////////////////////////////////
      // map processes to frontend metadata //
      ////////////////////////////////////////
      Map<String, QFrontendProcessMetaData> processes = new LinkedHashMap<>();
      for(Map.Entry<String, QProcessMetaData> entry : metaDataInput.getInstance().getProcesses().entrySet())
      {
         processes.put(entry.getKey(), new QFrontendProcessMetaData(entry.getValue(), false));
         treeNodes.put(entry.getKey(), new AppTreeNode(entry.getValue()));
      }
      metaDataOutput.setProcesses(processes);

      //////////////////////////////////////
      // map reports to frontend metadata //
      //////////////////////////////////////
      Map<String, QFrontendReportMetaData> reports = new LinkedHashMap<>();
      for(Map.Entry<String, QReportMetaData> entry : metaDataInput.getInstance().getReports().entrySet())
      {
         reports.put(entry.getKey(), new QFrontendReportMetaData(entry.getValue(), false));
         treeNodes.put(entry.getKey(), new AppTreeNode(entry.getValue()));
      }
      metaDataOutput.setReports(reports);

      //////////////////////////////////////
      // map widgets to frontend metadata //
      //////////////////////////////////////
      Map<String, QFrontendWidgetMetaData> widgets = new LinkedHashMap<>();
      for(Map.Entry<String, QWidgetMetaDataInterface> entry : metaDataInput.getInstance().getWidgets().entrySet())
      {
         widgets.put(entry.getKey(), new QFrontendWidgetMetaData(entry.getValue()));
      }
      metaDataOutput.setWidgets(widgets);

      ///////////////////////////////////
      // map apps to frontend metadata //
      ///////////////////////////////////
      Map<String, QFrontendAppMetaData> apps = new LinkedHashMap<>();
      for(Map.Entry<String, QAppMetaData> entry : metaDataInput.getInstance().getApps().entrySet())
      {
         apps.put(entry.getKey(), new QFrontendAppMetaData(entry.getValue()));
         treeNodes.put(entry.getKey(), new AppTreeNode(entry.getValue()));

         if(CollectionUtils.nullSafeHasContents(entry.getValue().getChildren()))
         {
            for(QAppChildMetaData child : entry.getValue().getChildren())
            {
               apps.get(entry.getKey()).addChild(new AppTreeNode(child));
            }
         }
      }
      metaDataOutput.setApps(apps);

      ////////////////////////////////////////////////
      // organize app tree nodes by their hierarchy //
      ////////////////////////////////////////////////
      List<AppTreeNode> appTree = new ArrayList<>();
      for(QAppMetaData appMetaData : metaDataInput.getInstance().getApps().values())
      {
         if(appMetaData.getParentAppName() == null)
         {
            buildAppTree(treeNodes, appTree, appMetaData);
         }
      }
      metaDataOutput.setAppTree(appTree);

      ////////////////////////////////////
      // add branding metadata if found //
      ////////////////////////////////////
      if(metaDataInput.getInstance().getBranding() != null)
      {
         metaDataOutput.setBranding(metaDataInput.getInstance().getBranding());
      }

      // todo post-customization - can do whatever w/ the result if you want?

      return metaDataOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void buildAppTree(Map<String, AppTreeNode> treeNodes, List<AppTreeNode> nodeList, QAppChildMetaData childMetaData)
   {
      AppTreeNode treeNode = treeNodes.get(childMetaData.getName());
      if(treeNode == null)
      {
         return;
      }

      nodeList.add(treeNode);
      if(childMetaData instanceof QAppMetaData app)
      {
         if(app.getChildren() != null)
         {
            for(QAppChildMetaData child : app.getChildren())
            {
               buildAppTree(treeNodes, treeNode.getChildren(), child);
            }
         }
      }
   }
}
