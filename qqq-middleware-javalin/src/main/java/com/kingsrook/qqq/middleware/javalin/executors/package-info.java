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

/*******************************************************************************
 ** This package contains (hopefully generally) api-version-agnostic classes
 ** that implement the actual QQQ Middleware.  That is to say, subclasses of
 ** `AbstractMiddlewareExecutor`, which use classes from the `.io` subpackage
 ** for I/O, to run code in a QQQ server.
 **
 ** As new versions of the middleware evolve, the idea is that the spec classes
 ** for new versions will be responsible for appropriately marshalling data
 ** in and out of the executors, via the I/O classes, with "feature flags", etc
 ** added to those input classes as needed (say if v N+1 adds a new feature,
 ** then a request for v N may omit the feature-flag that turns that feature on).
 **
 ** As functionality continues to evolve, the time may come when it's appropriate
 ** to fork an Executor.  Hypothetically, if version 5 of the QueryExecutor
 ** bears very little resemblance to versions 1 through 4 (due to additional
 ** pizzazz?) spawn a new QueryWithPizzazzExecutor.  Of course, naming here
 ** will be the hardest part (e.g., avoid NewNewQueryExecutorFinal2B...)
 *******************************************************************************/
package com.kingsrook.qqq.middleware.javalin.executors;