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

package com.kingsrook.qqq.backend.core.model.actions.tables.aggregate;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 ** Output for an aggregate action
 **
 *******************************************************************************/
public class AggregateOutput extends AbstractActionOutput
{
   private List<AggregateResult> results;



   /*******************************************************************************
    ** Getter for results
    **
    *******************************************************************************/
   public List<AggregateResult> getResults()
   {
      return results;
   }



   /*******************************************************************************
    ** Setter for results
    **
    *******************************************************************************/
   public void setResults(List<AggregateResult> results)
   {
      this.results = results;
   }

}
