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
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Model containing datastructure expected by frontend bar chart widget
 **
 *******************************************************************************/
public class ChartData extends QWidgetData
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

   private String  title;
   private String  description;
   private Data    chartData;
   private boolean isCurrency = false;
   private int     height;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ChartData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ChartData(String title, String description, String seriesLabel, List<String> labels, List<Number> data)
   {
      setTitle(title);
      setDescription(description);
      setChartData(new ChartData.Data()
         .withLabels(labels)
         .withDatasets(List.of(
            new ChartData.Data.Dataset()
               .withLabel(seriesLabel)
               .withData(data)
         ))
      );
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.CHART.getType();
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
    ** Getter for isCurrency
    **
    *******************************************************************************/
   public boolean getIsCurrency()
   {
      return isCurrency;
   }



   /*******************************************************************************
    ** Setter for isCurrency
    **
    *******************************************************************************/
   public void setIsCurrency(boolean isCurrency)
   {
      this.isCurrency = isCurrency;
   }



   /*******************************************************************************
    ** Fluent setter for isCurrency
    **
    *******************************************************************************/
   public ChartData withIsCurrency(boolean isCurrency)
   {
      this.isCurrency = isCurrency;
      return (this);
   }



   /*******************************************************************************
    ** Getter for height
    **
    *******************************************************************************/
   public int getHeight()
   {
      return height;
   }



   /*******************************************************************************
    ** Setter for height
    **
    *******************************************************************************/
   public void setHeight(int height)
   {
      this.height = height;
   }



   /*******************************************************************************
    ** Fluent setter for height
    **
    *******************************************************************************/
   public ChartData withHeight(int height)
   {
      this.height = height;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Data
   {
      private List<String>  labels;
      private List<Dataset> datasets;



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
      public List<Dataset> getDatasets()
      {
         return (datasets);
      }



      /*******************************************************************************
       ** Fluent setter for datasets
       **
       *******************************************************************************/
      public Data withDatasets(List<Dataset> datasets)
      {
         this.datasets = datasets;
         return (this);
      }



      /*******************************************************************************
       ** Getter for single dataset
       **
       *******************************************************************************/
      public Dataset getDataset()
      {
         if(CollectionUtils.nullSafeHasContents(getDatasets()))
         {
            return (getDatasets().get(0));

         }
         return (null);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public static class Dataset
      {
         private String       label;
         private List<Number> data;
         private String       color;
         private String       backgroundColor;
         private List<String> urls;



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
          ** Getter for backgroundColor
          **
          *******************************************************************************/
         public String getBackgroundColor()
         {
            return backgroundColor;
         }



         /*******************************************************************************
          ** Setter for backgroundColor
          **
          *******************************************************************************/
         public void setBackgroundColor(String backgroundColor)
         {
            this.backgroundColor = backgroundColor;
         }



         /*******************************************************************************
          ** Fluent setter for backgroundColor
          **
          *******************************************************************************/
         public Dataset withBackgroundColor(String backgroundColor)
         {
            this.backgroundColor = backgroundColor;
            return (this);
         }



         /*******************************************************************************
          ** Setter for color
          **
          *******************************************************************************/
         public void setColor(String color)
         {
            this.color = color;
         }



         /*******************************************************************************
          ** Fluent setter for color
          **
          *******************************************************************************/
         public Dataset withColor(String color)
         {
            this.color = color;
            return (this);
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



         /*******************************************************************************
          ** Getter for urls
          **
          *******************************************************************************/
         public List<String> getUrls()
         {
            return urls;
         }



         /*******************************************************************************
          ** Setter for urls
          **
          *******************************************************************************/
         public void setUrls(List<String> urls)
         {
            this.urls = urls;
         }



         /*******************************************************************************
          ** Fluent setter for urls
          **
          *******************************************************************************/
         public Dataset withUrls(List<String> urls)
         {
            this.urls = urls;
            return (this);
         }
      }
   }
}
