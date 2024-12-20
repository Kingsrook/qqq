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

package com.kingsrook.qqq.backend.core.model.actions.values;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;


/*******************************************************************************
 ** Output for the Search possible value source action
 *******************************************************************************/
public class SearchPossibleValueSourceOutput extends AbstractActionOutput
{
   private List<QPossibleValue<?>> results = new ArrayList<>();

   private String warning;


   /*******************************************************************************
    **
    *******************************************************************************/
   public SearchPossibleValueSourceOutput()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addResult(QPossibleValue<?> possibleValue)
   {
      results.add(possibleValue);
   }



   /*******************************************************************************
    ** Getter for results
    **
    *******************************************************************************/
   public List<QPossibleValue<?>> getResults()
   {
      return results;
   }



   /*******************************************************************************
    ** Setter for results
    **
    *******************************************************************************/
   public void setResults(List<QPossibleValue<?>> results)
   {
      this.results = results;
   }



   /*******************************************************************************
    ** Fluent setter for results
    **
    *******************************************************************************/
   public SearchPossibleValueSourceOutput withResults(List<QPossibleValue<?>> results)
   {
      this.results = results;
      return (this);
   }


   /*******************************************************************************
    ** Getter for warning
    *******************************************************************************/
   public String getWarning()
   {
      return (this.warning);
   }



   /*******************************************************************************
    ** Setter for warning
    *******************************************************************************/
   public void setWarning(String warning)
   {
      this.warning = warning;
   }



   /*******************************************************************************
    ** Fluent setter for warning
    *******************************************************************************/
   public SearchPossibleValueSourceOutput withWarning(String warning)
   {
      this.warning = warning;
      return (this);
   }


}
