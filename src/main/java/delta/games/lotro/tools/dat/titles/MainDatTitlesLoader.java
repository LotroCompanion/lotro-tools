package delta.games.lotro.tools.dat.titles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
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
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.StringRenderingUtils;

/**
 * Get titles definitions from DAT files.
 * @author DAM
 */
public class MainDatTitlesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatTitlesLoader.class);

  /**
   * Directory for title icons.
   */
  public static final File TITLE_ICONS_DIR=new File("data\\titles\\tmp").getAbsoluteFile();

  private DataFacade _facade;
  private EnumMapper _category;
  private StringRenderer _customRenderer;
  private EnumMapper _exclusionGroup;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatTitlesLoader(DataFacade facade)
  {
    _facade=facade;
    _category=_facade.getEnumsManager().getEnumMapper(587202682);
    _customRenderer=StringRenderingUtils.buildAllOptionsRenderer();
    _exclusionGroup=_facade.getEnumsManager().getEnumMapper(587202883);
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

  private TitleDescription load(int indexDataId)
  {
    TitleDescription title=null;
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      title=new TitleDescription();
      title.setIdentifier(indexDataId);
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
      // Name
      String titleFormat=DatUtils.getStringProperty(properties,"Title_String");
      String renderedTitle=renderTitle(titleFormat);
      title.setName(renderedTitle);
      // Category
      int categoryId=((Integer)properties.getProperty("Title_Category")).intValue();
      String category=_category.getString(categoryId);
      title.setCategory(category);
      // Description
      String description=DatUtils.getStringProperty(properties,"Title_Description");
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
          LOGGER.warn("Unexpected priority value: "+priority+" for title ID="+indexDataId);
        }
      }
      // Icon
      int iconId=((Integer)properties.getProperty("Title_Icon")).intValue();
      File titleIcon=new File(TITLE_ICONS_DIR,"titleIcons/"+iconId+".png").getAbsoluteFile();
      if (!titleIcon.exists())
      {
        DatIconsUtils.buildImageFile(_facade,iconId,titleIcon);
      }
      title.setIconId(iconId);
    }
    else
    {
      LOGGER.warn("Could not handle title ID="+indexDataId);
    }
    return title;
  }

  private String renderTitle(String titleFormat)
  {
    String renderedTitle=_customRenderer.render(titleFormat);
    renderedTitle=renderedTitle.replace(" ,","");
    renderedTitle=renderedTitle.replace("  "," ");
    renderedTitle=renderedTitle.trim();
    return renderedTitle;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
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
    int nbTitles=titles.size();
    LOGGER.info("Writing "+nbTitles+" titles");
    boolean ok=TitleXMLWriter.writeTitlesFile(GeneratedFiles.TITLES,titles);
    if (ok)
    {
      System.out.println("Wrote titles file: "+GeneratedFiles.TITLES);
    }
    // Write title icons
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.TITLE_ICONS,TITLE_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote title icons archive: "+GeneratedFiles.TITLE_ICONS);
    }
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
