/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.frontend;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QVirtualFieldMetaData;


/*******************************************************************************
 * Frontend-facing representation of a {@link com.kingsrook.qqq.backend.core.model.metadata.fields.QVirtualFieldMetaData},
 * extending {@link QFrontendFieldMetaData} with additional flags that indicate
 * whether the virtual field can be used as a query filter criterion and/or
 * included in query output selections.
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendVirtualFieldMetaData extends QFrontendFieldMetaData
{
   private boolean isQueryCriteria;
   private boolean isQuerySelectable;

   /*******************************************************************************
    ** Constructor
    *******************************************************************************/
   public QFrontendVirtualFieldMetaData(QVirtualFieldMetaData fieldMetaData)
   {
      super(fieldMetaData);

      this.isQueryCriteria = fieldMetaData.getIsQueryCriteria();
      this.isQuerySelectable = fieldMetaData.getIsQuerySelectable();
   }



   /*******************************************************************************
    ** Getter for isQuerySelectable
    **
    *******************************************************************************/
   public boolean getIsQuerySelectable()
   {
      return isQuerySelectable;
   }



   /*******************************************************************************
    ** Getter for isQueryCriteria
    **
    *******************************************************************************/
   public boolean getIsQueryCriteria()
   {
      return isQueryCriteria;
   }

}
