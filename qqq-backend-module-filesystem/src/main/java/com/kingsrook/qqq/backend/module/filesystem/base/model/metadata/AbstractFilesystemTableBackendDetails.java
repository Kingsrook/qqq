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


import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;


/*******************************************************************************
 ** Extension of QTableBackendDetails, with details specific to a Filesystem table.
 *******************************************************************************/
public class AbstractFilesystemTableBackendDetails extends QTableBackendDetails
{
   private String       basePath;
   private String       glob;
   private RecordFormat recordFormat;
   private Cardinality  cardinality;



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

}
