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
import java.util.List;
import com.google.common.reflect.ClassPath;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Help work with MetaDataProducers.
 *******************************************************************************/
public class MetaDataProducerHelper
{
   private static final QLogger LOG = QLogger.getLogger(MetaDataProducerHelper.class);



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

      ////////////////////////////////////////////////////////////////////////////////////////////
      // sort them by sort order, then by the type that they return - specifically - doing apps //
      // after all other types (as apps often try to get other types from the instance)         //
      // also - do backends earlier than others (e.g., tables may expect backends to exist)     //
      ////////////////////////////////////////////////////////////////////////////////////////////
      producers.sort(Comparator
         .comparing((MetaDataProducerInterface<?> p) -> p.getSortOrder())
         .thenComparing((MetaDataProducerInterface<?> p) ->
         {
            try
            {
               Class<?> outputType = p.getClass().getMethod("produce", QInstance.class).getReturnType();
               if(outputType.equals(QAppMetaData.class))
               {
                  return (2);
               }
               else if(outputType.equals(QBackendMetaData.class))
               {
                  return (0);
               }
               else
               {
                  return (1);
               }
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
               TopLevelMetaDataInterface metaData = producer.produce(instance);
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

      for(ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses())
      {
         if(info.getName().startsWith(packageName))
         {
            classes.add(info.load());
         }
      }

      return (classes);
   }

}
