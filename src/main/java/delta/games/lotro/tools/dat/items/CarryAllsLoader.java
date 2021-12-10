package delta.games.lotro.tools.dat.items;

import delta.games.lotro.character.storage.carryalls.CarryAllDefinition;
import delta.games.lotro.character.storage.carryalls.CarryAllsDefinitionsManager;
import delta.games.lotro.character.storage.carryalls.io.xml.CarryAllDefinitionXMLWriter;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Loader for carry-all definitions data.
 * @author DAM
 */
public class CarryAllsLoader
{
  /*
1879402646 Large Item Experience Rune Carry-all
******** Properties: 1879402646
BoS_AlwaysAutoGather: 0
BoS_Available_ItemClass_Array: 
  #1: 107 (Legendary Item XP)
BoS_Available_MaxDifferentTypesOfItems: 50
BoS_Available_MaxQuantity: 5000
BoS_Available_WeenieType: 129 (Item)
BoS_IsAutoGathering: 0
BoS_IsBlackHole: 0
BoS_UIElement_ID: 268453283 (SingletonBag)
BoS_UIElement_Layout: 570427612
Item_Class: 279 (Carry-all)
Item_ForceNotSellable: 1
WeenieType: 15728769 (BagOfSingletons)
   */

  private CarryAllsDefinitionsManager _mgr;

  /**
   * Constructor.
   */
  public CarryAllsLoader()
  {
    _mgr=new CarryAllsDefinitionsManager();
  }

  /**
   * Handle an item.
   * @param item Item.
   * @param properties Item properties.
   */
  public void handleItem(Item item, PropertiesSet properties)
  {
    Integer weenieType=(Integer)properties.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=15728769))
    {
      return;
    }
    int stackMax=((Integer)properties.getProperty("BoS_Available_MaxQuantity")).intValue();
    int maxItems=((Integer)properties.getProperty("BoS_Available_MaxDifferentTypesOfItems")).intValue();
    CarryAllDefinition def=new CarryAllDefinition(item,maxItems,stackMax);
    _mgr.addCarryAll(def);
  }

  /**
   * Save the loaded carry-alls to a file.
   */
  public void saveCarryAlls()
  {
    CarryAllDefinitionXMLWriter.writeCarryAllsFile(GeneratedFiles.CARRY_ALLS,_mgr);
  }
}
