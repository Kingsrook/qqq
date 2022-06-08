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

package com.kingsrook.qqq.backend.core.instances;


import java.util.Locale;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** As part of helping a QInstance be created and/or validated, apply some default
 ** transfomations to it, such as populating missing labels based on names.
 **
 *******************************************************************************/
public class QInstanceEnricher
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public void enrich(QInstance qInstance)
   {
      if (qInstance.getTables() != null)
      {
         qInstance.getTables().values().forEach(this::enrich);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QTableMetaData table)
   {
      if(!StringUtils.hasContent(table.getLabel()))
      {
         table.setLabel(nameToLabel(table.getName()));
      }

      if (table.getFields() != null)
      {
         table.getFields().values().forEach(this::enrich);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QFieldMetaData field)
   {
      if(!StringUtils.hasContent(field.getLabel()))
      {
         field.setLabel(nameToLabel(field.getName()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String nameToLabel(String name)
   {
      if(name == null)
      {
         return (null);
      }

      return (name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1).replaceAll("([A-Z])", " $1"));
   }

}
