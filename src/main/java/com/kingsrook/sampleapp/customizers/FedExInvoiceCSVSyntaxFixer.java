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

package com.kingsrook.sampleapp.customizers;


import java.util.function.Function;


/*******************************************************************************
 ** The fedex invoice files - they're CSV, but they have a handful of values
 ** that we can't read, because they have an extra quote (") character within
 ** a field.
 **
 ** It always looks like:  {"u
 ** This function fixes it by stripping the " out of the middle, to just be: {u
 *******************************************************************************/
public class FedExInvoiceCSVSyntaxFixer implements Function<String, String>
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String apply(String s)
   {
      return (s.replaceAll("\\{\"u", "{u"));
   }
}
