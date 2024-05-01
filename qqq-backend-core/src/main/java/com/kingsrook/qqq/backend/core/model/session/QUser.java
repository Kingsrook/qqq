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

package com.kingsrook.qqq.backend.core.model.session;


/*******************************************************************************
 **
 *******************************************************************************/
public class QUser implements Cloneable
{
   private String idReference;
   private String fullName;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QUser clone() throws CloneNotSupportedException
   {
      return (QUser) super.clone();
   }



   /*******************************************************************************
    ** Getter for idReference
    **
    *******************************************************************************/
   public String getIdReference()
   {
      return idReference;
   }



   /*******************************************************************************
    ** Setter for idReference
    **
    *******************************************************************************/
   public void setIdReference(String idReference)
   {
      this.idReference = idReference;
   }



   /*******************************************************************************
    ** Getter for fullName
    **
    *******************************************************************************/
   public String getFullName()
   {
      return fullName;
   }



   /*******************************************************************************
    ** Setter for fullName
    **
    *******************************************************************************/
   public void setFullName(String fullName)
   {
      this.fullName = fullName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return ("QUser{" + idReference + "," + fullName + "}");
   }

   /*******************************************************************************
    ** Fluent setter for idReference
    *******************************************************************************/
   public QUser withIdReference(String idReference)
   {
      this.idReference = idReference;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for fullName
    *******************************************************************************/
   public QUser withFullName(String fullName)
   {
      this.fullName = fullName;
      return (this);
   }


}
