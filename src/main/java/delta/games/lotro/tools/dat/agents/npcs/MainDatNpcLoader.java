package delta.games.lotro.tools.dat.agents.npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.common.requirements.ClassRequirement;
import delta.games.lotro.common.requirements.FactionRequirement;
import delta.games.lotro.common.requirements.QuestRequirement;
import delta.games.lotro.common.requirements.QuestStatus;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.trade.barter.BarterEntry;
import delta.games.lotro.lore.trade.barter.BarterEntryElement;
import delta.games.lotro.lore.trade.barter.BarterNpc;
import delta.games.lotro.lore.trade.barter.BarterProfile;
import delta.games.lotro.lore.trade.barter.ItemBarterEntryElement;
import delta.games.lotro.lore.trade.barter.ReputationBarterEntryElement;
import delta.games.lotro.lore.trade.barter.io.xml.BarterXMLWriter;
import delta.games.lotro.lore.trade.vendor.SellList;
import delta.games.lotro.lore.trade.vendor.VendorNpc;
import delta.games.lotro.lore.trade.vendor.io.xml.VendorXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.StringUtils;
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
  // Barterers
  private List<BarterNpc> _barterers;
  private Map<Integer,BarterProfile> _profiles;
  // Vendors
  private List<VendorNpc> _vendors;
  private Map<Integer,SellList> _sells;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatNpcLoader(DataFacade facade)
  {
    _facade=facade;
    _itemsManager=ItemsManager.getInstance();
    _barterers=new ArrayList<BarterNpc>();
    _profiles=new HashMap<Integer,BarterProfile>();
    _vendors=new ArrayList<VendorNpc>();
    _sells=new HashMap<Integer,SellList>();
  }

  private void handleNpc(int npcId)
  {
    // Ignore test NPC
    if (npcId==1879074078)
    {
      return;
    }
    PropertiesSet properties=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      if (npcId==DEBUG_ID)
      {
        System.out.println(properties.dump());
      }
      // Barter
      BarterNpc barterer=loadBarterData(npcId,properties);
      if (barterer!=null)
      {
        _barterers.add(barterer);
      }
      // Vendor
      VendorNpc vendor=loadVendorData(npcId,properties);
      if (vendor!=null)
      {
        _vendors.add(vendor);
      }
    }
    else
    {
      LOGGER.warn("Could not handle NPC ID="+npcId);
    }
  }

  private BarterNpc loadBarterData(int npcId, PropertiesSet properties)
  {
    BarterNpc barterer=null;
    // Barter_Profile_UseTabs
    /*
    Integer useTabs=(Integer)properties.getProperty("Barter_Profile_UseTabs");
    if ((useTabs!=null) && (useTabs.intValue()!=0) && (useTabs.intValue()!=1))
    {
      System.out.println("Use tab: "+useTabs);
    }
    */
    // Profiles
    Object[] barterProfiles=(Object[])properties.getProperty("Barter_ProfileArray");
    if (barterProfiles!=null)
    {
      NpcDescription npc=buildNpc(npcId,properties);
      barterer=new BarterNpc(npc);
      // Requirements
      loadRequirements(properties,barterer.getRequirements());
      // Barter profiles
      for(Object barterProfileObj : barterProfiles)
      {
        int profileId=((Integer)barterProfileObj).intValue();
        BarterProfile profile=loadBarterProfile(profileId);
        barterer.addBarterProfile(profile);
      }
      // TODO: WorldEvent_WorldEvent: 1879286443
    }
    return barterer;
  }

  private void loadRequirements(PropertiesSet properties, UsageRequirement requirements)
  {
    ClassRequirement classRequirement=loadClassRequirement(properties);
    requirements.setClassRequirement(classRequirement);
    FactionRequirement factionRequirement=loadReputationRequirement(properties);
    requirements.setFactionRequirement(factionRequirement);
    QuestRequirement questRequirement=loadQuestRequirements(properties);
    requirements.setQuestRequirement(questRequirement);
  }

  private ClassRequirement loadClassRequirement(PropertiesSet properties)
  {
    ClassRequirement ret=null;
    Object[] requiredClassArray=(Object[])properties.getProperty("Usage_RequiredClassList");
    if (requiredClassArray!=null)
    {
      for(Object requiredClassObj : requiredClassArray)
      {
        int classCode=((Integer)requiredClassObj).intValue();
        AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
        if (abstractClass!=null)
        {
          if (ret==null)
          {
            ret=new ClassRequirement();
          }
          ret.addAllowedClass(abstractClass);
        }
        else
        {
          LOGGER.warn("Unsupported class: "+abstractClass);
        }
      }
    }
    return ret;
  }

  private QuestRequirement loadQuestRequirements(PropertiesSet properties)
  {
    QuestRequirement ret=null;
    Object[] questRequirements=(Object[])properties.getProperty("Usage_QuestRequirements");
    if (questRequirements!=null)
    {
      if (questRequirements.length>1)
      {
        LOGGER.warn("Multiple quest requirements!");
      }
      for(Object questRequirementObj : questRequirements)
      {
        // {Usage_QuestStatus=805306368, Usage_Operator=3, Usage_QuestID=1879093911}
        PropertiesSet questReqProps=(PropertiesSet)questRequirementObj;
        int questId=((Integer)questReqProps.getProperty("Usage_QuestID")).intValue();
        int questStatus=((Integer)questReqProps.getProperty("Usage_QuestStatus")).intValue();
        Integer operator=(Integer)questReqProps.getProperty("Usage_Operator");
        if ((questStatus==805306368) && ((operator==null) || (operator.intValue()==3)))
        {
          ret=new QuestRequirement(questId,QuestStatus.COMPLETED);
        }
        else
        {
          LOGGER.warn("Unmanaged quest status:"+questStatus+/*"/operator:"+operator+*/" for quest ID: "+questId);
        }
      }
    }
    return ret;
  }

  private FactionRequirement loadReputationRequirement(PropertiesSet properties)
  {
    FactionRequirement ret=null;
    Object requiredFactionObj=properties.getProperty("Usage_RequiredFaction");
    if (requiredFactionObj!=null)
    {
      //{Usage_RequiredFaction_Tier=4, Usage_RequiredFaction_DataID=1879097420}
      PropertiesSet factionReqProps=(PropertiesSet)requiredFactionObj;
      int factionId=((Integer)factionReqProps.getProperty("Usage_RequiredFaction_DataID")).intValue();
      Faction faction=FactionsRegistry.getInstance().getById(factionId);
      if (faction!=null)
      {
        int factionTier=((Integer)factionReqProps.getProperty("Usage_RequiredFaction_Tier")).intValue();
        ret=new FactionRequirement(faction,factionTier);
      }
    }
    return ret;
  }

  private BarterProfile loadBarterProfile(int profileId)
  {
    BarterProfile profile=_profiles.get(Integer.valueOf(profileId));
    if (profile!=null)
    {
      return profile;
    }

    long dbPropertiesId=profileId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);

    profile=new BarterProfile(profileId);
    // Profile name
    String profileName=DatUtils.getStringProperty(properties,"Barter_Profile_Name");
    profile.setName(profileName);
    // Requirements
    PropertiesSet permissionsProps=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissionsProps!=null)
    {
      UsageRequirement requirements=profile.getRequirements();
      loadRequirements(permissionsProps,requirements);
    }
    // Barter list
    Object[] barterList=(Object[])properties.getProperty("Barter_ItemListArray");
    if (barterList!=null)
    {
      for(Object barterEntryObj : barterList)
      {
        BarterEntry barterEntry=new BarterEntry(profile);
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
        // Register entry
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
      else
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

  private VendorNpc loadVendorData(int npcId, PropertiesSet properties)
  {
    boolean hasVendorData=(properties.getProperty("Vendor_InventoryList")!=null);
    if (!hasVendorData)
    {
      return null;
    }
    NpcDescription npc=buildNpc(npcId,properties);
    VendorNpc ret=new VendorNpc(npc);
    // Sells lists
    Set<Integer> allSells=new HashSet<Integer>();
    Object[] inventoryArray=(Object[])properties.getProperty("Vendor_InventoryList");
    if (inventoryArray!=null)
    {
      for(Object inventoryObj : inventoryArray)
      {
        int sellListId=((Integer)inventoryObj).intValue();
        SellList sellList=getSellsList(sellListId);
        for(Proxy<Item> entry : sellList.getItems())
        {
          allSells.add(Integer.valueOf(entry.getId()));
        }
        ret.addSellList(sellList);
      }
    }
    // Item sells (for check only)
    Set<Integer> itemSells=loadItemSells(properties);
    for(Integer itemId : itemSells)
    {
      boolean alreadyKnown=ret.sells(itemId.intValue());
      if (!alreadyKnown)
      {
        LOGGER.warn("Found new item in item sells: "+itemId);
      }
    }
    int nbItemSells=itemSells.size();
    int nbAllSells=allSells.size();
    if ((nbItemSells!=0) && (nbItemSells!=nbAllSells))
    {
      LOGGER.warn("Mismatch sells="+nbItemSells+" all sells="+nbAllSells);
    }
    // Buys items?
    Integer buysItemsInt=(Integer)properties.getProperty("Vendor_BuysItems");
    boolean buysItems=(buysItemsInt!=null)?(buysItemsInt.intValue()==1):false;
    ret.setBuys(buysItems);
    // Discount list
    Object[] discountArray=(Object[])properties.getProperty("Vendor_DiscountList");
    if (discountArray!=null)
    {
      for(Object discountIdObj : discountArray)
      {
        int discountId=((Integer)discountIdObj).intValue();
        ret.addDiscount(discountId);
      }
    }
    // Sells wearable items?
    Integer sellsWearableItemsInt=(Integer)properties.getProperty("Vendor_SellsWearableItems");
    boolean sellsWearableItems=(sellsWearableItemsInt!=null)?(sellsWearableItemsInt.intValue()==1):false;
    ret.setSellsWearableItems(sellsWearableItems);
    // Sell modifier
    Float sellFactor=(Float)properties.getProperty("Vendor_SellModifier");
    float sellFactorValue=(sellFactor!=null)?sellFactor.floatValue():1;
    ret.setSellFactor(sellFactorValue);
    return ret;
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

  private SellList getSellsList(int listId)
  {
    SellList ret=_sells.get(Integer.valueOf(listId));
    if (ret!=null)
    {
      return ret;
    }
    ret=new SellList(listId);
    _sells.put(Integer.valueOf(listId),ret);
    PropertiesSet properties=_facade.loadProperties(listId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] inventoryArray=(Object[])properties.getProperty("VendorInventory_Items");
    if (inventoryArray!=null)
    {
      for(Object inventoryObj : inventoryArray)
      {
        Integer itemId=(Integer)inventoryObj;
        Item item=_itemsManager.getItem(itemId.intValue());
        if (item!=null)
        {
          Proxy<Item> itemProxy=new Proxy<Item>();
          itemProxy.setId(item.getIdentifier());
          itemProxy.setName(item.getName());
          itemProxy.setObject(item);
          ret.addItem(itemProxy);
        }
        else
        {
          LOGGER.warn("Unknown item: "+itemId);
        }
      }
    }
    return ret;
  }

  private NpcDescription buildNpc(int npcId, PropertiesSet properties)
  {
    // Name
    String npcName=DatUtils.getStringProperty(properties,"Name");
    npcName=StringUtils.removeMarks(npcName);
    NpcDescription npc=new NpcDescription(npcId,npcName);
    // Title
    String title=DatUtils.getStringProperty(properties,"OccupationTitle");
    title=StringUtils.fixName(title);
    npc.setTitle(title);
    return npc;
  }

  /**
   * Load barter and vendor data.
   */
  public void doIt()
  {
    // Scan for NPCs
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==WStateClass.NPC)
        {
          handleNpc(i);
        }
      }
    }
    // Save data
    save();
  }

  private void save()
  {
    // Barter tables
    BarterXMLWriter.writeBarterTablesFile(GeneratedFiles.BARTERS,_barterers);
    // Vendors
    VendorXMLWriter.writeVendorsFile(GeneratedFiles.VENDORS,_vendors);
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
