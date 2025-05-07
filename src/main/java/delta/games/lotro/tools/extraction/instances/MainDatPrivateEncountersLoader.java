package delta.games.lotro.tools.extraction.instances;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.math.geometry.Vector3D;
import delta.games.lotro.common.enums.Difficulty;
import delta.games.lotro.common.enums.GroupSize;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.WJEncounterCategory;
import delta.games.lotro.common.enums.WJEncounterType;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncounterQuests;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;
import delta.games.lotro.lore.instances.io.xml.PrivateEncountersXMLWriter;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.misc.PropertyResponseMapsLoader;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Get private encounters (instances) from DAT files.
 * @author DAM
 */
public class MainDatPrivateEncountersLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatPrivateEncountersLoader.class);

  private DataFacade _facade;
  private List<PrivateEncounter> _data;
  private InstanceMapDataBuilder _mapDataBuilder;
  private LotroEnum<WJEncounterType> _worldJoinType;
  private LotroEnum<WJEncounterCategory> _worldJoinCategory;
  private I18nUtils _i18n;
  private List<Proxy<QuestDescription>> _proxies;
  private PropertyResponseMapsLoader _propertyResponseMapsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param propertyResponseMapsLoader Loader for property response maps.
   */
  public MainDatPrivateEncountersLoader(DataFacade facade, PropertyResponseMapsLoader propertyResponseMapsLoader)
  {
    _facade=facade;
    _data=new ArrayList<PrivateEncounter>();
    _mapDataBuilder=new InstanceMapDataBuilder();
    _worldJoinType=LotroEnumsRegistry.getInstance().get(WJEncounterType.class);
    _worldJoinCategory=LotroEnumsRegistry.getInstance().get(WJEncounterCategory.class);
    _i18n=new I18nUtils("instances",facade.getGlobalStringsManager());
    _proxies=new ArrayList<Proxy<QuestDescription>>();
    _propertyResponseMapsLoader=propertyResponseMapsLoader;
  }

  private PrivateEncounter load(int privateEncounterId, boolean isSkirmish)
  {
    PropertiesSet props=_facade.loadProperties(privateEncounterId+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    PrivateEncounter ret=null;
    SkirmishPrivateEncounter skirmishPE=null;
    if (isSkirmish)
    {
      skirmishPE=new SkirmishPrivateEncounter(privateEncounterId);
      ret=skirmishPE;
    }
    else
    {
      ret=new PrivateEncounter(privateEncounterId);
    }
    // Name
    String name=_i18n.getNameStringProperty(props,"PrivateEncounterTemplate_Name",privateEncounterId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    ret.setName(name);
    // Description
    String description=_i18n.getStringProperty(props,"PrivateEncounterTemplate_Description");
    ret.setDescription(description);
    // Content Layer ID
    int contentLayerId=((Integer)props.getProperty("PrivateEncounterTemplate_ContentLayer")).intValue();
    ret.setContentLayerId(contentLayerId);
    // Public instances?
    Integer isPublicInt=(Integer)props.getProperty("PrivateEncounterTemplate_PublicInstance");
    @SuppressWarnings("unused")
    boolean isPublic=((isPublicInt!=null) && (isPublicInt.intValue()==1));
    // Block references
    List<BlockReference> blocks=new ArrayList<BlockReference>();
    Object[] blocksArray=(Object[])props.getProperty("PrivateEncounterTemplate_ContainedLandBlockArray");
    if ((blocksArray!=null) && (blocksArray.length>0))
    {
      for(Object blockObj : blocksArray)
      {
        Vector3D vector=(Vector3D)blockObj;
        int region=(int)vector.getX();
        int cellX=(int)vector.getY();
        if (cellX%8!=0)
        {
          LOGGER.warn("Cell X is not a multiple of 8: {} for PE {}",Integer.valueOf(cellX),ret);
        }
        int cellY=(int)vector.getZ();
        if (cellY%8!=0)
        {
          LOGGER.warn("Cell Y is not a multiple of 8: {} for PE {}",Integer.valueOf(cellY),ret);
        }
        BlockReference block=new BlockReference(region,cellX/8,cellY/8);
        blocks.add(block);
      }
    }
    // Quests
    loadQuests(ret,props);
    // Maximum number of players
    Integer maxPlayers=(Integer)props.getProperty("PrivateEncounterTemplate_MaxPlayers");
    ret.setMaxPlayers(maxPlayers);

    // Skirmish specifics
    if (skirmishPE!=null)
    {
      loadSkirmishSpecifics(skirmishPE,props);
    }
    if (!accept(ret))
    {
      return null;
    }
    // Additional content layers
    handleAdditionalContentLayers(ret);
    // Build maps
    _mapDataBuilder.handlePrivateEncounter(ret,blocks);
    // Property response maps
    loadPropertyResponseMap(props);
    return ret;
  }

  private void loadQuests(PrivateEncounter pe, PropertiesSet props)
  {
    PrivateEncounterQuests quests=pe.getQuests();
    Integer questId=(Integer)props.getProperty("PrivateEncounterTemplate_InstanceQuest");
    if (questId!=null)
    {
      Proxy<QuestDescription> parentQuest=buildProxy(questId.intValue());
      quests.setParentQuest(parentQuest);
    }
    // Quests to bestow
    /*
    PrivateEncounterTemplate_QuestToBestow_Array:
      #1: 1879172579
      #2: 1879172578
    */
    Object[] questToBestowArray=(Object[])props.getProperty("PrivateEncounterTemplate_QuestToBestow_Array");
    if ((questToBestowArray!=null) && (questToBestowArray.length>0))
    {
      for(Object questToBestowObj : questToBestowArray)
      {
        int id=((Integer)questToBestowObj).intValue();
        Proxy<QuestDescription> proxy=buildProxy(id);
        quests.addQuest(proxy);
      }
    }
    // Random quests
    /*
    PrivateEncounterTemplate_RandomBestowalQuests_Array:
      #1:
        PrivateEncounterTemplate_NumQuestsToBestow: 1
        PrivateEncounterTemplate_QuestToBestow_Array:
          #1: 1879172581
          #2: 1879172582
          #3: 1879172580
    */
    Object[] randomQuestsArray=(Object[])props.getProperty("PrivateEncounterTemplate_RandomBestowalQuests_Array");
    if ((randomQuestsArray!=null) && (randomQuestsArray.length>0))
    {
      int size=randomQuestsArray.length;
      if (size!=1)
      {
        LOGGER.warn("Size is not 1: {} for PE {}",Integer.valueOf(size),pe);
      }
      PropertiesSet randomProps=(PropertiesSet)randomQuestsArray[0];
      Integer nbRandomQuests=(Integer)randomProps.getProperty("PrivateEncounterTemplate_NumQuestsToBestow");
      if ((nbRandomQuests!=null) && (nbRandomQuests.intValue()>0))
      {
        quests.setRandomQuestsCount(nbRandomQuests.intValue());
      }
      Object[] questIdsArray=(Object[])randomProps.getProperty("PrivateEncounterTemplate_QuestToBestow_Array");
      for(Object questIdObj : questIdsArray)
      {
        int id=((Integer)questIdObj).intValue();
        Proxy<QuestDescription> proxy=buildProxy(id);
        quests.addRandomQuest(proxy);
      }
    }
  }

  private void loadPropertyResponseMap(PropertiesSet props)
  {
    Integer mapDID=(Integer)props.getProperty("PrivateEncounterTemplate_PropertyResponseMapDID");
    if ((mapDID!=null) && (mapDID.intValue()>0))
    {
      _propertyResponseMapsLoader.handlePropertyResponseMap(mapDID.intValue());
    }
  }

  private Proxy<QuestDescription> buildProxy(int questId)
  {
    Proxy<QuestDescription> proxy=new Proxy<QuestDescription>();
    proxy.setId(questId);
    _proxies.add(proxy);
    return proxy;
  }

  /**
   * Finish loading:
   * <ul>
   * <li>resolve proxies,
   * <li>save again
   * </ul>
   */
  public void finish()
  {
    for(Proxy<QuestDescription> proxy : _proxies)
    {
      int questId=proxy.getId();
      QuestDescription quest=QuestsManager.getInstance().getQuest(questId);
      if (quest==null)
      {
        LOGGER.warn("Quest not found: {}",proxy);
        continue;
      }
      proxy.setName(quest.getName());
      proxy.setObject(quest);
    }
    save();
  }

  private void loadSkirmishSpecifics(SkirmishPrivateEncounter skirmishPE, PropertiesSet props)
  {
    // - difficulty tiers
    /*
    Skirmish_Template_DifficultyTierArray:
      #1: 1 (Tier I)
      #2: 2 (Tier II)
     */
    Object[] difficultyTiersArray=(Object[])props.getProperty("Skirmish_Template_DifficultyTierArray");
    if (difficultyTiersArray!=null)
    {
      LotroEnum<Difficulty> difficultiesMgr=LotroEnumsRegistry.getInstance().get(Difficulty.class);
      for(Object difficultyTierObj : difficultyTiersArray)
      {
        int difficultyTierCode=((Integer)difficultyTierObj).intValue();
        Difficulty difficulty=difficultiesMgr.getEntry(difficultyTierCode);
        if (difficulty!=null)
        {
          skirmishPE.addDifficultyTier(difficulty);
        }
      }
    }
    else
    {
      LOGGER.warn("No difficulty tier!");
    }
    // - group sizes
    /*
    Skirmish_Template_GroupSizeArray:
      #1: 12 (Raid (12))
     */
    LotroEnum<GroupSize> groupSizesMgr=LotroEnumsRegistry.getInstance().get(GroupSize.class);
    Object[] groupSizesArray=(Object[])props.getProperty("Skirmish_Template_GroupSizeArray");
    if (groupSizesArray!=null)
    {
      for(Object groupSizeObj : groupSizesArray)
      {
        int groupSizeCode=((Integer)groupSizeObj).intValue();
        if (groupSizeCode!=0)
        {
          GroupSize groupSize=groupSizesMgr.getEntry(groupSizeCode);
          skirmishPE.addGroupSize(groupSize);
        }
      }
    }
    else
    {
      LOGGER.warn("No group size!");
    }
    // - level scale
    /*
    Skirmish_Template_MaxLevelScale: 130
    Skirmish_Template_MinLevelScale: 65
     */
    int minLevelScale=((Integer)props.getProperty("Skirmish_Template_MinLevelScale")).intValue();
    int maxLevelScale=((Integer)props.getProperty("Skirmish_Template_MaxLevelScale")).intValue();
    skirmishPE.setLevelScale(minLevelScale,maxLevelScale);
    // - category
    // WorldJoin_EncounterCategory: 11 (Dol Guldur)
    Integer categoryCodeValue=(Integer)props.getProperty("WorldJoin_EncounterCategory");
    int categoryCode=(categoryCodeValue!=null)?categoryCodeValue.intValue():0;
    WJEncounterCategory category=_worldJoinCategory.getEntry(categoryCode);
    skirmishPE.setCategory(category);
    // - type
    // WorldJoin_EncounterType: 4 (Classic)
    Integer typeCode=(Integer)props.getProperty("WorldJoin_EncounterType");
    if (typeCode!=null)
    {
      WJEncounterType type=_worldJoinType.getEntry(typeCode.intValue());
      skirmishPE.setType(type);
    }

    Integer levelScaling=(Integer)props.getProperty("Skirmish_Template_LevelScalingLevel");
    skirmishPE.setLevelScaling(levelScaling);
  }

  private boolean accept(PrivateEncounter pe)
  {
    int id=pe.getIdentifier();
    if (id==1879151688) return false; // Skirmish: Prototype
    if (id==1879077455) return false; // Private Instance: Test
    if (id==1879223788) return false; // PVMP_TEST: Arena
    if (pe.getName().contains("DNT")) return false;
    return true;
  }

  private static final int[] PE_THAT_USE_CONTENT_LAYER_0=
  {
    1879185298, // Urugarth
    1879184817, // Drake Wing
    1879083265, // Fire and Ice
    1879185310, // The Rift of Nûrz Ghâshu
    1879094769, // Instance: The Rift of Nûrz Ghâshu
    1879414603, // Agoroth, the Narrowdelve
  };

  private void handleAdditionalContentLayers(PrivateEncounter pe)
  {
    int peId=pe.getIdentifier();
    for(int id : PE_THAT_USE_CONTENT_LAYER_0)
    {
      if (peId==id)
      {
        pe.addAdditionalContentLayer(0);
        break;
      }
    }
  }

  /**
   * Load private encounters data.
   */
  public void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        // SkirmishEncounterTemplate: 2651
        // PrivateEncounterTemplate: 1368
        boolean useIt=(classDefIndex==2651) || (classDefIndex==1368);
        if (useIt)
        {
          PrivateEncounter instanceData=load(id,classDefIndex==2651);
          if (instanceData!=null)
          {
            _data.add(instanceData);
          }
        }
      }
    }
    save();
    // Save labels
    _i18n.save();
  }

  private void save()
  {
    // Save private encounters
    boolean ok=PrivateEncountersXMLWriter.writePrivateEncountersFile(GeneratedFiles.PRIVATE_ENCOUNTERS,_data);
    if (ok)
    {
      LOGGER.info("Wrote private encounters file: {}",GeneratedFiles.PRIVATE_ENCOUNTERS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    PropertyResponseMapsLoader propertyResponseMapsLoader=new PropertyResponseMapsLoader(facade,effectsLoader);
    new MainDatPrivateEncountersLoader(facade,propertyResponseMapsLoader).doIt();
    effectsLoader.save();
    facade.dispose();
  }
}
