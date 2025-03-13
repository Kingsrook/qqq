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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntityField;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.ReflectiveBeanLikeClassUtils;


/*******************************************************************************
 ** base-class for bean-like classes to represent the fields of a process.
 ** similar in spirit to QRecordEntity, but for processes.
 *******************************************************************************/
public class QProcessPayload
{
   private static final QLogger LOG = QLogger.getLogger(QProcessPayload.class);

   private static final ListingHash<Class<? extends QProcessPayload>, QRecordEntityField> fieldMapping = new ListingHash<>();



   /*******************************************************************************
    ** Build an entity of this QRecord type from a QRecord
    **
    *******************************************************************************/
   public static <T extends QProcessPayload> T fromProcessState(Class<T> c, ProcessState processState) throws QException
   {
      try
      {
         T entity = c.getConstructor().newInstance();
         entity.populateFromProcessState(processState);
         return (entity);
      }
      catch(Exception e)
      {
         throw (new QException("Error building process payload from state.", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void populateFromProcessState(ProcessState processState)
   {
      try
      {
         List<QRecordEntityField> fieldList = getFieldList(this.getClass());
         // originalRecordValues = new HashMap<>();

         for(QRecordEntityField qRecordEntityField : fieldList)
         {
            Serializable value      = processState.getValues().get(qRecordEntityField.getFieldName());
            Object       typedValue = qRecordEntityField.convertValueType(value);
            qRecordEntityField.getSetter().invoke(this, typedValue);
            // originalRecordValues.put(qRecordEntityField.getFieldName(), value);
         }

         // for(QRecordEntityAssociation qRecordEntityAssociation : getAssociationList(this.getClass()))
         // {
         //    List<QRecord> associatedRecords = qRecord.getAssociatedRecords().get(qRecordEntityAssociation.getAssociationAnnotation().name());
         //    if(associatedRecords == null)
         //    {
         //       qRecordEntityAssociation.getSetter().invoke(this, (Object) null);
         //    }
         //    else
         //    {
         //       List<QRecordEntity> associatedEntityList = new ArrayList<>();
         //       for(QRecord associatedRecord : CollectionUtils.nonNullList(associatedRecords))
         //       {
         //          associatedEntityList.add(QRecordEntity.fromQRecord(qRecordEntityAssociation.getAssociatedType(), associatedRecord));
         //       }
         //       qRecordEntityAssociation.getSetter().invoke(this, associatedEntityList);
         //    }
         // }

         // for(QRecordEntityAssociation qRecordEntityAssociation : getAssociationList(this.getClass()))
         // {
         //    List<QRecord> associatedRecords = qRecord.getAssociatedRecords().get(qRecordEntityAssociation.getAssociationAnnotation().name());
         //    if(associatedRecords == null)
         //    {
         //       qRecordEntityAssociation.getSetter().invoke(this, (Object) null);
         //    }
         //    else
         //    {
         //       List<QRecordEntity> associatedEntityList = new ArrayList<>();
         //       for(QRecord associatedRecord : CollectionUtils.nonNullList(associatedRecords))
         //       {
         //          associatedEntityList.add(QRecordEntity.fromQRecord(qRecordEntityAssociation.getAssociatedType(), associatedRecord));
         //       }
         //       qRecordEntityAssociation.getSetter().invoke(this, associatedEntityList);
         //    }
         // }
      }
      catch(Exception e)
      {
         throw (new QRuntimeException("Error building process payload from process state.", e));
      }
   }



   /*******************************************************************************
    ** Copy the values from this payload into the given process state.
    ** ALL fields in the entity will be set in the process state.
    **
    *******************************************************************************/
   public void toProcessState(ProcessState processState) throws QRuntimeException
   {
      try
      {
         for(QRecordEntityField qRecordEntityField : getFieldList(this.getClass()))
         {
            processState.getValues().put(qRecordEntityField.getFieldName(), (Serializable) qRecordEntityField.getGetter().invoke(this));
         }
      }
      catch(Exception e)
      {
         throw (new QRuntimeException("Error populating process state from process payload.", e));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static Set<Class<?>> allowedFieldTypes()
   {
      HashSet<Class<?>> classes = new HashSet<>(ReflectiveBeanLikeClassUtils.defaultAllowedTypes());
      classes.add(Map.class);
      classes.add(List.class);
      return (classes);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecordEntityField> getFieldList(Class<? extends QProcessPayload> c)
   {
      if(!fieldMapping.containsKey(c))
      {
         List<QRecordEntityField> fieldList = new ArrayList<>();
         for(Method possibleGetter : c.getMethods())
         {
            if(ReflectiveBeanLikeClassUtils.isGetter(possibleGetter, false, allowedFieldTypes()))
            {
               Optional<Method> setter = ReflectiveBeanLikeClassUtils.getSetterForGetter(c, possibleGetter);

               if(setter.isPresent())
               {
                  String fieldName = ReflectiveBeanLikeClassUtils.getFieldNameFromGetter(possibleGetter);
                  fieldList.add(new QRecordEntityField(fieldName, possibleGetter, setter.get(), possibleGetter.getReturnType(), null));
               }
               else
               {
                  LOG.debug("Getter method [" + possibleGetter.getName() + "] does not have a corresponding setter.");
               }
            }
         }
         fieldMapping.put(c, fieldList);
      }
      return (fieldMapping.get(c));
   }

}
