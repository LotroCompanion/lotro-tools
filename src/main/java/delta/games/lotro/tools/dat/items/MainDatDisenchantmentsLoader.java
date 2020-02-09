package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.io.xml.TreasureXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.CountedItem;
import delta.games.lotro.lore.items.DisenchantmentResult;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemProxy;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.io.xml.DisenchantmentResultXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.others.LootLoader;

/**
 * Get the disenchantment data from DAT files.
 * @author DAM
 */
public class MainDatDisenchantmentsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatDisenchantmentsLoader.class);

  private DataFacade _facade;
  private LootLoader _lootLoader;
  private LootsManager _loots;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatDisenchantmentsLoader(DataFacade facade)
  {
    _facade=facade;
    _loots=LootsManager.getInstance();
    _lootLoader=new LootLoader(facade,_loots);
  }

  /**
   * Load disenchantment data for an item.
   * @param indexDataId Item identifier.
   * @return the dienchantment result, if any.
   */
  public DisenchantmentResult load(int indexDataId)
  {
    DisenchantmentResult ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      Integer value=(Integer)properties.getProperty("Item_Disenchant_Value");
      Integer did=(Integer)properties.getProperty("Item_Disenchant_Component_Result");
      Integer trophyListId=(Integer)properties.getProperty("Item_Disenchant_Trophy_List");
      if ((value!=null) && (value.intValue()>0) && (did!=null))
      {
        ret=new DisenchantmentResult(indexDataId);
        if (did!=null)
        {
          ItemProxy proxy=new ItemProxy();
          Item item=ItemsManager.getInstance().getItem(did.intValue());
          proxy.setItem(item);
          CountedItem countedItem=new CountedItem(proxy,value.intValue());
          ret.setCountedItem(countedItem);
        }
      }
      else if (trophyListId!=null)
      {
        ret=new DisenchantmentResult(indexDataId);
        TrophyList trophyList=_lootLoader.handleTrophyList(trophyListId.intValue());
        ret.setTrophyList(trophyList);
      }
    }
    else
    {
      LOGGER.warn("Could not handle disenchantment for item ID="+indexDataId);
    }
    return ret;
  }

  private void doIt()
  {
    List<DisenchantmentResult> disenchantments=new ArrayList<DisenchantmentResult>();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      DisenchantmentResult disenchantment=load(item.getIdentifier());
      if (disenchantment!=null)
      {
        disenchantments.add(disenchantment);
      }
    }
    // Write disenchantment data
    DisenchantmentResultXMLWriter.writeDisenchantmentsFile(GeneratedFiles.DISENCHANTMENTS,disenchantments);
    // Write loot data 
    TreasureXMLWriter.writeLootsFile(GeneratedFiles.LOOTS,_loots);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatDisenchantmentsLoader(facade).doIt();
    facade.dispose();
  }
}
