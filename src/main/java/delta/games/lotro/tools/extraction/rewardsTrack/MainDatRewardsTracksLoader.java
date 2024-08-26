package delta.games.lotro.tools.extraction.rewardsTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.rewardsTrack.RewardsTrack;
import delta.games.lotro.lore.rewardsTrack.RewardsTrackStep;
import delta.games.lotro.lore.rewardsTrack.io.xml.RewardsTracksXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get definition of rewards tracks from DAT files.
 * @author DAM
 */
public class MainDatRewardsTracksLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRewardsTracksLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatRewardsTracksLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("rewardsTracks",facade.getGlobalStringsManager());
  }

  private String getPropertyName(PropertiesSet properties, String propertyName)
  {
    Integer propertyID=(Integer)properties.getProperty(propertyName);
    if (propertyID!=null)
    {
      PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyID.intValue());
      return propertyDef.getName();
    }
    LOGGER.warn("Property not found: "+propertyName);
    return "";
  }

  private RewardsTrack loadRewardsTrack(int rewardsTrackID)
  {
    RewardsTrack ret=null;
    PropertiesSet properties=_facade.loadProperties(rewardsTrackID+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new RewardsTrack(rewardsTrackID);
      // Name
      String name=_i18n.getNameStringProperty(properties,"Name",rewardsTrackID,0);
      ret.setName(name);
      // Monster play
      // ..
      boolean monsterPlay=false;
      PropertiesSet permissions=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
      if (permissions!=null)
      {
        Integer monsterPlayCanUse=(Integer)permissions.getProperty("Usage_MonsterPlayerCanUse");
        if ((monsterPlayCanUse!=null) && (monsterPlayCanUse.intValue()==1))
        {
          monsterPlay=true;
        }
      }
      ret.setMonsterPlay(monsterPlay);
      // Description
      String description=_i18n.getStringProperty(properties,"Description");
      ret.setDescription(description);
      // Progression
      int progressionID=((Integer)properties.getProperty("RewardTrack_RewardIntervalProgression")).intValue();
      ret.setXpIntervalsProgressionID(progressionID);
      // Properties
      ret.setActiveProperty(getPropertyName(properties,"RewardTrack_ActiveProperty"));
      ret.setClaimedMilestonesProperty(getPropertyName(properties,"RewardTrack_ClaimedMilestonesProperty"));
      ret.setCurrentMilestoneProperty(getPropertyName(properties,"RewardTrack_CurrentExperienceMilestoneProperty"));
      ret.setNextExperienceGoalProperty(getPropertyName(properties,"RewardTrack_CurrentExperienceGoalProperty"));
      ret.setCurrentExperienceProperty(getPropertyName(properties,"RewardTrack_ExperienceProperty"));
      ret.setLastExperienceGoalProperty(getPropertyName(properties,"RewardTrack_LastExperienceGoalProperty"));
      // Steps
      Object[] stepsList=(Object[])properties.getProperty("RewardTrack_Array");
      for(Object stepObj : stepsList)
      {
        PropertiesSet stepProps=(PropertiesSet)stepObj;
        RewardsTrackStep step=loadStep(stepProps);
        ret.addStep(step);
      }
    }
    else
    {
      LOGGER.warn("Could not handle collection ID="+rewardsTrackID);
    }
    return ret;
  }

  private RewardsTrackStep loadStep(PropertiesSet stepProps)
  {
    /*
        #1: RewardTrack_Array_Entry
          RewardTrack_Array_ExperienceCostMultiplier: 1.0
          RewardTrack_Array_PipElement: 268453791 (ItemAdvancementRewards_Pip)
          RewardTrack_Array_Reward: 1879440795
     */
    RewardsTrackStep ret=new RewardsTrackStep();
    float xpCostMultiplier=((Float)stepProps.getProperty("RewardTrack_Array_ExperienceCostMultiplier")).floatValue();
    ret.setXpCostMultiplier(xpCostMultiplier);
    int uiElementID=((Integer)stepProps.getProperty("RewardTrack_Array_PipElement")).intValue();
    ret.setUiElementID(uiElementID);
    // Reward item
    int rewardID=((Integer)stepProps.getProperty("RewardTrack_Array_Reward")).intValue();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    Item item=itemsMgr.getItem(rewardID);
    if (item!=null)
    {
      ret.setReward(item);
      int largeIconID=loadItemIcon(item.getIdentifier());
      ret.setLargeIconID(largeIconID);
    }
    return ret;
  }

  private int loadItemIcon(int itemID)
  {
    PropertiesSet props=_facade.loadProperties(itemID+DATConstants.DBPROPERTIES_OFFSET);
    Integer iconID=(Integer)props.getProperty("Icon_Layer_LargeImageDID");
    if (iconID!=null)
    {
      File iconFile=new File(GeneratedFiles.ITEM_LARGE_ICONS_DIR,iconID+".png").getAbsoluteFile();
      if (!iconFile.exists())
      {
        DatIconsUtils.buildImageFile(_facade,iconID.intValue(),iconFile);
      }
    }
    return (iconID!=null)?iconID.intValue():0;
  }

  /**
   * Load rewards tracks.
   */
  public void doIt()
  {
    List<RewardsTrack> rewardsTracks=new ArrayList<RewardsTrack>();
    PropertiesSet rewardTrackControlProps=_facade.loadProperties(1879441389+DATConstants.DBPROPERTIES_OFFSET);
    Object[] rewardsTracksList=(Object[])rewardTrackControlProps.getProperty("RewardTrackControl_RewardTrack_Array");
    for(Object rewardsTrackObj : rewardsTracksList)
    {
      int rewardsTrackId=((Integer)rewardsTrackObj).intValue();
      RewardsTrack rewardsTrack=loadRewardsTrack(rewardsTrackId);
      if (rewardsTrack!=null)
      {
        rewardsTracks.add(rewardsTrack);
      }
    }
    LOGGER.info("Loaded "+rewardsTracks.size()+" rewards tracks.");
    saveRewardsTracks(rewardsTracks);
  }

  /**
   * Save the loaded rewards tracks to a file.
   * @param rewardsTracks Rewards tracks to save.
   */
  private void saveRewardsTracks(List<RewardsTrack> rewardsTracks)
  {
    // Data
    Collections.sort(rewardsTracks,new IdentifiableComparator<RewardsTrack>());
    boolean ok=RewardsTracksXMLWriter.writeRewardsTracksFile(GeneratedFiles.REWARDS_TRACKS,rewardsTracks);
    if (ok)
    {
      LOGGER.info("Wrote rewards tracks file: "+GeneratedFiles.REWARDS_TRACKS);
    }
    // Labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatRewardsTracksLoader(facade).doIt();
    facade.dispose();
  }
}
