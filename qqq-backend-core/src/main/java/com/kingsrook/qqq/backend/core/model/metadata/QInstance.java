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

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.actions.metadata.JoinGraph;
import com.kingsrook.qqq.backend.core.actions.metadata.MetaDataAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidationKey;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.branding.QBrandingMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNodeType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QSchedulerMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;


/*******************************************************************************
 ** Container for all meta-data in a running instance of a QQQ application.
 **
 *******************************************************************************/
public class QInstance
{
   ///////////////////////////////////////////////////////////////////////////////
   // Do not let the backend data be serialized - e.g., sent to a frontend user //
   ///////////////////////////////////////////////////////////////////////////////
   @JsonIgnore
   private Map<String, QBackendMetaData> backends = new HashMap<>();

   private QAuthenticationMetaData                  authentication      = null;
   private QBrandingMetaData                        branding            = null;
   private Map<String, QAutomationProviderMetaData> automationProviders = new HashMap<>();

   ////////////////////////////////////////////////////////////////////////////////////////////
   // Important to use LinkedHashmap here, to preserve the order in which entries are added. //
   ////////////////////////////////////////////////////////////////////////////////////////////
   private Map<String, QTableMetaData>           tables               = new LinkedHashMap<>();
   private Map<String, QJoinMetaData>            joins                = new LinkedHashMap<>();
   private Map<String, QPossibleValueSource>     possibleValueSources = new LinkedHashMap<>();
   private Map<String, QProcessMetaData>         processes            = new LinkedHashMap<>();
   private Map<String, QAppMetaData>             apps                 = new LinkedHashMap<>();
   private Map<String, QReportMetaData>          reports              = new LinkedHashMap<>();
   private Map<String, QSecurityKeyType>         securityKeyTypes     = new LinkedHashMap<>();
   private Map<String, QWidgetMetaDataInterface> widgets              = new LinkedHashMap<>();
   private Map<String, QQueueProviderMetaData>   queueProviders       = new LinkedHashMap<>();
   private Map<String, QQueueMetaData>           queues               = new LinkedHashMap<>();
   private Map<String, QSchedulerMetaData>       schedulers           = new LinkedHashMap<>();

   private Map<String, QSupplementalInstanceMetaData> supplementalMetaData = new LinkedHashMap<>();

   private String              deploymentMode;
   private Map<String, String> environmentValues = new LinkedHashMap<>();
   private String              defaultTimeZoneId = "UTC";

   private QPermissionRules defaultPermissionRules = QPermissionRules.defaultInstance();
   private QAuditRules      defaultAuditRules      = QAuditRules.defaultInstanceLevelNone();

   // todo - lock down the object (no more changes allowed) after it's been validated?

   @JsonIgnore
   private boolean hasBeenValidated = false;

   private Map<String, String> memoizedTablePaths   = new HashMap<>();
   private Map<String, String> memoizedProcessPaths = new HashMap<>();

   private JoinGraph joinGraph;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QInstance()
   {
      loadEnvironmentValues();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void loadEnvironmentValues()
   {
      String prefix = "QQQ_ENV_";
      for(String name : System.getenv().keySet())
      {
         String value = System.getenv(name);
         addEnvironmentValueIfNameMatchesPrefix(prefix, name, value);
      }

      Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
      for(DotenvEntry entry : dotenv.entries())
      {
         addEnvironmentValueIfNameMatchesPrefix(prefix, entry.getKey(), entry.getValue());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addEnvironmentValueIfNameMatchesPrefix(String prefix, String name, String value)
   {
      if(name.startsWith(prefix))
      {
         name = name.replaceFirst(prefix, "");
         environmentValues.put(name, value);
      }
   }



   /*******************************************************************************
    ** Get the backend for a given table name
    *******************************************************************************/
   public QBackendMetaData getBackendForTable(String tableName)
   {
      QTableMetaData table = tables.get(tableName);
      if(table == null)
      {
         throw (new IllegalArgumentException("No table with name [" + tableName + "] found in this instance."));
      }

      //////////////////////////////////////////////////////////////////////////////////////////////
      // validation should already let us know that this is valid, so no need to check/throw here //
      //////////////////////////////////////////////////////////////////////////////////////////////
      return (backends.get(table.getBackendName()));
   }



   /*******************************************************************************
    ** Get the list of processes associated with a given table name
    *******************************************************************************/
   public List<QProcessMetaData> getProcessesForTable(String tableName)
   {
      List<QProcessMetaData> rs = new ArrayList<>();
      for(QProcessMetaData process : processes.values())
      {
         if(tableName.equals(process.getTableName()))
         {
            rs.add(process);
         }
      }
      return (rs);
   }



   /*******************************************************************************
    ** Get the full path to a table
    *******************************************************************************/
   public String getTablePath(String tableName) throws QException
   {
      if(!memoizedTablePaths.containsKey(tableName))
      {
         MetaDataInput  input  = new MetaDataInput();
         MetaDataOutput output = new MetaDataAction().execute(input);
         memoizedTablePaths.put(tableName, searchAppTree(output.getAppTree(), tableName, AppTreeNodeType.TABLE, ""));
      }
      return (memoizedTablePaths.get(tableName));
   }



   /*******************************************************************************
    ** Get the full path to a process
    *******************************************************************************/
   public String getProcessPath(AbstractActionInput actionInput, String processName) throws QException
   {
      if(!memoizedProcessPaths.containsKey(processName))
      {
         MetaDataInput  input  = new MetaDataInput();
         MetaDataOutput output = new MetaDataAction().execute(input);
         return searchAppTree(output.getAppTree(), processName, AppTreeNodeType.PROCESS, "");
      }
      return (memoizedProcessPaths.get(processName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String searchAppTree(List<AppTreeNode> appTree, String tableName, AppTreeNodeType treeNodeType, String path)
   {
      if(appTree == null)
      {
         return (null);
      }

      for(AppTreeNode appTreeNode : appTree)
      {
         if(appTreeNode.getType().equals(treeNodeType) && appTreeNode.getName().equals(tableName))
         {
            return (path + "/" + tableName);
         }
         else if(appTreeNode.getType().equals(AppTreeNodeType.APP))
         {
            String subResult = searchAppTree(appTreeNode.getChildren(), tableName, treeNodeType, path + "/" + appTreeNode.getName());
            if(subResult != null)
            {
               return (subResult);
            }
         }
      }

      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addBackend(QBackendMetaData backend)
   {
      String name = backend.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a backend without a name."));
      }
      if(this.backends.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second backend with name: " + name));
      }
      this.backends.put(name, backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendMetaData getBackend(String name)
   {
      return (this.backends.get(name));
   }



   /*******************************************************************************
    ** Getter for backends
    **
    *******************************************************************************/
   public Map<String, QBackendMetaData> getBackends()
   {
      return backends;
   }



   /*******************************************************************************
    ** Setter for backends
    **
    *******************************************************************************/
   public void setBackends(Map<String, QBackendMetaData> backends)
   {
      this.backends = backends;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTable(QTableMetaData table)
   {
      String name = table.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a table without a name."));
      }
      if(this.tables.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second table with name: " + name));
      }
      this.tables.put(name, table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData getTable(String name)
   {
      if(this.tables == null)
      {
         return (null);
      }

      return (this.tables.get(name));
   }



   /*******************************************************************************
    ** Getter for tables
    **
    *******************************************************************************/
   public Map<String, QTableMetaData> getTables()
   {
      return tables;
   }



   /*******************************************************************************
    ** Setter for tables
    **
    *******************************************************************************/
   public void setTables(Map<String, QTableMetaData> tables)
   {
      this.tables = tables;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addJoin(QJoinMetaData join)
   {
      String name = join.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a join without a name."));
      }
      if(this.joins.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second join with name: " + name));
      }
      this.joins.put(name, join);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJoinMetaData getJoin(String name)
   {
      if(this.joins == null)
      {
         return (null);
      }

      return (this.joins.get(name));
   }



   /*******************************************************************************
    ** Getter for joins
    **
    *******************************************************************************/
   public Map<String, QJoinMetaData> getJoins()
   {
      return joins;
   }



   /*******************************************************************************
    ** Setter for joins
    **
    *******************************************************************************/
   public void setJoins(Map<String, QJoinMetaData> joins)
   {
      this.joins = joins;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addPossibleValueSource(QPossibleValueSource possibleValueSource)
   {
      String name = possibleValueSource.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a possibleValueSource without a name."));
      }
      if(this.possibleValueSources.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second possibleValueSource with name: " + name));
      }
      this.possibleValueSources.put(name, possibleValueSource);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource getPossibleValueSource(String name)
   {
      return (this.possibleValueSources.get(name));
   }



   /*******************************************************************************
    ** Getter for possibleValueSources
    **
    *******************************************************************************/
   public Map<String, QPossibleValueSource> getPossibleValueSources()
   {
      return possibleValueSources;
   }



   /*******************************************************************************
    ** Setter for possibleValueSources
    **
    *******************************************************************************/
   public void setPossibleValueSources(Map<String, QPossibleValueSource> possibleValueSources)
   {
      this.possibleValueSources = possibleValueSources;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QStepMetaData getProcessStep(String processName, String functionName)
   {
      QProcessMetaData qProcessMetaData = this.processes.get(processName);
      if(qProcessMetaData == null)
      {
         return (null);
      }

      return (qProcessMetaData.getStep(functionName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addProcess(QProcessMetaData process)
   {
      String name = process.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a process without a name."));
      }
      if(this.processes.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second process with name: " + name));
      }
      this.processes.put(name, process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QProcessMetaData getProcess(String name)
   {
      return (this.processes.get(name));
   }



   /*******************************************************************************
    ** Getter for processes
    **
    *******************************************************************************/
   public Map<String, QProcessMetaData> getProcesses()
   {
      return processes;
   }



   /*******************************************************************************
    ** Setter for processes
    **
    *******************************************************************************/
   public void setProcesses(Map<String, QProcessMetaData> processes)
   {
      this.processes = processes;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addApp(QAppMetaData app)
   {
      String name = app.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a app without a name."));
      }
      if(this.apps.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second app with name: " + name));
      }
      this.apps.put(name, app);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAppMetaData getApp(String name)
   {
      return (this.apps.get(name));
   }



   /*******************************************************************************
    ** Getter for apps
    **
    *******************************************************************************/
   public Map<String, QAppMetaData> getApps()
   {
      return apps;
   }



   /*******************************************************************************
    ** Setter for apps
    **
    *******************************************************************************/
   public void setApps(Map<String, QAppMetaData> apps)
   {
      this.apps = apps;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addReport(QReportMetaData report)
   {
      String name = report.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a report without a name."));
      }
      if(this.reports.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second report with name: " + name));
      }
      this.reports.put(name, report);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QReportMetaData getReport(String name)
   {
      return (this.reports.get(name));
   }



   /*******************************************************************************
    ** Getter for reports
    **
    *******************************************************************************/
   public Map<String, QReportMetaData> getReports()
   {
      return reports;
   }



   /*******************************************************************************
    ** Setter for reports
    **
    *******************************************************************************/
   public void setReports(Map<String, QReportMetaData> reports)
   {
      this.reports = reports;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addSecurityKeyType(QSecurityKeyType securityKeyType)
   {
      String name = securityKeyType.getName();
      if(this.securityKeyTypes.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second securityKeyType with name: " + name));
      }
      this.securityKeyTypes.put(name, securityKeyType);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSecurityKeyType getSecurityKeyType(String name)
   {
      return (this.securityKeyTypes.get(name));
   }



   /*******************************************************************************
    ** Getter for securityKeyTypes
    **
    *******************************************************************************/
   public Map<String, QSecurityKeyType> getSecurityKeyTypes()
   {
      return securityKeyTypes;
   }



   /*******************************************************************************
    ** Setter for securityKeyTypes
    **
    *******************************************************************************/
   public void setSecurityKeyTypes(Map<String, QSecurityKeyType> securityKeyTypes)
   {
      this.securityKeyTypes = securityKeyTypes;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addAutomationProvider(QAutomationProviderMetaData automationProvider)
   {
      String name = automationProvider.getName();
      if(this.automationProviders.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second automationProvider with name: " + name));
      }
      this.automationProviders.put(name, automationProvider);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAutomationProviderMetaData getAutomationProvider(String name)
   {
      return (this.automationProviders.get(name));
   }



   /*******************************************************************************
    ** Getter for automationProviders
    **
    *******************************************************************************/
   public Map<String, QAutomationProviderMetaData> getAutomationProviders()
   {
      return automationProviders;
   }



   /*******************************************************************************
    ** Setter for automationProviders
    **
    *******************************************************************************/
   public void setAutomationProviders(Map<String, QAutomationProviderMetaData> automationProviders)
   {
      this.automationProviders = automationProviders;
   }



   /*******************************************************************************
    ** Getter for hasBeenValidated
    **
    *******************************************************************************/
   public boolean getHasBeenValidated()
   {
      return hasBeenValidated;
   }



   /*******************************************************************************
    ** If pass a QInstanceValidationKey (which can only be instantiated by the validator),
    ** then the hasBeenValidated field will be set to true.
    **
    ** Else, if passed a null, hasBeenValidated will be reset to false - e.g., to
    ** re-trigger validation (can be useful in tests).
    *******************************************************************************/
   public void setHasBeenValidated(QInstanceValidationKey key)
   {
      if(key == null)
      {
         this.hasBeenValidated = false;
      }
      else
      {
         this.hasBeenValidated = true;
      }
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
    ** Getter for authentication
    **
    *******************************************************************************/
   public QAuthenticationMetaData getAuthentication()
   {
      return authentication;
   }



   /*******************************************************************************
    ** Setter for authentication
    **
    *******************************************************************************/
   public void setAuthentication(QAuthenticationMetaData authentication)
   {
      this.authentication = authentication;
   }



   /*******************************************************************************
    ** Getter for widgets
    **
    *******************************************************************************/
   public Map<String, QWidgetMetaDataInterface> getWidgets()
   {
      return widgets;
   }



   /*******************************************************************************
    ** Setter for widgets
    **
    *******************************************************************************/
   public void setWidgets(Map<String, QWidgetMetaDataInterface> widgets)
   {
      this.widgets = widgets;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addWidget(QWidgetMetaDataInterface widget)
   {
      String name = widget.getName();
      if(this.widgets.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second widget with name: " + name));
      }
      this.widgets.put(name, widget);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QWidgetMetaDataInterface getWidget(String name)
   {
      return (this.widgets.get(name));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addQueueProvider(QQueueProviderMetaData queueProvider)
   {
      String name = queueProvider.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a queueProvider without a name."));
      }
      if(this.queueProviders.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second queueProvider with name: " + name));
      }
      this.queueProviders.put(name, queueProvider);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueueProviderMetaData getQueueProvider(String name)
   {
      return (this.queueProviders.get(name));
   }



   /*******************************************************************************
    ** Getter for queueProviders
    **
    *******************************************************************************/
   public Map<String, QQueueProviderMetaData> getQueueProviders()
   {
      return queueProviders;
   }



   /*******************************************************************************
    ** Setter for queueProviders
    **
    *******************************************************************************/
   public void setQueueProviders(Map<String, QQueueProviderMetaData> queueProviders)
   {
      this.queueProviders = queueProviders;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addQueue(QQueueMetaData queue)
   {
      String name = queue.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a queue without a name."));
      }
      if(this.queues.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second queue with name: " + name));
      }
      this.queues.put(name, queue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueueMetaData getQueue(String name)
   {
      return (this.queues.get(name));
   }



   /*******************************************************************************
    ** Getter for queues
    **
    *******************************************************************************/
   public Map<String, QQueueMetaData> getQueues()
   {
      return queues;
   }



   /*******************************************************************************
    ** Setter for queues
    **
    *******************************************************************************/
   public void setQueues(Map<String, QQueueMetaData> queues)
   {
      this.queues = queues;
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
    ** Getter for defaultPermissionRules
    *******************************************************************************/
   public QPermissionRules getDefaultPermissionRules()
   {
      return (this.defaultPermissionRules);
   }



   /*******************************************************************************
    ** Setter for defaultPermissionRules
    *******************************************************************************/
   public void setDefaultPermissionRules(QPermissionRules defaultPermissionRules)
   {
      this.defaultPermissionRules = defaultPermissionRules;
   }



   /*******************************************************************************
    ** Fluent setter for defaultPermissionRules
    *******************************************************************************/
   public QInstance withDefaultPermissionRules(QPermissionRules defaultPermissionRules)
   {
      this.defaultPermissionRules = defaultPermissionRules;
      return (this);
   }



   /*******************************************************************************
    ** Setter for defaultTimeZoneId
    *******************************************************************************/
   public void setDefaultTimeZoneId(String defaultTimeZoneId)
   {
      this.defaultTimeZoneId = defaultTimeZoneId;
   }



   /*******************************************************************************
    ** Fluent setter for defaultTimeZoneId
    *******************************************************************************/
   public QInstance withDefaultTimeZoneId(String defaultTimeZoneId)
   {
      this.defaultTimeZoneId = defaultTimeZoneId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultTimeZoneId
    *******************************************************************************/
   public String getDefaultTimeZoneId()
   {
      return (this.defaultTimeZoneId);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getAllowedSecurityKeyNames()
   {
      Set<String> rs = new LinkedHashSet<>();
      for(QSecurityKeyType securityKeyType : CollectionUtils.nonNullMap(getSecurityKeyTypes()).values())
      {
         rs.add(securityKeyType.getName());

         if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()))
         {
            rs.add(securityKeyType.getAllAccessKeyName());
         }

         if(StringUtils.hasContent(securityKeyType.getNullValueBehaviorKeyName()))
         {
            rs.add(securityKeyType.getNullValueBehaviorKeyName());
         }
      }
      return (rs);
   }



   /*******************************************************************************
    ** Getter for defaultAuditRules
    *******************************************************************************/
   public QAuditRules getDefaultAuditRules()
   {
      return (this.defaultAuditRules);
   }



   /*******************************************************************************
    ** Setter for defaultAuditRules
    *******************************************************************************/
   public void setDefaultAuditRules(QAuditRules defaultAuditRules)
   {
      this.defaultAuditRules = defaultAuditRules;
   }



   /*******************************************************************************
    ** Fluent setter for defaultAuditRules
    *******************************************************************************/
   public QInstance withDefaultAuditRules(QAuditRules defaultAuditRules)
   {
      this.defaultAuditRules = defaultAuditRules;
      return (this);
   }



   /*******************************************************************************
    ** Getter for supplementalMetaData
    *******************************************************************************/
   public Map<String, QSupplementalInstanceMetaData> getSupplementalMetaData()
   {
      return (this.supplementalMetaData);
   }



   /*******************************************************************************
    ** Getter for supplementalMetaData
    *******************************************************************************/
   public QSupplementalInstanceMetaData getSupplementalMetaData(String type)
   {
      if(this.supplementalMetaData == null)
      {
         return (null);
      }
      return this.supplementalMetaData.get(type);
   }



   /*******************************************************************************
    ** Setter for supplementalMetaData
    *******************************************************************************/
   public void setSupplementalMetaData(Map<String, QSupplementalInstanceMetaData> supplementalMetaData)
   {
      this.supplementalMetaData = supplementalMetaData;
   }



   /*******************************************************************************
    ** Fluent setter for supplementalMetaData
    *******************************************************************************/
   public QInstance withSupplementalMetaData(Map<String, QSupplementalInstanceMetaData> supplementalMetaData)
   {
      this.supplementalMetaData = supplementalMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for supplementalMetaData
    *******************************************************************************/
   public QInstance withSupplementalMetaData(QSupplementalInstanceMetaData supplementalMetaData)
   {
      if(this.supplementalMetaData == null)
      {
         this.supplementalMetaData = new HashMap<>();
      }
      this.supplementalMetaData.put(supplementalMetaData.getType(), supplementalMetaData);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public JoinGraph getJoinGraph()
   {
      return (this.joinGraph);
   }



   /*******************************************************************************
    ** Only the validation (and enrichment) code should set the instance's joinGraph
    ** so, we take a package-only-constructable validation key as a param along with
    ** the joinGraph - and we throw IllegalArgumentException if a non-null key is given.
    *******************************************************************************/
   public void setJoinGraph(QInstanceValidationKey key, JoinGraph joinGraph) throws IllegalArgumentException
   {
      if(key == null)
      {
         throw (new IllegalArgumentException("A ValidationKey must be provided"));
      }
      this.joinGraph = joinGraph;
   }



   /*******************************************************************************
    ** Getter for deploymentMode
    *******************************************************************************/
   public String getDeploymentMode()
   {
      return (this.deploymentMode);
   }



   /*******************************************************************************
    ** Setter for deploymentMode
    *******************************************************************************/
   public void setDeploymentMode(String deploymentMode)
   {
      this.deploymentMode = deploymentMode;
   }



   /*******************************************************************************
    ** Fluent setter for deploymentMode
    *******************************************************************************/
   public QInstance withDeploymentMode(String deploymentMode)
   {
      this.deploymentMode = deploymentMode;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void add(TopLevelMetaDataInterface metaData)
   {
      metaData.addSelfToInstance(this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addScheduler(QSchedulerMetaData scheduler)
   {
      String name = scheduler.getName();
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add a scheduler without a name."));
      }
      if(this.schedulers.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second scheduler with name: " + name));
      }
      this.schedulers.put(name, scheduler);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSchedulerMetaData getScheduler(String name)
   {
      return (this.schedulers.get(name));
   }



   /*******************************************************************************
    ** Getter for schedulers
    **
    *******************************************************************************/
   public Map<String, QSchedulerMetaData> getSchedulers()
   {
      return schedulers;
   }



   /*******************************************************************************
    ** Setter for schedulers
    **
    *******************************************************************************/
   public void setSchedulers(Map<String, QSchedulerMetaData> schedulers)
   {
      this.schedulers = schedulers;
   }


}
