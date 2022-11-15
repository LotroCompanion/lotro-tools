package delta.games.lotro.tools.dat.relics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.lore.items.legendary.relics.RelicsCategory;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

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

  private void loadRelic(int indexDataId)
  {
    //System.out.println(indexDataId);
    Relic relic=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      _currentId=indexDataId;
      _debug=(_currentId==1879000000);
      if (_debug)
      {
        FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
      }
      // Name
      String name=DatUtils.getStringProperty(properties,"Runic_Name");
      name=StringUtils.fixName(name);
      relic=new Relic(indexDataId,name);
      // Type
      int relicType=((Integer)properties.getProperty("Runic_Type")).intValue();
      List<RelicType> types=getRelicTypes(relicType);
      for(RelicType type : types)
      {
        relic.addType(type);
      }
      // Slots
      int slotsCode=((Integer)properties.getProperty("Relic_ValidContainerSlots")).intValue();
      List<EquipmentLocation> slots=getSlots(slotsCode);
      for(EquipmentLocation slot : slots)
      {
        relic.addAllowedSlot(slot);
      }
      // Category
      int categoryCode=((Integer)properties.getProperty("Runic_Tier")).intValue();
      RelicsCategory category=_relicsMgr.getRelicCategory(categoryCode,true);
      String categoryName=_categories.getString(categoryCode);
      category.setName(categoryName);
      relic.setCategory(category);
      // Level
      Integer level=(Integer)properties.getProperty("Runic_Level");
      // Stats
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(_facade,properties);
      BasicStatsSet stats=statsProvider.getStats(1,level.intValue());
      relic.getStats().addStats(stats);
      // Runic stats
      StatsProvider runicStatsProvider=DatStatUtils.buildStatProviders("Runic_",_facade,properties);
      if (runicStatsProvider.getNumberOfStatProviders()>0)
      {
        BasicStatsSet runicStats=runicStatsProvider.getStats(1,level.intValue());
        relic.getStats().addStats(runicStats);
      }
      // Required level
      Integer requiredLevel=(Integer)properties.getProperty("Runic_RequiredItemLevel");
      relic.setRequiredLevel(requiredLevel);
      // Required class
      Object[] classIdObjArray=(Object[])properties.getProperty("Usage_RequiredClassList");
      if (classIdObjArray!=null)
      {
        for(Object classIdObj : classIdObjArray)
        {
          int classId=((Integer)classIdObj).intValue();
          CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classId);
          relic.getUsageRequirement().addAllowedClass(characterClass);
        }
      }
      // Icons
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      Integer imageIconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      // Unused:
      //Integer shadowIconId=(Integer)properties.getProperty("Icon_Layer_ShadowDID");
      //Integer underlayIconId=(Integer)properties.getProperty("Icon_Layer_UnderlayDID");
      String iconFilename=imageIconId+"-"+backgroundIconId+".png";
      File to=new File(GeneratedFiles.RELIC_ICONS_DIR,iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        int[] imagesIDs=new int[]{backgroundIconId.intValue(),imageIconId.intValue()};
        boolean ok=DatIconsUtils.buildImageFile(_facade,imagesIDs,to);
        if (!ok)
        {
          LOGGER.warn("Could not build relic icon: "+iconFilename);
        }
      }
      relic.setIconFilename(iconFilename);
      // Check and add
      boolean useIt=checkRelic(category,relic);
      if (useIt)
      {
        category.addRelic(relic);
      }
    }
    else
    {
      LOGGER.warn("Could not handle relic ID="+indexDataId);
    }
  }

  private boolean checkRelic(RelicsCategory category, Relic relic)
  {
    return true;
  }

  private List<RelicType> getRelicTypes(int relicTypeEnum)
  {
    List<RelicType> types=new ArrayList<RelicType>();
    if ((relicTypeEnum&1)!=0) types.add(RelicType.RUNE);
    if ((relicTypeEnum&2)!=0) types.add(RelicType.SETTING);
    if ((relicTypeEnum&4)!=0) types.add(RelicType.GEM);
    if ((relicTypeEnum&8)!=0) types.add(RelicType.CRAFTED_RELIC);
    return types;
  }

  private List<EquipmentLocation> getSlots(int slotsCode)
  {
    List<EquipmentLocation> slots=new ArrayList<EquipmentLocation>();
    if ((slotsCode&1L<<16)!=0) slots.add(EquipmentLocation.MAIN_HAND);
    if ((slotsCode&1L<<18)!=0) slots.add(EquipmentLocation.RANGED_ITEM);
    if ((slotsCode&1L<<20)!=0) slots.add(EquipmentLocation.CLASS_SLOT);
    if ((slotsCode&1L<<21)!=0) slots.add(EquipmentLocation.BRIDLE);
    return slots;
  }

  /**
   * Load relics.
   */
  public void doIt()
  {
    DatStatUtils._doFilterStats=false;
    DatStatUtils.STATS_USAGE_STATISTICS.reset();
    _categories=_facade.getEnumsManager().getEnumMapper(587203232);
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==WStateClass.RELIC)
        {
          // Relics
          loadRelic(did);
        }
      }
    }
    // Write result file
    boolean ok=_relicsMgr.writeRelicsFile(GeneratedFiles.RELICS);
    if (ok)
    {
      System.out.println("Wrote relics file: "+GeneratedFiles.RELICS);
    }
    // Stats usage statistics
    System.out.println("Stats usage statistics (relics):");
    DatStatUtils.STATS_USAGE_STATISTICS.showResults();
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
