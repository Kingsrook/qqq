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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.metadata.MetaDataAction;
import com.kingsrook.qqq.backend.core.actions.metadata.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.reporting.ReportAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQFieldMappingAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.QInstanceAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportOutput;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.Auth0AuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.config.Configurator;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.utils.Log;
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
   public static final int DEFAULT_QUERY_LIMIT = 20;

   private static QInstance qInstance;
   private static QSession  session;



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
         String filePath      = args[0].replaceFirst("--.*=", "");
         String qInstanceJson = FileUtils.readFileToString(new File(filePath));
         qInstance = new QInstanceAdapter().jsonToQInstanceIncludingBackends(qInstanceJson);

         String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

         QPicoCliImplementation qPicoCliImplementation = new QPicoCliImplementation(qInstance);
         int                    exitCode               = qPicoCliImplementation.runCli("qapi", subArgs);
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
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // use the qqq-picocli log4j config, less the system property log4j.configurationFile was set by the runner //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(System.getProperty("log4j.configurationFile") == null)
      {
         Configurator.initialize(null, "qqq-picocli-log4j2.xml");
      }

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
   private static Optional<Dotenv> loadDotEnv()
   {
      Optional<Dotenv> dotenvOptional = Optional.empty();
      try
      {
         dotenvOptional = Optional.of(Dotenv.configure().load());
      }
      catch(Exception e)
      {
         Log.info("No session information found in environment");
      }

      return (dotenvOptional);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void setupSession(String[] args) throws QModuleDispatchException, QAuthenticationException
   {
      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());

      try
      {
         ////////////////////////////////////
         // look for .env environment file //
         ////////////////////////////////////
         String           sessionId = null;
         Optional<Dotenv> dotenv    = loadDotEnv();
         if(dotenv.isPresent())
         {
            sessionId = dotenv.get().get("SESSION_ID");
         }

         Map<String, String> authenticationContext = new HashMap<>();
         if(sessionId == null && authenticationModule instanceof Auth0AuthenticationModule)
         {
            LineReader lr      = LineReaderBuilder.builder().build();
            String     tokenId = lr.readLine("Create a .env file with the contents of the Auth0 JWT Id Token in the variable 'SESSION_ID': \nPress enter once complete...");
            dotenv = loadDotEnv();
            if(dotenv.isPresent())
            {
               sessionId = dotenv.get().get("SESSION_ID");
            }
         }

         authenticationContext.put("sessionId", sessionId);

         // todo - does this need some per-provider logic actually?  mmm...
         session = authenticationModule.createSession(qInstance, authenticationContext);
      }
      catch(QAuthenticationException qae)
      {
         throw (qae);
      }

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
         ParseResult subParseResult = parseResult.subcommand();
         String      subCommandName = subParseResult.commandSpec().name();
         CommandLine subCommandLine = commandLine.getSubcommands().get(subCommandName);
         switch(subCommandName)
         {
            case "processes":
            {
               return runProcessCommand(subCommandLine, subParseResult);
            }
            default:
            {
               /////////////////////////////////////////////////////////
               // by default, assume the command here is a table name //
               /////////////////////////////////////////////////////////
               return runTableLevelCommand(subCommandLine, subParseResult);
            }
         }
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
         String      subCommandName = subParseResult.commandSpec().name();
         switch(subCommandName)
         {
            case "meta-data":
            {
               return runTableMetaData(commandLine, tableName, subParseResult);
            }
            case "count":
            {
               return runTableCount(commandLine, tableName, subParseResult);
            }
            case "get":
            {
               CommandLine subCommandLine = commandLine.getSubcommands().get(subCommandName);
               return runTableGet(commandLine, tableName, subParseResult, subCommandLine);
            }
            case "query":
            {
               return runTableQuery(commandLine, tableName, subParseResult);
            }
            case "export":
            {
               return runTableExport(commandLine, tableName, subParseResult);
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
               return runProcessCommand(subCommandLine, subParseResult);
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
    ** Handle a command up to the point where 'process' was given
    *******************************************************************************/
   private int runProcessCommand(CommandLine commandLine, ParseResult subParseResult)
   {
      if(!subParseResult.hasSubcommand())
      {
         ////////////////////////////////////////////////////////////////
         // process name must be a sub-command, so, error if not given //
         ////////////////////////////////////////////////////////////////
         commandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }
      else
      {
         ///////////////////////////////////////////
         // move on to running the actual process //
         ///////////////////////////////////////////
         String      subCommandName = subParseResult.subcommand().commandSpec().name();
         CommandLine subCommandLine = commandLine.getSubcommands().get(subCommandName);
         return runActualProcess(subCommandLine, subParseResult.subcommand());
      }
   }



   /*******************************************************************************
    ** actually run a process (the process name should be at the start of the sub-command line)
    *******************************************************************************/
   private int runActualProcess(CommandLine subCommandLine, ParseResult processParseResult)
   {
      String           processName = processParseResult.commandSpec().name();
      QProcessMetaData process     = qInstance.getProcess(processName);
      RunProcessInput  request     = new RunProcessInput(qInstance);

      request.setSession(session);
      request.setProcessName(processName);
      request.setCallback(new PicoCliProcessCallback(subCommandLine));

      for(OptionSpec matchedOption : processParseResult.matchedOptions())
      {
         if(matchedOption.longestName().startsWith("--field-"))
         {
            String fieldName = matchedOption.longestName().substring(8);
            request.addValue(fieldName, matchedOption.getValue());
         }
      }

      try
      {
         RunProcessOutput result = new RunProcessAction().execute(request);
         subCommandLine.getOut().println("Process Results: "); // todo better!!
         for(QFieldMetaData outputField : process.getOutputFields())
         {
            subCommandLine.getOut().format("   %s: %s\n", outputField.getLabel(), result.getValues().get(outputField.getName()));
         }

         if(result.getException().isPresent())
         {
            // todo - user-facing, similar to javalin
            subCommandLine.getOut().println("Process Error message: " + result.getException().get().getMessage());
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         subCommandLine.getOut().println("Caught Exception running process.  See stack trace above for details.");
         return 1;
      }

      return 0;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableMetaData(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      TableMetaDataInput tableMetaDataInput = new TableMetaDataInput(qInstance);
      tableMetaDataInput.setSession(session);
      tableMetaDataInput.setTableName(tableName);
      TableMetaDataAction tableMetaDataAction = new TableMetaDataAction();
      TableMetaDataOutput tableMetaDataOutput = tableMetaDataAction.execute(tableMetaDataInput);
      commandLine.getOut().println(JsonUtils.toPrettyJson(tableMetaDataOutput));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableCount(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      CountInput countInput = new CountInput(qInstance);
      countInput.setSession(session);
      countInput.setTableName(tableName);
      countInput.setFilter(generateQueryFilter(subParseResult));

      CountAction countAction = new CountAction();
      CountOutput countOutput = countAction.execute(countInput);
      commandLine.getOut().println(JsonUtils.toPrettyJson(countOutput));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableGet(CommandLine commandLine, String tableName, ParseResult subParseResult, CommandLine subCommandLine) throws QException
   {
      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(session);
      queryInput.setTableName(tableName);
      queryInput.setSkip(subParseResult.matchedOptionValue("skip", null));
      String primaryKeyValue = subParseResult.matchedPositionalValue(0, null);

      if(primaryKeyValue == null)
      {
         subCommandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }

      QTableMetaData table = queryInput.getTable();
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName(table.getPrimaryKeyField())
            .withOperator(QCriteriaOperator.EQUALS)
            .withValues(List.of(primaryKeyValue)));
      queryInput.setFilter(filter);

      QueryAction   queryAction = new QueryAction();
      QueryOutput   queryOutput = queryAction.execute(queryInput);
      List<QRecord> records     = queryOutput.getRecords();
      if(records.isEmpty())
      {
         commandLine.getOut().println("No " + table.getLabel() + " found for " + table.getField(table.getPrimaryKeyField()).getLabel() + ": " + primaryKeyValue);
         return commandLine.getCommandSpec().exitCodeOnInvalidInput();
      }
      else
      {
         commandLine.getOut().println(JsonUtils.toPrettyJson(records.get(0)));
         return commandLine.getCommandSpec().exitCodeOnSuccess();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableQuery(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(session);
      queryInput.setTableName(tableName);
      queryInput.setSkip(subParseResult.matchedOptionValue("skip", null));
      queryInput.setLimit(subParseResult.matchedOptionValue("limit", null));
      queryInput.setFilter(generateQueryFilter(subParseResult));

      QueryAction queryAction = new QueryAction();
      QueryOutput queryOutput = queryAction.execute(queryInput);
      commandLine.getOut().println(JsonUtils.toPrettyJson(queryOutput));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableExport(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      String filename = subParseResult.matchedOptionValue("--filename", "");

      /////////////////////////////////////////////////////////////////////////////////////////
      // if a format query param wasn't given, then try to get file extension from file name //
      /////////////////////////////////////////////////////////////////////////////////////////
      ReportFormat reportFormat;
      if(filename.contains("."))
      {
         reportFormat = ReportFormat.fromString(filename.substring(filename.lastIndexOf(".") + 1));
      }
      else
      {
         throw (new QUserFacingException("File name did not contain an extension, so report format could not be inferred."));
      }

      OutputStream outputStream;
      try
      {
         outputStream = new FileOutputStream(filename);
      }
      catch(Exception e)
      {
         throw (new QException("Error opening report file: " + e.getMessage(), e));
      }

      try
      {
         /////////////////////////////////////////////
         // set up the report action's input object //
         /////////////////////////////////////////////
         ReportInput reportInput = new ReportInput(qInstance);
         reportInput.setSession(session);
         reportInput.setTableName(tableName);
         reportInput.setReportFormat(reportFormat);
         reportInput.setFilename(filename);
         reportInput.setReportOutputStream(outputStream);
         reportInput.setLimit(subParseResult.matchedOptionValue("limit", null));

         reportInput.setQueryFilter(generateQueryFilter(subParseResult));

         String fieldNames = subParseResult.matchedOptionValue("--fieldNames", "");
         if(StringUtils.hasContent(fieldNames))
         {
            reportInput.setFieldNames(Arrays.asList(fieldNames.split(",")));
         }

         ReportOutput reportOutput = new ReportAction().execute(reportInput);

         commandLine.getOut().println("Wrote " + reportOutput.getRecordCount() + " records to file " + filename);
         return commandLine.getCommandSpec().exitCodeOnSuccess();
      }
      finally
      {
         try
         {
            outputStream.close();
         }
         catch(IOException e)
         {
            throw (new QException("Error closing report file", e));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QQueryFilter generateQueryFilter(ParseResult subParseResult)
   {
      QQueryFilter filter = new QQueryFilter();

      String[] criteria = subParseResult.matchedOptionValue("criteria", new String[] {});
      for(String criterion : criteria)
      {
         // todo - parse!
         String[]        parts          = criterion.split(" ");
         QFilterCriteria qQueryCriteria = new QFilterCriteria();
         qQueryCriteria.setFieldName(parts[0]);
         qQueryCriteria.setOperator(QCriteriaOperator.valueOf(parts[1]));
         qQueryCriteria.setValues(List.of(parts[2]));
         filter.addCriteria(qQueryCriteria);
      }

      return filter;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableInsert(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(session);
      insertInput.setTableName(tableName);
      QTableMetaData table = qInstance.getTable(tableName);

      AbstractQFieldMapping<?> mapping = null;

      if(subParseResult.hasMatchedOption("--mapping"))
      {
         String json = subParseResult.matchedOptionValue("--mapping", "");
         mapping = new JsonToQFieldMappingAdapter().buildMappingFromJson(json);
      }
      else
      {
         mapping = new QKeyBasedFieldMapping();
         for(Map.Entry<String, QFieldMetaData> entry : table.getFields().entrySet())
         {
            ((QKeyBasedFieldMapping) mapping).addMapping(entry.getKey(), entry.getValue().getLabel());
         }
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
            String csv  = FileUtils.readFileToString(new File(path));
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

      insertInput.setRecords(recordList);

      InsertAction insertAction = new InsertAction();
      InsertOutput insertOutput = insertAction.execute(insertInput);
      commandLine.getOut().println(JsonUtils.toPrettyJson(insertOutput));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableUpdate(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      UpdateInput updateInput = new UpdateInput(qInstance);
      updateInput.setSession(session);
      updateInput.setTableName(tableName);
      QTableMetaData table = qInstance.getTable(tableName);

      List<QRecord> recordsToUpdate = new ArrayList<>();
      boolean       anyFields       = false;

      String   primaryKeyOption = subParseResult.matchedOptionValue("--primaryKey", "");
      String[] criteria         = subParseResult.matchedOptionValue("criteria", new String[] {});

      if(StringUtils.hasContent(primaryKeyOption))
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // if the primaryKey option was given, split it up and seed the recordToUpdate list //
         //////////////////////////////////////////////////////////////////////////////////////
         Serializable[] primaryKeyValues = primaryKeyOption.split(",");
         for(Serializable primaryKeyValue : primaryKeyValues)
         {
            recordsToUpdate.add(new QRecord().withValue(table.getPrimaryKeyField(), primaryKeyValue));
         }
      }
      else if(criteria.length > 0)
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // else if criteria were given, execute the query for the lsit of records to update //
         //////////////////////////////////////////////////////////////////////////////////////
         for(QRecord qRecord : executeQuery(tableName, subParseResult))
         {
            recordsToUpdate.add(new QRecord().withValue(table.getPrimaryKeyField(), qRecord.getValue(table.getPrimaryKeyField())));
         }
      }
      else
      {
         commandLine.getErr().println("Error: Either primaryKey or criteria must be specified.");
         CommandLine subCommandLine = commandLine.getSubcommands().get("update");
         subCommandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }

      ///////////////////////////////////////////////////
      // make sure at least one --field- arg was given //
      ///////////////////////////////////////////////////
      for(OptionSpec matchedOption : subParseResult.matchedOptions())
      {
         if(matchedOption.longestName().startsWith("--field-"))
         {
            anyFields = true;
         }
      }

      if(!anyFields)
      {
         commandLine.getErr().println("Error: At least one field to update must be specified.");
         CommandLine subCommandLine = commandLine.getSubcommands().get("update");
         subCommandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }

      if(recordsToUpdate.isEmpty())
      {
         commandLine.getErr().println("No rows to update were found.");
         CommandLine subCommandLine = commandLine.getSubcommands().get("update");
         subCommandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }

      for(QRecord record : recordsToUpdate)
      {
         for(OptionSpec matchedOption : subParseResult.matchedOptions())
         {
            if(matchedOption.longestName().startsWith("--field-"))
            {
               String fieldName = matchedOption.longestName().substring(8);
               record.setValue(fieldName, matchedOption.getValue());
            }
         }
      }

      updateInput.setRecords(recordsToUpdate);

      UpdateAction updateAction = new UpdateAction();
      UpdateOutput updateResult = updateAction.execute(updateInput);
      commandLine.getOut().println(JsonUtils.toPrettyJson(updateResult));
      return commandLine.getCommandSpec().exitCodeOnSuccess();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int runTableDelete(CommandLine commandLine, String tableName, ParseResult subParseResult) throws QException
   {
      DeleteInput deleteInput = new DeleteInput(qInstance);
      deleteInput.setSession(session);
      deleteInput.setTableName(tableName);

      /////////////////////////////////////////////
      // get the pKeys that the user specified //
      /////////////////////////////////////////////
      String   primaryKeyOption = subParseResult.matchedOptionValue("--primaryKey", "");
      String[] criteria         = subParseResult.matchedOptionValue("criteria", new String[] {});

      if(StringUtils.hasContent(primaryKeyOption))
      {
         deleteInput.setPrimaryKeys(Arrays.asList(primaryKeyOption.split(",")));
      }
      else if(criteria.length > 0)
      {
         deleteInput.setQueryFilter(generateQueryFilter(subParseResult));
      }
      else
      {
         commandLine.getErr().println("Error: Either primaryKey or criteria must be specified.");
         CommandLine subCommandLine = commandLine.getSubcommands().get("delete");
         subCommandLine.usage(commandLine.getOut());
         return commandLine.getCommandSpec().exitCodeOnUsageHelp();
      }

      DeleteAction deleteAction = new DeleteAction();
      DeleteOutput deleteResult = deleteAction.execute(deleteInput);
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
         MetaDataInput metaDataInput = new MetaDataInput(qInstance);
         metaDataInput.setSession(session);
         MetaDataAction metaDataAction = new MetaDataAction();
         MetaDataOutput metaDataOutput = metaDataAction.execute(metaDataInput);
         commandLine.getOut().println(JsonUtils.toPrettyJson(metaDataOutput));
         return commandLine.getCommandSpec().exitCodeOnSuccess();
      }

      commandLine.usage(commandLine.getOut());
      return commandLine.getCommandSpec().exitCodeOnUsageHelp();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> executeQuery(String tableName, ParseResult subParseResult) throws QException
   {
      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(session);
      queryInput.setTableName(tableName);
      queryInput.setFilter(generateQueryFilter(subParseResult));

      QueryAction queryAction = new QueryAction();
      QueryOutput queryOutput = queryAction.execute(queryInput);
      return (queryOutput.getRecords());
   }
}
