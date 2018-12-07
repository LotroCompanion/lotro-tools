package delta.games.lotro.tools.lore.relics.dat;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.lore.items.legendary.relics.RelicsCategory;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatStatUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;

/**
 * Get relic definitions from DAT files.
 * @author DAM
 */
public class MainDatRelicsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRelicsLoader.class);

  private DataFacade _facade;
  private RelicsManager _relicsMgr;
  private EnumMapper _categories;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatRelicsLoader(DataFacade facade)
  {
    _facade=facade;
    _relicsMgr=new RelicsManager();
  }

  private boolean _debug=false;
  private int _currentId;

  private Relic loadRelic(int indexDataId)
  {
    //System.out.println(indexDataId);
    Relic relic=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+0x9000000);
    if (properties!=null)
    {
      _currentId=indexDataId;
      _debug=((_currentId==1879114450) || (_currentId==1879200000));
      if (_debug)
      {
        FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
        System.out.println(properties.dump());
      }
      // Name
      String name=DatUtils.getStringProperty(properties,"Runic_Name");
      name=DatUtils.fixName(name);
      // Type
      Integer relicType=(Integer)properties.getProperty("Runic_Type");
      RelicType type=getRelicType(name,relicType.intValue());
      relic=new Relic(indexDataId,name,type,null);
      // Category
      Integer categoryEnum=(Integer)properties.getProperty("Runic_Tier");
      String categoryName=_categories.getString(categoryEnum.intValue());
      RelicsCategory category=_relicsMgr.getRelicCategory(categoryName,true);
      category.addRelic(relic);
      // Level
      Integer level=(Integer)properties.getProperty("Runic_Level");
      // Stats
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(_facade,properties);
      BasicStatsSet stats=statsProvider.getStats(1,level.intValue(),true);
      relic.getStats().addStats(stats);
      // Required level
      Integer requiredLevel=(Integer)properties.getProperty("Runic_RequiredItemLevel");
      relic.setRequiredLevel(requiredLevel);
      // Icons
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      Integer imageIconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      String iconFilename=imageIconId+"-"+backgroundIconId+".png";
      File to=new File("relicIcons/"+iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(_facade,imageIconId.intValue(),backgroundIconId.intValue(),to);
        if (!ok)
        {
          LOGGER.warn("Could not build relic icon: "+iconFilename);
        }
      }
      relic.setIconFilename(iconFilename);
    }
    else
    {
      LOGGER.warn("Could not handle relic ID="+indexDataId);
    }
    return relic;
  }

  private RelicType getRelicType(String name, int relicTypeEnum)
  {
    if (relicTypeEnum==1) return RelicType.RUNE;
    if (relicTypeEnum==2) return RelicType.SETTING;
    if (relicTypeEnum==4) return RelicType.GEM;
    if (relicTypeEnum==8) return RelicType.CRAFTED_RELIC;
    if (relicTypeEnum==7) return RelicType.CLASS_RELIC;
    if (relicTypeEnum==15) return RelicType.INSIGNIA;
    LOGGER.warn("Relic type not supported: name="+name+", type="+relicTypeEnum);
    return null;
  }

  private void doIt()
  {
    _categories=_facade.getEnumsManager().getEnumMapper(587203232);
    for(int i=1879114433;i<=1879368329;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==2219)
        {
          // Relics
          loadRelic(did);
        }
      }
    }
    // Write result file
    File toFile=new File("relics_dat.xml").getAbsoluteFile();
    _relicsMgr.writeRelicsFile(toFile);
    System.out.println("Wrote file: "+toFile);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatRelicsLoader(facade).doIt();
    facade.dispose();
  }
}
