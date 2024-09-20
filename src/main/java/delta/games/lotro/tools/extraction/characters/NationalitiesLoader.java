package delta.games.lotro.tools.extraction.characters;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.races.NationalityDescription;
import delta.games.lotro.character.races.io.xml.NationalityDescriptionXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get nationalities definitions from DAT files.
 * @author DAM
 */
public class NationalitiesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(NationalitiesLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public NationalitiesLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("nationalities",facade.getGlobalStringsManager());
  }

  /**
   * Load nationalities.
   */
  public void doIt()
  {
    List<NationalityDescription> nationalities=new ArrayList<NationalityDescription>();
    PropertiesSet properties=_facade.loadProperties(0x79000210);
    Object[] nationalityIdsArray=(Object[])properties.getProperty("NationalityTable_NationalityTableList");
    for(Object nationalityIdObj : nationalityIdsArray)
    {
      int nationalityId=((Integer)nationalityIdObj).intValue();
      NationalityDescription nationality=handleNationality(nationalityId);
      nationalities.add(nationality);
      int nationalityCode=nationality.getIdentifier();
      // Aliases
      if (nationalityCode==16) nationality.addAlias("Dale");
      if (nationalityCode==12) nationality.addAlias("Lonely Mountain");
      if (nationalityCode==18) nationality.addAlias("Fallohides");
      if (nationalityCode==13) nationality.addAlias("Stoors");
      if (nationalityCode==27) nationality.addAlias("Mordor Mountains");
    }
    // Save
    NationalityDescriptionXMLWriter.write(GeneratedFiles.NATIONALITIES,nationalities);
    _i18n.save();
  }

  private NationalityDescription handleNationality(int nationalityId)
  {
    PropertiesSet properties=_facade.loadProperties(nationalityId+DATConstants.DBPROPERTIES_OFFSET);
    // Code
    int code=((Integer)properties.getProperty("NationalityTable_Nationality")).intValue();
    NationalityDescription ret=new NationalityDescription(code);
    // Name
    String name=_i18n.getNameStringProperty(properties,"NationalityTable_Name",code,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    ret.setName(name);
    // Description
    String description=_i18n.getStringProperty(properties,"NationalityTable_Desc");
    ret.setDescription(description);
    // Icon ID
    int iconID=((Integer)properties.getProperty("NationalityTable_Icon")).intValue();
    ret.setIconID(iconID);
    // Guidelines
    Object[] guidelinesArray=(Object[])properties.getProperty("NationalityTable_Naming_Guideline_Array");
    for(Object guidelineEntryObj : guidelinesArray)
    {
      PropertiesSet guidelineProps=(PropertiesSet)guidelineEntryObj;
      String guideline=_i18n.getStringProperty(guidelineProps,"NationalityTable_Naming_Guideline");
      int sexCode=((Integer)guidelineProps.getProperty("NationalityTable_Sex")).intValue();
      if (sexCode==4096)
      {
        ret.setNamingGuidelineMale(guideline);
      }
      else if (sexCode==8192)
      {
        ret.setNamingGuidelineFemale(guideline);
      }
      else
      {
        LOGGER.warn("Unmanaged gender code: "+sexCode);
      }
    }
    // Title
    Integer titleID=(Integer)properties.getProperty("NationalityTable_Title");
    if (titleID!=null)
    {
      ret.setTitleID(titleID);
    }
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new NationalitiesLoader(facade).doIt();
    facade.dispose();
  }
}
