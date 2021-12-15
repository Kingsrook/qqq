/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.instances;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Class that knows how to take a look at the data in a QInstance, and report
 ** if it is all valid - e.g., non-null things are set; references line-up (e.g.,
 ** a table's backend must be a defined backend).
 **
 ** Prior to doing validation, the the QInstanceEnricher is ran over the QInstance,
 ** e.g., to fill in things that can be defaulted or assumed.  TODO let the instance
 ** customize or opt-out of Enrichment.
 **
 *******************************************************************************/
public class QInstanceValidator
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void validate(QInstance qInstance) throws QInstanceValidationException
   {
      if(qInstance.getHasBeenValidated())
      {
         //////////////////////////////////////////
         // don't re-validate if previously done //
         //////////////////////////////////////////
         return;
      }

      try
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // before validation, enrich the object (e.g., to fill in values that the user doesn't have to //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // TODO - possible point of customization (use a different enricher, or none, or pass it options).
         new QInstanceEnricher().enrich(qInstance);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QInstanceValidationException("Error enriching qInstance prior to validation.", e));
      }

      //////////////////////////////////////////////////////////////////////////
      // do the validation checks - a good qInstance has all conditions TRUE! //
      //////////////////////////////////////////////////////////////////////////
      List<String> errors = new ArrayList<>();
      try
      {
         if(assertCondition(errors, CollectionUtils.nullSafeHasContents(qInstance.getBackends()), "At least 1 backend must be defined."))
         {
            qInstance.getBackends().forEach((backendName, backend) ->
            {
               assertCondition(errors, Objects.equals(backendName, backend.getName()), "Inconsistent naming for backend: " + backendName + "/" + backend.getName() + ".");
            });
         }

         if(assertCondition(errors, CollectionUtils.nullSafeHasContents(qInstance.getTables()), "At least 1 table must be defined."))
         {
            qInstance.getTables().forEach((tableName, table) ->
            {
               assertCondition(errors, Objects.equals(tableName, table.getName()), "Inconsistent naming for table: " + tableName + "/" + table.getName() + ".");

               if(assertCondition(errors, StringUtils.hasContent(table.getBackendName()), "Missing backend name for table " + tableName + "."))
               {
                  if(CollectionUtils.nullSafeHasContents(qInstance.getBackends()))
                  {
                     assertCondition(errors, qInstance.getBackendForTable(tableName) != null, "Unrecognized backend " + table.getBackendName() + " for table " + tableName + ".");
                  }
               }
            });
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QInstanceValidationException("Error performing qInstance validation.", e));
      }

      if(!errors.isEmpty())
      {
         throw (new QInstanceValidationException(errors));
      }

      qInstance.setHasBeenValidated(new QInstanceValidationKey());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean assertCondition(List<String> errors, boolean condition, String message)
   {
      if(!condition)
      {
         errors.add(message);
      }

      return (condition);
   }

}
