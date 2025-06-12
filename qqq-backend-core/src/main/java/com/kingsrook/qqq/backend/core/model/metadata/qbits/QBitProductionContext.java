/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.qbits;


import java.util.Collections;
import java.util.List;
import java.util.Stack;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;


/*******************************************************************************
 ** While a qbit is being produced, track the context of the current config
 ** and metaDataProducerMultiOutput that is being used.  also, in case one
 ** qbit produces another, push these contextual objects on a stack.
 *******************************************************************************/
public class QBitProductionContext
{
   private static final QLogger LOG = QLogger.getLogger(QBitProductionContext.class);

   private static Stack<QBitConfig>                  qbitConfigStack                  = new Stack<>();
   private static Stack<MetaDataProducerMultiOutput> metaDataProducerMultiOutputStack = new Stack<>();



   /***************************************************************************
    **
    ***************************************************************************/
   public static void pushQBitConfig(QBitConfig qBitConfig)
   {
      qbitConfigStack.push(qBitConfig);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static QBitConfig peekQBitConfig()
   {
      if(qbitConfigStack.isEmpty())
      {
         LOG.warn("Request to peek at empty QBitProductionContext configStack - returning null");
         return (null);
      }
      return qbitConfigStack.peek();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void popQBitConfig()
   {
      if(qbitConfigStack.isEmpty())
      {
         LOG.warn("Request to pop empty QBitProductionContext configStack - returning with noop");
         return;
      }

      qbitConfigStack.pop();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void pushMetaDataProducerMultiOutput(MetaDataProducerMultiOutput metaDataProducerMultiOutput)
   {
      metaDataProducerMultiOutputStack.push(metaDataProducerMultiOutput);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static MetaDataProducerMultiOutput peekMetaDataProducerMultiOutput()
   {
      if(metaDataProducerMultiOutputStack.isEmpty())
      {
         LOG.warn("Request to peek at empty QBitProductionContext configStack - returning null");
         return (null);
      }
      return metaDataProducerMultiOutputStack.peek();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static List<MetaDataProducerMultiOutput> getReadOnlyViewOfMetaDataProducerMultiOutputStack()
   {
      return Collections.unmodifiableList(metaDataProducerMultiOutputStack);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void popMetaDataProducerMultiOutput()
   {
      if(metaDataProducerMultiOutputStack.isEmpty())
      {
         LOG.warn("Request to pop empty QBitProductionContext metaDataProducerMultiOutput - returning with noop");
         return;
      }

      metaDataProducerMultiOutputStack.pop();
   }

}
