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


import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/*******************************************************************************
 ** Excel styler that does nothing - just takes defaults (which are all no-op) from the interface.
 *******************************************************************************/
public class PlainPoiExcelStyler implements PoiExcelStylerInterface
{

   /*******************************************************************************
    ** ... sorry, but adding this gives us test coverage on this class, even though
    ** we're just deferring to super...
    *******************************************************************************/
   @Override
   public XSSFCellStyle createStyleForHeader(XSSFWorkbook workbook, CreationHelper createHelper)
   {
      return PoiExcelStylerInterface.super.createStyleForHeader(workbook, createHelper);
   }

}