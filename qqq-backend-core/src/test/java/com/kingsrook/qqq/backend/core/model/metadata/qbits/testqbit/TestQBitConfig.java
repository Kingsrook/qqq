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

package com.kingsrook.qqq.backend.core.model.metadata.qbits.testqbit;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.ProvidedOrSuppliedTableConfig;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestQBitConfig implements QBitConfig
{
   private MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer;

   private Boolean                       isSomeTableEnabled;
   private ProvidedOrSuppliedTableConfig otherTableConfig;

   private String someSetting;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      assertCondition(otherTableConfig != null, "otherTableConfig must be set", errors);
      assertCondition(isSomeTableEnabled != null, "isSomeTableEnabled must be set", errors);
   }



   /*******************************************************************************
    ** Getter for otherTableConfig
    *******************************************************************************/
   public ProvidedOrSuppliedTableConfig getOtherTableConfig()
   {
      return (this.otherTableConfig);
   }



   /*******************************************************************************
    ** Setter for otherTableConfig
    *******************************************************************************/
   public void setOtherTableConfig(ProvidedOrSuppliedTableConfig otherTableConfig)
   {
      this.otherTableConfig = otherTableConfig;
   }



   /*******************************************************************************
    ** Fluent setter for otherTableConfig
    *******************************************************************************/
   public TestQBitConfig withOtherTableConfig(ProvidedOrSuppliedTableConfig otherTableConfig)
   {
      this.otherTableConfig = otherTableConfig;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isSomeTableEnabled
    *******************************************************************************/
   public Boolean getIsSomeTableEnabled()
   {
      return (this.isSomeTableEnabled);
   }



   /*******************************************************************************
    ** Setter for isSomeTableEnabled
    *******************************************************************************/
   public void setIsSomeTableEnabled(Boolean isSomeTableEnabled)
   {
      this.isSomeTableEnabled = isSomeTableEnabled;
   }



   /*******************************************************************************
    ** Fluent setter for isSomeTableEnabled
    *******************************************************************************/
   public TestQBitConfig withIsSomeTableEnabled(Boolean isSomeTableEnabled)
   {
      this.isSomeTableEnabled = isSomeTableEnabled;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableMetaDataCustomizer
    *******************************************************************************/
   public MetaDataCustomizerInterface<QTableMetaData> getTableMetaDataCustomizer()
   {
      return (this.tableMetaDataCustomizer);
   }



   /*******************************************************************************
    ** Setter for tableMetaDataCustomizer
    *******************************************************************************/
   public void setTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer)
   {
      this.tableMetaDataCustomizer = tableMetaDataCustomizer;
   }



   /*******************************************************************************
    ** Fluent setter for tableMetaDataCustomizer
    *******************************************************************************/
   public TestQBitConfig withTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer)
   {
      this.tableMetaDataCustomizer = tableMetaDataCustomizer;
      return (this);
   }



   /*******************************************************************************
    ** Getter for someSetting
    *******************************************************************************/
   public String getSomeSetting()
   {
      return (this.someSetting);
   }



   /*******************************************************************************
    ** Setter for someSetting
    *******************************************************************************/
   public void setSomeSetting(String someSetting)
   {
      this.someSetting = someSetting;
   }



   /*******************************************************************************
    ** Fluent setter for someSetting
    *******************************************************************************/
   public TestQBitConfig withSomeSetting(String someSetting)
   {
      this.someSetting = someSetting;
      return (this);
   }

}
