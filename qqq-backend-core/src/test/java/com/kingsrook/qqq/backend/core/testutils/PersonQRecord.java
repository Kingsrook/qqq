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

package com.kingsrook.qqq.backend.core.testutils;


import java.math.BigDecimal;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class PersonQRecord extends QRecord
{
   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withLastName(String lastName)
   {
      setValue("lastName", lastName);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withFirstName(String firstName)
   {
      setValue("firstName", firstName);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withBirthDate(LocalDate birthDate)
   {
      setValue("birthDate", birthDate);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withNoOfShoes(Integer noOfShoes)
   {
      setValue("noOfShoes", noOfShoes);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withPrice(BigDecimal price)
   {
      setValue("price", price);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withCost(BigDecimal cost)
   {
      setValue("cost", cost);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public PersonQRecord withHomeStateId(int homeStateId)
   {
      setValue("homeStateId", homeStateId);
      return (this);
   }

}
