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

package com.kingsrook.qqq.backend.core.model.metadata.possiblevalues;


import java.util.List;


/*******************************************************************************
 ** Define some standard ways to format the value portion of a PossibleValueSource.
 **
 ** Can be passed to short-cut {set,with}ValueFormatAndFields methods in QPossibleValueSource
 ** class, or the format & field properties can be extracted and passed to regular field-level setters.
 *******************************************************************************/
public enum PVSValueFormatAndFields
{
   LABEL_ONLY("%s", "label"),
   LABEL_PARENS_ID("%s (%s)", "label", "id"),
   ID_COLON_LABEL("%s: %s", "id", "label");


   private final String       format;
   private final List<String> fields;



   /*******************************************************************************
    **
    *******************************************************************************/
   PVSValueFormatAndFields(String format, String... fields)
   {
      this.format = format;
      this.fields = List.of(fields);
   }



   /*******************************************************************************
    ** Getter for format
    **
    *******************************************************************************/
   public String getFormat()
   {
      return format;
   }



   /*******************************************************************************
    ** Getter for fields
    **
    *******************************************************************************/
   public List<String> getFields()
   {
      return fields;
   }
}
