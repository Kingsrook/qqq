/*
 * Copyright © 2022-2022. Nutrifresh Services <contact@nutrifreshservices.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.module.api;


import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.api.actions.BaseAPIActionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


/*******************************************************************************
 ** Utility methods for working with EasyPost API
 *******************************************************************************/
public class EasyPostUtils extends BaseAPIActionUtil
{
   private static final Logger LOG = LogManager.getLogger(EasyPostUtils.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected JSONObject recordToJsonObject(QTableMetaData table, QRecord record)
   {
      JSONObject inner = super.recordToJsonObject(table, record);
      JSONObject outer = new JSONObject();
      outer.put(getBackendDetails(table).getTableWrapperObjectName(), inner);
      return (outer);
   }
}
