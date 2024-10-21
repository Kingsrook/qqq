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

package com.kingsrook.qqq.middleware.javalin.tools.codegenerators;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;


/*******************************************************************************
 **
 *******************************************************************************/
class ExecutorCodeGenerator
{

   /***************************************************************************
    **
    ***************************************************************************/
   public static void main(String[] args)
   {
      try
      {
         String qqqDir = "/Users/dkelkhoff/git/kingsrook/qqq/";
         new ExecutorCodeGenerator().writeAllFiles(qqqDir, "ProcessMetaData"); // don't include "Executor" on the end.
      }
      catch(IOException e)
      {
         //noinspection CallToPrintStackTrace
         e.printStackTrace();
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void writeOne(String fullPath, String content) throws IOException
   {
      File file = new File(fullPath);
      File directory = file.getParentFile();

      if(!directory.exists())
      {
         throw (new RuntimeException("Directory for: " + fullPath + " does not exists, and I refuse to mkdir (do it yourself and/or fix your arguments)."));
      }

      if(file.exists())
      {
         throw (new RuntimeException("File at: " + fullPath + " already exists, and I refuse to overwrite files."));
      }

      System.out.println("Writing: " + file);
      FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   void writeAllFiles(String rootPath, String baseName) throws IOException
   {
      if(baseName.endsWith("Executor"))
      {
         throw new IllegalArgumentException("Base name must not end with 'Executor'.");
      }

      String basePath = rootPath + "qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/";
      writeOne(basePath + "executors/" + baseName + "Executor.java", makeExecutor(baseName));
      writeOne(basePath + "executors/io/" + baseName + "Input.java", makeInput(baseName));
      writeOne(basePath + "executors/io/" + baseName + "OutputInterface.java", makeOutputInterface(baseName));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String makeExecutor(String baseName)
   {
      return """
         package com.kingsrook.qqq.middleware.javalin.executors;
         
         
         import com.kingsrook.qqq.backend.core.exceptions.QException;
         import com.kingsrook.qqq.middleware.javalin.executors.io.${baseName}Input;
         import com.kingsrook.qqq.middleware.javalin.executors.io.${baseName}OutputInterface;
         
         
         /*******************************************************************************
          **
          *******************************************************************************/
         public class ${baseName}Executor extends AbstractMiddlewareExecutor<${baseName}Input, ${baseName}OutputInterface>
         {
         
            /***************************************************************************
             **
             ***************************************************************************/
            @Override
            public void execute(${baseName}Input input, ${baseName}OutputInterface output) throws QException
            {
            }
         
         }
         """.replaceAll("\\$\\{baseName}", baseName);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String makeInput(String baseName)
   {
      return """
         package com.kingsrook.qqq.middleware.javalin.executors.io;
         
         
         /*******************************************************************************
          **
          *******************************************************************************/
         public class ${baseName}Input extends AbstractMiddlewareInput
         {
         
         }
         """.replaceAll("\\$\\{baseName}", baseName);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String makeOutputInterface(String baseName)
   {
      return """
         package com.kingsrook.qqq.middleware.javalin.executors.io;
         
         
         /*******************************************************************************
          **
          *******************************************************************************/
         public interface ${baseName}OutputInterface extends AbstractMiddlewareOutputInterface
         {
         
         }
         """.replaceAll("\\$\\{baseName}", baseName);
   }

}
