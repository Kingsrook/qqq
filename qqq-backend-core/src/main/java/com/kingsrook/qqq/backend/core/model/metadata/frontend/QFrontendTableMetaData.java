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

package com.kingsrook.qqq.backend.core.model.metadata.frontend;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 * Version of QTableMetaData that's meant for transmitting to a frontend.
 * e.g., it excludes backend-only details.
 *
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendTableMetaData
{
   private String  name;
   private String  label;
   private boolean isHidden;
   private String  primaryKeyField;

   private String iconName;

   private Map<String, QFrontendFieldMetaData> fields;
   private List<QFieldSection>                 sections;

   private List<QFrontendExposedJoin> exposedJoins;

   private Set<String> capabilities;

   private boolean readPermission;
   private boolean insertPermission;
   private boolean editPermission;
   private boolean deletePermission;

   //////////////////////////////////////////////////////////////////////////////////
   // do not add setters.  take values from the source-object in the constructor!! //
   //////////////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendTableMetaData(AbstractActionInput actionInput, QBackendMetaData backendForTable, QTableMetaData tableMetaData, boolean includeFields, boolean includeJoins)
   {
      this.name = tableMetaData.getName();
      this.label = tableMetaData.getLabel();
      this.isHidden = tableMetaData.getIsHidden();

      if(includeFields)
      {
         this.primaryKeyField = tableMetaData.getPrimaryKeyField();
         this.fields = new HashMap<>();
         for(Map.Entry<String, QFieldMetaData> entry : tableMetaData.getFields().entrySet())
         {
            this.fields.put(entry.getKey(), new QFrontendFieldMetaData(entry.getValue()));
         }

         this.sections = tableMetaData.getSections();
      }

      if(includeJoins)
      {
         QInstance qInstance = QContext.getQInstance();

         this.exposedJoins = new ArrayList<>();
         for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(tableMetaData.getExposedJoins()))
         {
            QFrontendExposedJoin frontendExposedJoin = new QFrontendExposedJoin();
            this.exposedJoins.add(frontendExposedJoin);

            QTableMetaData joinTable = qInstance.getTable(exposedJoin.getJoinTable());
            frontendExposedJoin.setLabel(exposedJoin.getLabel());
            frontendExposedJoin.setIsMany(exposedJoin.getIsMany());
            frontendExposedJoin.setJoinTable(new QFrontendTableMetaData(actionInput, backendForTable, joinTable, includeFields, false));
            for(String joinName : exposedJoin.getJoinPath())
            {
               frontendExposedJoin.addJoin(qInstance.getJoin(joinName));
            }
         }
      }

      if(tableMetaData.getIcon() != null)
      {
         this.iconName = tableMetaData.getIcon().getName();
      }

      setCapabilities(backendForTable, tableMetaData);

      readPermission = PermissionsHelper.hasTablePermission(actionInput, tableMetaData.getName(), TablePermissionSubType.READ);
      insertPermission = PermissionsHelper.hasTablePermission(actionInput, tableMetaData.getName(), TablePermissionSubType.INSERT);
      editPermission = PermissionsHelper.hasTablePermission(actionInput, tableMetaData.getName(), TablePermissionSubType.EDIT);
      deletePermission = PermissionsHelper.hasTablePermission(actionInput, tableMetaData.getName(), TablePermissionSubType.DELETE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setCapabilities(QBackendMetaData backend, QTableMetaData table)
   {
      Set<Capability> enabledCapabilities = new HashSet<>();
      for(Capability capability : Capability.values())
      {
         if(table.isCapabilityEnabled(backend, capability))
         {
            ///////////////////////////////////////
            // todo - check if user is allowed!! //
            ///////////////////////////////////////

            enabledCapabilities.add(capability);
         }
      }

      this.capabilities = enabledCapabilities.stream().map(Enum::name).collect(Collectors.toSet());
   }



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Getter for primaryKeyField
    **
    *******************************************************************************/
   public String getPrimaryKeyField()
   {
      return primaryKeyField;
   }



   /*******************************************************************************
    ** Getter for fields
    **
    *******************************************************************************/
   public Map<String, QFrontendFieldMetaData> getFields()
   {
      return fields;
   }



   /*******************************************************************************
    ** Getter for sections
    **
    *******************************************************************************/
   public List<QFieldSection> getSections()
   {
      return sections;
   }



   /*******************************************************************************
    ** Getter for isHidden
    **
    *******************************************************************************/
   public boolean getIsHidden()
   {
      return isHidden;
   }



   /*******************************************************************************
    ** Getter for iconName
    **
    *******************************************************************************/
   public String getIconName()
   {
      return iconName;
   }



   /*******************************************************************************
    ** Getter for capabilities
    **
    *******************************************************************************/
   public Set<String> getCapabilities()
   {
      return capabilities;
   }



   /*******************************************************************************
    ** Getter for readPermission
    **
    *******************************************************************************/
   public boolean getReadPermission()
   {
      return readPermission;
   }



   /*******************************************************************************
    ** Getter for insertPermission
    **
    *******************************************************************************/
   public boolean getInsertPermission()
   {
      return insertPermission;
   }



   /*******************************************************************************
    ** Getter for editPermission
    **
    *******************************************************************************/
   public boolean getEditPermission()
   {
      return editPermission;
   }



   /*******************************************************************************
    ** Getter for deletePermission
    **
    *******************************************************************************/
   public boolean getDeletePermission()
   {
      return deletePermission;
   }



   /*******************************************************************************
    ** Getter for exposedJoins
    **
    *******************************************************************************/
   public List<QFrontendExposedJoin> getExposedJoins()
   {
      return exposedJoins;
   }
}
