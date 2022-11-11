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


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.serialization.QBackendMetaDataDeserializer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Meta-data to provide details of a backend (e.g., RDBMS instance, S3 buckets,
 ** NoSQL table, etc) within a qqq instance
 **
 *******************************************************************************/
@JsonDeserialize(using = QBackendMetaDataDeserializer.class)
public class QBackendMetaData
{
   private String name;
   private String backendType;

   private Set<Capability> enabledCapabilities  = new HashSet<>();
   private Set<Capability> disabledCapabilities = new HashSet<>();

   // todo - at some point, we may want to apply this to secret properties on subclasses?
   // @JsonFilter("secretsFilter")



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public QBackendMetaData()
   {
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
    ** Fluent setter, returning generically, to help sub-class fluent flows
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends QBackendMetaData> T withBackendType(String backendType)
   {
      this.backendType = backendType;
      return (T) this;
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
   }



   /*******************************************************************************
    ** Fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withEnabledCapabilities(Set<Capability> enabledCapabilities)
   {
      this.enabledCapabilities = enabledCapabilities;
      return (this);
   }



   /*******************************************************************************
    ** Alternative fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withCapabilities(Set<Capability> enabledCapabilities)
   {
      this.enabledCapabilities = enabledCapabilities;
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
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for enabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withCapabilities(Capability... enabledCapabilities)
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
   public QBackendMetaData withDisabledCapabilities(Set<Capability> disabledCapabilities)
   {
      this.disabledCapabilities = disabledCapabilities;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for disabledCapabilities
    **
    *******************************************************************************/
   public QBackendMetaData withoutCapabilities(Capability... disabledCapabilities)
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
   public QBackendMetaData withoutCapabilities(Set<Capability> disabledCapabilities)
   {
      this.disabledCapabilities = disabledCapabilities;
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
}
