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

package com.kingsrook.qqq.middleware.javalin.tools;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import com.fasterxml.jackson.databind.MapperFeature;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.YamlUtils;
import com.kingsrook.qqq.middleware.javalin.specs.AbstractMiddlewareVersion;
import com.kingsrook.qqq.middleware.javalin.specs.v1.MiddlewareVersionV1;
import com.kingsrook.qqq.openapi.model.OpenAPI;
import picocli.CommandLine;


/*******************************************************************************
 **
 *******************************************************************************/
@CommandLine.Command(name = "publishAPI")
public class PublishAPI implements Callable<Integer>
{
   @CommandLine.Option(names = { "-r", "--repoRoot" })
   private String repoRoot;

   @CommandLine.Option(names = { "--sortFileContentsForHuman" }, description = "By default, properties in the yaml are sorted alphabetically, to help with stability (for diffing).  This option preserves the 'natural' order of the file, so may look a little bette for human consumption")
   private boolean sortFileContentsForHuman = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args) throws Exception
   {
      // for a run from the IDE, to override args... args = new String[] { "-r", "/Users/dkelkhoff/git/kingsrook/qqq/" };
      int exitCode = new CommandLine(new PublishAPI()).execute(args);
      System.exit(exitCode);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer call() throws Exception
   {
      AbstractMiddlewareVersion middlewareVersion = new MiddlewareVersionV1();

      if(!StringUtils.hasContent(repoRoot))
      {
         throw (new QException("Repo root argument was not given."));
      }

      if(!new File(repoRoot).exists())
      {
         throw (new QException("Repo root directory [" + repoRoot + "] was not found."));
      }

      String allApisPath = repoRoot + "/" + APIUtils.PUBLISHED_API_LOCATION + "/";
      if(!new File(allApisPath).exists())
      {
         throw (new QException("APIs directory [" + allApisPath + "] was not found."));
      }

      File versionDirectory = new File(allApisPath + middlewareVersion.getVersion() + "/");
      if(!versionDirectory.exists())
      {
         if(!versionDirectory.mkdirs())
         {
            // CTEngCliUtils.printError("Error: An error occurred creating directory [" + apiDirectory.getPath() + "].");
            System.err.println("Error: An error occurred creating directory [" + versionDirectory.getPath() + "].");
            return (1);
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // build the openapi spec - then run it through a "grouping" function, which will make several //
      // subsets of it (e.g., grouped by table mostly) - then we'll write out each such file         //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      OpenAPI openAPI = middlewareVersion.generate("qqq");
      String yaml = YamlUtils.toYaml(openAPI, mapper ->
      {
         if(sortFileContentsForHuman)
         {
            ////////////////////////////////////////////////
            // this is actually the default mapper config //
            ////////////////////////////////////////////////
         }
         else
         {
            mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
         }
      });

      writeFile(yaml, versionDirectory, "openapi.yaml");

      //////////////////////////////////////////////////////////////////////////////////////
      // if we want to split up by some paths, components, we could use a version of this //
      //////////////////////////////////////////////////////////////////////////////////////
      // Map<String, Map<String, Object>> groupedPaths = APIUtils.splitUpYamlForPublishing(yaml);
      // for(String name : groupedPaths.keySet())
      // {
      //    writeFile(groupedPaths.get(name), versionDirectory, name + ".yaml");
      // }
      // CTEngCliUtils.printSuccess("Files for [" + apiInstanceMetaData.getName() + "] [" + apiVersion + "] have been successfully published.");
      // System.out.println("Files for [" + middlewareVersion.getClass().getSimpleName() + "] have been successfully published.");

      return (0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeFile(String yaml, File directory, String fileBaseName) throws IOException
   {
      String yamlFileName = directory.getAbsolutePath() + "/" + fileBaseName;
      Path   yamlPath     = Paths.get(yamlFileName);
      Files.write(yamlPath, yaml.getBytes());
      System.out.println("Wrote [" + yamlPath + "]");
   }

}
