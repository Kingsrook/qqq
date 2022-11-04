/*
 * Copyright © 2022-2022. Nutrifresh Services <contact@nutrifreshservices.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.module.api;


import java.io.IOException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.api.actions.BaseAPIActionUtil;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
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
    ** Build an HTTP Entity (e.g., for a PUT or POST) from a QRecord.  Can be
    ** overridden if an API doesn't do a basic json object.  Or, can override a
    ** helper method, such as recordToJsonObject.
    **
    *******************************************************************************/
   @Override
   protected AbstractHttpEntity recordToEntity(QTableMetaData table, QRecord record) throws IOException
   {
      JSONObject body      = recordToJsonObject(table, record);
      JSONObject wrapper   = new JSONObject();
      String     tablePath = getBackendDetails(table).getTableWrapperObjectName();
      wrapper.put(tablePath, body);
      String json = wrapper.toString();
      LOG.debug(json);
      return (new StringEntity(json));
   }

}
