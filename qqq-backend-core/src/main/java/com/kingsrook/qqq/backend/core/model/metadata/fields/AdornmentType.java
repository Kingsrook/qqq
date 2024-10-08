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


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 ** Types of adornments that can be added to fields - with utilities for
 ** constructing their values.
 *******************************************************************************/
public enum AdornmentType
{
   LINK,
   CHIP,
   SIZE,
   CODE_EDITOR,
   RENDER_HTML,
   REVEAL,
   FILE_DOWNLOAD,
   ERROR;
   //////////////////////////////////////////////////////////////////////////
   // keep these values in sync with AdornmentType.ts in qqq-frontend-core //
   //////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public interface LinkValues
   {
      String TARGET               = "target";
      String TO_RECORD_FROM_TABLE = "toRecordFromTable";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public interface FileDownloadValues
   {
      String FILE_NAME_FIELD   = "fileNameField";
      String DEFAULT_EXTENSION = "defaultExtension";
      String DEFAULT_MIME_TYPE = "defaultMimeType";

      ////////////////////////////////////////////////////
      // use these two together, as in:                 //
      // FILE_NAME_FORMAT = "Order %s Packing Slip.pdf" //
      // FILE_NAME_FORMAT_FIELDS = "orderId"            //
      ////////////////////////////////////////////////////
      String FILE_NAME_FORMAT        = "fileNameFormat";
      String FILE_NAME_FORMAT_FIELDS = "fileNameFormatFields";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public interface ChipValues
   {
      String COLOR_DEFAULT   = "default";
      String COLOR_INFO      = "info";
      String COLOR_PRIMARY   = "primary";
      String COLOR_SECONDARY = "secondary";
      String COLOR_SUCCESS   = "success";
      String COLOR_WARNING   = "warning";
      String COLOR_ERROR     = "error";

      /*******************************************************************************
       **
       *******************************************************************************/
      static Pair<String, Serializable> colorValue(Serializable value, String colorName)
      {
         return (new Pair<>("color." + value, colorName));
      }

      /*******************************************************************************
       **
       *******************************************************************************/
      static Pair<String, Serializable> iconValue(Serializable value, String iconName)
      {
         return (new Pair<>("icon." + value, iconName));
      }

      /*******************************************************************************
       **
       *******************************************************************************/
      @SuppressWarnings("unchecked")
      static Pair<String, Serializable>[] iconAndColorValues(Serializable value, String iconName, String colorName)
      {
         if(value instanceof PossibleValueEnum<?> possibleValueEnum)
         {
            value = (Serializable) possibleValueEnum.getPossibleValueId();
         }

         return (new Pair[] { iconValue(value, iconName), colorValue(value, colorName) });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum Size
   {
      XSMALL,
      SMALL,
      MEDIUM,
      MEDLARGE,
      LARGE,
      XLARGE;



      /*******************************************************************************
       **
       *******************************************************************************/
      public FieldAdornment toAdornment()
      {
         return (new FieldAdornment(AdornmentType.SIZE, MapBuilder.of("width", name().toLowerCase())));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public interface CodeEditorValues
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      static Pair<String, Serializable> languageMode(String languageMode)
      {
         return (new Pair<>("languageMode", languageMode));
      }
   }

}
