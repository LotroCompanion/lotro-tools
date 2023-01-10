package delta.games.lotro.tools.dat.instances;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.math.geometry.Vector3D;
import delta.games.lotro.common.enums.Difficulty;
import delta.games.lotro.common.enums.GroupSize;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;
import delta.games.lotro.lore.instances.io.xml.PrivateEncountersXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Get private encounters (instances) from DAT files.
 * @author DAM
 */
public class MainDatPrivateEncountersLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatPrivateEncountersLoader.class);

  private DataFacade _facade;
  private List<PrivateEncounter> _data;
  private InstanceMapDataBuilder _mapDataBuilder;
  private EnumMapper _worldJoinType;
  private EnumMapper _worldJoinCategory;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatPrivateEncountersLoader(DataFacade facade)
  {
    _facade=facade;
    _data=new ArrayList<PrivateEncounter>();
    _mapDataBuilder=new InstanceMapDataBuilder();
    _worldJoinType=facade.getEnumsManager().getEnumMapper(0x23000309);
    _worldJoinCategory=facade.getEnumsManager().getEnumMapper(0x23000350);
  }

  private PrivateEncounter load(int privateEncounterId, boolean isSkirmish)
  {
    PropertiesSet props=_facade.loadProperties(privateEncounterId+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    //System.out.println(props.dump());
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
    String name=DatUtils.getStringProperty(props,"PrivateEncounterTemplate_Name");
    name=StringUtils.fixName(name);
    ret.setName(name);
    // Description
    String description=DatUtils.getStringProperty(props,"PrivateEncounterTemplate_Description");
    ret.setDescription(description);
    // Content Layer ID
    int contentLayerId=((Integer)props.getProperty("PrivateEncounterTemplate_ContentLayer")).intValue();
    ret.setContentLayerId(contentLayerId);
    // Public instances?
    /*
    Integer isPublic=(Integer)props.getProperty("PrivateEncounterTemplate_PublicInstance");
    if ((isPublic!=null) && (isPublic.intValue()==1))
    {
      // Assume public instances do use the world layer!
      ret.addAdditionalContentLayer(0);
    }
    */
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
          LOGGER.warn("Cell X is not a multiple of 8: "+cellX+" for PE "+privateEncounterId);
        }
        int cellY=(int)vector.getZ();
        if (cellY%8!=0)
        {
          LOGGER.warn("Cell Y is not a multiple of 8: "+cellY+" for PE "+privateEncounterId);
        }
        BlockReference block=new BlockReference(region,cellX/8,cellY/8);
        blocks.add(block);
      }
    }
    // Quest ID
    Integer questId=(Integer)props.getProperty("PrivateEncounterTemplate_InstanceQuest");
    ret.setQuestId(questId);
    // Quests to bestow
    Object[] questToBestowArray=(Object[])props.getProperty("PrivateEncounterTemplate_QuestToBestow_Array");
    if ((questToBestowArray!=null) && (questToBestowArray.length>0))
    {
      for(Object questToBestowObj : questToBestowArray)
      {
        ret.addQuestToBestow(((Integer)questToBestowObj).intValue());
      }
    }
    /*
    PrivateEncounterTemplate_QuestToBestow_Array: 
      #1: 1879172579
      #2: 1879172578
    PrivateEncounterTemplate_RandomBestowalQuests_Array: 
      #1: 
        PrivateEncounterTemplate_NumQuestsToBestow: 1
        PrivateEncounterTemplate_QuestToBestow_Array: 
          #1: 1879172581
          #2: 1879172582
          #3: 1879172580
    */
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
    return ret;
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
    Integer categoryCode=(Integer)props.getProperty("WorldJoin_EncounterCategory");
    if (categoryCode!=null)
    {
      String category=_worldJoinCategory.getString(categoryCode.intValue());
      if (categoryCode.intValue()==0)
      {
        category="Other";
      }
      skirmishPE.setCategory(category);
    }
    else
    {
      //LOGGER.warn("No category code: "+skirmishPE.getName());
    }
    // - type
    // WorldJoin_EncounterType: 4 (Classic)
    Integer typeCode=(Integer)props.getProperty("WorldJoin_EncounterType");
    if (typeCode!=null)
    {
      String type=_worldJoinType.getString(typeCode.intValue());
      skirmishPE.setType(type);
    }
    else
    {
      //LOGGER.warn("No type code: "+skirmishPE.getName());
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

  private static int[] CL0=
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
    for(int id : CL0)
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
    // Save private encounters
    boolean ok=PrivateEncountersXMLWriter.writePrivateEncountersFile(GeneratedFiles.PRIVATE_ENCOUNTERS,_data);
    if (ok)
    {
      System.out.println("Wrote private encounters file: "+GeneratedFiles.PRIVATE_ENCOUNTERS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatPrivateEncountersLoader(facade).doIt();
    facade.dispose();
  }
}
