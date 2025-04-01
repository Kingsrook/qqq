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

package com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.s3.S3BackendModule;


/*******************************************************************************
 ** S3 specific Extension of QTableBackendDetails
 *******************************************************************************/
public class S3TableBackendDetails extends AbstractFilesystemTableBackendDetails
{
   private ContentTypeStrategy contentTypeStrategy = ContentTypeStrategy.NONE;
   private String contentTypeFieldName;
   private String hardcodedContentType;


   /***************************************************************************
    **
    ***************************************************************************/
   public enum ContentTypeStrategy
   {
      BASED_ON_FILE_NAME,
      FROM_FIELD,
      HARDCODED,
      NONE
   }



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public S3TableBackendDetails()
   {
      super();
      setBackendType(S3BackendModule.class);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void validate(QInstance qInstance, QTableMetaData table, QInstanceValidator qInstanceValidator)
   {
      super.validate(qInstance, table, qInstanceValidator);

      String prefix = "Table " + (table == null ? "null" : table.getName()) + " backend details - ";
      switch (Objects.requireNonNullElse(contentTypeStrategy, ContentTypeStrategy.NONE))
      {
         case FROM_FIELD ->
         {
            qInstanceValidator.assertCondition(!StringUtils.hasContent(hardcodedContentType), prefix + "hardcodedContentType should not be set when contentTypeStrategy is " + contentTypeStrategy);

            if(table != null && qInstanceValidator.assertCondition(StringUtils.hasContent(contentTypeFieldName), prefix + "contentTypeFieldName must be set when contentTypeStrategy is " + contentTypeStrategy))
            {
               qInstanceValidator.assertCondition(table.getFields().containsKey(contentTypeFieldName), prefix + "contentTypeFieldName must be a valid field name in the table");
            }
         }
         case HARDCODED ->
         {
            qInstanceValidator.assertCondition(!StringUtils.hasContent(contentTypeFieldName), prefix + "contentTypeFieldName should not be set when contentTypeStrategy is " + contentTypeStrategy);
            qInstanceValidator.assertCondition(StringUtils.hasContent(hardcodedContentType), prefix + "hardcodedContentType must be set when contentTypeStrategy is " + contentTypeStrategy);
         }
         case BASED_ON_FILE_NAME, NONE, default ->
         {
            qInstanceValidator.assertCondition(!StringUtils.hasContent(contentTypeFieldName), prefix + "contentTypeFieldName should not be set when contentTypeStrategy is " + contentTypeStrategy);
            qInstanceValidator.assertCondition(!StringUtils.hasContent(hardcodedContentType), prefix + "hardcodedContentType should not be set when contentTypeStrategy is " + contentTypeStrategy);
         }
      }
   }

   /*******************************************************************************
    ** Getter for contentTypeStrategy
    *******************************************************************************/
   public ContentTypeStrategy getContentTypeStrategy()
   {
      return (this.contentTypeStrategy);
   }



   /*******************************************************************************
    ** Setter for contentTypeStrategy
    *******************************************************************************/
   public void setContentTypeStrategy(ContentTypeStrategy contentTypeStrategy)
   {
      this.contentTypeStrategy = contentTypeStrategy;
   }



   /*******************************************************************************
    ** Fluent setter for contentTypeStrategy
    *******************************************************************************/
   public S3TableBackendDetails withContentTypeStrategy(ContentTypeStrategy contentTypeStrategy)
   {
      this.contentTypeStrategy = contentTypeStrategy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contentTypeFieldName
    *******************************************************************************/
   public String getContentTypeFieldName()
   {
      return (this.contentTypeFieldName);
   }



   /*******************************************************************************
    ** Setter for contentTypeFieldName
    *******************************************************************************/
   public void setContentTypeFieldName(String contentTypeFieldName)
   {
      this.contentTypeFieldName = contentTypeFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for contentTypeFieldName
    *******************************************************************************/
   public S3TableBackendDetails withContentTypeFieldName(String contentTypeFieldName)
   {
      this.contentTypeFieldName = contentTypeFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hardcodedContentType
    *******************************************************************************/
   public String getHardcodedContentType()
   {
      return (this.hardcodedContentType);
   }



   /*******************************************************************************
    ** Setter for hardcodedContentType
    *******************************************************************************/
   public void setHardcodedContentType(String hardcodedContentType)
   {
      this.hardcodedContentType = hardcodedContentType;
   }



   /*******************************************************************************
    ** Fluent setter for hardcodedContentType
    *******************************************************************************/
   public S3TableBackendDetails withHardcodedContentType(String hardcodedContentType)
   {
      this.hardcodedContentType = hardcodedContentType;
      return (this);
   }


}
