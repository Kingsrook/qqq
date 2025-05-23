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

package com.kingsrook.qqq.backend.core.model.metadata.variants;


import java.io.Serializable;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeFunction;


/*******************************************************************************
 ** Utility methods for backends working with Variants.
 *******************************************************************************/
public class BackendVariantsUtil
{

   /*******************************************************************************
    ** Get the variant id from the session for the backend.
    *******************************************************************************/
   public static Serializable getVariantId(QBackendMetaData backendMetaData) throws QException
   {
      QSession session        = QContext.getQSession();
      String   variantTypeKey = backendMetaData.getBackendVariantsConfig().getVariantTypeKey();
      if(session.getBackendVariants() == null || !session.getBackendVariants().containsKey(variantTypeKey))
      {
         throw (new QUserFacingException("Could not find Backend Variant information in session under key '" + variantTypeKey + "' for Backend '" + backendMetaData.getName() + "'"));
      }
      Serializable variantId = session.getBackendVariants().get(variantTypeKey);
      return variantId;
   }



   /*******************************************************************************
    ** For backends that use variants, look up the variant record (in theory, based
    ** on an id in the session's backend variants map, then fetched from the backend's
    ** variant options table.
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static QRecord getVariantRecord(QBackendMetaData backendMetaData) throws QException
   {
      Serializable variantId = getVariantId(backendMetaData);

      QRecord record;
      if(backendMetaData.getBackendVariantsConfig().getVariantRecordLookupFunction() != null)
      {
         Object o = QCodeLoader.getAdHoc(Object.class, backendMetaData.getBackendVariantsConfig().getVariantRecordLookupFunction());
         if(o instanceof UnsafeFunction<?,?,?> unsafeFunction)
         {
            record = ((UnsafeFunction<Serializable, QRecord, QException>) unsafeFunction).apply(variantId);
         }
         else if(o instanceof Function<?,?> function)
         {
            record = ((Function<Serializable, QRecord>) function).apply(variantId);
         }
         else
         {
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // note, we'll consider this a programmer-error, not a user-facing one (e.g., bad submitted data), so not throw user-facing //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            throw (new QException("Backend Variant's recordLookupFunction is not of any expected type (should have been caught by instance validation??)"));
         }
      }
      else
      {
         GetInput getInput = new GetInput();
         getInput.setShouldMaskPasswords(false);
         getInput.setTableName(backendMetaData.getBackendVariantsConfig().getOptionsTableName());
         getInput.setPrimaryKey(variantId);
         GetOutput getOutput = new GetAction().execute(getInput);

         record = getOutput.getRecord();
      }

      if(record == null)
      {
         throw (new QUserFacingException("Could not find Backend Variant in table " + backendMetaData.getBackendVariantsConfig().getOptionsTableName() + " with id '" + variantId + "'"));
      }
      return record;
   }
}
