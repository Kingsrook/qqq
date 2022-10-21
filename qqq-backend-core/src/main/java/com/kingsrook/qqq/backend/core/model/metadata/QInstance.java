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
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidationKey;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.branding.QBrandingMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.authentication.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


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
   private Map<String, QTableMetaData>       tables               = new LinkedHashMap<>();
   private Map<String, QPossibleValueSource> possibleValueSources = new LinkedHashMap<>();
   private Map<String, QProcessMetaData>     processes            = new LinkedHashMap<>();
   private Map<String, QAppMetaData>         apps                 = new LinkedHashMap<>();
   private Map<String, QReportMetaData>      reports              = new LinkedHashMap<>();

   private Map<String, QWidgetMetaDataInterface> widgets = new LinkedHashMap<>();

   private Map<String, QQueueProviderMetaData> queueProviders = new LinkedHashMap<>();
   private Map<String, QQueueMetaData>         queues         = new LinkedHashMap<>();

   // todo - lock down the object (no more changes allowed) after it's been validated?

   @JsonIgnore
   private boolean hasBeenValidated = false;



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
    **
    *******************************************************************************/
   public void addBackend(QBackendMetaData backend)
   {
      addBackend(backend.getName(), backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addBackend(String name, QBackendMetaData backend)
   {
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
      addTable(table.getName(), table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTable(String name, QTableMetaData table)
   {
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
   public void addPossibleValueSource(QPossibleValueSource possibleValueSource)
   {
      this.addPossibleValueSource(possibleValueSource.getName(), possibleValueSource);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addPossibleValueSource(String name, QPossibleValueSource possibleValueSource)
   {
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
      this.addProcess(process.getName(), process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addProcess(String name, QProcessMetaData process)
   {
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
      this.addApp(app.getName(), app);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addApp(String name, QAppMetaData app)
   {
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add an app without a name."));
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
      this.addReport(report.getName(), report);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addReport(String name, QReportMetaData report)
   {
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add an report without a name."));
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
   public void addAutomationProvider(QAutomationProviderMetaData automationProvider)
   {
      this.addAutomationProvider(automationProvider.getName(), automationProvider);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addAutomationProvider(String name, QAutomationProviderMetaData automationProvider)
   {
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
    ** Setter for hasBeenValidated
    **
    *******************************************************************************/
   public void setHasBeenValidated(QInstanceValidationKey key)
   {
      this.hasBeenValidated = true;
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
      this.addWidget(widget.getName(), widget);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addWidget(String name, QWidgetMetaDataInterface widget)
   {
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
      this.addQueueProvider(queueProvider.getName(), queueProvider);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addQueueProvider(String name, QQueueProviderMetaData queueProvider)
   {
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add an queueProvider without a name."));
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
      this.addQueue(queue.getName(), queue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addQueue(String name, QQueueMetaData queue)
   {
      if(!StringUtils.hasContent(name))
      {
         throw (new IllegalArgumentException("Attempted to add an queue without a name."));
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

}
