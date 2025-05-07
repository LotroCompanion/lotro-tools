package delta.games.lotro.tools.extraction.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.hobbies.HobbyDescription;
import delta.games.lotro.lore.hobbies.HobbyTitleEntry;
import delta.games.lotro.lore.hobbies.io.xml.HobbyDescriptionXMLWriter;
import delta.games.lotro.lore.hobbies.rewards.HobbyRewardEntry;
import delta.games.lotro.lore.hobbies.rewards.HobbyRewards;
import delta.games.lotro.lore.hobbies.rewards.HobbyRewardsProfile;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.TitlesManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for hobbies data.
 * @author DAM
 */
public class MainHobbiesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainHobbiesLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainHobbiesLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("hobbies",facade.getGlobalStringsManager());
  }

  /**
   * Load hobbies.
   */
  public void doIt()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"HobbyControl");
    Object[] ids=(Object[])props.getProperty("Hobby_List");
    List<HobbyDescription> hobbies=new ArrayList<HobbyDescription>();
    for(Object idObj : ids)
    {
      int id=((Integer)idObj).intValue();
      HobbyDescription hobby=handleHobby(id);
      hobbies.add(hobby);
    }
    saveHobbies(hobbies);
  }

  private HobbyDescription handleHobby(int hobbyId)
  {
    PropertiesSet props=_facade.loadProperties(hobbyId+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    HobbyDescription ret=new HobbyDescription();
    // ID
    ret.setIdentifier(hobbyId);
    // Name
    String name=_i18n.getNameStringProperty(props,"Hobby_Name",hobbyId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    ret.setName(name);
    // Type
    int type=((Integer)props.getProperty("Hobby_Type")).intValue();
    ret.setHobbyType(type);
    // Description
    String description=_i18n.getStringProperty(props,"Hobby_Description");
    ret.setDescription(description);
    // Trainer display info
    String trainerDisplayInfo=_i18n.getStringProperty(props,"Hobby_TrainerDisplayInfo");
    ret.setTrainerDisplayInfo(trainerDisplayInfo);
    // Icon
    int iconId=((Integer)props.getProperty("Hobby_Icon")).intValue();
    ret.setIconId(iconId);
    // Build icon file
    String iconFilename=iconId+".png";
    File to=new File(GeneratedFiles.HOBBY_ICONS,iconFilename).getAbsoluteFile();
    if (!to.exists())
    {
      boolean ok=DatIconsUtils.buildImageFile(_facade,iconId,to);
      if (!ok)
      {
        LOGGER.warn("Could not build hobby icon: {}",iconFilename);
      }
    }
    // Daily proficiency gain limit
    int dailyProficiencyGainLimit=((Integer)props.getProperty("Hobby_DailyProficiencyGainLimit")).intValue();
    ret.setDailyProficiencyGainLimit(dailyProficiencyGainLimit);
    // Items
    Object[] itemsArray=(Object[])props.getProperty("Hobby_ItemList");
    for(Object itemIdObj : itemsArray)
    {
      int itemID=((Integer)itemIdObj).intValue();
      Item item=ItemsManager.getInstance().getItem(itemID);
      ret.addItem(item);
    }
    // Min Level
    int minLevel=((Integer)props.getProperty("Hobby_MinLevel")).intValue();
    ret.setMinLevel(minLevel);
    // Property names
    PropertiesRegistry registry=_facade.getPropertiesRegistry();
    // - Proficiency property
    int proficiencyPropertyID=((Integer)props.getProperty("Hobby_Proficiency_PropertyName")).intValue();
    String proficiencyPropertyName=registry.getPropertyDef(proficiencyPropertyID).getName();
    ret.setProficiencyPropertyName(proficiencyPropertyName);
    // - Proficiency Modifier property
    int proficiencyModifierPropertyID=((Integer)props.getProperty("Hobby_ProficiencyModifier_PropertyName")).intValue();
    String proficiencyModifierPropertyName=registry.getPropertyDef(proficiencyModifierPropertyID).getName();
    ret.setProficiencyModifierPropertyName(proficiencyModifierPropertyName);
    // - Proficiency Modifier property
    int treasureProfileOverridePropertyID=((Integer)props.getProperty("Hobby_TreasureProfileOverride_PropertyName")).intValue();
    String treasureProfileOverridePropertyName=registry.getPropertyDef(treasureProfileOverridePropertyID).getName();
    ret.setTreasureProfileOverridePropertyName(treasureProfileOverridePropertyName);
    // Titles
    Object[] titlesArray=(Object[])props.getProperty("Hobby_TitleList");
    for(Object titleObj : titlesArray)
    {
      PropertiesSet titleProps=(PropertiesSet)titleObj;
      int proficiency=((Integer)titleProps.getProperty("Hobby_Proficiency")).intValue();
      int titleID=((Integer)titleProps.getProperty("Hobby_Title")).intValue();
      TitleDescription title=TitlesManager.getInstance().getTitle(titleID);
      if (title!=null)
      {
        HobbyTitleEntry entry=new HobbyTitleEntry(proficiency,title);
        ret.addTitle(entry);
      }
    }
    // Rewards
    handleRewards(props,ret.getRewards());
    return ret;
  }

  private void handleRewards(PropertiesSet props, HobbyRewards rewards)
  {
    Object[] profilesArray=(Object[])props.getProperty("Hobby_TreasureProfileList");
    for(Object profileObj : profilesArray)
    {
      PropertiesSet profileProps=(PropertiesSet)profileObj;
      int territoryID=((Integer)profileProps.getProperty("Hobby_Territory")).intValue();
      int profileID=((Integer)profileProps.getProperty("Hobby_TreasureProfile")).intValue();
      HobbyRewardsProfile profile=null;
      if (profileID!=0)
      {
        profile=handleProfile(profileID);
      }
      rewards.registerProfile(territoryID,profile);
    }
  }

  private HobbyRewardsProfile handleProfile(int profileID)
  {
    HobbyRewardsProfile profile=new HobbyRewardsProfile(profileID);
    PropertiesSet props=_facade.loadProperties(profileID+DATConstants.DBPROPERTIES_OFFSET);
    Object[] itemsArray=(Object[])props.getProperty("HobbyRewardProfile_ItemArray");
    for(Object itemObj : itemsArray)
    {
      PropertiesSet entryProps=(PropertiesSet)itemObj;
      int itemID=((Integer)entryProps.getProperty("HobbyRewardProfile_Item")).intValue();
      int minProficiency=((Integer)entryProps.getProperty("HobbyRewardProfile_MinProficiency")).intValue();
      int maxProficiency=((Integer)entryProps.getProperty("HobbyRewardProfile_MaxProficiency")).intValue();
      int weight=((Integer)entryProps.getProperty("HobbyRewardProfile_Weight")).intValue();
      Item item=ItemsManager.getInstance().getItem(itemID);
      if (item!=null)
      {
        HobbyRewardEntry entry=new HobbyRewardEntry(item,minProficiency,maxProficiency,weight);
        profile.addEntry(entry);
      }
    }
    return profile;
  }

  /**
   * Save hobbies to disk.
   * @param hobbies Data to save.
   */
  private void saveHobbies(List<HobbyDescription> hobbies)
  {
    int nbHobbies=hobbies.size();
    LOGGER.info("Writing {} hobbies",Integer.valueOf(nbHobbies));
    // Write hobbies file
    boolean ok=HobbyDescriptionXMLWriter.write(GeneratedFiles.HOBBIES,hobbies);
    if (ok)
    {
      LOGGER.info("Wrote hobbies file: {}",GeneratedFiles.HOBBIES);
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
    new MainHobbiesLoader(facade).doIt();
    facade.dispose();
  }
}
