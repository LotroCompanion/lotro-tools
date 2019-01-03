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
        System.out.println("NPC: "+npcName+" ("+title+")");
        for(Object barterProfileObj : barterProfiles)
        {
          int profileId=((Integer)barterProfileObj).intValue();
          loadBarterProfile(profileId);
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

  private void loadBarterProfile(int profileId)
  {
    if (profileIds.contains(Integer.valueOf(profileId)))
    {
      return;
    }
    profileIds.add(Integer.valueOf(profileId));
    int dbPropertiesId=profileId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    //System.out.println(properties.dump());

    String profileName=DatUtils.getStringProperty(properties,"Barter_Profile_Name");
    profileName=((profileName!=null)?profileName:"none");
    System.out.println("\tProfile: "+profileName);
    Object[] barterList=(Object[])properties.getProperty("Barter_ItemListArray");
    if (barterList!=null)
    {
      for(Object barterEntryObj : barterList)
      {
        Object[] barterEntry=(Object[])barterEntryObj;
        // First item: what to give
        PropertiesSet toGiveProps=(PropertiesSet)(barterEntry[0]);
        boolean doDumpGive=false;
        int idItemToGive=((Integer)toGiveProps.getProperty("Barter_Item")).intValue();
        Item itemToGive=_itemsManager.getItem(idItemToGive);
        String nameItemToGive=(itemToGive!=null)?itemToGive.getName():"?";
        Integer quantityToGive=(Integer)toGiveProps.getProperty("Barter_ItemQuantity");
        if ((itemToGive==null) || (quantityToGive==null))
        {
          doDumpGive=true;
        }
        // Second item: what to receive
        PropertiesSet toReceiveProps=(PropertiesSet)(barterEntry[1]);
        boolean doDumpReceive=false;
        String toReceive="?";
        // - item ?
        Integer idItemToReceive=(Integer)toReceiveProps.getProperty("Barter_Item");
        if (idItemToReceive!=null)
        {
          Item itemToReceive=_itemsManager.getItem(idItemToReceive.intValue());
          String nameItemToReceive=(itemToReceive!=null)?itemToReceive.getName():"?";
          Integer quantityToReceive=(Integer)toReceiveProps.getProperty("Barter_ItemQuantity");
          if ((itemToReceive==null) || (quantityToReceive==null))
          {
            doDumpReceive=true;
          }
          toReceive=quantityToReceive+" "+nameItemToReceive;
        }
        else
        {
          // - faction ?
          doDumpReceive=true;
        }
        if (doDumpGive)
        {
          System.out.println(toReceiveProps.dump());
        }
        if (doDumpReceive)
        {
          System.out.println(toGiveProps.dump());
        }
        String label=quantityToGive+" "+nameItemToGive+" => "+toReceive;
        System.out.println("\t\t"+label);
      }
    }
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
