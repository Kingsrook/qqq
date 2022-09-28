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


import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Meta-data definition of a report generated by QQQ
 *******************************************************************************/
public class QReportMetaData implements QAppChildMetaData
{
   private String name;
   private String label;

   private String               processName;
   private List<QFieldMetaData> inputFields;

   private List<QReportDataSource> dataSources;
   private List<QReportView>       views;

   private String parentAppName;
   private QIcon  icon;



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
   public QReportMetaData withName(String name)
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
   public QReportMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputFields
    **
    *******************************************************************************/
   public List<QFieldMetaData> getInputFields()
   {
      return inputFields;
   }



   /*******************************************************************************
    ** Setter for inputFields
    **
    *******************************************************************************/
   public void setInputFields(List<QFieldMetaData> inputFields)
   {
      this.inputFields = inputFields;
   }



   /*******************************************************************************
    ** Fluent setter for inputFields
    **
    *******************************************************************************/
   public QReportMetaData withInputFields(List<QFieldMetaData> inputFields)
   {
      this.inputFields = inputFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processName
    **
    *******************************************************************************/
   public String getProcessName()
   {
      return processName;
   }



   /*******************************************************************************
    ** Setter for processName
    **
    *******************************************************************************/
   public void setProcessName(String processName)
   {
      this.processName = processName;
   }



   /*******************************************************************************
    ** Fluent setter for processName
    **
    *******************************************************************************/
   public QReportMetaData withProcessName(String processName)
   {
      this.processName = processName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dataSources
    **
    *******************************************************************************/
   public List<QReportDataSource> getDataSources()
   {
      return dataSources;
   }



   /*******************************************************************************
    ** Setter for dataSources
    **
    *******************************************************************************/
   public void setDataSources(List<QReportDataSource> dataSources)
   {
      this.dataSources = dataSources;
   }



   /*******************************************************************************
    ** Fluent setter for dataSources
    **
    *******************************************************************************/
   public QReportMetaData withDataSources(List<QReportDataSource> dataSources)
   {
      this.dataSources = dataSources;
      return (this);
   }



   /*******************************************************************************
    ** Getter for views
    **
    *******************************************************************************/
   public List<QReportView> getViews()
   {
      return views;
   }



   /*******************************************************************************
    ** Setter for views
    **
    *******************************************************************************/
   public void setViews(List<QReportView> views)
   {
      this.views = views;
   }



   /*******************************************************************************
    ** Fluent setter for views
    **
    *******************************************************************************/
   public QReportMetaData withViews(List<QReportView> views)
   {
      this.views = views;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getParentAppName()
   {
      return (this.parentAppName);
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
   public QReportMetaData withIcon(QIcon icon)
   {
      this.icon = icon;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QReportDataSource getDataSource(String dataSourceName)
   {
      for(QReportDataSource dataSource : CollectionUtils.nonNullList(dataSources))
      {
         if(dataSource.getName().equals(dataSourceName))
         {
            return (dataSource);
         }
      }

      return (null);
   }

}