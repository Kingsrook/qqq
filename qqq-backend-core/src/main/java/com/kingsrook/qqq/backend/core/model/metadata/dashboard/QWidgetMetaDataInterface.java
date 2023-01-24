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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;


/*******************************************************************************
 ** Interface for qqq widget meta data
 **
 *******************************************************************************/
public interface QWidgetMetaDataInterface extends MetaDataWithPermissionRules
{
   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   String getName();

   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   void setName(String name);

   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   QWidgetMetaDataInterface withName(String name);

   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   String getLabel();

   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   void setLabel(String label);

   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   QWidgetMetaDataInterface withLabel(String label);

   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   String getType();

   /*******************************************************************************
    ** Setter for type
    *******************************************************************************/
   void setType(String type);

   /*******************************************************************************
    ** Fluent setter for type
    *******************************************************************************/
   QWidgetMetaDataInterface withType(String type);

   /*******************************************************************************
    ** Getter for gridColumns
    *******************************************************************************/
   Integer getGridColumns();

   /*******************************************************************************
    ** Setter for gridColumns
    *******************************************************************************/
   void setGridColumns(Integer gridColumns);

   /*******************************************************************************
    ** Fluent setter for gridColumns
    *******************************************************************************/
   QWidgetMetaDataInterface withGridColumns(Integer gridColumns);

   /*******************************************************************************
    ** Getter for codeReference
    *******************************************************************************/
   QCodeReference getCodeReference();

   /*******************************************************************************
    ** Setter for codeReference
    *******************************************************************************/
   void setCodeReference(QCodeReference codeReference);

   /*******************************************************************************
    ** Fluent setter for codeReference
    *******************************************************************************/
   QWidgetMetaDataInterface withCodeReference(QCodeReference codeReference);

   /*******************************************************************************
    ** Getter for icon
    *******************************************************************************/
   String getIcon();

   /*******************************************************************************
    ** Setter for icon
    *******************************************************************************/
   void setIcon(String type);

   /*******************************************************************************
    ** Getter for isCard
    *******************************************************************************/
   boolean getIsCard();

   /*******************************************************************************
    ** Setter for isCard
    *******************************************************************************/
   void setIsCard(boolean isCard);


   /*******************************************************************************
    ** Getter for storeDropdownSelections
    *******************************************************************************/
   boolean getStoreDropdownSelections();

   /*******************************************************************************
    ** Setter for storeDropdownSelections
    *******************************************************************************/
   void setStoreDropdownSelections(boolean storeDropdownSelections);

   /*******************************************************************************
    ** Getter for defaultValues
    *******************************************************************************/
   Map<String, Serializable> getDefaultValues();

   /*******************************************************************************
    ** Setter for defaultValues
    *******************************************************************************/
   void setDefaultValues(Map<String, Serializable> defaultValues);

   /*******************************************************************************
    ** Fluent setter for defaultValues
    *******************************************************************************/
   QWidgetMetaData withDefaultValues(Map<String, Serializable> defaultValues);

   /*******************************************************************************
    ** Fluent setter for a single defaultValue
    *******************************************************************************/
   QWidgetMetaData withDefaultValue(String key, Serializable value);


   /*******************************************************************************
    ** Getter for permissionRules
    *******************************************************************************/
   QPermissionRules getPermissionRules();


   /*******************************************************************************
    ** Setter for permissionRules
    *******************************************************************************/
   void setPermissionRules(QPermissionRules permissionRules);

}

