/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.scheduledjobs;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QSchedulerMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SchedulersPossibleValueSource implements QCustomPossibleValueProvider<String>
{
   public static final String NAME = "schedulers";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QPossibleValue<String> getPossibleValue(Serializable idValue)
   {
      QSchedulerMetaData scheduler = QContext.getQInstance().getScheduler(String.valueOf(idValue));
      if(scheduler != null)
      {
         return schedulerToPossibleValue(scheduler);
      }

      return null;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QPossibleValue<String>> search(SearchPossibleValueSourceInput input) throws QException
   {
      List<QPossibleValue<String>> rs = new ArrayList<>();
      for(QSchedulerMetaData scheduler : CollectionUtils.nonNullMap(QContext.getQInstance().getSchedulers()).values())
      {
         rs.add(schedulerToPossibleValue(scheduler));
      }
      return rs;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QPossibleValue<String> schedulerToPossibleValue(QSchedulerMetaData scheduler)
   {
      return new QPossibleValue<>(scheduler.getName(), scheduler.getName());
   }

}
