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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionCheckResult;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
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
         String         tableName = entry.getKey();
         QTableMetaData table     = entry.getValue();

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, table);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         QBackendMetaData backendForTable = metaDataInput.getInstance().getBackendForTable(tableName);
         tables.put(tableName, new QFrontendTableMetaData(metaDataInput, backendForTable, table, false));
         treeNodes.put(tableName, new AppTreeNode(table));
      }
      metaDataOutput.setTables(tables);

      // addJoinsToTables(tables);
      // addJoinedTablesToTables(tables);

      ////////////////////////////////////////
      // map processes to frontend metadata //
      ////////////////////////////////////////
      Map<String, QFrontendProcessMetaData> processes = new LinkedHashMap<>();
      for(Map.Entry<String, QProcessMetaData> entry : metaDataInput.getInstance().getProcesses().entrySet())
      {
         String           processName = entry.getKey();
         QProcessMetaData process     = entry.getValue();

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, process);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         processes.put(processName, new QFrontendProcessMetaData(metaDataInput, process, false));
         treeNodes.put(processName, new AppTreeNode(process));
      }
      metaDataOutput.setProcesses(processes);

      //////////////////////////////////////
      // map reports to frontend metadata //
      //////////////////////////////////////
      Map<String, QFrontendReportMetaData> reports = new LinkedHashMap<>();
      for(Map.Entry<String, QReportMetaData> entry : metaDataInput.getInstance().getReports().entrySet())
      {
         String          reportName = entry.getKey();
         QReportMetaData report     = entry.getValue();

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, report);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         reports.put(reportName, new QFrontendReportMetaData(metaDataInput, report, false));
         treeNodes.put(reportName, new AppTreeNode(report));
      }
      metaDataOutput.setReports(reports);

      //////////////////////////////////////
      // map widgets to frontend metadata //
      //////////////////////////////////////
      Map<String, QFrontendWidgetMetaData> widgets = new LinkedHashMap<>();
      for(Map.Entry<String, QWidgetMetaDataInterface> entry : metaDataInput.getInstance().getWidgets().entrySet())
      {
         String                   widgetName = entry.getKey();
         QWidgetMetaDataInterface widget     = entry.getValue();

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, widget);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         widgets.put(widgetName, new QFrontendWidgetMetaData(metaDataInput, widget));
      }
      metaDataOutput.setWidgets(widgets);

      ///////////////////////////////////
      // map apps to frontend metadata //
      ///////////////////////////////////
      Map<String, QFrontendAppMetaData> apps = new LinkedHashMap<>();
      for(Map.Entry<String, QAppMetaData> entry : metaDataInput.getInstance().getApps().entrySet())
      {
         String       appName = entry.getKey();
         QAppMetaData app     = entry.getValue();

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, app);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         apps.put(appName, new QFrontendAppMetaData(app, metaDataOutput));
         treeNodes.put(appName, new AppTreeNode(app));

         if(CollectionUtils.nullSafeHasContents(app.getChildren()))
         {
            for(QAppChildMetaData child : app.getChildren())
            {
               if(child instanceof MetaDataWithPermissionRules metaDataWithPermissionRules)
               {
                  PermissionCheckResult childPermissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, metaDataWithPermissionRules);
                  if(childPermissionResult.equals(PermissionCheckResult.DENY_HIDE))
                  {
                     continue;
                  }
               }

               apps.get(appName).addChild(new AppTreeNode(child));
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
            buildAppTree(metaDataInput, treeNodes, appTree, appMetaData);
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

      metaDataOutput.setEnvironmentValues(metaDataInput.getInstance().getEnvironmentValues());

      // todo post-customization - can do whatever w/ the result if you want?

      return metaDataOutput;
   }

   ////////////////////////////////////// start v1 //////////////////////////////////////



   private record JoinedTable(String joinedTableName, List<String> joinPath)
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addJoinedTablesToTables(Map<String, QFrontendTableMetaData> tables)
   {
      for(QFrontendTableMetaData table : tables.values())
      {
         List<JoinedTable> joinedTables = new ArrayList<>();
         addJoinedTablesToTable(tables, table, joinedTables, new ArrayList<>());

         if(joinedTables.size() > 0)
         {
            System.out.println("For [" + table.getName() + "] we have:\n   " + joinedTables.stream().map(String::valueOf).collect(Collectors.joining("\n   ")) + "\n");
         }
         else
         {
            System.out.println("No joins for [" + table.getName() + "]\n");
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addJoinedTablesToTable(Map<String, QFrontendTableMetaData> tables, QFrontendTableMetaData table, List<JoinedTable> joinedTables, List<String> joinPath)
   {
      QInstance qInstance = QContext.getQInstance();
      for(QJoinMetaData join : qInstance.getJoins().values())
      {
         if(join.getLeftTable().equals(table.getName()))
         {
            String      joinName    = join.getName();
            JoinedTable joinedTable = new JoinedTable(join.getRightTable(), joinPath);
            System.out.println("Adding to [" + table.getName() + "]: " + joinedTable);
            joinedTables.add(joinedTable);

            ArrayList<String> subJoinPath = new ArrayList<>(joinPath);
            subJoinPath.add(joinName);
            addJoinedTablesToTable(tables, tables.get(join.getRightTable()), joinedTables, subJoinPath);
         }
         if(join.getRightTable().equals(table.getName()))
         {
            String      joinName    = join.getName() + ".flipped";
            JoinedTable joinedTable = new JoinedTable(join.getLeftTable(), joinPath);
            System.out.println("Adding to [" + table.getName() + "]: " + joinedTable);
            joinedTables.add(joinedTable);

            ArrayList<String> subJoinPath = new ArrayList<>(joinPath);
            subJoinPath.add(joinName);
            addJoinedTablesToTable(tables, tables.get(join.getLeftTable()), joinedTables, subJoinPath);
         }
      }
   }

   ////////////////////////////////////// end v1 //////////////////////////////////////

   ////////////////////////////////////// start v0 //////////////////////////////////////



   private record Something(String joinName, List<String> joinPath)
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addJoinsToTables(Map<String, QFrontendTableMetaData> tables)
   {
      for(QFrontendTableMetaData table : tables.values())
      {
         List<Something> something = new ArrayList<>();
         addJoinsToTable(tables, table, something, new ArrayList<>(), new HashSet<>());
         if(something.size() > 0)
         {
            System.out.println("For [" + table.getName() + "] we have:\n   " + something.stream().map(String::valueOf).collect(Collectors.joining("\n   ")) + "\n");
         }
         else
         {
            System.out.println("No joins for [" + table.getName() + "]\n");
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addJoinsToTable(Map<String, QFrontendTableMetaData> tables, QFrontendTableMetaData table, List<Something> something, List<String> joinPath, Set<String> usedJoins)
   {
      QInstance qInstance = QContext.getQInstance();
      for(QJoinMetaData join : qInstance.getJoins().values())
      {
         if(join.getLeftTable().equals(table.getName()))
         {
            String joinName = join.getName();
            if(!usedJoins.contains(joinName))
            {
               usedJoins.add(joinName);
               something.add(new Something(joinName, joinPath));

               ArrayList<String> subJoinPath = new ArrayList<>(joinPath);
               subJoinPath.add(joinName);

               QFrontendTableMetaData rightTable = tables.get(join.getRightTable());
               addJoinsToTable(tables, rightTable, something, subJoinPath, usedJoins);
            }
         }
         else if(join.getRightTable().equals(table.getName()))
         {
            String joinName = join.getName() + ".flipped";
            if(!usedJoins.contains(joinName))
            {
               usedJoins.add(joinName);
               something.add(new Something(joinName, joinPath));

               ArrayList<String> subJoinPath = new ArrayList<>(joinPath);
               subJoinPath.add(joinName);

               QFrontendTableMetaData leftTable = tables.get(join.getLeftTable());
               addJoinsToTable(tables, leftTable, something, subJoinPath, usedJoins);
            }
         }
      }
   }

   ////////////////////////////////////// end v0 //////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   private void buildAppTree(MetaDataInput metaDataInput, Map<String, AppTreeNode> treeNodes, List<AppTreeNode> nodeList, QAppChildMetaData childMetaData)
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
               if(child instanceof MetaDataWithPermissionRules metaDataWithPermissionRules)
               {
                  PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, metaDataWithPermissionRules);
                  if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
                  {
                     continue;
                  }
               }

               buildAppTree(metaDataInput, treeNodes, treeNode.getChildren(), child);
            }
         }
      }
   }
}
