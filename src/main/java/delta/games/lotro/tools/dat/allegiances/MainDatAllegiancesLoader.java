package delta.games.lotro.tools.dat.allegiances;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.allegiances.AllegianceDescription;
import delta.games.lotro.lore.allegiances.AllegiancesManager;
import delta.games.lotro.lore.allegiances.Points2LevelCurve;
import delta.games.lotro.lore.allegiances.io.xml.AllegianceXMLWriter;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get allegiances definitions from DAT files.
 * @author DAM
 */
public class MainDatAllegiancesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatAllegiancesLoader.class);

  /**
   * Directory for allegiances icons.
   */
  public static final File ALLEGIANCE_ICONS_DIR=new File("data\\allegiances\\tmp").getAbsoluteFile();

  private DataFacade _facade;
  private EnumMapper _groups;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatAllegiancesLoader(DataFacade facade)
  {
    _facade=facade;
    _groups=_facade.getEnumsManager().getEnumMapper(587203638);
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
    //System.out.println("************* "+allegianceID+" *****************");
    //System.out.println(properties.dump());
    // Name
    String name=DatUtils.getStringProperty(properties,"Allegiance_Name");
    ret.setName(name);
    // Group
    int groupID=((Integer)properties.getProperty("Allegiance_Groups")).intValue();
    String group=_groups.getString(groupID);
    ret.setGroup(group);
    // Description
    String description=DatUtils.getStringProperty(properties,"Allegiance_Description");
    ret.setDescription(description);
    // Min Level
    Integer minLevel=(Integer)properties.getProperty("Allegiance_MinimumLeveltoStart");
    ret.setMinLevel(minLevel);
    // Icon
    int iconId=((Integer)properties.getProperty("Allegiance_Image")).intValue();
    File allegianceImage=new File(ALLEGIANCE_ICONS_DIR,"allegianceIcons/"+iconId+".png").getAbsoluteFile();
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
    // Load AllegianceControl
    PropertiesSet props=_facade.loadProperties(0x7904A21F);
    //System.out.println(props.dump());

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
    int[] curveIDs={1879353332,1879353333,1879353334,1879353335};
    //Object[] curveIDs=(Object[])props.getProperty("Allegiance_Advancement_Progressions_Array");
    //for(Object curveIDObj : curveIDs)
    for(int curveID : curveIDs)
    {
      //int curveID=((Integer)curveIDObj).intValue();
      Points2LevelCurve curve=loadCurve(curveID);
      mgr.getCurvesManager().addCurve(curve);
    }
    boolean ok=AllegianceXMLWriter.writeAllegiancesFile(GeneratedFiles.ALLEGIANCES,mgr);
    if (ok)
    {
      System.out.println("Wrote allegiances file: "+GeneratedFiles.ALLEGIANCES);
    }
    // Write allegiances icons
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.ALLEGIANCES_ICONS,ALLEGIANCE_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote allegiance icons archive: "+GeneratedFiles.ALLEGIANCES_ICONS);
    }
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
