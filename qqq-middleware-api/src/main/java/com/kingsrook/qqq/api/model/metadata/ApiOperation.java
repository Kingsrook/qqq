/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.model.metadata;


import java.util.List;
import java.util.Set;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;


/*******************************************************************************
 **
 *******************************************************************************/
public enum ApiOperation
{
   QUERY_BY_QUERY_STRING(Capability.TABLE_QUERY),
   GET(Capability.TABLE_GET),
   INSERT(Capability.TABLE_INSERT),
   UPDATE(Capability.TABLE_UPDATE),
   DELETE(Capability.TABLE_DELETE),
   BULK_INSERT(Capability.TABLE_INSERT),
   BULK_UPDATE(Capability.TABLE_UPDATE),
   BULK_DELETE(Capability.TABLE_DELETE);


   private final Capability capability;



   /*******************************************************************************
    **
    *******************************************************************************/
   ApiOperation(Capability capability)
   {
      this.capability = capability;
   }



   /*******************************************************************************
    ** Getter for capability
    **
    *******************************************************************************/
   public Capability getCapability()
   {
      return capability;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean isOperationEnabled(List<EnabledOperationsProvider> enabledOperationsProviders)
   {
      /////////////////////////////
      // by default, assume yes. //
      /////////////////////////////
      boolean result = true;

      for(EnabledOperationsProvider enabledOperationsProvider : enabledOperationsProviders)
      {
         Boolean answerAtThisLevel = enabledOperationsProvider.getAnswer(this);
         if(answerAtThisLevel != null)
         {
            result = answerAtThisLevel;
         }
      }

      return (result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public interface EnabledOperationsProvider
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      Set<ApiOperation> getEnabledOperations();

      /*******************************************************************************
       **
       *******************************************************************************/
      Set<ApiOperation> getDisabledOperations();

      /*******************************************************************************
       **
       *******************************************************************************/
      default Boolean getAnswer(ApiOperation operation)
      {
         Boolean answer = null;
         if(getEnabledOperations() != null && getEnabledOperations().contains(operation))
         {
            answer = true;
         }
         if(getDisabledOperations() != null && getDisabledOperations().contains(operation))
         {
            answer = false;
         }
         return (answer);
      }
   }
}
