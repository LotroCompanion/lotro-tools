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
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.io.xml.TitleXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

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
  private static File TITLE_ICONS_DIR=new File("data\\titles\\tmp").getAbsoluteFile();

  private DataFacade _facade;
  private EnumMapper _category;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatTitlesLoader(DataFacade facade)
  {
    _facade=facade;
    _category=_facade.getEnumsManager().getEnumMapper(587202682);
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
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      title=new TitleDescription();
      title.setIdentifier(indexDataId);
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
      // Name
      String name=getName(properties);
      title.setName(name);
      // Category
      int categoryId=((Integer)properties.getProperty("Title_Category")).intValue();
      String category=_category.getString(categoryId);
      title.setCategory(category);
      // Description
      String description=DatUtils.getStringProperty(properties,"Title_Description");
      title.setDescription(description);
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

  private String getName(PropertiesSet properties)
  {
    String ret=null;
    Object[] titleStrings=(Object[])properties.getProperty("Title_String");
    if (titleStrings!=null)
    {
      StringBuilder sb=new StringBuilder();
      for(Object titleString : titleStrings)
      {
        String line=(String)titleString;
        if (line.startsWith(", ")) line=line.substring(2);
        sb.append(line);
      }
      ret=cleanupLine(sb.toString());
      ret=removeChoiceArtefacts(ret);
    }
    return ret;
  }

  private String cleanupLine(String line)
  {
    for(int i=0;i<2;i++)
    {
      line=line.trim();
      line=line.replace("#1:","");
      line=line.replace("#2:","");
      line=line.replace("#3:","");
      line=line.replace("#4:","");
      line=line.replace("#-4:","");
      line=line.replace("{ [E]}","");
      line=line.replace("  "," ");
      line=line.trim();
    }
    return line;
  }

  private String removeChoiceArtefacts(String line)
  {
    String ret=line;
    int index=line.indexOf('{');
    if (index!=-1)
    {
      int index2=line.indexOf('}',index+1);
      if (index2!=-1)
      {
        String choice=line.substring(index+1,index2);
        choice=updateChoiceString(choice);
        ret=line.substring(0,index)+choice+line.substring(index2+1);
      }
    }
    return ret;
  }

  private String updateChoiceString(String choice)
  {
    StringBuilder sb=new StringBuilder();
    String[] items=choice.split("\\|");
    for(String item : items)
    {
      item=item.trim();
      int index=item.indexOf('[');
      if (index!=-1)
      {
        int index2=item.indexOf(']',index+1);
        if (index2!=-1)
        {
          item=item.substring(0,index)+item.substring(index2+1);
          item=item.trim();
        }
      }
      if (sb.length()>0)
      {
        sb.append(" / ");
      }
      sb.append(item);
    }
    return sb.toString();
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

  private void doIt()
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
