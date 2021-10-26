package com.kingsrook.qqq.backend.core.modules;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class QModuleDispatcher
{
   private Map<String, String> backendTypeToModuleClassNameMap;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QModuleDispatcher()
   {
      backendTypeToModuleClassNameMap = new HashMap<>();
      backendTypeToModuleClassNameMap.put("rdbms", "com.kingsrook.qqq.backend.module.rdbms.RDBSMModule");
      backendTypeToModuleClassNameMap.put("nosql", "com.kingsrook.qqq.backend.module.nosql.NoSQLModule");
      // todo - let user define custom type -> classes
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QModuleInterface getQModule(QBackendMetaData backend) throws QModuleDispatchException
   {
      try
      {
         String className = backendTypeToModuleClassNameMap.get(backend.getType());
         if (className == null)
         {
            throw (new QModuleDispatchException("Unrecognized backend type [" + backend.getType() + "] in dispatcher."));
         }

         Class<?> moduleClass = Class.forName(className);
         return (QModuleInterface) moduleClass.getDeclaredConstructor().newInstance();
      }
      catch(QModuleDispatchException qmde)
      {
         throw (qmde);
      }
      catch(Exception e)
      {
         throw (new QModuleDispatchException("Error getting module", e));
      }
   }
}
