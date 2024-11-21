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

package com.kingsrook.qqq.backend.core.actions.reporting.excel.fastexcel;


import java.io.ByteArrayOutputStream;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.dhatim.fastexcel.StyleSetter;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for BoldHeaderAndFooterFastExcelStyler 
 *******************************************************************************/
class BoldHeaderAndFooterFastExcelStylerTest extends BaseTest
{

   /*******************************************************************************
    ** ... kinda just here to add test coverage to the class.  I suppose, it
    ** makes sure there's not an NPE inside that method at least...?
    *******************************************************************************/
   @Test
   void test()
   {
      Workbook    workbook    = new Workbook(new ByteArrayOutputStream(), "Test", null);
      Worksheet   worksheet   = workbook.newWorksheet("Sheet 1");
      StyleSetter headerStyle = worksheet.range(0, 0, 1, 1).style();
      new BoldHeaderAndFooterFastExcelStyler().styleHeaderRow(headerStyle);
   }

}