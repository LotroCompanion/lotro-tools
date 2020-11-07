package delta.games.lotro.tools.dat.maps.classification;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.agents.AgentClassification;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.tools.dat.agents.ClassificationLoader;

/**
 * Classifier for markers.
 * @author DAM
 */
public class MarkerClassifier
{
  private static final Logger LOGGER=Logger.getLogger(MarkerClassifier.class);
  private static final boolean VERBOSE=false;

  private DataFacade _facade;
  private EnumMapper _mapNoteType;
  private ClassificationLoader _agentSpecLoader;

  // Map of resolved items
  private Map<Integer,Classification> _cache;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MarkerClassifier(DataFacade facade)
  {
    _facade=facade;
    _cache=new HashMap<Integer,Classification>();
    _mapNoteType=_facade.getEnumsManager().getEnumMapper(587202775);
    _agentSpecLoader=new ClassificationLoader(facade);
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
          System.out.println(dataId+" => Classification: "+classification);
        }
        else
        {
          System.out.println(dataId+" => NO CLASSIFICATION");
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
    rc=tryMonsterClassification(did,props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryItemClassification(did,props);
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
    rc=tryLandmarkClassification(did,props);
    if (rc!=null)
    {
      return rc;
    }
    rc=tryMilestoneClassification(did,props);
    return rc;
  }

  private ResourceClassification tryResourceCropClassification(int did, PropertiesSet props)
  {
    Integer craftResourceTypeCode=(Integer)props.getProperty("Craft_Resource_Type");
    if (craftResourceTypeCode==null)
    {
      return null;
    }
    int typeCode=craftResourceTypeCode.intValue();
    if (typeCode!=4) // Crop (4)
    {
      return null;
    }
    Integer usedInCrafting=(Integer)props.getProperty("Craft_UsedInCrafting");
    if ((usedInCrafting==null) || (usedInCrafting.intValue()!=1))
    {
      return null;
    }
    Integer weenieType=(Integer)props.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=129)) // Item
    {
      LOGGER.warn("No/bad weenie type while craft resource type is set! DID="+did);
      return null;
    }

    Integer craftTierCode=(Integer)props.getProperty("CraftTrinket_Tier");
    int professionId=((Integer)props.getProperty("CraftTrinket_Profession")).intValue();
    CraftingData craftingData=CraftingSystem.getInstance().getData();
    Profession profession=craftingData.getProfessionsRegistry().getProfessionById(professionId);
    CraftingLevel level=profession.getByTier(craftTierCode.intValue());
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

  private ResourceClassification tryResourceNodeClassification(int did, PropertiesSet props)
  {
    Integer craftResourceTypeCode=(Integer)props.getProperty("Craft_Resource_Type");
    Integer weenieType=(Integer)props.getProperty("WeenieType");

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
      LOGGER.warn("No/bad weenie type while craft resource type is set! DID="+did);
      return null;
    }

    Integer craftTierCode=(Integer)props.getProperty("Usage_RequiredCraftTier");
    int professionId=((Integer)props.getProperty("Usage_RequiredCraftProfession")).intValue();
    CraftingData craftingData=CraftingSystem.getInstance().getData();
    Profession profession=craftingData.getProfessionsRegistry().getProfessionById(professionId);
    CraftingLevel level=profession.getByTier(craftTierCode.intValue());
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

  private MonsterClassification tryMonsterClassification(int did, PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=65615))
    {
      return null;
    }

    AgentClassification mobClassification=new AgentClassification();
    _agentSpecLoader.loadClassification(props,mobClassification);
    MonsterClassification ret=new MonsterClassification(mobClassification);
    return ret;
  }

  // Critters
  /*
Agent_Alignment: 2 (Neutral)
Agent_Class: 87 (Critter)
Agent_Species: 27 (Critter)
WeenieType: 65615 (Monster)
   */

  // !! Map Note type of type "No Icon" is found for NPC, Landmarks...
  // Workbench is sometimes an Item, sometimes a Hotspot (should be classified as Crafting Facility)

  private ItemClassification tryItemClassification(int did, PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=129)) // Item
    {
      return null;
    }
    ItemClassification ret=null;
    Long mapNoteType=(Long)props.getProperty("MapNote_Type");
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
      //int code=typeSet.nextSetBit(0)+1;
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
    Integer craftResourceTypeCode=(Integer)props.getProperty("Craft_Resource_Type");
    Integer weenieType=(Integer)props.getProperty("WeenieType");

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
      LOGGER.warn("No/bad weenie type while craft resource type is 1! DID="+did);
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

  private NpcClassification tryNpcClassification(int did, PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=131151)) // RealNPC
    {
      return null;
    }
    NpcClassification ret=null;
    Long mapNoteType=(Long)props.getProperty("MapNote_Type");
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
    if (ret==null)
    {
      ret=new NpcClassification("");
    }
    return ret;
  }

  private CategoryClassification tryLandmarkClassification(int did, PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=4194305)) // Landmark
    {
      return null;
    }
    Long mapNoteType=(Long)props.getProperty("MapNote_Type");
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

  private CategoryClassification tryMilestoneClassification(int did, PropertiesSet props)
  {
    Integer weenieType=(Integer)props.getProperty("WeenieType");
    if ((weenieType==null) || (weenieType.intValue()!=327809)) // Milestone
    {
      return null;
    }
    Long mapNoteType=(Long)props.getProperty("MapNote_Type");
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
