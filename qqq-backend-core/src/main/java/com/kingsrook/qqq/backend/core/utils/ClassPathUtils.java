/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;


/*******************************************************************************
 ** Utilities for reading classes - e.g., finding all in a package
 *******************************************************************************/
@SuppressWarnings("ALL") // the api we're using here, from google, is marked Beta
public class ClassPathUtils
{
   private static ImmutableSet<ClassPath.ClassInfo> topLevelClasses;



   /*******************************************************************************
    ** from https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
    **
    *******************************************************************************/
   public static List<Class<?>> getClassesInPackage(String packageName) throws IOException
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
