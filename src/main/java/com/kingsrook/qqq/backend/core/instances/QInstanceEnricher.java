/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
