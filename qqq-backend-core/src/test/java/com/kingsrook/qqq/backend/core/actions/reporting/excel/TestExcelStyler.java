/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.reporting.excel;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.reporting.excel.poi.ExcelPoiStyleCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestExcelStyler implements ExcelPoiStyleCustomizerInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<Integer> getColumnWidthsForView(QReportView view)
   {
      return List.of(60, 50, 40);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<String> getMergedRangesForView(QReportView view)
   {
      return List.of("A1:B1");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void customizeStyles(Map<String, XSSFCellStyle> styles, XSSFWorkbook workbook, CreationHelper createHelper)
   {
      Font font = workbook.createFont();
      font.setFontHeightInPoints((short) 16);
      font.setBold(true);
      XSSFCellStyle cellStyle = workbook.createCellStyle();
      cellStyle.setFont(font);
      styles.put("header", cellStyle);
   }
}
