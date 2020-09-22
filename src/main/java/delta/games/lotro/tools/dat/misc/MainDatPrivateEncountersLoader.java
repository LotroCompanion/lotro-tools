package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.Vector3D;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;
import delta.games.lotro.lore.instances.io.xml.PrivateEncountersXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
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

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatPrivateEncountersLoader(DataFacade facade)
  {
    _facade=facade;
    _data=new ArrayList<PrivateEncounter>();
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
    String name=DatStringUtils.getStringProperty(props,"PrivateEncounterTemplate_Name");
    name=StringUtils.fixName(name);
    ret.setName(name);
    // Description
    String description=DatStringUtils.getStringProperty(props,"PrivateEncounterTemplate_Description");
    ret.setDescription(description);
    // Content Layer ID
    int contentLayerId=((Integer)props.getProperty("PrivateEncounterTemplate_ContentLayer")).intValue();
    ret.setContentLayerId(contentLayerId);
    // Block references
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
        BlockReference block=new BlockReference();
        block.setRegion(region);
        block.setBlock(cellX/8,cellY/8);
        ret.addBlock(block);
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
      Integer levelScaling=(Integer)props.getProperty("Skirmish_Template_LevelScalingLevel");
      skirmishPE.setLevelScaling(levelScaling);
    }
    return ret;
  }

  private void doIt()
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
