package com.kingsrook.qqq.backend.core.model.metadata.automation;


/*******************************************************************************
 ** Metadata specifically for the polling automation provider.
 *******************************************************************************/
public class PollingAutomationProviderMetaData extends QAutomationProviderMetaData
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public PollingAutomationProviderMetaData()
   {
      super();
      setType(QAutomationProviderType.POLLING);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public PollingAutomationProviderMetaData withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public PollingAutomationProviderMetaData withType(QAutomationProviderType type)
   {
      setType(type);
      return (this);
   }

}
