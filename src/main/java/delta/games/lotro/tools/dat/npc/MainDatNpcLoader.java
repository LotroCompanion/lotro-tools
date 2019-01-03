package delta.games.lotro.tools.dat.npc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.io.xml.ItemSaxParser;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get NPC definitions from DAT files.
 * @author DAM
 */
public class MainDatNpcLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatNpcLoader.class);

  private DataFacade _facade;
  private ItemsManager _itemsManager;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatNpcLoader(DataFacade facade)
  {
    _facade=facade;
    List<Item> items=ItemSaxParser.parseItemsFile(GeneratedFiles.ITEMS);
    _itemsManager=new ItemsManager(items);
  }

  private Recipe load(int indexDataId)
  {
    if (indexDataId==1879074078)
    {
      return null;
    }
    Recipe recipe=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      Object[] barterProfiles=(Object[])properties.getProperty("Barter_ProfileArray");
      if (barterProfiles!=null)
      {
        String npcName=DatUtils.getStringProperty(properties,"Name");
        String title=DatUtils.getStringProperty(properties,"OccupationTitle");
        title=((title!=null)?title:"no title/occupation");
        System.out.println("NPC #"+indexDataId+": "+npcName+" ("+title+")");
        for(Object barterProfileObj : barterProfiles)
        {
          int profileId=((Integer)barterProfileObj).intValue();
          loadBarterProfile(indexDataId,profileId);
        }

        /*
        Usage_RequiredClassList: 
          #1: 162
        Vendor_BuysItems: 1
        Vendor_DiscountList: 
          #1: 268450539
        Vendor_InventoryList: 
          #1: 1879052429
        Vendor_ItemList: 
          #1: 1879205686
          #2: 1879052507
          #3: 1879143219
        Vendor_SellModifier: 4.0
        Vendor_SellsWearableItems: 0
        */
        //System.out.println(properties.dump());
      }
    }
    else
    {
      LOGGER.warn("Could not handle NPC ID="+indexDataId);
    }
    return recipe;
  }

  private Set<Integer> profileIds=new HashSet<Integer>();

  private void loadBarterProfile(int npcId,int profileId)
  {
    if (profileIds.contains(Integer.valueOf(profileId)))
    {
      return;
    }
    profileIds.add(Integer.valueOf(profileId));
    int dbPropertiesId=profileId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (npcId==0)
    {
      System.out.println(properties.dump());
    }

    String profileName=DatUtils.getStringProperty(properties,"Barter_Profile_Name");
    profileName=((profileName!=null)?profileName:"none");
    System.out.println("\tProfile: "+profileName);
    Object[] barterList=(Object[])properties.getProperty("Barter_ItemListArray");
    if (barterList!=null)
    {
      for(Object barterEntryObj : barterList)
      {
        Object[] barterEntry=(Object[])barterEntryObj;
        int nbItems=barterEntry.length;
        int nbItemsToGive=nbItems-1;
        // What to give?
        String itemsToGive="";
        for(int i=0;i<nbItemsToGive;i++)
        {
          PropertiesSet toGiveProps=(PropertiesSet)(barterEntry[i]);
          String toGive=handleItemToGiveOrReceive(toGiveProps);
          if (toGive.length()>0)
          {
            if (itemsToGive.length()>0) itemsToGive+=" / ";
            itemsToGive+=toGive;
          }
        }
        // What to receive?
        PropertiesSet toReceiveProps=(PropertiesSet)(barterEntry[nbItems-1]);
        String itemToReceive=handleItemToGiveOrReceive(toReceiveProps);
        String label=itemsToGive+" => "+itemToReceive;
        System.out.println("\t\t"+label);
      }
    }
  }

  private String handleItemToGiveOrReceive(PropertiesSet itemProps)
  {
    Integer itemId=(Integer)itemProps.getProperty("Barter_Item");
    Integer quantity=(Integer)itemProps.getProperty("Barter_ItemQuantity");
    if ((itemId==null) && (quantity!=null))
    {
      return "";
    }
    Integer faction=(Integer)itemProps.getProperty("Barter_ReputationFaction");
    Integer rewardTier=(Integer)itemProps.getProperty("Barter_ReputationRewardTier");

    String label="";
    if (itemId!=null)
    {
      Item item=_itemsManager.getItem(itemId.intValue());
      String itemName=(item!=null)?item.getName():"?";
      if (quantity!=null)
      {
        label=quantity+" "+itemName;
      }
      else
      {
        int lookupTable=((Integer)itemProps.getProperty("Barter_ItemQuantity_LookupTable")).intValue();
        label="(table:"+lookupTable+") "+itemName;
      }
      if (item==null)
      {
        System.out.println(itemProps.dump());
      }
    }
    else if ((faction!=null) && (rewardTier!=null))
    {
      label="NN (tier"+rewardTier+") reputation with faction "+faction;
    }
    else
    {
      System.out.println(itemProps.dump());
    }
    return label;
  }

  /*
  // NPC (Quartermaster of Court of Lothlorien)
  //showProperties(facade,0x7004AADB+0x9000000);
  // Barter_ProfileArray: #1 (Heavy Armour) 
  //showProperties(facade,1879353979+0x9000000);
  // Barter_ProfileArray: #17 (Lockbox Keys) 
  //showProperties(facade,1879361624+0x9000000);
   */

  private void doIt()
  {
    RecipesManager recipesManager=new RecipesManager();
    scanAll(recipesManager);
  }

  private void scanAll(RecipesManager recipesManager)
  {
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        //int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        //System.out.println(classDefIndex);
        if (classDefIndex==1724)
        {
          Recipe recipe=load(i);
          if (recipe!=null)
          {
            recipesManager.registerRecipe(recipe);
          }
        }
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
    new MainDatNpcLoader(facade).doIt();
    facade.dispose();
  }
}
