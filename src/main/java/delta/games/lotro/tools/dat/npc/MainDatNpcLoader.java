package delta.games.lotro.tools.dat.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.comparators.ItemNameComparator;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.trade.barter.BarterEntry;
import delta.games.lotro.lore.trade.barter.BarterEntryElement;
import delta.games.lotro.lore.trade.barter.BarterNpc;
import delta.games.lotro.lore.trade.barter.BarterProfile;
import delta.games.lotro.lore.trade.barter.ItemBarterEntryElement;
import delta.games.lotro.lore.trade.barter.ReputationBarterEntryElement;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get NPC definitions from DAT files.
 * @author DAM
 */
public class MainDatNpcLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatNpcLoader.class);

  private static final int DEBUG_ID=0;
  private DataFacade _facade;
  private ItemsManager _itemsManager;
  private EnumMapper _characterClass;
  private Map<Integer,BarterProfile> _profiles=new HashMap<Integer,BarterProfile>();
  private Map<Integer,Set<Integer>> _sellsListIds=new HashMap<Integer,Set<Integer>>();

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatNpcLoader(DataFacade facade)
  {
    _facade=facade;
    _itemsManager=ItemsManager.getInstance();
    _characterClass=facade.getEnumsManager().getEnumMapper(587202574);
  }

  private void load(int indexDataId)
  {
    // Ignore test NPC
    if (indexDataId==1879074078)
    {
      return;
    }
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      if (indexDataId==DEBUG_ID)
      {
        System.out.println(properties.dump());
      }
      Object minUsageLevelForCost=properties.getProperty("Barter_ItemUsesMinUsageLevelForCost");
      if (minUsageLevelForCost!=null)
      {
        System.out.println("Min usage level for cost: "+minUsageLevelForCost);
      }
      // Barter_Profile_UseTabs
      Integer useTabs=(Integer)properties.getProperty("Barter_Profile_UseTabs");
      if ((useTabs!=null) && (useTabs.intValue()!=0) && (useTabs.intValue()!=1))
      {
        System.out.println("Use tab: "+useTabs);
      }
      // Profiles
      Object[] barterProfiles=(Object[])properties.getProperty("Barter_ProfileArray");
      if (barterProfiles!=null)
      {
        BarterNpc npc=new BarterNpc(indexDataId);
        // Name
        String npcName=DatUtils.getStringProperty(properties,"Name");
        npc.setNpcName(npcName);
        // Title
        String title=DatUtils.getStringProperty(properties,"OccupationTitle");
        npc.setNpcTitle(title);
        // Requirements
        loadClassRequirement(properties);
        loadReputationRequirement(properties);
        loadQuestRequirements(properties);
        // Barter profiles
        for(Object barterProfileObj : barterProfiles)
        {
          int profileId=((Integer)barterProfileObj).intValue();
          BarterProfile profile=loadBarterProfile(profileId);
          npc.addBarterProfile(profile);
        }
        // TODO: WorldEvent_WorldEvent: 1879286443
        System.out.println(npc.dump());
      }
      // Vendor
      loadVendorData(properties);
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

  private BarterProfile loadBarterProfile(int profileId)
  {
    BarterProfile profile=_profiles.get(Integer.valueOf(profileId));
    if (profile!=null)
    {
      return profile;
    }

    int dbPropertiesId=profileId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);

    profile=new BarterProfile(profileId);
    // Profile name
    String profileName=DatUtils.getStringProperty(properties,"Barter_Profile_Name");
    profile.setName(profileName);

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
        BarterEntry barterEntry=new BarterEntry();
        Object[] barterEntryArray=(Object[])barterEntryObj;
        int nbItems=barterEntryArray.length;
        int nbItemsToGive=nbItems-1;
        Integer level=null;
        // What to receive?
        PropertiesSet toReceiveProps=(PropertiesSet)(barterEntryArray[nbItems-1]);
        Integer useMinLevelInteger=(Integer)toReceiveProps.getProperty("Barter_ItemUsesMinUsageLevelForCost");
        boolean useMinLevel=((useMinLevelInteger!=null)&&(useMinLevelInteger.intValue()!=0));
        BarterEntryElement itemToReceive=handleItemToGiveOrReceive(toReceiveProps,level);
        barterEntry.setElementToReceive(itemToReceive);
        if (itemToReceive instanceof ItemBarterEntryElement)
        {
          ItemBarterEntryElement itemReward=(ItemBarterEntryElement)itemToReceive;
          Item item=itemReward.getItemProxy().getObject();
          Integer itemLevel=item.getItemLevel();
          Integer minLevel=item.getMinLevel();
          level=useMinLevel?minLevel:itemLevel;
        }
        // What to give?
        for(int i=0;i<nbItemsToGive;i++)
        {
          PropertiesSet toGiveProps=(PropertiesSet)(barterEntryArray[i]);
          BarterEntryElement toGive=handleItemToGiveOrReceive(toGiveProps,level);
          if (toGive!=null)
          {
            barterEntry.addElementToGive((ItemBarterEntryElement)toGive);
          }
        }
        // Result
        String label=barterEntry.getLabel();
        System.out.println("\t\t"+label);
        profile.addEntry(barterEntry);
      }
    }
    _profiles.put(Integer.valueOf(profileId),profile);
    return profile;
  }

  private BarterEntryElement handleItemToGiveOrReceive(PropertiesSet itemProps, Integer level)
  {
    BarterEntryElement ret=null;
    Integer itemId=(Integer)itemProps.getProperty("Barter_Item");
    Integer quantity=(Integer)itemProps.getProperty("Barter_ItemQuantity");
    Integer factionId=(Integer)itemProps.getProperty("Barter_ReputationFaction");
    Integer rewardTier=(Integer)itemProps.getProperty("Barter_ReputationRewardTier");

    if ((itemId==null) && (quantity!=null))
    {
      LOGGER.warn("No item, but found quantity: "+quantity);
      return null;
    }

    if (itemId!=null)
    {
      // Item
      Item item=_itemsManager.getItem(itemId.intValue());
      if (item==null)
      {
        LOGGER.warn("Item not found: ID="+itemId);
        return null;
      }
      if (quantity==null)
      {
        Integer lookupTableId=(Integer)itemProps.getProperty("Barter_ItemQuantity_LookupTable");
        if (lookupTableId!=null)
        {
          Progression progression=DatStatUtils.getProgression(_facade,lookupTableId.intValue());
          if (level!=null)
          {
            Float yValue=progression.getValue(level.intValue());
            quantity=Integer.valueOf(yValue.intValue());
          }
        }
      }
      if (quantity!=null)
      {
        Proxy<Item> itemProxy=new Proxy<Item>();
        itemProxy.setId(item.getIdentifier());
        itemProxy.setName(item.getName());
        itemProxy.setObject(item);
        ret=new ItemBarterEntryElement(itemProxy,quantity.intValue());
      }
      {
        LOGGER.warn("Quantity not found!");
      }
    }
    else if ((factionId!=null) && (rewardTier!=null))
    {
      // Reputation
      Faction faction=FactionsRegistry.getInstance().getById(factionId.intValue());
      // Amount
      int amount=getAmountForTier(rewardTier.intValue());
      // Result
      ReputationBarterEntryElement repElement=new ReputationBarterEntryElement(faction);
      repElement.setAmount(amount);
      ret=repElement;
    }
    else
    {
      LOGGER.warn("Unmanaged barter entry element!");
    }
    return ret;
  }

  private int getAmountForTier(int tier)
  {
    if (tier==5) return 1200;
    return 0;
  }

  private void loadVendorData(PropertiesSet properties)
  {
    // Sells lists
    Set<Integer> allSells=new HashSet<Integer>();
    Object[] inventoryArray=(Object[])properties.getProperty("Vendor_InventoryList");
    if (inventoryArray!=null)
    {
      for(Object inventoryObj : inventoryArray)
      {
        int itemId=((Integer)inventoryObj).intValue();
        Set<Integer> itemsInSellsList=loadSellsList(itemId);
        allSells.addAll(itemsInSellsList);
      }
    }
    // Item sells
    Set<Integer> itemSells=loadItemSells(properties);
    Set<Integer> newItemSells=new HashSet<Integer>();
    for(Integer itemSell : itemSells)
    {
      if (!allSells.contains(itemSell))
      {
        System.out.println("Found new item in item sells: "+itemSell);
        newItemSells.add(itemSell);
      }
    }
    int nbItemSells=itemSells.size();
    int nbAllSells=allSells.size();
    if ((nbItemSells!=0) && (nbItemSells!=nbAllSells))
    {
      System.out.println("Mismatch sells="+nbItemSells+" all sells="+nbAllSells);
    }
    if (newItemSells.size()>0)
    {
      System.out.println("Additional sells:");
      displayItems(newItemSells);
    }
    // Buys items?
    Integer buysItemsInt=(Integer)properties.getProperty("Vendor_BuysItems");
    boolean buysItems=(buysItemsInt!=null)?(buysItemsInt.intValue()==1):false;
    if (buysItems)
    {
      System.out.println("\tBuys items");
    }
    // Discount list
    Object[] discountArray=(Object[])properties.getProperty("Vendor_DiscountList");
    if (discountArray!=null)
    {
      for(Object discountIdObj : discountArray)
      {
        int discountId=((Integer)discountIdObj).intValue();
        System.out.println("\tDiscount ID: "+discountId);
      }
    }
    // Sells wearable items?
    Integer sellsWearableItemsInt=(Integer)properties.getProperty("Vendor_SellsWearableItems");
    boolean sellsWearableItems=(sellsWearableItemsInt!=null)?(sellsWearableItemsInt.intValue()==1):false;
    if (sellsWearableItems)
    {
      System.out.println("\tSells wearable items");
    }
    // Sell modifier
    Float sellFactor=(Float)properties.getProperty("Vendor_SellModifier");
    if (sellFactor!=null)
    {
      System.out.println("\tSells factor: "+sellFactor);
    }
  }

  private Set<Integer> loadItemSells(PropertiesSet properties)
  {
    Set<Integer> set=new HashSet<Integer>();
    Object[] itemsList=(Object[])properties.getProperty("Vendor_ItemList");
    if (itemsList!=null)
    {
      for(Object itemIdObj : itemsList)
      {
        Integer itemId=(Integer)itemIdObj;
        set.add(itemId);
      }
    }
    return set;
  }

  private Set<Integer> loadSellsList(int listId)
  {
    Set<Integer> set=_sellsListIds.get(Integer.valueOf(listId));
    if (set!=null)
    {
      return set;
    }
    set=new HashSet<Integer>();
    _sellsListIds.put(Integer.valueOf(listId),set);
    PropertiesSet properties=_facade.loadProperties(listId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] inventoryArray=(Object[])properties.getProperty("VendorInventory_Items");
    if (inventoryArray!=null)
    {
      System.out.println("Sells list ID="+listId);
      for(Object inventoryObj : inventoryArray)
      {
        Integer itemId=(Integer)inventoryObj;
        set.add(itemId);
      }
      displayItems(set);
    }
    return set;
  }

  private void displayItems(Set<Integer> ids)
  {
    List<Item> items=new ArrayList<Item>();
    for(Integer id : ids)
    {
      Item item=_itemsManager.getItem(id.intValue());
      if (item!=null)
      {
        items.add(item);
      }
    }
    Collections.sort(items,new ItemNameComparator());
    int index=1;
    for(Item item : items)
    {
      String itemName=item.getName();
      System.out.println("\t#"+index+": "+itemName);
      index++;
    }
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
        if (classDefIndex==WStateClass.NPC)
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
