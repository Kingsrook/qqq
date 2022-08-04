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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntityField;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


/*******************************************************************************
 ** Meta-Data to define a table in a QQQ instance.
 **
 *******************************************************************************/
public class QTableMetaData implements QAppChildMetaData, Serializable
{
   private String name;
   private String label;

   // TODO:  resolve confusion over:
   //    Is this name of what backend the table is stored in (yes)
   //    Or the "name" of the table WITHIN the backend (no)
   //       although that's how "backendName" is used in QFieldMetaData.
   //    Idea:
   //       rename "backendName" here to "backend"
   //       add "nameInBackend" (or similar) for the table name in the backend
   //       OR - add a whole "backendDetails" object, with different details per backend-type
   private String  backendName;
   private String  primaryKeyField;
   private boolean isHidden = false;

   private Map<String, QFieldMetaData> fields;

   private QTableBackendDetails backendDetails;

   private Map<String, QCodeReference> customizers;

   private String parentAppName;
   private QIcon  icon;


   /*******************************************************************************
    ** Default constructor.
    *******************************************************************************/
   public QTableMetaData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData getField(String fieldName)
   {
      if(fields == null)
      {
         throw (new IllegalArgumentException("Table [" + name + "] does not have its fields defined."));
      }

      QFieldMetaData field = getFields().get(fieldName);
      if(field == null)
      {
         throw (new IllegalArgumentException("Field [" + fieldName + "] was not found in table [" + name + "]."));
      }

      return (field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withFieldsFromEntity(Class<? extends QRecordEntity> entityClass) throws QException
   {
      List<QRecordEntityField> recordEntityFieldList = QRecordEntity.getFieldList(entityClass);
      for(QRecordEntityField recordEntityField : recordEntityFieldList)
      {
         QFieldMetaData field = new QFieldMetaData(recordEntityField.getGetter());
         addField(field);
      }
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendName
    **
    *******************************************************************************/
   public String getBackendName()
   {
      return backendName;
   }



   /*******************************************************************************
    ** Setter for backendName
    **
    *******************************************************************************/
   public void setBackendName(String backendName)
   {
      this.backendName = backendName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getPrimaryKeyField()
   {
      return primaryKeyField;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setPrimaryKeyField(String primaryKeyField)
   {
      this.primaryKeyField = primaryKeyField;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withPrimaryKeyField(String primaryKeyField)
   {
      this.primaryKeyField = primaryKeyField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isHidden
    **
    *******************************************************************************/
   public boolean getIsHidden()
   {
      return (isHidden);
   }



   /*******************************************************************************
    ** Setter for isHidden
    **
    *******************************************************************************/
   public void setIsHidden(boolean isHidden)
   {
      this.isHidden = isHidden;
   }



   /*******************************************************************************
    ** Fluent Setter for isHidden
    **
    *******************************************************************************/
   public QTableMetaData withIsHidden(boolean isHidden)
   {
      this.isHidden = isHidden;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, QFieldMetaData> getFields()
   {
      return fields;
   }



   /*******************************************************************************
    ** Setter for fields
    **
    *******************************************************************************/
   public void setFields(Map<String, QFieldMetaData> fields)
   {
      this.fields = fields;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withFields(Map<String, QFieldMetaData> fields)
   {
      this.fields = fields;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withFields(List<QFieldMetaData> fields)
   {
      this.fields = new LinkedHashMap<>();
      for(QFieldMetaData field : fields)
      {
         this.addField(field);
      }
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new LinkedHashMap<>();
      }

      if(this.fields.containsKey(field.getName()))
      {
         throw (new IllegalArgumentException("Attempt to add a second field with name [" + field.getName() + "] to table [" + name + "]."));
      }

      this.fields.put(field.getName(), field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new LinkedHashMap<>();
      }
      this.fields.put(field.getName(), field);
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendDetails
    **
    *******************************************************************************/
   public QTableBackendDetails getBackendDetails()
   {
      return backendDetails;
   }



   /*******************************************************************************
    ** Setter for backendDetails
    **
    *******************************************************************************/
   public void setBackendDetails(QTableBackendDetails backendDetails)
   {
      this.backendDetails = backendDetails;
   }



   /*******************************************************************************
    ** Fluent Setter for backendDetails
    **
    *******************************************************************************/
   public QTableMetaData withBackendDetails(QTableBackendDetails backendDetails)
   {
      this.backendDetails = backendDetails;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Optional<QCodeReference> getCustomizer(String customizerName)
   {
      if(customizers == null)
      {
         return (Optional.empty());
      }

      QCodeReference function = customizers.get(customizerName);
      if(function == null)
      {
         throw (new IllegalArgumentException("Customizer  [" + customizerName + "] was not found in table [" + name + "]."));
      }

      return (Optional.of(function));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, QCodeReference> getCustomizers()
   {
      return customizers;
   }



   /*******************************************************************************
    ** Setter for customizers
    **
    *******************************************************************************/
   public void setCustomizers(Map<String, QCodeReference> customizers)
   {
      this.customizers = customizers;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withCustomizer(String role, QCodeReference customizer)
   {
      if(this.customizers == null)
      {
         this.customizers = new HashMap<>();
      }
      // todo - check for dupes?
      this.customizers.put(role, customizer);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withCustomizers(Map<String, QCodeReference> customizers)
   {
      this.customizers = customizers;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withInferredFieldBackendNames()
   {
      QInstanceEnricher.setInferredFieldBackendNames(this);
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentAppName
    **
    *******************************************************************************/
   @Override
   public String getParentAppName()
   {
      return parentAppName;
   }



   /*******************************************************************************
    ** Setter for parentAppName
    **
    *******************************************************************************/
   @Override
   public void setParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
   }



   /*******************************************************************************
    ** Getter for icon
    **
    *******************************************************************************/
   public QIcon getIcon()
   {
      return icon;
   }



   /*******************************************************************************
    ** Setter for icon
    **
    *******************************************************************************/
   public void setIcon(QIcon icon)
   {
      this.icon = icon;
   }


   /*******************************************************************************
    ** Fluent setter for icon
    **
    *******************************************************************************/
   public QTableMetaData withIcon(QIcon icon)
   {
      this.icon = icon;
      return (this);
   }

}
