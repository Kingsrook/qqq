/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.metadata.personalization;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.personalization.TableMetaDataPersonalizerInput;
import com.kingsrook.qqq.backend.core.model.metadata.SupplementalCustomizerType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 * interface for objects that personalize TableMetaData for (user) actions.
 *
 * e.g., to hide fields from a subset of users - as the query action passes the
 * table being queried through this class, so removing a field from the table
 * should effectively hide such a field's data.
 *
 * An application's implementation class needs to be registered in a QInstance
 * via:
 * <code>
 * qInstance.addSupplementalCustomizer(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE,
 *    new QCodeReference(SomeImplementation.class));
 * </code>
 *******************************************************************************/
public interface TableMetaDataPersonalizerInterface
{
   /***************************************************************************
    * SupplementalCustomizerType reference used to refer to instances of this interface.
    ***************************************************************************/
   SupplementalCustomizerType CUSTOMIZER_TYPE = () -> TableMetaDataPersonalizerInterface.class;

   /***************************************************************************
    * It is vitally important that the {@link QTableMetaData} returned is a clone
    * if it has any changes, to avoid changing the meta data for the whole application!
    ***************************************************************************/
   QTableMetaData execute(TableMetaDataPersonalizerInput tableMetaDataPersonalizerInput) throws QException;

}
