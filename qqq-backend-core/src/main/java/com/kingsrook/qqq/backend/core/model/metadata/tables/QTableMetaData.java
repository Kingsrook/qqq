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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntityField;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.QTableAutomationDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.cache.CacheOf;


/*******************************************************************************
 ** Meta-Data to define a table in a QQQ instance.
 **
 *******************************************************************************/
public class QTableMetaData implements QAppChildMetaData, Serializable, MetaDataWithPermissionRules
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
   private List<UniqueKey>             uniqueKeys;

   private List<RecordSecurityLock> recordSecurityLocks;
   private QPermissionRules         permissionRules;
   private QAuditRules              auditRules;

   private QTableBackendDetails    backendDetails;
   private QTableAutomationDetails automationDetails;

   private Map<String, QCodeReference> customizers;

   private QIcon icon;

   private String       recordLabelFormat;
   private List<String> recordLabelFields;

   private List<QFieldSection> sections;

   private List<AssociatedScript> associatedScripts;

   private Set<Capability> enabledCapabilities  = new HashSet<>();
   private Set<Capability> disabledCapabilities = new HashSet<>();

   private CacheOf cacheOf;



   /*******************************************************************************
    ** Default constructor.
    *******************************************************************************/
   public QTableMetaData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return ("QTableMetaData[" + name + "]");
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
    ** Getter for automationDetails
    **
    *******************************************************************************/
   public QTableAutomationDetails getAutomationDetails()
   {
      return automationDetails;
   }



   /*******************************************************************************
    ** Setter for automationDetails
    **
    *******************************************************************************/
   public void setAutomationDetails(QTableAutomationDetails automationDetails)
   {
      this.automationDetails = automationDetails;
   }



   /*******************************************************************************
    ** Fluent Setter for automationDetails
    **
    *******************************************************************************/
   public QTableMetaData withAutomationDetails(QTableAutomationDetails automationDetails)
   {
      this.automationDetails = automationDetails;
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
      return (Optional.ofNullable(function));
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
   public QTableMetaData withCustomizer(TableCustomizer tableCustomizer, QCodeReference customizer)
   {
      return (withCustomizer(tableCustomizer.getRole(), customizer));
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



   /*******************************************************************************
    ** Getter for recordLabelFormat
    **
    *******************************************************************************/
   public String getRecordLabelFormat()
   {
      return recordLabelFormat;
   }



   /*******************************************************************************
    ** Setter for recordLabelFormat
    **
    *******************************************************************************/
   public void setRecordLabelFormat(String recordLabelFormat)
   {
      this.recordLabelFormat = recordLabelFormat;
   }



   /*******************************************************************************
    ** Fluent setter for recordLabelFormat
    **
    *******************************************************************************/
   public QTableMetaData withRecordLabelFormat(String recordLabelFormat)
   {
      this.recordLabelFormat = recordLabelFormat;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordLabelFields
    **
    *******************************************************************************/
   public List<String> getRecordLabelFields()
   {
      return recordLabelFields;
   }



   /*******************************************************************************
    ** Setter for recordLabelFields
    **
    *******************************************************************************/
   public void setRecordLabelFields(List<String> recordLabelFields)
   {
      this.recordLabelFields = recordLabelFields;
   }



   /*******************************************************************************
    ** Fluent setter for recordLabelFields
    **
    *******************************************************************************/
   public QTableMetaData withRecordLabelFields(List<String> recordLabelFields)
   {
      this.recordLabelFields = recordLabelFields;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for recordLabelFields
    **
    *******************************************************************************/
   public QTableMetaData withRecordLabelFields(String... recordLabelFields)
   {
      this.recordLabelFields = Arrays.asList(recordLabelFields);
      return (this);
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
    ** Setter for sections
    **
    *******************************************************************************/
   public void setSections(List<QFieldSection> sections)
   {
      this.sections = sections;
   }



   /*******************************************************************************
    ** Fluent setter for sections
    **
    *******************************************************************************/
   public QTableMetaData withSections(List<QFieldSection> fieldSections)
   {
      this.sections = fieldSections;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addSection(QFieldSection fieldSection)
   {
      if(this.sections == null)
      {
         this.sections = new ArrayList<>();
      }
      this.sections.add(fieldSection);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withSection(QFieldSection fieldSection)
   {
      addSection(fieldSection);
      return (this);
   }



   /*******************************************************************************
    ** Getter for associatedScripts
    **
    *******************************************************************************/
   public List<AssociatedScript> getAssociatedScripts()
   {
      return associatedScripts;
   }



   /*******************************************************************************
    ** Setter for associatedScripts
    **
    *******************************************************************************/
   public void setAssociatedScripts(List<AssociatedScript> associatedScripts)
   {
      this.associatedScripts = associatedScripts;
   }



   /*******************************************************************************
    ** Fluent setter for associatedScripts
    **
    *******************************************************************************/
   public QTableMetaData withAssociatedScripts(List<AssociatedScript> associatedScripts)
   {
      this.associatedScripts = associatedScripts;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for associatedScripts
    **
    *******************************************************************************/
   public QTableMetaData withAssociatedScript(AssociatedScript associatedScript)
   {
      if(this.associatedScripts == null)
      {
         this.associatedScripts = new ArrayList();
      }
      this.associatedScripts.add(associatedScript);
      return (this);
   }



   /*******************************************************************************
    ** Getter for uniqueKeys
    **
    *******************************************************************************/
   public List<UniqueKey> getUniqueKeys()
   {
      return uniqueKeys;
   }



   /*******************************************************************************
    ** Setter for uniqueKeys
    **
    *******************************************************************************/
   public void setUniqueKeys(List<UniqueKey> uniqueKeys)
   {
      this.uniqueKeys = uniqueKeys;
   }



   /*******************************************************************************
    ** Fluent setter for uniqueKeys
    **
    *******************************************************************************/
   public QTableMetaData withUniqueKeys(List<UniqueKey> uniqueKeys)
   {
      this.uniqueKeys = uniqueKeys;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for uniqueKeys
    **
    *******************************************************************************/
   public QTableMetaData withUniqueKey(UniqueKey uniqueKey)
   {
      if(this.uniqueKeys == null)
      {
         this.uniqueKeys = new ArrayList<>();
      }
      this.uniqueKeys.add(uniqueKey);
      return (this);
   }



   /*******************************************************************************
    ** Fluently add a section and fields in that section.
    *******************************************************************************/
   public QTableMetaData withSectionOfFields(QFieldSection fieldSection, QFieldMetaData... fields)
   {
      withSection(fieldSection);

      List<String> fieldNames = new ArrayList<>();
      for(QFieldMetaData field : fields)
      {
         withField(field);
         fieldNames.add(field.getName());
      }

      fieldSection.setFieldNames(fieldNames);

      return (this);
   }



   /*******************************************************************************
    ** Getter for enabledCapabilities
    **
    *******************************************************************************/
   public Set<Capability> getEnabledCapabilities()
   {
      return enabledCapabilities;
   }



   /*******************************************************************************
    ** Setter for enabledCapabilities
    **
    *******************************************************************************/
   public void setEnabledCapabilities(Set<Capability> enabledCapabilities)
   {
      this.enabledCapabilities = enabledCapabilities;
   }



   /*******************************************************************************
    ** Fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withEnabledCapabilities(Set<Capability> enabledCapabilities)
   {
      this.enabledCapabilities = enabledCapabilities;
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withCapabilities(Set<Capability> enabledCapabilities)
   {
      this.enabledCapabilities = enabledCapabilities;
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for a single enabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withCapability(Capability capability)
   {
      if(this.enabledCapabilities == null)
      {
         this.enabledCapabilities = new HashSet<>();
      }
      this.enabledCapabilities.add(capability);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withCapabilities(Capability... enabledCapabilities)
   {
      if(this.enabledCapabilities == null)
      {
         this.enabledCapabilities = new HashSet<>();
      }
      this.enabledCapabilities.addAll(Arrays.stream(enabledCapabilities).toList());
      return (this);
   }



   /*******************************************************************************
    ** Getter for disabledCapabilities
    **
    *******************************************************************************/
   public Set<Capability> getDisabledCapabilities()
   {
      return disabledCapabilities;
   }



   /*******************************************************************************
    ** Setter for disabledCapabilities
    **
    *******************************************************************************/
   public void setDisabledCapabilities(Set<Capability> disabledCapabilities)
   {
      this.disabledCapabilities = disabledCapabilities;
   }



   /*******************************************************************************
    ** Fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withDisabledCapabilities(Set<Capability> disabledCapabilities)
   {
      this.disabledCapabilities = disabledCapabilities;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withoutCapabilities(Capability... disabledCapabilities)
   {
      if(this.disabledCapabilities == null)
      {
         this.disabledCapabilities = new HashSet<>();
      }
      this.disabledCapabilities.addAll(Arrays.stream(disabledCapabilities).toList());
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withoutCapabilities(Set<Capability> disabledCapabilities)
   {
      this.disabledCapabilities = disabledCapabilities;
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for a single disabledCapabilities
    **
    *******************************************************************************/
   public QTableMetaData withoutCapability(Capability capability)
   {
      if(this.disabledCapabilities == null)
      {
         this.disabledCapabilities = new HashSet<>();
      }
      this.disabledCapabilities.add(capability);
      return (this);
   }



   /*******************************************************************************
    ** Getter for cacheOf
    **
    *******************************************************************************/
   public CacheOf getCacheOf()
   {
      return cacheOf;
   }



   /*******************************************************************************
    ** Setter for cacheOf
    **
    *******************************************************************************/
   public void setCacheOf(CacheOf cacheOf)
   {
      this.cacheOf = cacheOf;
   }



   /*******************************************************************************
    ** Fluent setter for cacheOf
    **
    *******************************************************************************/
   public QTableMetaData withCacheOf(CacheOf cacheOf)
   {
      this.cacheOf = cacheOf;
      return (this);
   }



   /*******************************************************************************
    ** Test if a capability is enabled - checking both at the table level and
    ** at the backend level.
    **
    ** If backend says disabled, then disable - UNLESS - the table says enable.
    ** If backend either doesn't specify, or says enable, return what the table says (if it says).
    ** else, return the default (of enabled).
    *******************************************************************************/
   public boolean isCapabilityEnabled(QBackendMetaData backend, Capability capability)
   {
      ///////////////////////////////////////////////
      // by default, every table can do everything //
      ///////////////////////////////////////////////
      boolean hasCapability = true;

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if the table's backend says the capability is disabled, then by default, then the capability is disabled... //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(backend.getDisabledCapabilities().contains(capability))
      {
         hasCapability = false;

         /////////////////////////////////////////////////////////////////
         // unless the table overrides that and says that it IS enabled //
         /////////////////////////////////////////////////////////////////
         if(getEnabledCapabilities().contains(capability))
         {
            hasCapability = true;
         }
      }
      else
      {
         /////////////////////////////////////////////////////////////////////////////////////////
         // if the backend doesn't specify the capability, then disable it if the table says so //
         /////////////////////////////////////////////////////////////////////////////////////////
         if(getDisabledCapabilities().contains(capability))
         {
            hasCapability = false;
         }
      }

      return (hasCapability);
   }



   /*******************************************************************************
    ** Getter for recordSecurityLocks
    *******************************************************************************/
   public List<RecordSecurityLock> getRecordSecurityLocks()
   {
      return (this.recordSecurityLocks);
   }



   /*******************************************************************************
    ** Setter for recordSecurityLocks
    *******************************************************************************/
   public void setRecordSecurityLocks(List<RecordSecurityLock> recordSecurityLocks)
   {
      this.recordSecurityLocks = recordSecurityLocks;
   }



   /*******************************************************************************
    ** Fluent setter for recordSecurityLocks
    *******************************************************************************/
   public QTableMetaData withRecordSecurityLocks(List<RecordSecurityLock> recordSecurityLocks)
   {
      this.recordSecurityLocks = recordSecurityLocks;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for recordSecurityLocks
    *******************************************************************************/
   public QTableMetaData withRecordSecurityLock(RecordSecurityLock recordSecurityLock)
   {
      if(this.recordSecurityLocks == null)
      {
         this.recordSecurityLocks = new ArrayList<>();
      }
      this.recordSecurityLocks.add(recordSecurityLock);
      return (this);
   }



   /*******************************************************************************
    ** Getter for permissionRules
    *******************************************************************************/
   public QPermissionRules getPermissionRules()
   {
      return (this.permissionRules);
   }



   /*******************************************************************************
    ** Setter for permissionRules
    *******************************************************************************/
   public void setPermissionRules(QPermissionRules permissionRules)
   {
      this.permissionRules = permissionRules;
   }



   /*******************************************************************************
    ** Fluent setter for permissionRules
    *******************************************************************************/
   public QTableMetaData withPermissionRules(QPermissionRules permissionRules)
   {
      this.permissionRules = permissionRules;
      return (this);
   }



   /*******************************************************************************
    ** Getter for auditRules
    *******************************************************************************/
   public QAuditRules getAuditRules()
   {
      return (this.auditRules);
   }



   /*******************************************************************************
    ** Setter for auditRules
    *******************************************************************************/
   public void setAuditRules(QAuditRules auditRules)
   {
      this.auditRules = auditRules;
   }



   /*******************************************************************************
    ** Fluent setter for auditRules
    *******************************************************************************/
   public QTableMetaData withAuditRules(QAuditRules auditRules)
   {
      this.auditRules = auditRules;
      return (this);
   }

}
