package delta.games.lotro.tools.extraction.allegiances;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.enums.AllegianceGroup;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.allegiances.AllegianceDescription;
import delta.games.lotro.lore.allegiances.AllegiancesManager;
import delta.games.lotro.lore.allegiances.Points2LevelCurve;
import delta.games.lotro.lore.allegiances.io.xml.AllegianceXMLWriter;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get allegiances definitions from DAT files.
 * @author DAM
 */
public class MainDatAllegiancesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatAllegiancesLoader.class);

  private DataFacade _facade;
  private LotroEnum<AllegianceGroup> _groups;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatAllegiancesLoader(DataFacade facade)
  {
    _facade=facade;
    _groups=LotroEnumsRegistry.getInstance().get(AllegianceGroup.class);
    _i18n=new I18nUtils("allegiances",facade.getGlobalStringsManager());
  }

  private AllegianceDescription load(int allegianceID)
  {
    PropertiesSet properties=_facade.loadProperties(allegianceID+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    AllegianceDescription ret=new AllegianceDescription();
    ret.setIdentifier(allegianceID);
    // Name
    String name=_i18n.getNameStringProperty(properties,"Allegiance_Name",allegianceID,0);
    ret.setName(name);
    // Advancement Progression ID
    int advancementProgressionID=((Integer)properties.getProperty("Allegiance_Advancement_Progression")).intValue();
    ret.setAdvancementProgressionID(advancementProgressionID);
    // Group
    int groupID=((Integer)properties.getProperty("Allegiance_Groups")).intValue();
    AllegianceGroup group=_groups.getEntry(groupID);
    ret.setGroup(group);
    // Description
    String description=_i18n.getStringProperty(properties,"Allegiance_Description");
    ret.setDescription(description);
    // Min Level
    Integer minLevel=(Integer)properties.getProperty("Allegiance_MinimumLeveltoStart");
    ret.setMinLevel(minLevel);
    // Icon
    int iconId=((Integer)properties.getProperty("Allegiance_Image")).intValue();
    File allegianceImage=new File(GeneratedFiles.ALLEGIANCES_ICONS,iconId+".png").getAbsoluteFile();
    if (!allegianceImage.exists())
    {
      DatIconsUtils.buildImageFile(_facade,iconId,allegianceImage);
    }
    ret.setIconId(iconId);
    // Travel skill
    Integer travelSkillID=(Integer)properties.getProperty("Allegiance_TravelSkill");
    if (travelSkillID!=null)
    {
      SkillDescription travelSkill=SkillsManager.getInstance().getSkill(travelSkillID.intValue());
      ret.setTravelSkill(travelSkill);
    }
    // Deeds
    Object[] deedIDs=(Object[])properties.getProperty("Allegiance_DeedList");
    for(Object deedIDObj : deedIDs)
    {
      int deedID=((Integer)deedIDObj).intValue();
      DeedDescription deed=DeedsManager.getInstance().getDeed(deedID);
      if (deed!=null)
      {
        ret.addDeed(deed);
      }
    }
    return ret;
  }

  private Points2LevelCurve loadCurve(int curveID)
  {
    WStateDataSet data=_facade.loadWState(curveID);
    ClassInstance advancementTable=(ClassInstance)data.getValue(data.getOrphanReferences().get(0).intValue());
    long[] longValues=(long[])advancementTable.getAttributeValue("267720940");
    int [] values=new int[longValues.length];
    for(int i=0;i<values.length;i++)
    {
      values[i]=(int)longValues[i];
    }
    Points2LevelCurve ret=new Points2LevelCurve(curveID,values);
    return ret;
  }

  /**
   * Load allegiances.
   */
  public void doIt()
  {
    // Load
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"AllegianceControl");

    AllegiancesManager mgr=new AllegiancesManager();
    Object[] allegianceIds=(Object[])props.getProperty("Allegiance_Type_Array");
    for(Object allegianceIdObj : allegianceIds)
    {
      int allegianceId=((Integer)allegianceIdObj).intValue();
      AllegianceDescription allegiance=load(allegianceId);
      if (allegiance!=null)
      {
        mgr.addAllegiance(allegiance);
      }
      else
      {
        LOGGER.warn("Could not handle allegiance ID="+allegianceId);
      }
    }
    // Load curves
    // - legacy curves
    {
      int[] curveIDs={1879353332,1879353333,1879353334,1879353335,1879478784};
      for(int curveID : curveIDs)
      {
        Points2LevelCurve curve=loadCurve(curveID);
        mgr.getCurvesManager().addCurve(curve);
      }
    }
    // - current curves
    {
      Object[] curveIDs=(Object[])props.getProperty("Allegiance_Advancement_Progressions_Array");
      for(Object curveIDObj : curveIDs)
      {
        int curveID=((Integer)curveIDObj).intValue();
        Points2LevelCurve curve=loadCurve(curveID);
        mgr.getCurvesManager().addCurve(curve);
      }
    }
    // Save
    boolean ok=AllegianceXMLWriter.writeAllegiancesFile(GeneratedFiles.ALLEGIANCES,mgr);
    if (ok)
    {
      LOGGER.info("Wrote allegiances file: "+GeneratedFiles.ALLEGIANCES);
    }
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatAllegiancesLoader(facade).doIt();
    facade.dispose();
  }
}
