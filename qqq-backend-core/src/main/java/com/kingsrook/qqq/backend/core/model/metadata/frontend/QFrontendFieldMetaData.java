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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldBehaviorForFrontend;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 * Version of QFieldMetaData that's meant for transmitting to a frontend.
 * e.g., it excludes backend-only details.
 *
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendFieldMetaData implements Serializable
{
   private String       name;
   private String       label;
   private QFieldType   type;
   private boolean      isRequired;
   private boolean      isEditable;
   private boolean      isHeavy;
   private String       possibleValueSourceName;
   private String       displayFormat;
   private Serializable defaultValue;

   private List<FieldAdornment> adornments;
   private List<QHelpContent>   helpContents;

   private List<FieldBehaviorForFrontend> behaviors;

   //////////////////////////////////////////////////////////////////////////////////
   // do not add setters.  take values from the source-object in the constructor!! //
   //////////////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    ** Constructor
    *******************************************************************************/
   public QFrontendFieldMetaData(QFieldMetaData fieldMetaData)
   {
      this.name = fieldMetaData.getName();
      this.label = fieldMetaData.getLabel();
      this.type = fieldMetaData.getType();
      this.isRequired = fieldMetaData.getIsRequired();
      this.isEditable = fieldMetaData.getIsEditable();
      this.isHeavy = fieldMetaData.getIsHeavy();
      this.possibleValueSourceName = fieldMetaData.getPossibleValueSourceName();
      this.displayFormat = fieldMetaData.getDisplayFormat();
      this.adornments = fieldMetaData.getAdornments();
      this.defaultValue = fieldMetaData.getDefaultValue();
      this.helpContents = fieldMetaData.getHelpContents();

      for(FieldBehavior<?> behavior : CollectionUtils.nonNullCollection(fieldMetaData.getBehaviors()))
      {
         if(behavior instanceof FieldBehaviorForFrontend fbff)
         {
            if(behaviors == null)
            {
               behaviors = new ArrayList<>();
            }
            behaviors.add(fbff);
         }
      }
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
    ** Getter for type
    **
    *******************************************************************************/
   public QFieldType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Getter for isRequired
    **
    *******************************************************************************/
   public boolean getIsRequired()
   {
      return isRequired;
   }



   /*******************************************************************************
    ** Getter for isEditable
    **
    *******************************************************************************/
   public boolean getIsEditable()
   {
      return isEditable;
   }



   /*******************************************************************************
    ** Getter for isHeavy
    **
    *******************************************************************************/
   public boolean getIsHeavy()
   {
      return isHeavy;
   }



   /*******************************************************************************
    ** Getter for displayFormat
    **
    *******************************************************************************/
   public String getDisplayFormat()
   {
      return displayFormat;
   }



   /*******************************************************************************
    ** Getter for adornments
    **
    *******************************************************************************/
   public List<FieldAdornment> getAdornments()
   {
      return adornments;
   }



   /*******************************************************************************
    ** Getter for possibleValueSourceName
    **
    *******************************************************************************/
   public String getPossibleValueSourceName()
   {
      return possibleValueSourceName;
   }



   /*******************************************************************************
    ** Getter for defaultValue
    **
    *******************************************************************************/
   public Serializable getDefaultValue()
   {
      return defaultValue;
   }



   /*******************************************************************************
    ** Getter for helpContents
    **
    *******************************************************************************/
   public List<QHelpContent> getHelpContents()
   {
      return helpContents;
   }



   /*******************************************************************************
    ** Getter for fieldBehaviors
    **
    *******************************************************************************/
   public List<FieldBehaviorForFrontend> getBehaviors()
   {
      return behaviors;
   }
}
