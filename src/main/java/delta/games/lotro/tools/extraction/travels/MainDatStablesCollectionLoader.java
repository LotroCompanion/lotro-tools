package delta.games.lotro.tools.extraction.travels;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;
import delta.games.lotro.lore.agents.npcs.NPCsManager;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.travels.TravelNode;
import delta.games.lotro.lore.travels.TravelNpc;
import delta.games.lotro.lore.travels.TravelsManager;
import delta.games.lotro.lore.travels.io.xml.TravelNPCXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;

/**
 * Loads travel NPC for the stables collection UI.
 * @author DAM
 */
public class MainDatStablesCollectionLoader
{
  private DataFacade _facade;
  private PlacesLoader _placesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatStablesCollectionLoader(DataFacade facade)
  {
    _facade=facade;
    _placesLoader=new PlacesLoader(facade);
  }

  private void doIt()
  {
    loadTravelNPCs();
  }

  void loadTravelUINodes()
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
    // Bounds: width=1280,height=1500
    for(UIElement buttonElement : uiElement.getChildElements())
    {
      Rectangle bounds=buttonElement.getRelativeBounds();
      PropertiesSet props=buttonElement.getProperties();
      String name=(String)props.getProperty("UICore_Element_tooltip_entry");
      int npcID=((Integer)props.getProperty("UI_StablesCollection_TravelNPC")).intValue();
      System.out.println("Name: "+name+", NPC="+npcID);
      System.out.println("\tBounds: "+bounds);
      NpcDescription npc=NPCsManager.getInstance().getNPCById(npcID);
      handleNpc(npc);
    }
  }

  private TravelNpc handleNpc(NpcDescription npc)
  {
    /*
TravelWebSellMultiplier: 1.0
TravelWebWC: 1879216497
Travel_DiscountArray:
  #1: 268446908 (Discount_Travel_Special)
  #2: 268452164 (Discount_Travel_Theodred)
     */
    int npcId=npc.getIdentifier();
    PropertiesSet props=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    Integer travelNodeId=(Integer)props.getProperty("TravelWebWC");
    if (travelNodeId==null)
    {
      return null;
    }
    TravelNpc ret=new TravelNpc(npc);
    // Node
    TravelsManager travelsMgr=TravelsManager.getInstance();
    TravelNode node=travelsMgr.getNode(travelNodeId.intValue());
    ret.setNode(node);
    // Sell factor
    float sellFactor=((Float)props.getProperty("TravelWebSellMultiplier")).floatValue();
    ret.setSellFactor(sellFactor);
    // Location
    String travelPad=(String)props.getProperty("TravelPad_Location");
    ExtendedPosition position=_placesLoader.getPositionForName(travelPad);
    ret.setPosition(position);
    // Must be discovered
    Integer mustBeDiscoveredInt=(Integer)props.getProperty("Travel_Stablemaster_MustBeDiscovered");
    boolean mustBeDiscovered=((mustBeDiscoveredInt!=null) && (mustBeDiscoveredInt.intValue()==1));
    ret.setMustBeDiscovered(mustBeDiscovered);
    // Discounts
    Object[] discountArray=(Object[])props.getProperty("Travel_DiscountArray");
    if (discountArray!=null)
    {
      for(Object discountEntry : discountArray)
      {
        int discountID=((Integer)discountEntry).intValue();
        ret.addDiscount(discountID);
      }
    }
    return ret;
  }

  private void loadTravelNPCs()
  {
    List<TravelNpc> travelNPCs=new ArrayList<TravelNpc>();
    NPCsManager npcsManager=NPCsManager.getInstance();
    for(NpcDescription npc : npcsManager.getNPCs())
    {
      TravelNpc travelNPC=handleNpc(npc);
      if (travelNPC!=null)
      {
        travelNPCs.add(travelNPC);
      }
    }
    // Save data
    TravelNPCXMLWriter.writeTravelNPCsFile(GeneratedFiles.TRAVEL_NPCS,travelNPCs);
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
