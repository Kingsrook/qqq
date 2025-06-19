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

package com.kingsrook.qqq.backend.core.actions.values;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Interface to be implemented by user-defined code that serves as the backing
 ** for a CUSTOM type possibleValueSource
 **
 ** Type parameter `T` is the id-type of the possible value.
 *******************************************************************************/
public interface QCustomPossibleValueProvider<T extends Serializable>
{
   /*******************************************************************************
    **
    *******************************************************************************/
   QPossibleValue<T> getPossibleValue(Serializable idValue) throws QException;

   /*******************************************************************************
    **
    *******************************************************************************/
   List<QPossibleValue<T>> search(SearchPossibleValueSourceInput input) throws QException;


   /*******************************************************************************
    ** The input list of ids might come through as a type that isn't the same as
    ** the type of the ids in the enum (e.g., strings from a frontend, integers
    ** in an enum).  So, this method looks maps a list of input ids to the requested type.
    *******************************************************************************/
   default List<T> convertInputIdsToIdType(Class<T> type, List<Serializable> inputIdList)
   {
      List<T> rs = new ArrayList<>();
      if(CollectionUtils.nullSafeIsEmpty(inputIdList))
      {
         return (rs);
      }

      for(Serializable serializable : inputIdList)
      {
         rs.add(ValueUtils.getValueAsType(type, serializable));
      }

      return (rs);
   }


   /***************************************************************************
    ** meant to be protected (but interface...) - for a custom PVS implementation
    ** to complete its search (e.g., after it generates the list of PVS objects,
    ** let this method do the filtering).
    ***************************************************************************/
   default List<QPossibleValue<T>> completeCustomPVSSearch(SearchPossibleValueSourceInput input, List<QPossibleValue<T>> possibleValues)
   {
      SearchPossibleValueSourceAction.PreparedSearchPossibleValueSourceInput preparedInput = SearchPossibleValueSourceAction.prepareSearchPossibleValueSourceInput(input);

      List<QPossibleValue<T>> rs = new ArrayList<>();

      for(QPossibleValue<T> possibleValue : possibleValues)
      {
         if(possibleValue != null && SearchPossibleValueSourceAction.doesPossibleValueMatchSearchInput(possibleValue, preparedInput))
         {
            rs.add(possibleValue);
         }
      }

      rs.sort(Comparator.nullsLast(Comparator.comparing((QPossibleValue<T> pv) -> pv.getLabel())));

      return (rs);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default boolean doesPossibleValueMatchSearchInput(List<T> idsInType, QPossibleValue<T> possibleValue, SearchPossibleValueSourceInput input)
   {
      boolean match = false;
      if(input.getIdList() != null)
      {
         if(idsInType.contains(possibleValue.getId()))
         {
            match = true;
         }
      }
      else
      {
         if(StringUtils.hasContent(input.getSearchTerm()))
         {
            match = possibleValue.getLabel().toLowerCase().startsWith(input.getSearchTerm().toLowerCase());
         }
         else
         {
            match = true;
         }
      }
      return match;
   }
}
