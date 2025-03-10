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

package com.kingsrook.qqq.backend.core.model.metadata.processes;


/*******************************************************************************
 ** Types of UI Components that can be specified in frontend process steps.
 *******************************************************************************/
public enum QComponentType
{
   HELP_TEXT,
   BULK_EDIT_FORM,
   BULK_LOAD_FILE_MAPPING_FORM,
   BULK_LOAD_VALUE_MAPPING_FORM,
   BULK_LOAD_PROFILE_FORM,
   VALIDATION_REVIEW_SCREEN,
   EDIT_FORM,
   VIEW_FORM,
   DOWNLOAD_FORM,
   RECORD_LIST,
   PROCESS_SUMMARY_RESULTS,
   GOOGLE_DRIVE_SELECT_FOLDER,
   WIDGET,
   HTML;
   ///////////////////////////////////////////////////////////////////////////
   // keep these values in sync with QComponentType.ts in qqq-frontend-core //
   ///////////////////////////////////////////////////////////////////////////
}
