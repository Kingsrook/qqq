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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.github.hervian.reflection.Fun;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Meta-data to represent a single field in a table.
 **
 *******************************************************************************/
public class QFieldMetaData implements Cloneable
{
   private String     name;
   private String     label;
   private String     backendName;
   private QFieldType type;
   private boolean    isRequired = false;
   private boolean    isEditable = true;

   ///////////////////////////////////////////////////////////////////////////////////
   // if we need "only edit on insert" or "only edit on update" in the future,      //
   // propose doing that in a secondary field, e.g., "onlyEditableOn=insert|update" //
   ///////////////////////////////////////////////////////////////////////////////////

   private String       displayFormat = "%s";
   private Serializable defaultValue;
   private String       possibleValueSourceName;

   private Integer            maxLength;
   private Set<FieldBehavior> behaviors;

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // w/ longer-term vision for FieldBehaviors                                                                           //
   // - more enums that implement ValueTooLongBehavior e.g., NumberOutsideRangeBehavior or DecimalPrecisionErrorBehavior //
   // - QInstance.Set<FieldBehavior> defaultFieldBehaviors                                                               //
   // - QBackendMetaData.Set<FieldBehavior> defaultFieldBehaviors                                                        //
   // - QTableMetaData.Set<FieldBehavior> defaultFieldBehaviors                                                          //
   // - inherit behaviors all the way down (up?)                                                                         //
   // - instance validation to make sure you don’t specify more than 1 behavior of a given type at a given level.        //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   private List<FieldAdornment> adornments;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QFieldMetaData clone()
   {
      try
      {
         QFieldMetaData clone = (QFieldMetaData) super.clone();
         if(adornments != null)
         {
            clone.setAdornments(new ArrayList<>(adornments));
         }
         return (clone);
      }
      catch(CloneNotSupportedException e)
      {
         throw new RuntimeException(e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData(String name, QFieldType type)
   {
      this.name = name;
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return ("QFieldMetaData[" + name + "]");
   }



   /*******************************************************************************
    ** Initialize a fieldMetaData from a reference to a getter on an entity.
    ** e.g., new QFieldMetaData(Order::getOrderNo).
    *******************************************************************************/
   public <T> QFieldMetaData(Fun.With1ParamAndVoid<T> getterRef) throws QException
   {
      Method getter = Fun.toMethod(getterRef);
      constructFromGetter(getter);
   }



   /*******************************************************************************
    ** Initialize a fieldMetaData from a getter method from an entity
    **
    *******************************************************************************/
   public <T> QFieldMetaData(Method getter) throws QException
   {
      constructFromGetter(getter);
   }



   /*******************************************************************************
    ** From a getter method, populate attributes in this field meta-data, including
    ** those from the @QField annotation on the field in the class, if present.
    *******************************************************************************/
   private void constructFromGetter(Method getter) throws QException
   {
      try
      {
         this.name = QRecordEntity.getFieldNameFromGetter(getter);
         this.type = QFieldType.fromClass(getter.getReturnType());

         @SuppressWarnings("unchecked")
         Optional<QField> optionalFieldAnnotation = QRecordEntity.getQFieldAnnotation((Class<? extends QRecordEntity>) getter.getDeclaringClass(), this.name);

         if(optionalFieldAnnotation.isPresent())
         {
            QField fieldAnnotation = optionalFieldAnnotation.get();
            setIsRequired(fieldAnnotation.isRequired());
            setIsEditable(fieldAnnotation.isEditable());

            if(StringUtils.hasContent(fieldAnnotation.label()))
            {
               setLabel(fieldAnnotation.label());
            }

            if(StringUtils.hasContent(fieldAnnotation.backendName()))
            {
               setBackendName(fieldAnnotation.backendName());
            }

            if(StringUtils.hasContent(fieldAnnotation.displayFormat()))
            {
               setDisplayFormat(fieldAnnotation.displayFormat());
            }

            if(StringUtils.hasContent(fieldAnnotation.possibleValueSourceName()))
            {
               setPossibleValueSourceName(fieldAnnotation.possibleValueSourceName());
            }

            if(fieldAnnotation.maxLength() != Integer.MAX_VALUE)
            {
               setMaxLength(fieldAnnotation.maxLength());
            }

            if(fieldAnnotation.valueTooLongBehavior() != ValueTooLongBehavior.PASS_THROUGH)
            {
               withBehavior(fieldAnnotation.valueTooLongBehavior());
            }
         }
      }
      catch(QException qe)
      {
         throw (qe);
      }
      catch(Exception e)
      {
         throw (new QException("Error constructing field from getter method: " + getter.getName(), e));
      }
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
   public QFieldMetaData withName(String name)
   {
      this.name = name;
      return (this);
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
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(QFieldType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withType(QFieldType type)
   {
      this.type = type;
      return (this);
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
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withLabel(String label)
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
   public QFieldMetaData withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getPossibleValueSourceName()
   {
      return possibleValueSourceName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
      return (this);
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
    ** Setter for defaultValue
    **
    *******************************************************************************/
   public void setDefaultValue(Serializable defaultValue)
   {
      this.defaultValue = defaultValue;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withDefaultValue(Serializable defaultValue)
   {
      this.defaultValue = defaultValue;
      return (this);
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
    ** Setter for isRequired
    **
    *******************************************************************************/
   public void setIsRequired(boolean isRequired)
   {
      this.isRequired = isRequired;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withIsRequired(boolean isRequired)
   {
      this.isRequired = isRequired;
      return (this);
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
    ** Setter for isEditable
    **
    *******************************************************************************/
   public void setIsEditable(boolean isEditable)
   {
      this.isEditable = isEditable;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withIsEditable(boolean isEditable)
   {
      this.isEditable = isEditable;
      return (this);
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
    ** Setter for displayFormat
    **
    *******************************************************************************/
   public void setDisplayFormat(String displayFormat)
   {
      this.displayFormat = displayFormat;
   }



   /*******************************************************************************
    ** Fluent setter for displayFormat
    **
    *******************************************************************************/
   public QFieldMetaData withDisplayFormat(String displayFormat)
   {
      this.displayFormat = displayFormat;
      return (this);
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
    ** Setter for adornments
    **
    *******************************************************************************/
   public void setAdornments(List<FieldAdornment> adornments)
   {
      this.adornments = adornments;
   }



   /*******************************************************************************
    ** Fluent setter for adornments
    **
    *******************************************************************************/
   public QFieldMetaData withFieldAdornments(List<FieldAdornment> adornments)
   {
      this.adornments = adornments;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for adornments
    **
    *******************************************************************************/
   public QFieldMetaData withFieldAdornment(FieldAdornment adornment)
   {
      if(this.adornments == null)
      {
         this.adornments = new ArrayList<>();
      }
      this.adornments.add(adornment);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for adornments
    **
    *******************************************************************************/
   public QFieldMetaData withFieldAdornment(AdornmentType adornmentType)
   {
      if(this.adornments == null)
      {
         this.adornments = new ArrayList<>();
      }
      this.adornments.add(new FieldAdornment(adornmentType));
      return (this);
   }



   /*******************************************************************************
    ** Getter for maxLength
    **
    *******************************************************************************/
   public Integer getMaxLength()
   {
      return maxLength;
   }



   /*******************************************************************************
    ** Setter for maxLength
    **
    *******************************************************************************/
   public void setMaxLength(Integer maxLength)
   {
      this.maxLength = maxLength;
   }



   /*******************************************************************************
    ** Fluent setter for maxLength
    **
    *******************************************************************************/
   public QFieldMetaData withMaxLength(Integer maxLength)
   {
      this.maxLength = maxLength;
      return (this);
   }



   /*******************************************************************************
    ** Getter for behaviors
    **
    *******************************************************************************/
   public Set<FieldBehavior> getBehaviors()
   {
      return behaviors;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public <T extends FieldBehavior> T getBehavior(QInstance instance, Class<T> behaviorType)
   {
      for(FieldBehavior fieldBehavior : CollectionUtils.nonNullCollection(behaviors))
      {
         if(behaviorType.isInstance(fieldBehavior))
         {
            return (behaviorType.cast(fieldBehavior));
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // todo - cascade/inherit behaviors down from table, backend, instance //
      /////////////////////////////////////////////////////////////////////////

      ///////////////////////////////////////////
      // return default behavior for this type //
      ///////////////////////////////////////////
      if(behaviorType.equals(ValueTooLongBehavior.class))
      {
         return behaviorType.cast(ValueTooLongBehavior.getDefault());
      }

      return (null);
   }



   /*******************************************************************************
    ** Setter for behaviors
    **
    *******************************************************************************/
   public void setBehaviors(Set<FieldBehavior> behaviors)
   {
      this.behaviors = behaviors;
   }



   /*******************************************************************************
    ** Fluent setter for behaviors
    **
    *******************************************************************************/
   public QFieldMetaData withBehaviors(Set<FieldBehavior> behaviors)
   {
      this.behaviors = behaviors;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for behaviors
    **
    *******************************************************************************/
   public QFieldMetaData withBehavior(FieldBehavior behavior)
   {
      if(behaviors == null)
      {
         behaviors = new HashSet<>();
      }
      this.behaviors.add(behavior);
      return (this);
   }

}
