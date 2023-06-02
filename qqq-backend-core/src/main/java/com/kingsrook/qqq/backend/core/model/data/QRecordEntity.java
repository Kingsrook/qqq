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

package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;


/*******************************************************************************
 ** Base class for entity beans that are interoperable with QRecords.
 *******************************************************************************/
public abstract class QRecordEntity
{
   private static final QLogger LOG = QLogger.getLogger(QRecordEntity.class);

   private static final ListingHash<Class<? extends QRecordEntity>, QRecordEntityField>       fieldMapping       = new ListingHash<>();
   private static final ListingHash<Class<? extends QRecordEntity>, QRecordEntityAssociation> associationMapping = new ListingHash<>();



   /*******************************************************************************
    ** Build an entity of this QRecord type from a QRecord
    **
    *******************************************************************************/
   public static <T extends QRecordEntity> T fromQRecord(Class<T> c, QRecord qRecord) throws QException
   {
      try
      {
         T entity = c.getConstructor().newInstance();
         entity.populateFromQRecord(qRecord);
         return (entity);
      }
      catch(Exception e)
      {
         throw (new QException("Error building entity from qRecord.", e));
      }
   }



   /*******************************************************************************
    ** Build an entity of this QRecord type from a QRecord
    **
    *******************************************************************************/
   protected void populateFromQRecord(QRecord qRecord) throws QRuntimeException
   {
      try
      {
         for(QRecordEntityField qRecordEntityField : getFieldList(this.getClass()))
         {
            Serializable value      = qRecord.getValue(qRecordEntityField.getFieldName());
            Object       typedValue = qRecordEntityField.convertValueType(value);
            qRecordEntityField.getSetter().invoke(this, typedValue);
         }

         for(QRecordEntityAssociation qRecordEntityAssociation : getAssociationList(this.getClass()))
         {
            List<QRecord> associatedRecords = qRecord.getAssociatedRecords().get(qRecordEntityAssociation.getAssociationAnnotation().name());
            if(associatedRecords == null)
            {
               qRecordEntityAssociation.getSetter().invoke(this, (Object) null);
            }
            else
            {
               List<QRecordEntity> associatedEntityList = new ArrayList<>();
               for(QRecord associatedRecord : CollectionUtils.nonNullList(associatedRecords))
               {
                  associatedEntityList.add(QRecordEntity.fromQRecord(qRecordEntityAssociation.getAssociatedType(), associatedRecord));
               }
               qRecordEntityAssociation.getSetter().invoke(this, associatedEntityList);
            }
         }
      }
      catch(Exception e)
      {
         throw (new QRuntimeException("Error building entity from qRecord.", e));
      }
   }



   /*******************************************************************************
    ** Convert this entity to a QRecord.
    **
    *******************************************************************************/
   public QRecord toQRecord() throws QRuntimeException
   {
      try
      {
         QRecord qRecord = new QRecord();

         for(QRecordEntityField qRecordEntityField : getFieldList(this.getClass()))
         {
            qRecord.setValue(qRecordEntityField.getFieldName(), (Serializable) qRecordEntityField.getGetter().invoke(this));
         }

         for(QRecordEntityAssociation qRecordEntityAssociation : getAssociationList(this.getClass()))
         {
            List<? extends QRecordEntity> associatedEntities = (List<? extends QRecordEntity>) qRecordEntityAssociation.getGetter().invoke(this);
            String                        associationName    = qRecordEntityAssociation.getAssociationAnnotation().name();

            if(associatedEntities != null)
            {
               /////////////////////////////////////////////////////////////////////////////////
               // do this so an empty list in the entity becomes an empty list in the QRecord //
               /////////////////////////////////////////////////////////////////////////////////
               qRecord.withAssociatedRecords(associationName, new ArrayList<>());
            }

            for(QRecordEntity associatedEntity : CollectionUtils.nonNullList(associatedEntities))
            {
               qRecord.withAssociatedRecord(associationName, associatedEntity.toQRecord());
            }
         }

         return (qRecord);
      }
      catch(Exception e)
      {
         throw (new QRuntimeException("Error building qRecord from entity.", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecordEntityField> getFieldList(Class<? extends QRecordEntity> c)
   {
      if(!fieldMapping.containsKey(c))
      {
         List<QRecordEntityField> fieldList = new ArrayList<>();
         for(Method possibleGetter : c.getMethods())
         {
            if(isGetter(possibleGetter))
            {
               Optional<Method> setter = getSetterForGetter(c, possibleGetter);

               if(setter.isPresent())
               {
                  String           fieldName       = getFieldNameFromGetter(possibleGetter);
                  Optional<QField> fieldAnnotation = getQFieldAnnotation(c, fieldName);
                  fieldList.add(new QRecordEntityField(fieldName, possibleGetter, setter.get(), possibleGetter.getReturnType(), fieldAnnotation.orElse(null)));
               }
               else
               {
                  LOG.info("Getter method [" + possibleGetter.getName() + "] does not have a corresponding setter.");
               }
            }
         }
         fieldMapping.put(c, fieldList);
      }
      return (fieldMapping.get(c));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecordEntityAssociation> getAssociationList(Class<? extends QRecordEntity> c)
   {
      if(!associationMapping.containsKey(c))
      {
         List<QRecordEntityAssociation> associationList = new ArrayList<>();
         for(Method possibleGetter : c.getMethods())
         {
            if(isGetter(possibleGetter))
            {
               Optional<Method> setter = getSetterForGetter(c, possibleGetter);

               if(setter.isPresent())
               {
                  String                 fieldName             = getFieldNameFromGetter(possibleGetter);
                  Optional<QAssociation> associationAnnotation = getQAssociationAnnotation(c, fieldName);

                  if(associationAnnotation.isPresent())
                  {
                     Class<? extends QRecordEntity> listTypeParam = (Class<? extends QRecordEntity>) getListTypeParam(possibleGetter.getReturnType(), possibleGetter.getAnnotatedReturnType());
                     associationList.add(new QRecordEntityAssociation(fieldName, possibleGetter, setter.get(), listTypeParam, associationAnnotation.orElse(null)));
                  }
               }
               else
               {
                  LOG.info("Getter method [" + possibleGetter.getName() + "] does not have a corresponding setter.");
               }
            }
         }
         associationMapping.put(c, associationList);
      }
      return (associationMapping.get(c));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<QField> getQFieldAnnotation(Class<? extends QRecordEntity> c, String fieldName)
   {
      return (getAnnotationOnField(c, QField.class, fieldName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<QAssociation> getQAssociationAnnotation(Class<? extends QRecordEntity> c, String fieldName)
   {
      return (getAnnotationOnField(c, QAssociation.class, fieldName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <A extends Annotation> Optional<A> getAnnotationOnField(Class<? extends QRecordEntity> c, Class<A> annotationClass, String fieldName)
   {
      try
      {
         Field field = c.getDeclaredField(fieldName);
         return (Optional.ofNullable(field.getAnnotation(annotationClass)));
      }
      catch(NoSuchFieldException e)
      {
         //////////////////////////////////////////
         // ok, we just won't have an annotation //
         //////////////////////////////////////////
      }
      return (Optional.empty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getFieldNameFromGetter(Method getter)
   {
      String nameWithoutGet = getter.getName().replaceFirst("^get", "");
      if(nameWithoutGet.length() == 1)
      {
         return (nameWithoutGet.toLowerCase(Locale.ROOT));
      }
      return (nameWithoutGet.substring(0, 1).toLowerCase(Locale.ROOT) + nameWithoutGet.substring(1));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isGetter(Method method)
   {
      if(method.getParameterTypes().length == 0 && method.getName().matches("^get[A-Z].*"))
      {
         if(isSupportedFieldType(method.getReturnType()) || isSupportedAssociation(method.getReturnType(), method.getAnnotatedReturnType()))
         {
            return (true);
         }
         else
         {
            if(!method.getName().equals("getClass"))
            {
               LOG.info("Method [" + method.getName() + "] looks like a getter, but its return type, [" + method.getReturnType() + "], isn't supported.");
            }
         }
      }
      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Optional<Method> getSetterForGetter(Class<? extends QRecordEntity> c, Method getter)
   {
      String setterName = getter.getName().replaceFirst("^get", "set");
      for(Method method : c.getMethods())
      {
         if(method.getName().equals(setterName))
         {
            if(method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(getter.getReturnType()))
            {
               return (Optional.of(method));
            }
            else
            {
               LOG.info("Method [" + method.getName() + "] looks like a setter for [" + getter.getName() + "], but its parameters, [" + Arrays.toString(method.getParameterTypes()) + "], don't match the getter's return type [" + getter.getReturnType() + "]");
            }
         }
      }
      return (Optional.empty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isSupportedFieldType(Class<?> returnType)
   {
      // todo - more types!!
      return (returnType.equals(String.class)
         || returnType.equals(Integer.class)
         || returnType.equals(int.class)
         || returnType.equals(Boolean.class)
         || returnType.equals(boolean.class)
         || returnType.equals(BigDecimal.class)
         || returnType.equals(Instant.class)
         || returnType.equals(LocalDate.class)
         || returnType.equals(LocalTime.class));
      /////////////////////////////////////////////
      // note - this list has implications upon: //
      // - QFieldType.fromClass                  //
      // - QRecordEntityField.convertValueType   //
      /////////////////////////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isSupportedAssociation(Class<?> returnType, AnnotatedType annotatedType)
   {
      Class<?> listTypeParam = getListTypeParam(returnType, annotatedType);
      return (listTypeParam != null && QRecordEntity.class.isAssignableFrom(listTypeParam));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Class<?> getListTypeParam(Class<?> listType, AnnotatedType annotatedType)
   {
      if(listType.equals(List.class))
      {
         if(annotatedType instanceof AnnotatedParameterizedType apt)
         {
            AnnotatedType[] annotatedActualTypeArguments = apt.getAnnotatedActualTypeArguments();
            for(AnnotatedType annotatedActualTypeArgument : annotatedActualTypeArguments)
            {
               Type type = annotatedActualTypeArgument.getType();
               if(type instanceof Class<?> c)
               {
                  return (c);
               }
            }
         }
      }

      return (null);
   }

}
