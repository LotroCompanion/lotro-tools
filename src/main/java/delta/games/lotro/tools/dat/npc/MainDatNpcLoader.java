package delta.games.lotro.tools.dat.npc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
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
  private EnumMapper _characterClass;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatNpcLoader(DataFacade facade)
  {
    _facade=facade;
    List<Item> items=ItemSaxParser.parseItemsFile(GeneratedFiles.ITEMS);
    _itemsManager=new ItemsManager(items);
    _characterClass=facade.getEnumsManager().getEnumMapper(587202574);
  }

  private void load(int indexDataId)
  {
    // Ignore test NPC
    if (indexDataId==1879074078)
    {
      return;
    }
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      // Profiles
      Object[] barterProfiles=(Object[])properties.getProperty("Barter_ProfileArray");
      if (barterProfiles!=null)
      {
        // Name / title
        String npcName=DatUtils.getStringProperty(properties,"Name");
        String title=DatUtils.getStringProperty(properties,"OccupationTitle");
        title=((title!=null)?title:"no title/occupation");
        System.out.println("NPC #"+indexDataId+": "+npcName+" ("+title+")");
        // Requirements
        loadClassRequirement(properties);
        loadReputationRequirement(properties);
        loadQuestRequirements(properties);
        // Profiles
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
  }

  private void loadClassRequirement(PropertiesSet properties)
  {
    Object[] requiredClassArray=(Object[])properties.getProperty("Usage_RequiredClassList");
    if (requiredClassArray!=null)
    {
      for(Object requiredClassObj : requiredClassArray)
      {
        Integer requiredClass=(Integer)requiredClassObj;
        String className=_characterClass.getString(requiredClass.intValue());
        System.out.println("\tRequired class:"+className);
      }
    }
  }

  private void loadQuestRequirements(PropertiesSet properties)
  {
    Object[] questRequirements=(Object[])properties.getProperty("Usage_QuestRequirements");
    if (questRequirements!=null)
    {
      for(Object questRequirementObj : questRequirements)
      {
        // {Usage_QuestStatus=805306368, Usage_Operator=3, Usage_QuestID=1879093911}
        PropertiesSet questReqProps=(PropertiesSet)questRequirementObj;
        int questId=((Integer)questReqProps.getProperty("Usage_QuestID")).intValue();
        int questStatus=((Integer)questReqProps.getProperty("Usage_QuestStatus")).intValue();
        Integer operator=(Integer)questReqProps.getProperty("Usage_Operator");
        System.out.println("\tQuest requirement: ID="+questId+", status="+questStatus+", operator="+operator);
      }
    }
  }

  private void loadReputationRequirement(PropertiesSet properties)
  {
    Object requiredFactionObj=properties.getProperty("Usage_RequiredFaction");
    if (requiredFactionObj!=null)
    {
      //{Usage_RequiredFaction_Tier=4, Usage_RequiredFaction_DataID=1879097420}
      PropertiesSet factionReqProps=(PropertiesSet)requiredFactionObj;
      int factionId=((Integer)factionReqProps.getProperty("Usage_RequiredFaction_DataID")).intValue();
      int factionTier=((Integer)factionReqProps.getProperty("Usage_RequiredFaction_Tier")).intValue();
      System.out.println("\tFaction requirement: ID="+factionId+", tier="+factionTier);
    }
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

    // Profile name
    String profileName=DatUtils.getStringProperty(properties,"Barter_Profile_Name");
    profileName=((profileName!=null)?profileName:"none");
    System.out.println("\tProfile: "+profileName);

    PropertiesSet permissionsProps=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissionsProps!=null)
    {
      loadClassRequirement(permissionsProps);
      loadReputationRequirement(permissionsProps);
      loadQuestRequirements(permissionsProps);
    }

    // Barter list
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

  private void doIt()
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
          load(i);
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
