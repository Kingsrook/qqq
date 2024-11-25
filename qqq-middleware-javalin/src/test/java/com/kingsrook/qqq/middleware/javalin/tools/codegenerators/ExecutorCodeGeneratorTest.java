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
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ExecutorCodeGenerator 
 *******************************************************************************/
class ExecutorCodeGeneratorTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws IOException
   {
      String rootPath = "/tmp/" + UUID.randomUUID() + "/";
      File   dir      = new File(rootPath + "/qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/executors/io");
      assertTrue(dir.mkdirs());
      new ExecutorCodeGenerator().writeAllFiles(rootPath, "SomeTest");

      File anExpectedFile = new File(dir.getAbsolutePath() + "/SomeTestOutputInterface.java");
      assertTrue(anExpectedFile.exists());

      FileUtils.deleteDirectory(new File(rootPath));
   }

}