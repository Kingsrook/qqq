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
import java.util.Map;


/*******************************************************************************
 ** Model containing datastructure expected by frontend bar chart widget
 **
 *******************************************************************************/
public class TableData extends QWidgetData
{
   private String                    linkText;
   private String                    linkURL;
   private String                    noRowsFoundHTML;
   private List<Column>              columns;
   private List<Map<String, Object>> rows;
   private List<Map<String, String>> dropdownOptions;



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableData(String label, List<Column> columns, List<Map<String, Object>> rows, List<Map<String, String>> dropdownOptions)
   {
      setLabel(label);
      setColumns(columns);
      setRows(rows);
      setDropdownOptions(dropdownOptions);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.TABLE.getType();
   }



   /*******************************************************************************
    ** Getter for columns
    **
    *******************************************************************************/
   public List<Column> getColumns()
   {
      return columns;
   }



   /*******************************************************************************
    ** Setter for columns
    **
    *******************************************************************************/
   public void setColumns(List<Column> columns)
   {
      this.columns = columns;
   }



   /*******************************************************************************
    ** Fluent setter for columns
    **
    *******************************************************************************/
   public TableData withColumns(List<Column> columns)
   {
      this.columns = columns;
      return (this);
   }



   /*******************************************************************************
    ** Getter for rows
    **
    *******************************************************************************/
   public List<Map<String, Object>> getRows()
   {
      return rows;
   }



   /*******************************************************************************
    ** Setter for rows
    **
    *******************************************************************************/
   public void setRows(List<Map<String, Object>> rows)
   {
      this.rows = rows;
   }



   /*******************************************************************************
    ** Fluent setter for rows
    **
    *******************************************************************************/
   public TableData withRows(List<Map<String, Object>> rows)
   {
      this.rows = rows;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dropdownOptions
    **
    *******************************************************************************/
   public List<Map<String, String>> getDropdownOptions()
   {
      return dropdownOptions;
   }



   /*******************************************************************************
    ** Setter for dropdownOptions
    **
    *******************************************************************************/
   public void setDropdownOptions(List<Map<String, String>> dropdownOptions)
   {
      this.dropdownOptions = dropdownOptions;
   }



   /*******************************************************************************
    ** Fluent setter for dropdownOptions
    **
    *******************************************************************************/
   public TableData withDropdownOptions(List<Map<String, String>> dropdownOptions)
   {
      this.dropdownOptions = dropdownOptions;
      return (this);
   }



   /*******************************************************************************
    ** Getter for noRowsFoundHTML
    **
    *******************************************************************************/
   public String getNoRowsFoundHTML()
   {
      return noRowsFoundHTML;
   }



   /*******************************************************************************
    ** Setter for noRowsFoundHTML
    **
    *******************************************************************************/
   public void setNoRowsFoundHTML(String noRowsFoundHTML)
   {
      this.noRowsFoundHTML = noRowsFoundHTML;
   }



   /*******************************************************************************
    ** Fluent setter for noRowsFoundHTML
    **
    *******************************************************************************/
   public TableData withNoRowsFoundHTML(String noRowsFoundHTML)
   {
      this.noRowsFoundHTML = noRowsFoundHTML;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkText
    **
    *******************************************************************************/
   public String getLinkText()
   {
      return linkText;
   }



   /*******************************************************************************
    ** Setter for linkText
    **
    *******************************************************************************/
   public void setLinkText(String linkText)
   {
      this.linkText = linkText;
   }



   /*******************************************************************************
    ** Fluent setter for linkText
    **
    *******************************************************************************/
   public TableData withLinkText(String linkText)
   {
      this.linkText = linkText;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkURL
    **
    *******************************************************************************/
   public String getLinkURL()
   {
      return linkURL;
   }



   /*******************************************************************************
    ** Setter for linkURL
    **
    *******************************************************************************/
   public void setLinkURL(String linkURL)
   {
      this.linkURL = linkURL;
   }



   /*******************************************************************************
    ** Fluent setter for linkURL
    **
    *******************************************************************************/
   public TableData withLinkURL(String linkURL)
   {
      this.linkURL = linkURL;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Column
   {
      private String type;
      private String header;
      private String accessor;
      private String width;
      private String align;
      private String verticalAlign;



      /*******************************************************************************
       **
       *******************************************************************************/
      public Column(String type, String header, String accessor, String width, String align)
      {
         this.type = type;
         this.header = header;
         this.accessor = accessor;
         this.width = width;
         this.align = align;
      }



      /*******************************************************************************
       ** Getter for type
       **
       *******************************************************************************/
      public String getType()
      {
         return type;
      }



      /*******************************************************************************
       ** Setter for type
       **
       *******************************************************************************/
      public void setType(String type)
      {
         this.type = type;
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
       ** Getter for accessor
       **
       *******************************************************************************/
      public String getAccessor()
      {
         return accessor;
      }



      /*******************************************************************************
       ** Setter for accessor
       **
       *******************************************************************************/
      public void setAccessor(String accessor)
      {
         this.accessor = accessor;
      }



      /*******************************************************************************
       ** Getter for width
       **
       *******************************************************************************/
      public String getWidth()
      {
         return width;
      }



      /*******************************************************************************
       ** Setter for width
       **
       *******************************************************************************/
      public void setWidth(String width)
      {
         this.width = width;
      }



      /*******************************************************************************
       ** Getter for align
       **
       *******************************************************************************/
      public String getAlign()
      {
         return align;
      }



      /*******************************************************************************
       ** Setter for align
       **
       *******************************************************************************/
      public void setAlign(String align)
      {
         this.align = align;
      }



      /*******************************************************************************
       ** fluent setter for header
       **
       *******************************************************************************/
      public Column withHeader(String header)
      {
         this.header = header;
         return this;
      }



      /*******************************************************************************
       ** fluent setter for accessor
       **
       *******************************************************************************/
      public Column withAccessor(String accessor)
      {
         this.accessor = accessor;
         return this;
      }



      /*******************************************************************************
       ** fluent setter for width
       **
       *******************************************************************************/
      public Column withWidth(String width)
      {
         this.width = width;
         return this;
      }



      /*******************************************************************************
       ** fluent setter for align
       **
       *******************************************************************************/
      public Column withAlign(String align)
      {
         this.align = align;
         return this;
      }



      /*******************************************************************************
       ** fluent setter for type
       **
       *******************************************************************************/
      public Column withType(String type)
      {
         this.type = type;
         return this;
      }



      /*******************************************************************************
       ** Getter for verticalAlign
       **
       *******************************************************************************/
      public String getVerticalAlign()
      {
         return verticalAlign;
      }



      /*******************************************************************************
       ** Setter for verticalAlign
       **
       *******************************************************************************/
      public void setVerticalAlign(String verticalAlign)
      {
         this.verticalAlign = verticalAlign;
      }



      /*******************************************************************************
       ** Fluent setter for verticalAlign
       **
       *******************************************************************************/
      public Column withVerticalAlign(String verticalAlign)
      {
         this.verticalAlign = verticalAlign;
         return (this);
      }

   }
}
