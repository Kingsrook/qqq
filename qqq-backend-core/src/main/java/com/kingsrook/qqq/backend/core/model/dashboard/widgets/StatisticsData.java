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


/*******************************************************************************
 ** Model containing datastructure expected by frontend bar chart widget
 **
 *******************************************************************************/
public class StatisticsData implements QWidget
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
   private int    count;
   private Number percentageAmount;
   private String percentageLabel;



   /*******************************************************************************
    **
    *******************************************************************************/
   public StatisticsData(String title, int count, Number percentageAmount, String percentageLabel)
   {
      this.title = title;
      this.count = count;
      this.percentageLabel = percentageLabel;
      this.percentageAmount = percentageAmount;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return "statistics";
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
   public StatisticsData withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for count
    **
    *******************************************************************************/
   public int getCount()
   {
      return count;
   }



   /*******************************************************************************
    ** Setter for count
    **
    *******************************************************************************/
   public void setCount(int count)
   {
      this.count = count;
   }



   /*******************************************************************************
    ** Fluent setter for count
    **
    *******************************************************************************/
   public StatisticsData withCount(int count)
   {
      this.count = count;
      return (this);
   }



   /*******************************************************************************
    ** Getter for percentageAmount
    **
    *******************************************************************************/
   public Number getPercentageAmount()
   {
      return percentageAmount;
   }



   /*******************************************************************************
    ** Setter for percentageAmount
    **
    *******************************************************************************/
   public void setPercentageAmount(Number percentageAmount)
   {
      this.percentageAmount = percentageAmount;
   }



   /*******************************************************************************
    ** Fluent setter for percentageAmount
    **
    *******************************************************************************/
   public StatisticsData withPercentageAmount(Number percentageAmount)
   {
      this.percentageAmount = percentageAmount;
      return (this);
   }



   /*******************************************************************************
    ** Getter for percentageLabel
    **
    *******************************************************************************/
   public String getPercentageLabel()
   {
      return percentageLabel;
   }



   /*******************************************************************************
    ** Setter for percentageLabel
    **
    *******************************************************************************/
   public void setPercentageLabel(String percentageLabel)
   {
      this.percentageLabel = percentageLabel;
   }



   /*******************************************************************************
    ** Fluent setter for percentageLabel
    **
    *******************************************************************************/
   public StatisticsData withPercentageLabel(String percentageLabel)
   {
      this.percentageLabel = percentageLabel;
      return (this);
   }

}