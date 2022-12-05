package delta.games.lotro.tools.dat.travels;

import java.util.List;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;

/**
 * Loads travel NPC for the stables collection UI.
 * @author DAM
 */
public class MainDatStablesCollectionLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatStablesCollectionLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void doIt()
  {
    UILayout layout=new UILayoutLoader(_facade).loadUiLayout(0x220008BB);
    inspect(layout.getChildElements());
  }

  private void inspect(List<UIElement> uiElements)
  {
    for(UIElement uiElement : uiElements)
    {
      if (uiElement.getIdentifier()==268452723) // CollectionView_ME_Map_Buttons
      {
        inspectStablemasterButtons(uiElement);
        break;
      }
      inspect(uiElement.getChildElements());
    }
  }

  private void inspectStablemasterButtons(UIElement uiElement)
  {
    for(UIElement buttonElement : uiElement.getChildElements())
    {
      PropertiesSet props=buttonElement.getProperties();
      String name=(String)props.getProperty("UICore_Element_tooltip_entry");
      Integer npc=(Integer)props.getProperty("UI_StablesCollection_TravelNPC");
      System.out.println("Name: "+name+", NPC="+npc);
      handleNpc(npc.intValue());
    }
  }

  private void handleNpc(int npcId)
  {
    /*
TravelWebSellMultiplier: 1.0
TravelWebWC: 1879216497
Travel_DiscountArray: 
  #1: 268446908 (Discount_Travel_Special)
  #2: 268452164 (Discount_Travel_Theodred)
     */
    PropertiesSet props=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    Integer travelNodeId=(Integer)props.getProperty("TravelWebWC");
    System.out.println("\tTravel node ID: "+travelNodeId);
    ArrayPropertyValue discountArray=(ArrayPropertyValue)props.getPropertyValueByName("Travel_DiscountArray");
    if (discountArray!=null)
    {
      PropertyValue[] values=discountArray.getValues();
      for(PropertyValue value : values)
      {
        System.out.println("\t\tDiscount: "+value);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatStablesCollectionLoader(facade).doIt();
    facade.dispose();
  }
}
