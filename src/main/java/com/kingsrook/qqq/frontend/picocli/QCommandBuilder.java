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

package com.kingsrook.qqq.frontend.picocli;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import picocli.CommandLine;


/*******************************************************************************
 ** Helper class for QPicCliImplementation to build the Command
 **
 *******************************************************************************/
public class QCommandBuilder
{
   private final QInstance qInstance;



   /*******************************************************************************
    ** Constructor.
    **
    *******************************************************************************/
   public QCommandBuilder(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   CommandLine.Model.CommandSpec buildCommandSpec(String topCommandName)
   {
      //////////////////////////////////
      // define the top-level command //
      //////////////////////////////////
      CommandLine.Model.CommandSpec topCommandSpec = CommandLine.Model.CommandSpec.create();
      topCommandSpec.name(topCommandName);
      topCommandSpec.version(topCommandName + " v1.0"); // todo... uh?
      topCommandSpec.mixinStandardHelpOptions(true); // usageHelp and versionHelp options
      topCommandSpec.addOption(CommandLine.Model.OptionSpec.builder("-m", "--meta-data")
         .type(boolean.class)
         .description("Output the meta-data for this CLI")
         .build());

      /////////////////////////////////////
      // add each table as a sub-command //
      /////////////////////////////////////
      qInstance.getTables().keySet().stream().sorted().forEach(tableName ->
      {
         QTableMetaData table = qInstance.getTable(tableName);

         CommandLine.Model.CommandSpec tableCommand = CommandLine.Model.CommandSpec.create();
         topCommandSpec.addSubcommand(table.getName(), tableCommand);

         ///////////////////////////////////////////////////
         // add table-specific sub-commands for the table //
         ///////////////////////////////////////////////////
         tableCommand.addSubcommand("meta-data", defineMetaDataCommand(table));
         tableCommand.addSubcommand("query", defineQueryCommand(table));
         tableCommand.addSubcommand("insert", defineInsertCommand(table));
         tableCommand.addSubcommand("update", defineUpdateCommand(table));
         tableCommand.addSubcommand("delete", defineDeleteCommand(table));

         List<QProcessMetaData> processes = qInstance.getProcessesForTable(tableName);
         if(CollectionUtils.nullSafeHasContents(processes))
         {
            tableCommand.addSubcommand("process", defineTableProcessesCommand(table, processes));
         }
      });
      return topCommandSpec;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CommandLine.Model.CommandSpec defineMetaDataCommand(QTableMetaData table)
   {
      return CommandLine.Model.CommandSpec.create();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CommandLine.Model.CommandSpec defineQueryCommand(QTableMetaData table)
   {
      CommandLine.Model.CommandSpec queryCommand = CommandLine.Model.CommandSpec.create();
      queryCommand.addOption(CommandLine.Model.OptionSpec.builder("-l", "--limit")
         .type(int.class)
         .build());
      queryCommand.addOption(CommandLine.Model.OptionSpec.builder("-s", "--skip")
         .type(int.class)
         .build());
      queryCommand.addOption(CommandLine.Model.OptionSpec.builder("-c", "--criteria")
         .type(String[].class)
         .build());

      // todo - add the fields as explicit params?

      return queryCommand;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CommandLine.Model.CommandSpec defineUpdateCommand(QTableMetaData table)
   {
      CommandLine.Model.CommandSpec updateCommand = CommandLine.Model.CommandSpec.create();

      /*
      updateCommand.addOption(CommandLine.Model.OptionSpec.builder("--jsonBody")
         .type(String.class)
         .build());

      updateCommand.addOption(CommandLine.Model.OptionSpec.builder("--jsonFile")
         .type(String.class)
         .build());

      updateCommand.addOption(CommandLine.Model.OptionSpec.builder("--csvFile")
         .type(String.class)
         .build());

      updateCommand.addOption(CommandLine.Model.OptionSpec.builder("--mapping")
         .type(String.class)
         .build());
      */

      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());
      updateCommand.addOption(CommandLine.Model.OptionSpec.builder("--primaryKey")
         // type(getClassForField(primaryKeyField))
         .type(String.class) // todo - mmm, better as picocli's "compound" thing, w/ the actual pkey's type?
         .build());

      for(QFieldMetaData field : table.getFields().values())
      {
         if(!field.equals(primaryKeyField))
         {
            updateCommand.addOption(CommandLine.Model.OptionSpec.builder("--field-" + field.getName())
               .type(getClassForField(field))
               .build());
         }
      }
      return updateCommand;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CommandLine.Model.CommandSpec defineInsertCommand(QTableMetaData table)
   {
      CommandLine.Model.CommandSpec insertCommand = CommandLine.Model.CommandSpec.create();

      insertCommand.addOption(CommandLine.Model.OptionSpec.builder("--jsonBody")
         .type(String.class)
         .build());

      insertCommand.addOption(CommandLine.Model.OptionSpec.builder("--jsonFile")
         .type(String.class)
         .build());

      insertCommand.addOption(CommandLine.Model.OptionSpec.builder("--csvFile")
         .type(String.class)
         .build());

      insertCommand.addOption(CommandLine.Model.OptionSpec.builder("--mapping")
         .type(String.class)
         .build());

      for(QFieldMetaData field : table.getFields().values())
      {
         insertCommand.addOption(CommandLine.Model.OptionSpec.builder("--field-" + field.getName())
            .type(getClassForField(field))
            .build());
      }
      return insertCommand;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CommandLine.Model.CommandSpec defineDeleteCommand(QTableMetaData table)
   {
      CommandLine.Model.CommandSpec deleteCommand = CommandLine.Model.CommandSpec.create();

      deleteCommand.addOption(CommandLine.Model.OptionSpec.builder("--primaryKey")
         .type(String.class) // todo - mmm, better as picocli's "compound" thing, w/ the actual pkey's type?
         .build());

      return deleteCommand;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CommandLine.Model.CommandSpec defineTableProcessesCommand(QTableMetaData table, List<QProcessMetaData> processes)
   {
      CommandLine.Model.CommandSpec processesCommand = CommandLine.Model.CommandSpec.create();

      for(QProcessMetaData process : processes)
      {
         CommandLine.Model.CommandSpec processCommand = CommandLine.Model.CommandSpec.create();
         processesCommand.addSubcommand(process.getName(), processCommand);
      }

      return (processesCommand);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   private Class<?> getClassForField(QFieldMetaData field)
   {
      // @formatter:off // IJ can't do new-style switch correctly yet...
      return switch(field.getType())
      {
         case STRING, TEXT, HTML, PASSWORD -> String.class;
         case INTEGER -> Integer.class;
         case DECIMAL -> BigDecimal.class;
         case DATE -> LocalDate.class;
         // case TIME -> LocalTime.class;
         case DATE_TIME -> LocalDateTime.class;
      };
      // @formatter:on
   }

}
