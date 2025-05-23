package delta.games.lotro.tools.extraction.geo.markers.classification;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.io.Console;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.crafting.CraftingLevel;

/**
 * Classifier for markers.
 * @author DAM
 */
public class MarkerClassifier
{
  /**
   * Craft resource type property name.
   */
  private static final String CRAFT_RESOURCE_TYPE="Craft_Resource_Type";
  /**
   * Weenie type property name.
   */
  private static final String WEENIE_TYPE="WeenieType";
  /**
   * Map note type property name.
   */
  private static final String MAP_NOTE_TYPE="MapNote_Type";
  private static final Logger LOGGER=LoggerFactory.getLogger(MarkerClassifier.class);
  private static final boolean VERBOSE=false;

  private DataFacade _facade;
  private EnumMapper _mapNoteType;
  private BasicCraftingData _crafting;

  // Map of resolved items
  private Map<Integer,Classification> _cache;

  private static final int[] CRITTER_NPC = {
    1879363746, // Black Squirrel
    1879080635, // Deer
    1879061185, // Dora's Chicken
    1879077970, // Frog
    1879078196, // Fox
    1879358942, // Goat
    1879255968, // Horse
    1879339414, // Ithilien Fox
    1879079062, // Rabbit
    1879077352, // Restless Shade
    1879078796, // Squirrel
    1879076795, // Sheep
    1879204319, // Shrew
    1879078100, // Snake
    1879363747, // Turtle
  };

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MarkerClassifier(DataFacade facade)
  {
    _facade=facade;
    _cache=new HashMap<Integer,Classification>();
    _mapNoteType=_facade.getEnumsManager().getEnumMapper(587202775);
    _crafting=new BasicCraftingData();
  }

  /**
   * Perform classification of a single DID.
   * @param did DID to use.
   * @return A classification or <code>null</code>.
   */
  public Classification classifyDid(int did)
  {
    Integer key=Integer.valueOf(did);
    Classification classification=null;
    if (!_cache.containsKey(key))
    {
      classification=getClassification(did);
      _cache.put(key,classification);
      if (VERBOSE)
      {
        DataIdentification dataId=DataIdentificationTools.identify(_facade,did);
        if (classification!=null)
        {
          Console.println(dataId+" => Classification: "+classification);
        }
        else
        {
          Console.println(dataId+" => NO CLASSIFICATION");
        }
      }
    }
    else
    {
      classification=_cache.get(key);
    }
    return classification;
  }

  private Classification getClassification(int did)
  {
    PropertiesSet props=_facade.loadProperties(did+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    Classification rc=tryResourceNodeClassification(did,props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryResourceCropClassification(did,props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryMonsterClassification(props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryItemClassification(props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryContainerClassification(did,props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryNpcClassification(did,props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryLandmarkClassification(props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryMilestoneClassification(props);
    return rc;
  }

  private ResourceClassification tryResourceCropClassification(int did, PropertiesSet props)
  {
    boolean ok=false;
    Integer craftResourceTypeCode=(Integer)props.getProperty(CRAFT_RESOURCE_TYPE);
    if ((craftResourceTypeCode!=null) && (craftResourceTypeCode.intValue()==4))
    {
      ok=true;
    }
    else
    {
      Integer usedInCrafting=(Integer)props.getProperty("Craft_UsedInCrafting");
      if ((usedInCrafting!=null) && (usedInCrafting.intValue()==1))
      {
        ok=true;
      }
    }
    if (!ok)
    {
      return null;
    }
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);
    if ((weenieType==null) || (weenieType.intValue()!=129)) // Item
    {
      LOGGER.warn("No/bad weenie type while craft resource type is set! DID={}",Integer.valueOf(did));
      return null;
    }

    Integer craftTierCodeInt=(Integer)props.getProperty("CraftTrinket_Tier");
    int craftTierCode=(craftTierCodeInt!=null)?craftTierCodeInt.intValue():getTierFromDID(did);
    Integer professionIdInt=(Integer)props.getProperty("CraftTrinket_Profession");
    int professionId=(professionIdInt!=null)?professionIdInt.intValue():getProfessionFromDID(did);
    // Fix Farmer -> Cook
    if (professionId==1879062816) professionId=1879061252;
    CraftingLevel level=_crafting.getLevel(professionId,craftTierCode);
    CropClassification c=new CropClassification(level);
    return c;
    /*
    CraftTrinket_Profession: 1879061252
    CraftTrinket_Tier: 3 (Expert)
    Craft_Resource_Type: 4 (Crop)
    Craft_UsedInCrafting: 1
    WeenieType: 129 (Item)
     */
  }

  private int getTierFromDID(int did)
  {
    // Scholar tier 1
    if ((did==1879054949) || (did==1879066722) || (did==1879070248)) return 1;
    // Cook tier 2
    if ((did==1879061257) || (did==1879061342) || (did==1879061395) || (did==1879061345)) return 2;
    // Cook tier 3
    if (did==1879061259) return 3;
    // Cook tier 4
    if ((did==1879061415) || (did==1879061417)) return 4;
    // Cook tier 5
    if ((did==1879061399) || (did==1879061414) || (did==1879061416) || (did==1879061418)) return 5;
    LOGGER.warn("unmanaged DID: {}",Integer.valueOf(did));
    return 0;
  }

  private int getProfessionFromDID(int did)
  {
    // Scholar
    if ((did==1879054949) || (did==1879066722) || (did==1879070248)) return 1879054946;
    // Otherwise, cook
    return 1879061252;
  }

  private ResourceClassification tryResourceNodeClassification(int did, PropertiesSet props)
  {
    Integer craftResourceTypeCode=(Integer)props.getProperty(CRAFT_RESOURCE_TYPE);
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);

    if (craftResourceTypeCode==null)
    {
      return null;
    }
    int typeCode=craftResourceTypeCode.intValue();
    if ((typeCode==0) || (typeCode==1) || (typeCode==4))
    {
      // Undef (0), Treasure (1) or Crop (4)
      return null;
    }
    if ((weenieType==null) || (weenieType.intValue()!=259)) // GameplayContainer
    {
      LOGGER.warn("No/bad weenie type while craft resource type is set! DID={}",Integer.valueOf(did));
      return null;
    }
    int craftTier=0;
    boolean live=Context.isLive();
    if (live)
    {
      Integer craftTierCode=(Integer)props.getProperty("Usage_RequiredCraftTier");
      craftTier=(craftTierCode!=null)?craftTierCode.intValue():1;
    }
    else
    {
      Integer craftTierCode=(Integer)props.getProperty("Usage_RequiredCraftProficiency");
      craftTier=1+((craftTierCode!=null)?craftTierCode.intValue():0);
    }
    int professionId=((Integer)props.getProperty("Usage_RequiredCraftProfession")).intValue();
    CraftingLevel level=_crafting.getLevel(professionId,craftTier);
    ResourceClassification c=new ResourceClassification(level);
    return c;
    /*
    Craft_Resource_Type: 2 (Mine)
    Usage_RequiredCraftProfession: 1879062818
    Usage_RequiredCraftTier: 5 (Master)
    Usage_RequiredCraftTool: 8 (Prospector's Tools)
    WeenieType: 259 (GameplayContainer)
    */
  }

  private MonsterClassification tryMonsterClassification(PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);
    if ((weenieType==null) || (weenieType.intValue()!=65615))
    {
      return null;
    }
    Integer speciesCode=(Integer)props.getProperty("Agent_Species");
    // Critters
    /*
    Agent_Alignment: 2 (Neutral)
    Agent_Class: 87 (Critter)
    Agent_Species: 27 (Critter)
    WeenieType: 65615 (Monster)
    */
    boolean critter=((speciesCode!=null) && (speciesCode.intValue()==27));
    MonsterClassification ret=new MonsterClassification(critter);
    return ret;
  }

  // !! Map Note type of type "No Icon" is found for NPC, Landmarks...
  // Workbench is sometimes an Item, sometimes a Hotspot (should be classified as Crafting Facility)

  private ItemClassification tryItemClassification(PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);
    if ((weenieType==null) || (weenieType.intValue()!=129)) // Item
    {
      return null;
    }
    ItemClassification ret=null;
    Long mapNoteType=(Long)props.getProperty(MAP_NOTE_TYPE);
    if ((mapNoteType!=null) && (mapNoteType.longValue()!=0))
    {
      // Expect:
      // - Mailbox
      // - Crafting Facility (Oven, Forge, Study, Workbench, Campfire)
      // !! Farmland is a Hotspot, not an Item...
      /*
******** Properties: 1879063084
EffectGenerator_HotspotEffectList:
  #1:
    EffectGenerator_EffectID: 1879063024 => "Superior Farmland"
    EffectGenerator_EffectSpellcraft: 1.0
  #2:
    EffectGenerator_EffectID: 1879062982 => "Farmland"
    EffectGenerator_EffectSpellcraft: 1.0
Hotspot_IsHotspot: 1
MapNote_Enabled: 1
MapNote_Level: 65536 (World)
MapNote_StringInfo:
  #1: Farmland
MapNote_Type: 17592186044416 (Crafting Facility)
Name:
  #1: Farmland
WeenieType: 262145 (Hotspot)
       */
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(mapNoteType.longValue());
      String typeStr=BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType,"/");
      ret=new ItemClassification(typeStr);
    }
    if (ret==null)
    {
      ret=new ItemClassification("");
    }
    return ret;
  }

  private ItemClassification tryContainerClassification(int did, PropertiesSet props)
  {
    // Chests
    Integer craftResourceTypeCode=(Integer)props.getProperty(CRAFT_RESOURCE_TYPE);
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);

    if (craftResourceTypeCode==null)
    {
      return null;
    }
    if (craftResourceTypeCode.intValue()!=1)
    {
      // Treasure
      return null;
    }
    if ((weenieType==null) || (weenieType.intValue()!=259)) // GameplayContainer
    {
      LOGGER.warn("No/bad weenie type while craft resource type is 1! DID={}",Integer.valueOf(did));
      return null;
    }
    /*
******** Properties: 1879158306
Container_DecayPolicy: 1 (OnLoot)
Craft_Resource_Type: 1 (Treasure)
Item_Class: 53 (Special)
LootGen_CustomSkirmishLootLookupTable: 1879187116
WeenieType: 259 (GameplayContainer)
     */
    return new ItemClassification("Container");
  }

  private Classification tryNpcClassification(int did, PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);
    if ((weenieType==null) || (weenieType.intValue()!=131151)) // RealNPC
    {
      return null;
    }
    NpcClassification ret=null;
    Long mapNoteType=(Long)props.getProperty(MAP_NOTE_TYPE);
    if ((mapNoteType!=null) && (mapNoteType.longValue()!=0))
    {
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(mapNoteType.longValue());
      String typeStr=BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType,"/");
      int code=typeSet.nextSetBit(0)+1;
      if (code!=39)
      {
        ret=new NpcClassification(typeStr);
      }
    }
    // NPC that are critters
    if (ret==null)
    {
      for(int critterId : CRITTER_NPC)
      {
        if (did==critterId)
        {
          return new MonsterClassification(true);
        }
      }
    }
    if (ret==null)
    {
      ret=new NpcClassification("");
    }
    return ret;
  }

  private CategoryClassification tryLandmarkClassification(PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);
    if ((weenieType==null) || (weenieType.intValue()!=4194305)) // Landmark
    {
      return null;
    }
    Long mapNoteType=(Long)props.getProperty(MAP_NOTE_TYPE);
    if ((mapNoteType!=null) && (mapNoteType.longValue()!=0))
    {
      // Point of Interest, Settlement, No Icon (39)
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(mapNoteType.longValue());
      String typeStr=BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType,"/");
      int code=typeSet.nextSetBit(0)+1;
      if (code==39)
      {
        code=43; // Point of Interest
        typeStr=_mapNoteType.getLabel(code);
      }
      return new CategoryClassification(code,typeStr);
    }
    return null;
    /*
    ******** Properties: 1879399128
    MapNote_Discoverable: 1789 (Discoverable_lm_breeland_wildwoods_switchstate_fields_west)
    MapNote_Type: 4398046511104 (Point of Interest)
    Name:
      #1: Blomley Sward
    WeenieType: 4194305 (Landmark)
    */
  }

  private CategoryClassification tryMilestoneClassification(PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty(WEENIE_TYPE);
    if ((weenieType==null) || (weenieType.intValue()!=327809)) // Milestone
    {
      return null;
    }
    Long mapNoteType=(Long)props.getProperty(MAP_NOTE_TYPE);
    if ((mapNoteType!=null) && (mapNoteType.longValue()!=0))
    {
      // Camp Site, Milestone
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(mapNoteType.longValue());
      String typeStr=BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType,"/");
      int code=typeSet.nextSetBit(0)+1;
      return new CategoryClassification(code,typeStr);
    }
    return null;
    /*
    ******** Properties: 1879089870
    MapNote_Type: 72057594037927936 (Camp Site[e])
    Name:
      #1: Camp Site Fire
    WeenieType: 327809 (Milestone)
    */
  }
}
