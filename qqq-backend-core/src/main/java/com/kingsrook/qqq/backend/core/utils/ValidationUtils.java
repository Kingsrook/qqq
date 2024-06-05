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

package com.kingsrook.qqq.backend.core.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import org.apache.commons.validator.EmailValidator;


/*******************************************************************************
 **
 *******************************************************************************/
public class ValidationUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<String> parseAndValidateEmailAddresses(String emailAddresses) throws QUserFacingException
   {
      ////////////////////////////////////////////////////////////////
      // split email address string on spaces, comma, and semicolon //
      ////////////////////////////////////////////////////////////////
      List<String> toEmailAddressList = Arrays.asList(emailAddresses.split("[\\s,;]+"));

      //////////////////////////////////////////////////////
      // check each address keeping track of any bad ones //
      //////////////////////////////////////////////////////
      List<String>   invalidEmails = new ArrayList<>();
      EmailValidator validator     = EmailValidator.getInstance();
      for(String emailAddress : toEmailAddressList)
      {
         if(!validator.isValid(emailAddress))
         {
            invalidEmails.add(emailAddress);
         }
      }

      ///////////////////////////////////////
      // if bad one found, throw exception //
      ///////////////////////////////////////
      if(!invalidEmails.isEmpty())
      {
         throw (new QUserFacingException("The following email addresses were invalid: " + StringUtils.join(",", invalidEmails)));
      }

      return (toEmailAddressList);
   }

}
