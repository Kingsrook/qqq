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
public class MultiStatisticsData implements QWidget
{
   private String                    title;
   private List<StatisticsGroupData> statisticsGroupData;



   /*******************************************************************************
    **
    *******************************************************************************/
   public MultiStatisticsData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public MultiStatisticsData(String title, List<StatisticsGroupData> statisticsGroupData)
   {
      this.title = title;
      this.statisticsGroupData = statisticsGroupData;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.MULTI_STATISTICS.getType();
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
   public MultiStatisticsData withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for statisticsGroupData
    **
    *******************************************************************************/
   public List<StatisticsGroupData> getStatisticsGroupData()
   {
      return statisticsGroupData;
   }



   /*******************************************************************************
    ** Setter for statisticsGroupData
    **
    *******************************************************************************/
   public void setStatisticsGroupData(List<StatisticsGroupData> statisticsGroupData)
   {
      this.statisticsGroupData = statisticsGroupData;
   }



   /*******************************************************************************
    ** Fluent setter for statisticsGroupData
    **
    *******************************************************************************/
   public MultiStatisticsData withStatisticsGroupData(List<StatisticsGroupData> statisticsGroupData)
   {
      this.statisticsGroupData = statisticsGroupData;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class StatisticsGroupData
   {
      private String icon;
      private String iconColor;
      private String header;
      private String subheader;
      List<Statistic> statisticList;



      /*******************************************************************************
       **
       *******************************************************************************/
      public StatisticsGroupData()
      {
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public StatisticsGroupData(String icon, String iconColor, String header, String subheader, List<Statistic> statisticList)
      {
         this.icon = icon;
         this.iconColor = iconColor;
         this.header = header;
         this.subheader = subheader;
         this.statisticList = statisticList;
      }



      /*******************************************************************************
       ** Getter for header
       **
       *******************************************************************************/
      public String getHeader()
      {
         return header;
      }



      /*******************************************************************************
       ** Setter for header
       **
       *******************************************************************************/
      public void setHeader(String header)
      {
         this.header = header;
      }



      /*******************************************************************************
       ** Fluent setter for header
       **
       *******************************************************************************/
      public StatisticsGroupData withHeader(String header)
      {
         this.header = header;
         return (this);
      }



      /*******************************************************************************
       ** Getter for subheader
       **
       *******************************************************************************/
      public String getSubheader()
      {
         return subheader;
      }



      /*******************************************************************************
       ** Setter for subheader
       **
       *******************************************************************************/
      public void setSubheader(String subheader)
      {
         this.subheader = subheader;
      }



      /*******************************************************************************
       ** Fluent setter for subheader
       **
       *******************************************************************************/
      public StatisticsGroupData withSubheader(String subheader)
      {
         this.subheader = subheader;
         return (this);
      }



      /*******************************************************************************
       ** Getter for statisticList
       **
       *******************************************************************************/
      public List<Statistic> getStatisticList()
      {
         return statisticList;
      }



      /*******************************************************************************
       ** Setter for statisticList
       **
       *******************************************************************************/
      public void setStatisticList(List<Statistic> statisticList)
      {
         this.statisticList = statisticList;
      }



      /*******************************************************************************
       ** Fluent setter for statisticList
       **
       *******************************************************************************/
      public StatisticsGroupData withStatisticList(List<Statistic> statisticList)
      {
         this.statisticList = statisticList;
         return (this);
      }



      /*******************************************************************************
       ** Getter for icon
       **
       *******************************************************************************/
      public String getIcon()
      {
         return icon;
      }



      /*******************************************************************************
       ** Setter for icon
       **
       *******************************************************************************/
      public void setIcon(String icon)
      {
         this.icon = icon;
      }



      /*******************************************************************************
       ** Fluent setter for icon
       **
       *******************************************************************************/
      public StatisticsGroupData withIcon(String icon)
      {
         this.icon = icon;
         return (this);
      }



      /*******************************************************************************
       ** Getter for iconColor
       **
       *******************************************************************************/
      public String getIconColor()
      {
         return iconColor;
      }



      /*******************************************************************************
       ** Setter for iconColor
       **
       *******************************************************************************/
      public void setIconColor(String iconColor)
      {
         this.iconColor = iconColor;
      }



      /*******************************************************************************
       ** Fluent setter for iconColor
       **
       *******************************************************************************/
      public StatisticsGroupData withIconColor(String iconColor)
      {
         this.iconColor = iconColor;
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public static class Statistic
      {
         private String  label;
         private Integer value;
         private String  url;



         /*******************************************************************************
          **
          *******************************************************************************/
         public Statistic(String label, Integer value, String url)
         {
            this.label = label;
            this.value = value;
            this.url = url;
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
         public Statistic withLabel(String label)
         {
            this.label = label;
            return (this);
         }



         /*******************************************************************************
          ** Getter for value
          **
          *******************************************************************************/
         public Integer getValue()
         {
            return value;
         }



         /*******************************************************************************
          ** Setter for value
          **
          *******************************************************************************/
         public void setValue(Integer value)
         {
            this.value = value;
         }



         /*******************************************************************************
          ** Fluent setter for value
          **
          *******************************************************************************/
         public Statistic withValue(Integer value)
         {
            this.value = value;
            return (this);
         }



         /*******************************************************************************
          ** Getter for url
          **
          *******************************************************************************/
         public String getUrl()
         {
            return url;
         }



         /*******************************************************************************
          ** Setter for url
          **
          *******************************************************************************/
         public void setUrl(String url)
         {
            this.url = url;
         }



         /*******************************************************************************
          ** Fluent setter for url
          **
          *******************************************************************************/
         public Statistic withUrl(String url)
         {
            this.url = url;
            return (this);
         }

      }

   }
}
