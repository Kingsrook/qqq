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

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.serialization.QBackendMetaDataDeserializer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.variants.BackendVariantsConfig;
import com.kingsrook.qqq.backend.core.model.metadata.variants.LegacyBackendVariantSetting;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Meta-data to provide details of a backend (e.g., RDBMS instance, S3 buckets,
 ** NoSQL table, etc) within a qqq instance
 **
 *******************************************************************************/
@JsonDeserialize(using = QBackendMetaDataDeserializer.class)
public class QBackendMetaData implements TopLevelMetaDataInterface
{
   private String name;
   private String backendType;

   private Set<Capability> enabledCapabilities  = new HashSet<>();
   private Set<Capability> disabledCapabilities = new HashSet<>();

   private Boolean               usesVariants = false;
   private BackendVariantsConfig backendVariantsConfig;

   // todo - at some point, we may want to apply this to secret properties on subclasses?
   // @JsonFilter("secretsFilter")

   @Deprecated(since = "Replaced by filter in backendVariantsConfig - but leaving as field to pair with ...TypeValue for building filter")
   private String variantOptionsTableTypeField; // a field on which to filter the variant-options table, to limit which records in it are available as variants

   @Deprecated(since = "Replaced by variantTypeKey and value in filter in backendVariantsConfig - but leaving as field to pair with ...TypeField for building filter")
   private String variantOptionsTableTypeValue; // value for the type-field, to limit which records in it are available as variants; but also, the key in the session.backendVariants map!



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public QBackendMetaData()
   {
      /////////////////////////////////////////////////////////////////////////////
      // by default, we will turn off the query stats capability on all backends //
      /////////////////////////////////////////////////////////////////////////////
      withoutCapability(Capability.QUERY_STATS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean requiresPrimaryKeyOnTables()
   {
      return (true);
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
    ** Fluent setter, returning generically, to help sub-class fluent flows
    *******************************************************************************/
   public QBackendMetaData withName(String name)
   {
      this.name = name;
      return this;
   }



   /*******************************************************************************
    ** Getter for backendType
    **
    *******************************************************************************/
   public String getBackendType()
   {
      return backendType;
   }



   /*******************************************************************************
    ** Setter for backendType
    **
    *******************************************************************************/
   public void setBackendType(String backendType)
   {
      this.backendType = backendType;
   }



   /*******************************************************************************
    ** Setter for backendType
    **
    *******************************************************************************/
   public void setBackendType(Class<? extends QBackendModuleInterface> backendModuleClass)
   {
      try
      {
         QBackendModuleInterface qBackendModuleInterface = backendModuleClass.getConstructor().newInstance();
         this.backendType = qBackendModuleInterface.getBackendType();
      }
      catch(Exception e)
      {
         throw new IllegalArgumentException("Error dynamically getting backend type (name) from class [" + backendModuleClass.getName() + "], e)");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendMetaData withBackendType(Class<? extends QBackendModuleInterface> backendModuleClass)
   {
      setBackendType(backendModuleClass);
      return (this);
   }



   /*******************************************************************************
    ** Called by the QInstanceEnricher - to do backend-type-specific enrichments.
    ** Original use case is:  reading secrets into fields (e.g., passwords).
    *******************************************************************************/
   public void enrich()
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
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
      if(this.disabledCapabilities != null)
      {
         this.disabledCapabilities.removeAll(enabledCapabilities);
      }
   }



   /*******************************************************************************
    ** Fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withEnabledCapabilities(Set<Capability> enabledCapabilities)
   {
      setEnabledCapabilities(enabledCapabilities);
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withCapabilities(Set<Capability> enabledCapabilities)
   {
      for(Capability enabledCapability : enabledCapabilities)
      {
         withCapability(enabledCapability);
      }
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for a single enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withCapability(Capability capability)
   {
      if(this.enabledCapabilities == null)
      {
         this.enabledCapabilities = new HashSet<>();
      }
      this.enabledCapabilities.add(capability);
      this.disabledCapabilities.remove(capability);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withCapabilities(Capability... enabledCapabilities)
   {
      for(Capability enabledCapability : enabledCapabilities)
      {
         withCapability(enabledCapability);
      }
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
      if(this.enabledCapabilities != null)
      {
         this.enabledCapabilities.removeAll(disabledCapabilities);
      }
   }



   /*******************************************************************************
    ** Fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withDisabledCapabilities(Set<Capability> disabledCapabilities)
   {
      setDisabledCapabilities(disabledCapabilities);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withoutCapabilities(Capability... disabledCapabilities)
   {
      for(Capability disabledCapability : disabledCapabilities)
      {
         withoutCapability(disabledCapability);
      }
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withoutCapabilities(Set<Capability> disabledCapabilities)
   {
      for(Capability disabledCapability : disabledCapabilities)
      {
         withCapability(disabledCapability);
      }
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for a single disabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withoutCapability(Capability capability)
   {
      if(this.disabledCapabilities == null)
      {
         this.disabledCapabilities = new HashSet<>();
      }
      this.disabledCapabilities.add(capability);
      this.enabledCapabilities.remove(capability);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void performValidation(QInstanceValidator qInstanceValidator)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    ** Getter for usesVariants
    *******************************************************************************/
   public Boolean getUsesVariants()
   {
      return (this.usesVariants);
   }



   /*******************************************************************************
    ** Setter for usesVariants
    *******************************************************************************/
   public void setUsesVariants(Boolean usesVariants)
   {
      this.usesVariants = usesVariants;
   }



   /*******************************************************************************
    ** Fluent setter for usesVariants
    *******************************************************************************/
   public QBackendMetaData withUsesVariants(Boolean usesVariants)
   {
      this.usesVariants = usesVariants;
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableIdField
    *******************************************************************************/
   @Deprecated(since = "backendVariantsConfig will infer this from the variant options table's primary key")
   public void setVariantOptionsTableIdField(String variantOptionsTableIdField)
   {
      /////////////////////////////////////////////////
      // noop as we migrate to backendVariantsConfig //
      /////////////////////////////////////////////////
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableIdField
    *******************************************************************************/
   @Deprecated(since = "backendVariantsConfig will infer this from the variant options table's primary key")
   public QBackendMetaData withVariantOptionsTableIdField(String variantOptionsTableIdField)
   {
      this.setVariantOptionsTableIdField(variantOptionsTableIdField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableNameField
    *******************************************************************************/
   @Deprecated(since = "backendVariantsConfig will infer this from the variant options table's recordLabel")
   public void setVariantOptionsTableNameField(String variantOptionsTableNameField)
   {
      /////////////////////////////////////////////////
      // noop as we migrate to backendVariantsConfig //
      /////////////////////////////////////////////////
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableNameField
    *******************************************************************************/
   @Deprecated(since = "backendVariantsConfig will infer this from the variant options table's recordLabel")
   public QBackendMetaData withVariantOptionsTableNameField(String variantOptionsTableNameField)
   {
      this.setVariantOptionsTableNameField(variantOptionsTableNameField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableTypeField
    *******************************************************************************/
   @Deprecated(since = "Replaced by fieldName in filter in backendVariantsConfig - but leaving as field to pair with ...TypeValue for building filter")
   public void setVariantOptionsTableTypeField(String variantOptionsTableTypeField)
   {
      this.variantOptionsTableTypeField = variantOptionsTableTypeField;
      if(this.variantOptionsTableTypeValue != null)
      {
         this.getOrWithNewBackendVariantsConfig().setOptionsFilter(new QQueryFilter(new QFilterCriteria(variantOptionsTableTypeField, QCriteriaOperator.EQUALS, variantOptionsTableTypeValue)));
      }
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableTypeField
    *******************************************************************************/
   @Deprecated(since = "Replaced by fieldName in filter in backendVariantsConfig - but leaving as field to pair with ...TypeValue for building filter")
   public QBackendMetaData withVariantOptionsTableTypeField(String variantOptionsTableTypeField)
   {
      this.setVariantOptionsTableTypeField(variantOptionsTableTypeField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableTypeValue
    *******************************************************************************/
   @Deprecated(since = "Replaced by variantTypeKey and value in filter in backendVariantsConfig - but leaving as field to pair with ...TypeField for building filter")
   public void setVariantOptionsTableTypeValue(String variantOptionsTableTypeValue)
   {
      this.getOrWithNewBackendVariantsConfig().setVariantTypeKey(variantOptionsTableTypeValue);

      this.variantOptionsTableTypeValue = variantOptionsTableTypeValue;
      if(this.variantOptionsTableTypeField != null)
      {
         this.getOrWithNewBackendVariantsConfig().setOptionsFilter(new QQueryFilter(new QFilterCriteria(variantOptionsTableTypeField, QCriteriaOperator.EQUALS, variantOptionsTableTypeValue)));
      }
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableTypeValue
    *******************************************************************************/
   @Deprecated(since = "Replaced by variantTypeKey and value in filter in backendVariantsConfig - but leaving as field to pair with ...TypeField for building filter")
   public QBackendMetaData withVariantOptionsTableTypeValue(String variantOptionsTableTypeValue)
   {
      this.setVariantOptionsTableTypeValue(variantOptionsTableTypeValue);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableUsernameField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public void setVariantOptionsTableUsernameField(String variantOptionsTableUsernameField)
   {
      this.getOrWithNewBackendVariantsConfig().withBackendSettingSourceFieldName(LegacyBackendVariantSetting.USERNAME, variantOptionsTableUsernameField);
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableUsernameField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public QBackendMetaData withVariantOptionsTableUsernameField(String variantOptionsTableUsernameField)
   {
      this.setVariantOptionsTableUsernameField(variantOptionsTableUsernameField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTablePasswordField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public void setVariantOptionsTablePasswordField(String variantOptionsTablePasswordField)
   {
      this.getOrWithNewBackendVariantsConfig().withBackendSettingSourceFieldName(LegacyBackendVariantSetting.PASSWORD, variantOptionsTablePasswordField);
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTablePasswordField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public QBackendMetaData withVariantOptionsTablePasswordField(String variantOptionsTablePasswordField)
   {
      this.setVariantOptionsTablePasswordField(variantOptionsTablePasswordField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableApiKeyField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public void setVariantOptionsTableApiKeyField(String variantOptionsTableApiKeyField)
   {
      this.getOrWithNewBackendVariantsConfig().withBackendSettingSourceFieldName(LegacyBackendVariantSetting.API_KEY, variantOptionsTableApiKeyField);
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableApiKeyField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public QBackendMetaData withVariantOptionsTableApiKeyField(String variantOptionsTableApiKeyField)
   {
      this.setVariantOptionsTableApiKeyField(variantOptionsTableApiKeyField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableName
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.tableName")
   public void setVariantOptionsTableName(String variantOptionsTableName)
   {
      this.getOrWithNewBackendVariantsConfig().withOptionsTableName(variantOptionsTableName);
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableName
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.tableName")
   public QBackendMetaData withVariantOptionsTableName(String variantOptionsTableName)
   {
      this.setVariantOptionsTableName(variantOptionsTableName);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void addSelfToInstance(QInstance qInstance)
   {
      qInstance.addBackend(this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableClientIdField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public void setVariantOptionsTableClientIdField(String variantOptionsTableClientIdField)
   {
      this.getOrWithNewBackendVariantsConfig().withBackendSettingSourceFieldName(LegacyBackendVariantSetting.CLIENT_ID, variantOptionsTableClientIdField);
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableClientIdField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public QBackendMetaData withVariantOptionsTableClientIdField(String variantOptionsTableClientIdField)
   {
      this.setVariantOptionsTableClientIdField(variantOptionsTableClientIdField);
      return (this);
   }



   /*******************************************************************************
    ** Setter for variantOptionsTableClientSecretField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public void setVariantOptionsTableClientSecretField(String variantOptionsTableClientSecretField)
   {
      this.getOrWithNewBackendVariantsConfig().withBackendSettingSourceFieldName(LegacyBackendVariantSetting.CLIENT_SECRET, variantOptionsTableClientSecretField);
   }



   /*******************************************************************************
    ** Fluent setter for variantOptionsTableClientSecretField
    *******************************************************************************/
   @Deprecated(since = "Replaced by backendVariantsConfig.backendSettingSourceFieldNameMap")
   public QBackendMetaData withVariantOptionsTableClientSecretField(String variantOptionsTableClientSecretField)
   {
      this.setVariantOptionsTableClientSecretField(variantOptionsTableClientSecretField);
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendVariantsConfig
    *******************************************************************************/
   public BackendVariantsConfig getBackendVariantsConfig()
   {
      return (this.backendVariantsConfig);
   }



   /*******************************************************************************
    ** Setter for backendVariantsConfig
    *******************************************************************************/
   public void setBackendVariantsConfig(BackendVariantsConfig backendVariantsConfig)
   {
      this.backendVariantsConfig = backendVariantsConfig;
   }



   /*******************************************************************************
    ** Fluent setter for backendVariantsConfig
    *******************************************************************************/
   public QBackendMetaData withBackendVariantsConfig(BackendVariantsConfig backendVariantsConfig)
   {
      this.backendVariantsConfig = backendVariantsConfig;
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private BackendVariantsConfig getOrWithNewBackendVariantsConfig()
   {
      if(backendVariantsConfig == null)
      {
         setBackendVariantsConfig(new BackendVariantsConfig());
      }
      return backendVariantsConfig;
   }
}
