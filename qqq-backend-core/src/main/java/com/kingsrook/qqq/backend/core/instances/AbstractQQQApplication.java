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

package com.kingsrook.qqq.backend.core.instances;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.validation.plugins.QInstanceValidatorPluginInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Base class to provide the definition of a QQQ-based application.
 **
 ** Essentially, just how to define its meta-data - in the form of a QInstance.
 **
 ** Also provides means to define the instance validation plugins to be used.
 *******************************************************************************/
public abstract class AbstractQQQApplication
{

   /***************************************************************************
    **
    ***************************************************************************/
   public abstract QInstance defineQInstance() throws QException;



   /***************************************************************************
    **
    ***************************************************************************/
   public QInstance defineValidatedQInstance() throws QException, QInstanceValidationException
   {
      QInstance qInstance = defineQInstance();

      QInstanceValidator.removeAllValidatorPlugins();
      for(QInstanceValidatorPluginInterface<?> validatorPlugin : CollectionUtils.nonNullList(getValidatorPlugins()))
      {
         QInstanceValidator.addValidatorPlugin(validatorPlugin);
      }

      QInstanceValidator qInstanceValidator = new QInstanceValidator();
      qInstanceValidator.validate(qInstance);
      return (qInstance);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected List<QInstanceValidatorPluginInterface<?>> getValidatorPlugins()
   {
      return new ArrayList<>();
   }
}
