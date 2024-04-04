/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.reporting.excel.poi;


import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.util.CellReference;


/*******************************************************************************
 ** Write excel formatted XML to a Writer.
 ** Originally from https://coderanch.com/t/548897/java/Generate-large-excel-POI
 *******************************************************************************/
public class StreamedSheetWriter
{
   private final Writer writer;
   private       int    rowNo;



   /*******************************************************************************
    **
    *******************************************************************************/
   public StreamedSheetWriter(Writer writer)
   {
      this.writer = writer;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void beginSheet() throws IOException
   {
      writer.write("""
         <?xml version="1.0" encoding="UTF-8"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
               <sheetData>""");

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void endSheet() throws IOException
   {
      writer.write("""
            </sheetData>
         </worksheet>""");
   }



   /*******************************************************************************
    ** Insert a new row
    **
    ** @param rowNo 0-based row number
    *******************************************************************************/
   public void insertRow(int rowNo) throws IOException
   {
      writer.write("<row r=\"" + (rowNo + 1) + "\">\n");
      this.rowNo = rowNo;
   }



   /*******************************************************************************
    ** Insert row end marker
    *******************************************************************************/
   public void endRow() throws IOException
   {
      writer.write("</row>\n");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void createCell(int columnIndex, String value, int styleIndex) throws IOException
   {
      String ref = new CellReference(rowNo, columnIndex).formatAsString();
      writer.write("<c r=\"" + ref + "\" t=\"inlineStr\"");
      if(styleIndex != -1)
      {
         writer.write(" s=\"" + styleIndex + "\"");
      }

      String cleanValue = cleanseValue(value);

      writer.write(">");
      writer.write("<is><t>" + cleanValue + "</t></is>");
      writer.write("</c>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String cleanseValue(String value)
   {
      if(value != null)
      {
         StringBuilder rs = new StringBuilder();
         for(int i = 0; i < value.length(); i++)
         {
            char c = value.charAt(i);
            if(c == '&')
            {
               rs.append("&amp;");
            }
            else if(c == '<')
            {
               rs.append("&lt;");
            }
            else if(c == '>')
            {
               rs.append("&gt;");
            }
            else if(c == '\'')
            {
               rs.append("&apos;");
            }
            else if(c == '"')
            {
               rs.append("&quot;");
            }
            else if (c < 32 && c != '\t' && c != '\n')
            {
               rs.append(' ');
            }
            else
            {
               rs.append(c);
            }
         }

         Map<String, Integer> m = new HashMap();
         m.computeIfAbsent("s", (s) -> 3);

         value = rs.toString();
      }

      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void createCell(int columnIndex, String value) throws IOException
   {
      createCell(columnIndex, value, -1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void createCell(int columnIndex, double value, int styleIndex) throws IOException
   {
      String ref = new CellReference(rowNo, columnIndex).formatAsString();
      writer.write("<c r=\"" + ref + "\" t=\"n\"");
      if(styleIndex != -1)
      {
         writer.write(" s=\"" + styleIndex + "\"");
      }
      writer.write(">");
      writer.write("<v>" + value + "</v>");
      writer.write("</c>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void createCell(int columnIndex, double value) throws IOException
   {
      createCell(columnIndex, value, -1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void createCell(int columnIndex, Boolean value) throws IOException
   {
      createCell(columnIndex, value, -1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void createCell(int columnIndex, Boolean value, int styleIndex) throws IOException
   {
      String ref = new CellReference(rowNo, columnIndex).formatAsString();
      writer.write("<c r=\"" + ref + "\" t=\"b\"");
      if(styleIndex != -1)
      {
         writer.write(" s=\"" + styleIndex + "\"");
      }
      writer.write(">");
      if(value != null)
      {
         writer.write("<v>" + (value ? 1 : 0) + "</v>");
      }
      writer.write("</c>");
   }

}
