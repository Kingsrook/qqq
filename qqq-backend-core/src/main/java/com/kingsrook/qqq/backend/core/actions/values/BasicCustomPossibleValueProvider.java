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

package com.kingsrook.qqq.backend.core.actions.values;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;


/*******************************************************************************
 ** Basic implementation of a possible value provider, for where there's a limited
 ** set of possible source objects - so you just have to define how to make one
 ** PV from a source object, how to list all of the source objects, and how to
 ** look up a PV from an id.
 *******************************************************************************/
public abstract class BasicCustomPossibleValueProvider<S, ID extends Serializable> implements QCustomPossibleValueProvider<ID>
{

   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract QPossibleValue<ID> makePossibleValue(S sourceObject);

   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract S getSourceObject(Serializable id) throws QException;

   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract List<S> getAllSourceObjects() throws QException;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValue<ID> getPossibleValue(Serializable idValue) throws QException
   {
      S sourceObject = getSourceObject(idValue);
      if(sourceObject == null)
      {
         return (null);
      }

      return makePossibleValue(sourceObject);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QPossibleValue<ID>> search(SearchPossibleValueSourceInput input) throws QException
   {
      List<QPossibleValue<ID>> allPossibleValues = new ArrayList<>();
      List<S>                  allSourceObjects  = getAllSourceObjects();
      for(S sourceObject : allSourceObjects)
      {
         allPossibleValues.add(makePossibleValue(sourceObject));
      }

      return completeCustomPVSSearch(input, allPossibleValues);
   }
}
