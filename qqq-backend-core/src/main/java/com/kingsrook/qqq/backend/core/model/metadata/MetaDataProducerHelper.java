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


import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Help work with MetaDataProducers.
 *******************************************************************************/
public class MetaDataProducerHelper
{
   private static final QLogger LOG = QLogger.getLogger(MetaDataProducerHelper.class);



   /*******************************************************************************
    ** Recursively find all classes in the given package, that extend MetaDataProducer,
    ** run them, and add their output to the given qInstance.
    **
    ** Note - they'll be sorted by the sortOrder they provide.
    *******************************************************************************/
   public static void processAllMetaDataProducersInPackage(QInstance instance, String packageName)
   {
      ////////////////////////////////////////////////////////////
      // find all the meta data producer classes in the package //
      ////////////////////////////////////////////////////////////
      List<Class<?>>            classesInPackage = getClassesInPackage(packageName);
      List<MetaDataProducer<?>> producers        = new ArrayList<>();
      for(Class<?> aClass : classesInPackage)
      {
         try
         {
            if(Modifier.isAbstract(aClass.getModifiers()))
            {
               continue;
            }

            for(Constructor<?> constructor : aClass.getConstructors())
            {
               if(constructor.getParameterCount() == 0)
               {
                  Object o = constructor.newInstance();
                  if(o instanceof MetaDataProducer<?> metaDataProducer)
                  {
                     producers.add(metaDataProducer);
                  }
                  break;
               }
            }
         }
         catch(Exception e)
         {
            LOG.info("Error adding metaData from producer", logPair("producer", aClass.getSimpleName()), e);
         }
      }

      /////////////////////////////
      // sort them by sort order //
      /////////////////////////////
      producers.sort(Comparator.comparing(p -> p.getSortOrder()));

      //////////////////////////////////////////////////////////////
      // execute each one, adding their meta data to the instance //
      //////////////////////////////////////////////////////////////
      for(MetaDataProducer<?> producer : producers)
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
            LOG.warn("error executing metaDataProducer", logPair("producer", producer.getClass().getSimpleName()), e);
         }
      }

   }



   /*******************************************************************************
    ** Thanks, Chat GPT.
    *******************************************************************************/
   private static List<Class<?>> getClassesInPackage(String packageName)
   {
      List<Class<?>> classes = new ArrayList<>();

      String path      = packageName.replace('.', '/');
      File   directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());

      if(directory.exists())
      {
         File[] files = directory.listFiles();
         if(files != null)
         {
            for(File file : files)
            {
               if(file.isFile() && file.getName().endsWith(".class"))
               {
                  String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                  try
                  {
                     classes.add(Class.forName(className));
                  }
                  catch(ClassNotFoundException e)
                  {
                     // Ignore, class not found
                  }
               }
               else if(file.isDirectory())
               {
                  List<Class<?>> subClasses = getClassesInPackage(packageName + "." + file.getName());
                  classes.addAll(subClasses);
               }
            }
         }
      }

      return (classes);
   }

}
