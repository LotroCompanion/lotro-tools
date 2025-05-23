package delta.games.lotro.tools.extraction.titles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.io.xml.TitleXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.StringRenderingUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.extraction.utils.i18n.StringProcessor;

/**
 * Get titles definitions from DAT files.
 * @author DAM
 */
public class MainDatTitlesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatTitlesLoader.class);

  private DataFacade _facade;
  private EnumMapper _category;
  private EnumMapper _exclusionGroup;
  private I18nUtils _i18n;
  private StringProcessor _titleProcessor;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatTitlesLoader(DataFacade facade)
  {
    _facade=facade;
    _category=_facade.getEnumsManager().getEnumMapper(587202682);
    _exclusionGroup=_facade.getEnumsManager().getEnumMapper(587202883);
    _i18n=new I18nUtils("titles",facade.getGlobalStringsManager());
    _titleProcessor=buildTitlesProcessor();
  }

  /*
Sample title properties:
************* 1879051517 *****************
Title_Category: 17
Title_Description:
  #1: You have aided the Bounders of the Shire greatly and been granted the title of Honorary Shirriff.
Title_Exclusion_Group: 0
Title_Icon: 1090641175
Title_Priority: 1
Title_String:
  #1: #1:
  #2: #1:{ [E]}#2:
  #3: #3:{ [E]}#3:
  #4: , Honorary Shirriff
   */

  private TitleDescription load(int titleID)
  {
    TitleDescription title=null;
    long dbPropertiesId=titleID+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      title=new TitleDescription();
      title.setIdentifier(titleID);
      // Name
      String name=_i18n.getNameStringProperty(properties,"Title_String",titleID,_titleProcessor);
      title.setName(name);
      String rawName=_i18n.getStringProperty(properties,"Title_String");
      title.setRawName(rawName);
      // Category
      int categoryId=((Integer)properties.getProperty("Title_Category")).intValue();
      String category=_category.getString(categoryId);
      title.setCategory(category);
      // Description
      String description=_i18n.getStringProperty(properties,"Title_Description");
      title.setDescription(description);
      // Exclusion group
      Integer exclusionGroupId=(Integer)properties.getProperty("Title_Exclusion_Group");
      if ((exclusionGroupId!=null) && (exclusionGroupId.intValue()!=0))
      {
        String exclusionGroup=_exclusionGroup.getLabel(exclusionGroupId.intValue());
        title.setExclusionGroup(exclusionGroup);
        // Priority
        int priority=((Integer)properties.getProperty("Title_Priority")).intValue();
        title.setPriority(Integer.valueOf(priority));
        if (priority<1)
        {
          LOGGER.warn("Unexpected priority value: {} for title ID={}",Integer.valueOf(priority),Integer.valueOf(titleID));
        }
      }
      // Icon
      int iconId=((Integer)properties.getProperty("Title_Icon")).intValue();
      File titleIcon=new File(GeneratedFiles.TITLE_ICONS,iconId+".png").getAbsoluteFile();
      if (!titleIcon.exists())
      {
        DatIconsUtils.buildImageFile(_facade,iconId,titleIcon);
      }
      title.setIconId(iconId);
    }
    else
    {
      LOGGER.warn("Could not handle title ID={}",Integer.valueOf(titleID));
    }
    return title;
  }

  private StringProcessor buildTitlesProcessor()
  {
    StringRenderer customRenderer=StringRenderingUtils.buildAllOptionsRenderer();
    StringProcessor p=new StringProcessor()
    {
      @Override
      public String processString(String input)
      {
        String renderedTitle=customRenderer.render(input);
        renderedTitle=renderedTitle.replace(" ,","");
        renderedTitle=renderedTitle.replace("  "," ");
        renderedTitle=renderedTitle.trim();
        return renderedTitle;
      }
    };
    return p;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==WStateClass.TITLE);
    }
    return false;
  }

  /**
   * Load titles.
   */
  public void doIt()
  {
    List<TitleDescription> titles=new ArrayList<TitleDescription>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        TitleDescription title=load(id);
        if (title!=null)
        {
          titles.add(title);
        }
      }
    }
    // Save titles
    save(titles);
  }

  private void save(List<TitleDescription> titles)
  {
    // Data
    int nbTitles=titles.size();
    LOGGER.info("Writing {} titles",Integer.valueOf(nbTitles));
    boolean ok=TitleXMLWriter.writeTitlesFile(GeneratedFiles.TITLES,titles);
    if (ok)
    {
      LOGGER.info("Wrote titles file: {}",GeneratedFiles.TITLES);
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
    new MainDatTitlesLoader(facade).doIt();
    facade.dispose();
  }
}
