package com.kingsrook.qqq.backend.core.actions.dashboard;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract Object render(QInstance qInstance, QSession session) throws QException;

}
