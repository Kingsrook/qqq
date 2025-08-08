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

package com.kingsrook.qqq.backend.core.model.metadata.code;


import java.util.Objects;


/*******************************************************************************
 ** Specialized type of QCodeReference that takes a lambda function object.
 **
 ** Originally intended for more concise setup of backend steps in tests - but,
 ** may be generally useful.
 *******************************************************************************/
public class QCodeReferenceLambda<T> extends QCodeReference
{
   private final T lambda;



   /***************************************************************************
    **
    ***************************************************************************/
   public QCodeReferenceLambda(T lambda)
   {
      this.lambda = lambda;
      this.setCodeType(QCodeType.JAVA);
      this.setName("[Lambda:" + lambda.toString() + "]");
   }



   /*******************************************************************************
    ** Getter for lambda
    **
    *******************************************************************************/
   public T getLambda()
   {
      return lambda;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(o == null || getClass() != o.getClass())
      {
         return false;
      }
      if(!super.equals(o))
      {
         return false;
      }
      QCodeReferenceLambda<?> that = (QCodeReferenceLambda<?>) o;
      return Objects.equals(lambda, that.lambda);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(super.hashCode(), lambda);
   }
}
