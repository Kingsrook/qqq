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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionCheckResult;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;


/*******************************************************************************
 ** Action to fetch top-level meta-data in a qqq instance.
 **
 *******************************************************************************/
public class MetaDataAction
{
   private static final QLogger LOG = QLogger.getLogger(MetaDataAction.class);

   private static Memoization<QInstance, MetaDataFilterInterface> metaDataFilterMemoization = new Memoization<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public MetaDataOutput execute(MetaDataInput metaDataInput) throws QException
   {
      ActionHelper.validateSession(metaDataInput);

      MetaDataOutput           metaDataOutput = new MetaDataOutput();
      Map<String, AppTreeNode> treeNodes      = new LinkedHashMap<>();

      MetaDataFilterInterface filter = getMetaDataFilter();

      /////////////////////////////////////
      // map tables to frontend metadata //
      /////////////////////////////////////
      Map<String, QFrontendTableMetaData> tables = new LinkedHashMap<>();
      for(Map.Entry<String, QTableMetaData> entry : QContext.getQInstance().getTables().entrySet())
      {
         String         tableName = entry.getKey();
         QTableMetaData table     = entry.getValue();

         if(!filter.allowTable(metaDataInput, table))
         {
            continue;
         }

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, table);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         QBackendMetaData backendForTable = QContext.getQInstance().getBackendForTable(tableName);
         tables.put(tableName, new QFrontendTableMetaData(metaDataInput, backendForTable, table, false, false));
         treeNodes.put(tableName, new AppTreeNode(table));
      }
      metaDataOutput.setTables(tables);

      // addJoinsToTables(tables);
      // addJoinedTablesToTables(tables);

      ////////////////////////////////////////
      // map processes to frontend metadata //
      ////////////////////////////////////////
      Map<String, QFrontendProcessMetaData> processes = new LinkedHashMap<>();
      for(Map.Entry<String, QProcessMetaData> entry : QContext.getQInstance().getProcesses().entrySet())
      {
         String           processName = entry.getKey();
         QProcessMetaData process     = entry.getValue();

         if(!filter.allowProcess(metaDataInput, process))
         {
            continue;
         }

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
      for(Map.Entry<String, QReportMetaData> entry : QContext.getQInstance().getReports().entrySet())
      {
         String          reportName = entry.getKey();
         QReportMetaData report     = entry.getValue();

         if(!filter.allowReport(metaDataInput, report))
         {
            continue;
         }

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
      for(Map.Entry<String, QWidgetMetaDataInterface> entry : QContext.getQInstance().getWidgets().entrySet())
      {
         String                   widgetName = entry.getKey();
         QWidgetMetaDataInterface widget     = entry.getValue();

         if(!filter.allowWidget(metaDataInput, widget))
         {
            continue;
         }

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, widget);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         widgets.put(widgetName, new QFrontendWidgetMetaData(metaDataInput, widget));
      }
      metaDataOutput.setWidgets(widgets);

      ///////////////////////////////////////////////////////
      // sort apps - by sortOrder (integer), then by label //
      ///////////////////////////////////////////////////////
      List<QAppMetaData> sortedApps = QContext.getQInstance().getApps().values().stream()
         .sorted(Comparator.comparing((QAppMetaData a) -> a.getSortOrder())
            .thenComparing((QAppMetaData a) -> a.getLabel()))
         .toList();

      ///////////////////////////////////
      // map apps to frontend metadata //
      ///////////////////////////////////
      Map<String, QFrontendAppMetaData> apps = new LinkedHashMap<>();
      for(QAppMetaData app : sortedApps)
      {
         String appName = app.getName();

         PermissionCheckResult permissionResult = PermissionsHelper.getPermissionCheckResult(metaDataInput, app);
         if(permissionResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            continue;
         }

         if(!filter.allowApp(metaDataInput, app))
         {
            continue;
         }

         //////////////////////////////////////
         // build the frontend-app meta-data //
         //////////////////////////////////////
         QFrontendAppMetaData frontendAppMetaData = new QFrontendAppMetaData(app, metaDataOutput);

         /////////////////////////////////////////
         // add children (if they're permitted) //
         /////////////////////////////////////////
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

               //////////////////////////////////////////////////////////////////////////////////////////////////////
               // if the child was filtered away, so it isn't in its corresponding map, then don't include it here //
               //////////////////////////////////////////////////////////////////////////////////////////////////////
               if(child instanceof QTableMetaData table && !tables.containsKey(table.getName()))
               {
                  continue;
               }
               if(child instanceof QProcessMetaData process && !processes.containsKey(process.getName()))
               {
                  continue;
               }
               if(child instanceof QReportMetaData report && !reports.containsKey(report.getName()))
               {
                  continue;
               }
               if(child instanceof QAppMetaData childApp && !apps.containsKey(childApp.getName()))
               {
                  // continue;
               }

               frontendAppMetaData.addChild(new AppTreeNode(child));
            }
         }

         //////////////////////////////////////////////////////////////////////////////////////////////////////
         // if the app ended up having no children, then discard it                                          //
         // todo - i think this was wrong, because it didn't take into account ... something nested maybe... //
         //////////////////////////////////////////////////////////////////////////////////////////////////////
         if(CollectionUtils.nullSafeIsEmpty(frontendAppMetaData.getChildren()) && CollectionUtils.nullSafeIsEmpty(frontendAppMetaData.getWidgets()))
         {
            // LOG.debug("Discarding empty app", logPair("name", frontendAppMetaData.getName()));
            // continue;
         }

         apps.put(appName, frontendAppMetaData);
         treeNodes.put(appName, new AppTreeNode(app));
      }
      metaDataOutput.setApps(apps);

      ////////////////////////////////////////////////
      // organize app tree nodes by their hierarchy //
      ////////////////////////////////////////////////
      List<AppTreeNode> appTree = new ArrayList<>();
      for(QAppMetaData appMetaData : sortedApps)
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
      if(QContext.getQInstance().getBranding() != null)
      {
         metaDataOutput.setBranding(QContext.getQInstance().getBranding());
      }

      metaDataOutput.setEnvironmentValues(QContext.getQInstance().getEnvironmentValues());

      metaDataOutput.setHelpContents(QContext.getQInstance().getHelpContent());

      // todo post-customization - can do whatever w/ the result if you want?

      return metaDataOutput;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private MetaDataFilterInterface getMetaDataFilter()
   {
      return metaDataFilterMemoization.getResult(QContext.getQInstance(), i ->
      {
         MetaDataFilterInterface filter                  = null;
         QCodeReference          metaDataFilterReference = QContext.getQInstance().getMetaDataFilter();
         if(metaDataFilterReference != null)
         {
            filter = QCodeLoader.getAdHoc(MetaDataFilterInterface.class, metaDataFilterReference);
            LOG.debug("Using new meta-data filter of type: " + filter.getClass().getSimpleName());
         }

         if(filter == null)
         {
            filter = new AllowAllMetaDataFilter();
            LOG.debug("Using new default (allow-all) meta-data filter");
         }

         return (filter);
      }).orElseThrow(() -> new QRuntimeException("Error getting metaDataFilter"));
   }



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
