/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.InsertAction;
import com.kingsrook.qqq.backend.core.actions.MetaDataAction;
import com.kingsrook.qqq.backend.core.actions.QueryAction;
import com.kingsrook.qqq.backend.core.actions.RunFunctionAction;
import com.kingsrook.qqq.backend.core.actions.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.UpdateAction;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQFieldMappingAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.QInstanceAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteResult;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataResult;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;


/*******************************************************************************
 ** QQQ PicoCLI implementation.  Given a QInstance, produces an entire CLI
 ** for working with all tables in that instance.
 **
 ** Note:  Please do not use System.out or .err here -- rather, use the CommandLine
 ** object's out & err members - so the unit test can see the output!
 **
 *******************************************************************************/
public class QPicoCliImplementation
{
   public static final int DEFAULT_LIMIT = 20;

   private static QInstance qInstance;
   private static QSession session;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args) throws IOException
   {
      // todo - authentication
      // qInstance.addBackend(QMetaDataProvider.getQBackend());

      // parse args to look up metaData and prime instance
      if(args.length > 0 && args[0].startsWith("--qInstanceJsonFile="))
      {
         String filePath = args[0].replaceFirst("--.*=", "");
         String qInstanceJson = FileUtils.readFileToString(new File(filePath));
         qInstance = new QInstanceAdapter().jsonToQInstanceIncludingBackends(qInstanceJson);

         String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

         QPicoCliImplementation qPicoCliImplementation = new QPicoCliImplementation(qInstance);
         int exitCode = qPicoCliImplementation.runCli("qapi", subArgs);
         System.exit(exitCode);
      }
      else
      {
         System.err.println("To run this main class directly, you must specify: --qInstanceJsonFile=path/to/qInstance.json");
         System.exit(1);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPicoCliImplementation(QInstance qInstance)
   {
      QPicoCliImplementation.qInstance = qInstance;
   }



   /*******************************************************************************
    ** Driver method that uses System out & err streams.
    *
    *******************************************************************************/
   public int runCli(String name, String[] args)
   {
      return (runCli(name, args, System.out, System.err));
   }



   /*******************************************************************************
    ** Actual driver methods that takes streams as params.
    *
    ** examples - todo, make docs complete!
    **  my-app-cli [--all] [--format=]
    **  my-app-cli $table meta-data [--format=]
    **  my-app-cli $table query [--filterId=]|[--filter=]|[--criteria=...]
    **  my-app-cli $table get (--primaryKey=|--$uc=...)
    **  my-app-cli $table delete (--primaryKey=|--$uc=...)
    **  my-app-cli $table insert (--body=|--$field=...)
    **  my-app-cli $table update (--primaryKey=|--$uc=...) (--body=|--$field=...)
    **  my-app-cli $table process $process ...
    **
    *******************************************************************************/
   public int runCli(String name, String[] args, PrintStream out, PrintStream err)
   {
      CommandSpec topCommandSpec = new QCommandBuilder(qInstance).buildCommandSpec(name);

      CommandLine commandLine = new CommandLine(topCommandSpec);
      commandLine.setOut(new PrintWriter(out, true));
      commandLine.setErr(new PrintWriter(err, true));

      try
      {
         setupSession(args);
         // todo - think about, do some tables get turned off based on authentication?

         ParseResult parseResult = commandLine.parseArgs(args);

         ///////////////////////////////////////////
         // Did user request usage help (--help)? //
         ///////////////////////////////////////////
         if(commandLine.isUsageHelpRequested())
         {
            commandLine.usage(commandLine.getOut());
            return commandLine.getCommandSpec().exitCodeOnUsageHelp();
         }
         ////////////////////////////////////////////////
         // Did user request version help (--version)? //
         ////////////////////////////////////////////////
         else if(commandLine.isVersionHelpRequested())
         {
            commandLine.printVersionHelp(commandLine.getOut());
            return commandLine.getCommandSpec().exitCodeOnVersionHelp();
         }

         ///////////////////////////
         // else, run the command //
         ///////////////////////////
         return run(commandLine, parseResult);
      }
      catch(ParameterException ex)
      {
         //////////////////////////////////////////////////
         // handle command-line/param parsing exceptions //
         //////////////////////////////////////////////////
         commandLine.getErr().println(ex.getMessage());
         UnmatchedArgumentException.printSuggestions(ex, commandLine.getErr());
         ex.getCommandLine().usage(commandLine.getErr());
         return commandLine.getCommandSpec().exitCodeOnInvalidInput();
      }
      catch(Exception ex)
      {
         ///////////////////////////////////////////
         // handle exceptions from business logic //
         ///////////////////////////////////////////
         ex.printStackTrace();
         commandLine.getErr().println("Error: " + ex.getMessage());
         return (commandLine.getCommandSpec().exitCodeOnExecutionException());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void setupSession(String[] args) throws QModuleDispatchException
   {
      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface authenticationModule = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());

      // todo - does this need some per-provider logic actually?  mmm...
      Map<String, String> authenticationContext = new HashMap<>();
      authenticationContext.put("sessionId", System.getenv("sessionId"));
      session = authenticationModule.createSession(authenticationContext);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int run(CommandLine commandLine, ParseResult parseResult) throws QException
   {
      if(!parseResult.hasSubcommand())
      {
         return runTopLevelCommand(commandLine, parseResult);
      }
      else
      {
         String subCommandName = parseResult.subcommand().commandSpec().name();
         CommandLine subCommandLine = commandLine.getSubcommands().get(subCommandName);
         return runTableLevelCommand(subCommandLine, parseResult.subcommand());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableLevelCommand(CommandLine commandLine, ParseResult tableParseResult) throws QException
   {
      String tableName = tableParseResult.commandSpec().name();

      if(tableParseResult.hasSubcommand())
      {
         ParseResult subParseResult = tableParseResult.subcommand();
         String subCommandName = subParseResult.commandSpec().name();
         switch(subCommandName)
         {
            case "meta-data":
            {
               return runTableMetaData(commandLine, tableName, subParseResult);
            }
            case "query":
            {
               return runTableQuery(commandLine, tableName, subParseResult);
            }
            case "insert":
            {
               return runTableInsert(commandLine, tableName, subParseResult);
            }
            case "update":
            {
               return runTableUpdate(commandLine, tableName, subParseResult);
            }
            case "delete":
            {
               return runTableDelete(commandLine, tableName, subParseResult);
            }
            case "process":
            {
               CommandLine subCommandLine = commandLine.getSubcommands().get(subCommandName);
               return runTableProcess(subCommandLine, tableName, subParseResult);
            }
            default:
            {
               commandLine.getErr().println("Unknown command: " + subCommandName);
               commandLine.usage(commandLine.getOut());
               return commandLine.getCommandSpec().exitCodeOnUsageHelp();
            }
         }
      }
      else
      {
         commandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableProcess(CommandLine commandLine, String tableName, ParseResult subParseResult)
   {
      if(!subParseResult.hasSubcommand())
      {
         commandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }
      else
      {
         String subCommandName = subParseResult.subcommand().commandSpec().name();
         CommandLine subCommandLine = commandLine.getSubcommands().get(subCommandName);
         return runTableProcessLevelCommand(subCommandLine, tableName, subParseResult.subcommand());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableProcessLevelCommand(CommandLine subCommandLine, String tableName, ParseResult processParseResult)
   {
      String processName = processParseResult.commandSpec().name();
      QTableMetaData table = qInstance.getTable(tableName);
      QProcessMetaData process = qInstance.getProcess(processName);
      RunFunctionAction runFunctionAction = new RunFunctionAction();
      // todo!
      return 0;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableMetaData(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      TableMetaDataRequest tableMetaDataRequest = new TableMetaDataRequest(qInstance);
      tableMetaDataRequest.setSession(session);
      tableMetaDataRequest.setTableName(tableName);
      TableMetaDataAction tableMetaDataAction = new TableMetaDataAction();
      TableMetaDataResult tableMetaDataResult = tableMetaDataAction.execute(tableMetaDataRequest);
      commandLine.getOut().println(JsonUtils.toPrettyJson(tableMetaDataResult));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableQuery(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      QueryRequest queryRequest = new QueryRequest(qInstance);
      queryRequest.setSession(session);
      queryRequest.setTableName(tableName);
      queryRequest.setSkip(subParseResult.matchedOptionValue("skip", null));
      queryRequest.setLimit(subParseResult.matchedOptionValue("limit", DEFAULT_LIMIT));

      QQueryFilter filter = new QQueryFilter();
      queryRequest.setFilter(filter);

      String[] criteria = subParseResult.matchedOptionValue("criteria", new String[] {});
      for(String criterion : criteria)
      {
         // todo - parse!
         String[] parts = criterion.split(" ");
         QFilterCriteria qQueryCriteria = new QFilterCriteria();
         qQueryCriteria.setFieldName(parts[0]);
         qQueryCriteria.setOperator(QCriteriaOperator.valueOf(parts[1]));
         qQueryCriteria.setValues(List.of(parts[2]));
         filter.addCriteria(qQueryCriteria);
      }

      QueryAction queryAction = new QueryAction();
      QueryResult queryResult = queryAction.execute(queryRequest);
      commandLine.getOut().println(JsonUtils.toPrettyJson(queryResult));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableInsert(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      InsertRequest insertRequest = new InsertRequest(qInstance);
      insertRequest.setSession(session);
      insertRequest.setTableName(tableName);
      QTableMetaData table = qInstance.getTable(tableName);

      AbstractQFieldMapping<?> mapping = null;

      if(subParseResult.hasMatchedOption("--mapping"))
      {
         String json = subParseResult.matchedOptionValue("--mapping", "");
         mapping = new JsonToQFieldMappingAdapter().buildMappingFromJson(json);
      }

      /////////////////////////////////////////////
      // get the records that the user specified //
      /////////////////////////////////////////////
      List<QRecord> recordList;
      if(subParseResult.hasMatchedOption("--jsonBody"))
      {
         String json = subParseResult.matchedOptionValue("--jsonBody", "");
         recordList = new JsonToQRecordAdapter().buildRecordsFromJson(json, table, mapping);
      }
      else if(subParseResult.hasMatchedOption("--jsonFile"))
      {
         try
         {
            String path = subParseResult.matchedOptionValue("--jsonFile", "");
            String json = FileUtils.readFileToString(new File(path));
            recordList = new JsonToQRecordAdapter().buildRecordsFromJson(json, table, mapping);
         }
         catch(IOException e)
         {
            throw (new QException("Error building records from file:" + e.getMessage(), e));
         }
      }
      else if(subParseResult.hasMatchedOption("--csvFile"))
      {
         try
         {
            String path = subParseResult.matchedOptionValue("--csvFile", "");
            String csv = FileUtils.readFileToString(new File(path));
            recordList = new CsvToQRecordAdapter().buildRecordsFromCsv(csv, table, mapping);
         }
         catch(IOException e)
         {
            throw (new QException("Error building records from file:" + e.getMessage(), e));
         }
      }
      else
      {
         QRecord record = new QRecord();
         recordList = new ArrayList<>();
         recordList.add(record);

         boolean anyFields = false;
         for(OptionSpec matchedOption : subParseResult.matchedOptions())
         {
            if(matchedOption.longestName().startsWith("--field-"))
            {
               anyFields = true;
               String fieldName = matchedOption.longestName().substring(8);
               record.setValue(fieldName, matchedOption.getValue());
            }
         }

         if(!anyFields)
         {
            CommandLine subCommandLine = commandLine.getSubcommands().get("insert");
            subCommandLine.usage(commandLine.getOut());
            return commandLine.getCommandSpec().exitCodeOnUsageHelp();
         }
      }

      insertRequest.setRecords(recordList);

      InsertAction insertAction = new InsertAction();
      InsertResult insertResult = insertAction.execute(insertRequest);
      commandLine.getOut().println(JsonUtils.toPrettyJson(insertResult));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableUpdate(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      UpdateRequest updateRequest = new UpdateRequest(qInstance);
      updateRequest.setSession(session);
      updateRequest.setTableName(tableName);
      QTableMetaData table = qInstance.getTable(tableName);

      List<QRecord> recordList = new ArrayList<>();

      boolean anyFields = false;

      String primaryKeyOption = subParseResult.matchedOptionValue("--primaryKey", "");
      Serializable[] primaryKeyValues = primaryKeyOption.split(",");
      for(Serializable primaryKeyValue : primaryKeyValues)
      {
         QRecord record = new QRecord();

         recordList.add(record);
         record.setValue(table.getPrimaryKeyField(), primaryKeyValue);

         for(OptionSpec matchedOption : subParseResult.matchedOptions())
         {
            if(matchedOption.longestName().startsWith("--field-"))
            {
               anyFields = true;
               String fieldName = matchedOption.longestName().substring(8);
               record.setValue(fieldName, matchedOption.getValue());
            }
         }
      }

      if(!anyFields || recordList.isEmpty())
      {
         CommandLine subCommandLine = commandLine.getSubcommands().get("update");
         subCommandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }

      updateRequest.setRecords(recordList);

      UpdateAction updateAction = new UpdateAction();
      UpdateResult updateResult = updateAction.execute(updateRequest);
      commandLine.getOut().println(JsonUtils.toPrettyJson(updateResult));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableDelete(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      DeleteRequest deleteRequest = new DeleteRequest(qInstance);
      deleteRequest.setSession(session);
      deleteRequest.setTableName(tableName);

      /////////////////////////////////////////////
      // get the pKeys that the user specified //
      /////////////////////////////////////////////
      String primaryKeyOption = subParseResult.matchedOptionValue("--primaryKey", "");
      Serializable[] primaryKeyValues = primaryKeyOption.split(",");
      deleteRequest.setPrimaryKeys(Arrays.asList(primaryKeyValues));

      DeleteAction deleteAction = new DeleteAction();
      DeleteResult deleteResult = deleteAction.execute(deleteRequest);
      commandLine.getOut().println(JsonUtils.toPrettyJson(deleteResult));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTopLevelCommand(CommandLine commandLine, ParseResult parseResult) throws QException
   {
      if(parseResult.hasMatchedOption("--meta-data"))
      {
         MetaDataRequest metaDataRequest = new MetaDataRequest(qInstance);
         metaDataRequest.setSession(session);
         MetaDataAction metaDataAction = new MetaDataAction();
         MetaDataResult metaDataResult = metaDataAction.execute(metaDataRequest);
         commandLine.getOut().println(JsonUtils.toPrettyJson(metaDataResult));
         return commandLine.getCommandSpec().exitCodeOnSuccess();
      }

      commandLine.usage(commandLine.getOut());
      return commandLine.getCommandSpec().exitCodeOnUsageHelp();
   }
}
