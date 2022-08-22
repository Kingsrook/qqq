package com.kingsrook.qqq.backend.core.actions.dashboard;


import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 **
 *******************************************************************************/
public class WidgetDataLoader
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public Object execute(QInstance qInstance, QSession session, String name) throws QException
   {
      QWidgetMetaData widget = qInstance.getWidget(name);
      AbstractWidgetRenderer widgetRenderer = QCodeLoader.getAdHoc(AbstractWidgetRenderer.class, widget.getCodeReference());
      return (widgetRenderer.render(qInstance, session));
   }
}
