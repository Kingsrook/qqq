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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


/*******************************************************************************
 **
 *******************************************************************************/
public interface DisplayFormat
{
   String DEFAULT = "%s";
   String STRING  = "%s";
   String COMMAS  = "%,d";

   String DECIMAL1_COMMAS = "%,.1f";
   String DECIMAL2_COMMAS = "%,.2f";
   String DECIMAL3_COMMAS = "%,.3f";

   String DECIMAL1 = "%.1f";
   String DECIMAL2 = "%.2f";
   String DECIMAL3 = "%.3f";

   String CURRENCY = "$%,.2f";

   String PERCENT        = "%.0f%%";
   String PERCENT_POINT1 = "%.1f%%";
   String PERCENT_POINT2 = "%.2f%%";


   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   static String getExcelFormat(String javaDisplayFormat)
   {
      if(javaDisplayFormat == null)
      {
         return (null);
      }

      return switch(javaDisplayFormat)
         {
            case DisplayFormat.DEFAULT -> null;
            case DisplayFormat.COMMAS -> "#,##0";
            case DisplayFormat.DECIMAL1 -> "0.0";
            case DisplayFormat.DECIMAL2 -> "0.00";
            case DisplayFormat.DECIMAL3 -> "0.000";
            case DisplayFormat.DECIMAL1_COMMAS -> "#,##0.0";
            case DisplayFormat.DECIMAL2_COMMAS -> "#,##0.00";
            case DisplayFormat.DECIMAL3_COMMAS -> "#,##0.000";
            case DisplayFormat.CURRENCY -> "$#,##0.00";
            case DisplayFormat.PERCENT -> "0%";
            case DisplayFormat.PERCENT_POINT1 -> "0.0%";
            case DisplayFormat.PERCENT_POINT2 -> "0.00%";
            default -> null;
         };

   }
}
