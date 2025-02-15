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

package com.kingsrook.qqq.backend.module.filesystem.base.model.metadata;


import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Extension of QTableBackendDetails, with details specific to a Filesystem table.
 *******************************************************************************/
public class AbstractFilesystemTableBackendDetails extends QTableBackendDetails
{
   private String       basePath;
   private String       glob;
   private RecordFormat recordFormat;
   private Cardinality  cardinality;

   private String contentsFieldName;
   private String fileNameFieldName;
   private String sizeFieldName;
   private String createDateFieldName;
   private String modifyDateFieldName;



   /*******************************************************************************
    ** Getter for basePath
    **
    *******************************************************************************/
   public String getBasePath()
   {
      return basePath;
   }



   /*******************************************************************************
    ** Setter for basePath
    **
    *******************************************************************************/
   public void setBasePath(String basePath)
   {
      this.basePath = basePath;
   }



   /*******************************************************************************
    ** Fluent Setter for basePath
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends AbstractFilesystemTableBackendDetails> T withBasePath(String basePath)
   {
      this.basePath = basePath;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for glob
    **
    *******************************************************************************/
   public String getGlob()
   {
      return glob;
   }



   /*******************************************************************************
    ** Setter for glob
    **
    *******************************************************************************/
   public void setGlob(String glob)
   {
      this.glob = glob;
   }



   /*******************************************************************************
    ** Fluent Setter for glob
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends AbstractFilesystemTableBackendDetails> T withGlob(String glob)
   {
      this.glob = glob;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for recordFormat
    **
    *******************************************************************************/
   public RecordFormat getRecordFormat()
   {
      return recordFormat;
   }



   /*******************************************************************************
    ** Setter for recordFormat
    **
    *******************************************************************************/
   public void setRecordFormat(RecordFormat recordFormat)
   {
      this.recordFormat = recordFormat;
   }



   /*******************************************************************************
    ** Fluent Setter for recordFormat
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends AbstractFilesystemTableBackendDetails> T withRecordFormat(RecordFormat recordFormat)
   {
      this.recordFormat = recordFormat;
      return ((T) this);
   }



   /*******************************************************************************
    ** Getter for cardinality
    **
    *******************************************************************************/
   public Cardinality getCardinality()
   {
      return cardinality;
   }



   /*******************************************************************************
    ** Setter for cardinality
    **
    *******************************************************************************/
   public void setCardinality(Cardinality cardinality)
   {
      this.cardinality = cardinality;
   }



   /*******************************************************************************
    ** Fluent Setter for cardinality
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends AbstractFilesystemTableBackendDetails> T withCardinality(Cardinality cardinality)
   {
      this.cardinality = cardinality;
      return ((T) this);
   }



   /*******************************************************************************
    ** Getter for contentsFieldName
    *******************************************************************************/
   public String getContentsFieldName()
   {
      return (this.contentsFieldName);
   }



   /*******************************************************************************
    ** Setter for contentsFieldName
    *******************************************************************************/
   public void setContentsFieldName(String contentsFieldName)
   {
      this.contentsFieldName = contentsFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for contentsFieldName
    *******************************************************************************/
   public AbstractFilesystemTableBackendDetails withContentsFieldName(String contentsFieldName)
   {
      this.contentsFieldName = contentsFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fileNameFieldName
    *******************************************************************************/
   public String getFileNameFieldName()
   {
      return (this.fileNameFieldName);
   }



   /*******************************************************************************
    ** Setter for fileNameFieldName
    *******************************************************************************/
   public void setFileNameFieldName(String fileNameFieldName)
   {
      this.fileNameFieldName = fileNameFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fileNameFieldName
    *******************************************************************************/
   public AbstractFilesystemTableBackendDetails withFileNameFieldName(String fileNameFieldName)
   {
      this.fileNameFieldName = fileNameFieldName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance, QTableMetaData table, QInstanceValidator qInstanceValidator)
   {
      super.validate(qInstance, table, qInstanceValidator);

      String prefix = "Table " + (table == null ? "null" : table.getName()) + " backend details - ";
      if(qInstanceValidator.assertCondition(cardinality != null, prefix + "missing cardinality"))
      {
         if(cardinality.equals(Cardinality.ONE))
         {
            if(qInstanceValidator.assertCondition(StringUtils.hasContent(contentsFieldName), prefix + "missing contentsFieldName, which is required for Cardinality ONE"))
            {
               qInstanceValidator.assertCondition(table != null && table.getFields().containsKey(contentsFieldName), prefix + "contentsFieldName [" + contentsFieldName + "] is not a field on this table.");
            }

            if(qInstanceValidator.assertCondition(StringUtils.hasContent(fileNameFieldName), prefix + "missing fileNameFieldName, which is required for Cardinality ONE"))
            {
               qInstanceValidator.assertCondition(table != null && table.getFields().containsKey(fileNameFieldName), prefix + "fileNameFieldName [" + fileNameFieldName + "] is not a field on this table.");
            }

            qInstanceValidator.assertCondition(recordFormat == null, prefix + "has a recordFormat, which is not allowed for Cardinality ONE");
         }

         if(cardinality.equals(Cardinality.MANY))
         {
            qInstanceValidator.assertCondition(!StringUtils.hasContent(contentsFieldName), prefix + "has a contentsFieldName, which is not allowed for Cardinality MANY");
            qInstanceValidator.assertCondition(!StringUtils.hasContent(fileNameFieldName), prefix + "has a fileNameFieldName, which is not allowed for Cardinality MANY");
            qInstanceValidator.assertCondition(recordFormat != null, prefix + "missing recordFormat, which is required for Cardinality MANY");
         }
      }

   }

   /*******************************************************************************
    ** Getter for sizeFieldName
    *******************************************************************************/
   public String getSizeFieldName()
   {
      return (this.sizeFieldName);
   }



   /*******************************************************************************
    ** Setter for sizeFieldName
    *******************************************************************************/
   public void setSizeFieldName(String sizeFieldName)
   {
      this.sizeFieldName = sizeFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for sizeFieldName
    *******************************************************************************/
   public AbstractFilesystemTableBackendDetails withSizeFieldName(String sizeFieldName)
   {
      this.sizeFieldName = sizeFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDateFieldName
    *******************************************************************************/
   public String getCreateDateFieldName()
   {
      return (this.createDateFieldName);
   }



   /*******************************************************************************
    ** Setter for createDateFieldName
    *******************************************************************************/
   public void setCreateDateFieldName(String createDateFieldName)
   {
      this.createDateFieldName = createDateFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for createDateFieldName
    *******************************************************************************/
   public AbstractFilesystemTableBackendDetails withCreateDateFieldName(String createDateFieldName)
   {
      this.createDateFieldName = createDateFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDateFieldName
    *******************************************************************************/
   public String getModifyDateFieldName()
   {
      return (this.modifyDateFieldName);
   }



   /*******************************************************************************
    ** Setter for modifyDateFieldName
    *******************************************************************************/
   public void setModifyDateFieldName(String modifyDateFieldName)
   {
      this.modifyDateFieldName = modifyDateFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDateFieldName
    *******************************************************************************/
   public AbstractFilesystemTableBackendDetails withModifyDateFieldName(String modifyDateFieldName)
   {
      this.modifyDateFieldName = modifyDateFieldName;
      return (this);
   }


}
