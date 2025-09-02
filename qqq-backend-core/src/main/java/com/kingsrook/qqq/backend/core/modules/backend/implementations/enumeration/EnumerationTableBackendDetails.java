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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.enumeration;


import com.kingsrook.qqq.backend.core.model.data.QRecordEnum;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;


/*******************************************************************************
 **
 *******************************************************************************/
public class EnumerationTableBackendDetails extends QTableBackendDetails
{
   private Class<? extends QRecordEnum> enumClass;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public EnumerationTableBackendDetails()
   {
      super();
      setBackendType(EnumerationBackendModule.class);
   }



   /*******************************************************************************
    ** Getter for enumClass
    **
    *******************************************************************************/
   public Class<? extends QRecordEnum> getEnumClass()
   {
      return enumClass;
   }



   /*******************************************************************************
    ** Setter for enumClass
    **
    *******************************************************************************/
   public void setEnumClass(Class<? extends QRecordEnum> enumClass)
   {
      this.enumClass = enumClass;
   }



   /*******************************************************************************
    ** Fluent setter for enumClass
    **
    *******************************************************************************/
   public EnumerationTableBackendDetails withEnumClass(Class<? extends QRecordEnum> enumClass)
   {
      this.enumClass = enumClass;
      return (this);
   }



   /***************************************************************************
    * finish the cloning operation started in the base class. copy all state
    * from the subclass into the input clone (which can be safely casted to
    * the subclass's type, as it was obtained by super.clone())
    ***************************************************************************/
   @Override
   protected QTableBackendDetails finishClone(QTableBackendDetails abstractClone)
   {
      EnumerationTableBackendDetails clone = (EnumerationTableBackendDetails) abstractClone;
      clone.enumClass = enumClass;
      return (clone);
   }

}
