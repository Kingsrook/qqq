/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Help work with MetaDataProducers.
 *******************************************************************************/
public class MetaDataProducerHelper
{
   private static final QLogger LOG = QLogger.getLogger(MetaDataProducerHelper.class);

   private static Map<Class<?>, Integer> comparatorValuesByType = new HashMap<>();
   private static Integer                defaultComparatorValue;

   private static ImmutableSet<ClassPath.ClassInfo> topLevelClasses;

   static
   {
      ////////////////////////////////////////////////////////////////////////////////////////
      // define how we break ties in sort-order based on the meta-dta type.  e.g., do apps  //
      // after all other types (as apps often try to get other types from the instance)     //
      // also - do backends earlier than others (e.g., tables may expect backends to exist) //
      // any types not in the map get the default value.                                    //
      ////////////////////////////////////////////////////////////////////////////////////////
      comparatorValuesByType.put(QBackendMetaData.class, 1);

      /////////////////////////////////////
      // unspecified ones will come here //
      /////////////////////////////////////
      defaultComparatorValue = 10;

      comparatorValuesByType.put(QJoinMetaData.class, 21);
      comparatorValuesByType.put(QWidgetMetaData.class, 22);
      comparatorValuesByType.put(QAppMetaData.class, 23);
   }

   /*******************************************************************************
    ** Recursively find all classes in the given package, that implement MetaDataProducerInterface
    ** run them, and add their output to the given qInstance.
    **
    ** Note - they'll be sorted by the sortOrder they provide.
    *******************************************************************************/
   public static void processAllMetaDataProducersInPackage(QInstance instance, String packageName) throws QException
   {
      List<Class<?>> classesInPackage;
      try
      {
         ////////////////////////////////////////////////////////////////////////
         // find all the meta data producer classes in (and under) the package //
         ////////////////////////////////////////////////////////////////////////
         classesInPackage = getClassesInPackage(packageName);
      }
      catch(Exception e)
      {
         throw (new QException("Error getting classes in package [" + packageName + "]", e));
      }
      List<MetaDataProducerInterface<?>> producers = new ArrayList<>();

      for(Class<?> aClass : classesInPackage)
      {
         try
         {
            if(Modifier.isAbstract(aClass.getModifiers()))
            {
               continue;
            }

            if(MetaDataProducerInterface.class.isAssignableFrom(aClass))
            {
               boolean foundValidConstructor = false;
               for(Constructor<?> constructor : aClass.getConstructors())
               {
                  if(constructor.getParameterCount() == 0)
                  {
                     Object o = constructor.newInstance();
                     producers.add((MetaDataProducerInterface<?>) o);
                     foundValidConstructor = true;
                     break;
                  }
               }

               if(!foundValidConstructor)
               {
                  LOG.warn("Found a class which implements MetaDataProducerInterface, but it does not have a no-arg constructor, so it cannot be used.", logPair("class", aClass.getSimpleName()));
               }
            }
         }
         catch(Exception e)
         {
            LOG.warn("Error evaluating a possible meta-data producer class", e, logPair("class", aClass.getSimpleName()));
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////
      // sort them by sort order, then by the type that they return, as set up in the static map //
      /////////////////////////////////////////////////////////////////////////////////////////////
      producers.sort(Comparator
         .comparing((MetaDataProducerInterface<?> p) -> p.getSortOrder())
         .thenComparing((MetaDataProducerInterface<?> p) ->
         {
            try
            {
               Class<?> outputType = p.getClass().getMethod("produce", QInstance.class).getReturnType();
               return comparatorValuesByType.getOrDefault(outputType, defaultComparatorValue);
            }
            catch(Exception e)
            {
               return (0);
            }
         }));

      ///////////////////////////////////////////////////////////////////////////
      // execute each one (if enabled), adding their meta data to the instance //
      ///////////////////////////////////////////////////////////////////////////
      for(MetaDataProducerInterface<?> producer : producers)
      {
         if(producer.isEnabled())
         {
            try
            {
               MetaDataProducerOutput metaData = producer.produce(instance);
               if(metaData != null)
               {
                  metaData.addSelfToInstance(instance);
               }
            }
            catch(Exception e)
            {
               LOG.warn("error executing metaDataProducer", e, logPair("producer", producer.getClass().getSimpleName()));
            }
         }
         else
         {
            LOG.debug("Not using producer which is not enabled", logPair("producer", producer.getClass().getSimpleName()));
         }
      }

   }



   /*******************************************************************************
    ** from https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
    ** (since the original, from ChatGPT, didn't work in jars, despite GPT hallucinating that it would)
    *******************************************************************************/
   private static List<Class<?>> getClassesInPackage(String packageName) throws IOException
   {
      List<Class<?>> classes = new ArrayList<>();
      ClassLoader    loader  = Thread.currentThread().getContextClassLoader();

      for(ClassPath.ClassInfo info : getTopLevelClasses(loader))
      {
         if(info.getName().startsWith(packageName))
         {
            classes.add(info.load());
         }
      }

      return (classes);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses(ClassLoader loader) throws IOException
   {
      if(topLevelClasses == null)
      {
         topLevelClasses = ClassPath.from(loader).getTopLevelClasses();
      }

      return (topLevelClasses);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void clearTopLevelClassCache()
   {
      topLevelClasses = null;
   }

}
