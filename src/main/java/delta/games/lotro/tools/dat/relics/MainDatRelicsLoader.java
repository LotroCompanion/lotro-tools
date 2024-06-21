package delta.games.lotro.tools.dat.relics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.RunicTier;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.EquipmentLocations;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.lore.items.legendary.relics.RelicTypes;
import delta.games.lotro.lore.items.legendary.relics.RelicsCategory;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get relic definitions from DAT files.
 * @author DAM
 */
public class MainDatRelicsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRelicsLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;
  private RelicsManager _relicsMgr;
  private LotroEnum<RunicTier> _tiers;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatRelicsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("relics",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
    _relicsMgr=new RelicsManager();
    _tiers=LotroEnumsRegistry.getInstance().get(RunicTier.class);
  }

  private void loadRelic(int relicId)
  {
    Relic relic=null;
    PropertiesSet properties=_facade.loadProperties(relicId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // Name
      String name=_i18n.getNameStringProperty(properties,"Runic_Name",relicId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      relic=new Relic(relicId,name);
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
      int tierCode=((Integer)properties.getProperty("Runic_Tier")).intValue();
      RunicTier tier=_tiers.getEntry(tierCode);
      relic.setTier(tier);
      // Level
      Integer level=(Integer)properties.getProperty("Runic_Level");
      // Stats
      StatsProvider statsProvider=_statUtils.buildStatProviders(properties);
      BasicStatsSet stats=statsProvider.getStats(1,level.intValue());
      relic.getStats().addStats(stats);
      // Runic stats
      StatsProvider runicStatsProvider=_statUtils.buildStatProviders("Runic_",properties);
      if (runicStatsProvider.getEntriesCount()>0)
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
          AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classId);
          relic.getUsageRequirement().addAllowedClass(abstractClass);
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
      // Register relic
      RelicsCategory category=_relicsMgr.getRelicCategory(tier,true);
      category.addRelic(relic);
    }
    else
    {
      LOGGER.warn("Could not handle relic ID="+relicId);
    }
  }

  private List<RelicType> getRelicTypes(int relicTypeEnum)
  {
    List<RelicType> types=new ArrayList<RelicType>();
    if ((relicTypeEnum&1)!=0) types.add(RelicTypes.RUNE);
    if ((relicTypeEnum&2)!=0) types.add(RelicTypes.SETTING);
    if ((relicTypeEnum&4)!=0) types.add(RelicTypes.GEM);
    if ((relicTypeEnum&8)!=0) types.add(RelicTypes.CRAFTED_RELIC);
    return types;
  }

  private List<EquipmentLocation> getSlots(int slotsCode)
  {
    List<EquipmentLocation> slots=new ArrayList<EquipmentLocation>();
    if ((slotsCode&1L<<16)!=0) slots.add(EquipmentLocations.MAIN_HAND);
    if ((slotsCode&1L<<18)!=0) slots.add(EquipmentLocations.RANGED_ITEM);
    if ((slotsCode&1L<<20)!=0) slots.add(EquipmentLocations.CLASS_SLOT);
    if ((slotsCode&1L<<21)!=0) slots.add(EquipmentLocations.BRIDLE);
    return slots;
  }

  /**
   * Load relics.
   */
  public void doIt()
  {
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
    // Save
    // - data file
    boolean ok=_relicsMgr.writeRelicsFile(GeneratedFiles.RELICS);
    if (ok)
    {
      LOGGER.info("Wrote relics file: "+GeneratedFiles.RELICS);
    }
    // - labels
    _i18n.save();
    // Stats usage statistics
    System.out.println("Stats usage statistics (relics):");
    _statUtils.showStatistics();
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
