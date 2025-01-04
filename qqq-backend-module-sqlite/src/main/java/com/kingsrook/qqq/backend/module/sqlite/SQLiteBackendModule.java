package com.kingsrook.qqq.backend.module.sqlite;


import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendModule;
import com.kingsrook.qqq.backend.module.sqlite.model.metadata.SQLiteBackendMetaData;
import com.kingsrook.qqq.backend.module.sqlite.model.metadata.SQLiteTableBackendDetails;


/*******************************************************************************
 **
 *******************************************************************************/
public class SQLiteBackendModule extends RDBMSBackendModule
{
   private static final QLogger LOG = QLogger.getLogger(SQLiteBackendModule.class);

   private static final String NAME = "sqlite";

   static
   {
      QBackendModuleDispatcher.registerBackendModule(new SQLiteBackendModule());
   }

   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   public String getBackendType()
   {
      return NAME;
   }



   /*******************************************************************************
    ** Method to identify the class used for backend meta data for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QBackendMetaData> getBackendMetaDataClass()
   {
      return (SQLiteBackendMetaData.class);
   }



   /*******************************************************************************
    ** Method to identify the class used for table-backend details for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QTableBackendDetails> getTableBackendDetailsClass()
   {
      return (SQLiteTableBackendDetails.class);
   }

}
