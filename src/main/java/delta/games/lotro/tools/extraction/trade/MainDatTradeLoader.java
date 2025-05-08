package delta.games.lotro.tools.extraction.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.common.requirements.ClassRequirement;
import delta.games.lotro.common.requirements.FactionRequirement;
import delta.games.lotro.common.requirements.QuestRequirement;
import delta.games.lotro.common.requirements.QuestStatus;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.agents.npcs.NPCsManager;
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
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.progressions.ProgressionUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get trading data from DAT files.
 * @author DAM
 */
public class MainDatTradeLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatTradeLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
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
  public MainDatTradeLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("barterers",facade.getGlobalStringsManager());
    _itemsManager=ItemsManager.getInstance();
    _barterers=new ArrayList<BarterNpc>();
    _profiles=new HashMap<Integer,BarterProfile>();
    _vendors=new ArrayList<VendorNpc>();
    _sells=new HashMap<Integer,SellList>();
  }

  private void handleNpc(NpcDescription npc)
  {
    int npcId=npc.getIdentifier();
    // Ignore test NPC
    if (npcId==1879074078)
    {
      return;
    }
    PropertiesSet properties=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // Barter
      BarterNpc barterer=loadBarterData(npc,properties);
      if (barterer!=null)
      {
        _barterers.add(barterer);
      }
      // Vendor
      VendorNpc vendor=loadVendorData(npc,properties);
      if (vendor!=null)
      {
        _vendors.add(vendor);
      }
    }
    else
    {
      LOGGER.warn("Could not handle NPC ID={}",Integer.valueOf(npcId));
    }
  }

  private BarterNpc loadBarterData(NpcDescription npc, PropertiesSet properties)
  {
    BarterNpc barterer=null;
    // Barter_Profile_UseTabs
    @SuppressWarnings("unused")
    Integer useTabs=(Integer)properties.getProperty("Barter_Profile_UseTabs");
    // Profiles
    Object[] barterProfiles=(Object[])properties.getProperty("Barter_ProfileArray");
    if (barterProfiles!=null)
    {
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
      List<AbstractClassDescription> classes=new ArrayList<AbstractClassDescription>();
      for(Object requiredClassObj : requiredClassArray)
      {
        int classCode=((Integer)requiredClassObj).intValue();
        AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
        if (abstractClass!=null)
        {
          classes.add(abstractClass);
        }
        else
        {
          LOGGER.warn("Unsupported class: {}",abstractClass);
        }
      }
      if (!classes.isEmpty())
      {
        ret=new ClassRequirement(classes);
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
        // Usage_QuestStatus=805306368, Usage_Operator=3, Usage_QuestID=1879093911
        PropertiesSet questReqProps=(PropertiesSet)questRequirementObj;
        Integer questId=(Integer)questReqProps.getProperty("Usage_QuestID");
        Integer questStatus=(Integer)questReqProps.getProperty("Usage_QuestStatus");
        Integer operator=(Integer)questReqProps.getProperty("Usage_Operator");
        if ((questStatus.intValue()==805306368) && ((operator==null) || (operator.intValue()==3)))
        {
          ret=new QuestRequirement(questId.intValue(),QuestStatus.COMPLETED);
        }
        else
        {
          LOGGER.warn("Unmanaged quest status: {} for quest ID: {}",questStatus,questId);
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
      // Usage_RequiredFaction_Tier=4, Usage_RequiredFaction_DataID=1879097420
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
    String profileName=_i18n.getNameStringProperty(properties,"Barter_Profile_Name",profileId,0);
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
          Item item=itemReward.getItem();
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
      LOGGER.warn("No item, but found quantity: {}",quantity);
      return null;
    }

    if (itemId!=null)
    {
      // Item
      Item item=_itemsManager.getItem(itemId.intValue());
      if (item==null)
      {
        LOGGER.warn("Item not found: ID={}",itemId);
        return null;
      }
      if (quantity==null)
      {
        Integer lookupTableId=(Integer)itemProps.getProperty("Barter_ItemQuantity_LookupTable");
        if (lookupTableId!=null)
        {
          Progression progression=ProgressionUtils.getProgression(_facade,lookupTableId.intValue());
          if (level!=null)
          {
            Float yValue=progression.getValue(level.intValue());
            quantity=Integer.valueOf(yValue.intValue());
          }
        }
      }
      if (quantity!=null)
      {
        ret=new ItemBarterEntryElement(item,quantity.intValue());
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

  private VendorNpc loadVendorData(NpcDescription npc, PropertiesSet properties)
  {
    boolean hasVendorData=(properties.getProperty("Vendor_InventoryList")!=null);
    if (!hasVendorData)
    {
      return null;
    }
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
        for(Item entry : sellList.getItems())
        {
          allSells.add(Integer.valueOf(entry.getIdentifier()));
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
        LOGGER.warn("Found new item in item sells: {}",itemId);
      }
    }
    int nbItemSells=itemSells.size();
    int nbAllSells=allSells.size();
    if ((nbItemSells!=0) && (nbItemSells!=nbAllSells))
    {
      LOGGER.warn("Mismatch sells={} all sells={}",Integer.valueOf(nbItemSells),Integer.valueOf(nbAllSells));
    }
    // Buys items?
    Integer buysItemsInt=(Integer)properties.getProperty("Vendor_BuysItems");
    boolean buysItems=((buysItemsInt!=null) && (buysItemsInt.intValue()==1));
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
    boolean sellsWearableItems=((sellsWearableItemsInt!=null) && (sellsWearableItemsInt.intValue()==1));
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
          ret.addItem(item);
        }
        else
        {
          LOGGER.warn("Unknown item: {}",itemId);
        }
      }
    }
    return ret;
  }

  /**
   * Load barter and vendor data.
   */
  public void doIt()
  {
    NPCsManager npcsManager=NPCsManager.getInstance();
    for(NpcDescription npc : npcsManager.getNPCs())
    {
      handleNpc(npc);
    }
    // Save data
    save();
  }

  private void save()
  {
    // Barter tables
    BarterXMLWriter.writeBarterTablesFile(GeneratedFiles.BARTERS,_barterers);
    // Bareter labels
    _i18n.save();
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
    new MainDatTradeLoader(facade).doIt();
    facade.dispose();
  }
}
