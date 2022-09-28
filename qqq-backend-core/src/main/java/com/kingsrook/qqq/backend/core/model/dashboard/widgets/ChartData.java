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
public class ChartData implements QWidget
{
   /*
      interface BarChartData{
         labels: string[];
         datasets: {
            label: string;
            data: number[];
         }[];
      }
    */

   private String title;
   private String description;
   private Data   chartData;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ChartData(String title, String description, String seriesLabel, List<String> labels, List<Number> data)
   {
      setTitle(title);
      setDescription(description);
      setChartData(new ChartData.Data()
         .withLabels(labels)
         .withDatasets(new ChartData.Data.Dataset()
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
   public ChartData withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    **
    *******************************************************************************/
   public String getDescription()
   {
      return description;
   }



   /*******************************************************************************
    ** Setter for description
    **
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    **
    *******************************************************************************/
   public ChartData withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for chartData
    **
    *******************************************************************************/
   public Data getChartData()
   {
      return chartData;
   }



   /*******************************************************************************
    ** Setter for chartData
    **
    *******************************************************************************/
   public void setChartData(Data chartData)
   {
      this.chartData = chartData;
   }



   /*******************************************************************************
    ** Fluent setter for chartData
    **
    *******************************************************************************/
   public ChartData withChartData(Data chartData)
   {
      this.chartData = chartData;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Data
   {
      private List<String> labels;
      private Dataset      dataset;



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
      public Dataset getDataset()
      {
         return dataset;
      }



      /*******************************************************************************
       ** Setter for datasets
       **
       *******************************************************************************/
      public void setDataset(Dataset dataset)
      {
         this.dataset = dataset;
      }



      /*******************************************************************************
       ** Fluent setter for datasets
       **
       *******************************************************************************/
      public Data withDatasets(Dataset datasets)
      {
         this.dataset = datasets;
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public static class Dataset
      {
         private String       label;
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
         public Dataset withLabel(String label)
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
         public Dataset withData(List<Number> data)
         {
            this.data = data;
            return (this);
         }
      }
   }
}