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

package com.kingsrook.qqq.backend.core.exceptions;


import java.util.List;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Exception thrown during qqq-starup, if a QInstance is found to have validation
 ** issues.  Contains a list of reasons (to avoid spoon-feeding as much as possible).
 **
 *******************************************************************************/
public class QInstanceValidationException extends QException
{
   private List<String> reasons;



   /*******************************************************************************
    ** Constructor of message - does not populate reasons!
    **
    *******************************************************************************/
   public QInstanceValidationException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    ** Constructor of a list of reasons.  They feed into the core exception message.
    **
    *******************************************************************************/
   public QInstanceValidationException(List<String> reasons)
   {
      super((CollectionUtils.nullSafeHasContents(reasons))
         ? "Instance validation failed for the following reasons:\n - " + StringUtils.join("\n - ", reasons) + "\n(" + reasons.size() + " Total reason" + StringUtils.plural(reasons) + ")"
         : "Validation failed, but no reasons were provided");

      if(CollectionUtils.nullSafeHasContents(reasons))
      {
         this.reasons = reasons;
      }
   }



   /*******************************************************************************
    ** Constructor of message & cause - does not populate reasons!
    **
    *******************************************************************************/
   public QInstanceValidationException(String message, Throwable cause)
   {
      super(message, cause);
   }



   /*******************************************************************************
    ** Getter for reasons
    **
    *******************************************************************************/
   public List<String> getReasons()
   {
      return reasons;
   }
}
