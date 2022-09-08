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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets;


import java.util.List;


/*******************************************************************************
 ** Model containing datastructure expected by frontend bar chart widget
 **
 *******************************************************************************/
public class BarChart implements QWidget
{

   /*
      type: "barChart",
         title: "Parcel Invoice Lines per Month",
         barChartData: {
            labels: ["Feb 22", "Mar 22", "Apr 22", "May 22", "Jun 22", "Jul 22", "Aug 22"],
            datasets: {label: "Parcel Invoice Lines", data: [50000, 22000, 11111, 22333, 40404, 9876, 2355]},
         },
    */

   private String title;
   private Data barChartData;



   /*******************************************************************************
    **
    *******************************************************************************/
   public BarChart(String title, String seriesLabel, List<String> labels, List<Number> data)
   {
      setTitle(title);
      setBarChartData(new BarChart.Data()
         .withLabels(labels)
         .withDatasets(new BarChart.Data.DataSet()
            .withLabel(seriesLabel)
            .withData(data)));
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return "barChart";
   }



   /*******************************************************************************
    ** Getter for title
    **
    *******************************************************************************/
   public String getTitle()
   {
      return title;
   }



   /*******************************************************************************
    ** Setter for title
    **
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    **
    *******************************************************************************/
   public BarChart withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for barChartData
    **
    *******************************************************************************/
   public Data getBarChartData()
   {
      return barChartData;
   }



   /*******************************************************************************
    ** Setter for barChartData
    **
    *******************************************************************************/
   public void setBarChartData(Data barChartData)
   {
      this.barChartData = barChartData;
   }



   /*******************************************************************************
    ** Fluent setter for barChartData
    **
    *******************************************************************************/
   public BarChart withBarChartData(Data barChartData)
   {
      this.barChartData = barChartData;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Data
   {
      private List<String> labels;
      private DataSet datasets;



      /*******************************************************************************
       ** Getter for labels
       **
       *******************************************************************************/
      public List<String> getLabels()
      {
         return labels;
      }



      /*******************************************************************************
       ** Setter for labels
       **
       *******************************************************************************/
      public void setLabels(List<String> labels)
      {
         this.labels = labels;
      }



      /*******************************************************************************
       ** Fluent setter for labels
       **
       *******************************************************************************/
      public Data withLabels(List<String> labels)
      {
         this.labels = labels;
         return (this);
      }



      /*******************************************************************************
       ** Getter for datasets
       **
       *******************************************************************************/
      public DataSet getDatasets()
      {
         return datasets;
      }



      /*******************************************************************************
       ** Setter for datasets
       **
       *******************************************************************************/
      public void setDatasets(DataSet datasets)
      {
         this.datasets = datasets;
      }



      /*******************************************************************************
       ** Fluent setter for datasets
       **
       *******************************************************************************/
      public Data withDatasets(DataSet datasets)
      {
         this.datasets = datasets;
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public static class DataSet
      {
         private String label;
         private List<Number> data;



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
         public DataSet withLabel(String label)
         {
            this.label = label;
            return (this);
         }



         /*******************************************************************************
          ** Getter for data
          **
          *******************************************************************************/
         public List<Number> getData()
         {
            return data;
         }



         /*******************************************************************************
          ** Setter for data
          **
          *******************************************************************************/
         public void setData(List<Number> data)
         {
            this.data = data;
         }



         /*******************************************************************************
          ** Fluent setter for data
          **
          *******************************************************************************/
         public DataSet withData(List<Number> data)
         {
            this.data = data;
            return (this);
         }
      }
   }
}
