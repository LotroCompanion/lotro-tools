package delta.games.lotro.tools.extraction.items.legendary;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.legendary.LegendaryConstants;
import delta.games.lotro.lore.items.legendary.global.LegendaryData;
import delta.games.lotro.lore.items.legendary.global.QualityBasedData;
import delta.games.lotro.lore.items.legendary.global.io.xml.LegendaryDataXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.DatEnumsUtils;

/**
 * Loader for data related to the legendary items system.
 * @author DAM
 */
public class MainDatLegendarySystemLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatLegendarySystemLoader.class);

  private DataFacade _facade;
  private EnumMapper _slotMapper;
  private LegendaryData _data;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegendarySystemLoader(DataFacade facade)
  {
    _facade=facade;
    _slotMapper=facade.getEnumsManager().getEnumMapper(587202798);
  }

  /**
   * Load legendary system data.
   */
  public void doIt()
  {
    _data=new LegendaryData();
    loadLegendaryData();
    save();
  }

  /**
   * Get the loaded data.
   * @return the loaded data.
   */
  public LegendaryData getData()
  {
    return _data;
  }

  /**
   * Load legendary data.
   */
  private void loadLegendaryData()
  {
    // Is it defined somewhere in the DAT files?
    // TODO => yes, on each ItemInfo entry
    _data.setMaxUiRank(9);
    // Load properties for ItemAdvancementControl
    PropertiesSet itemAdvancementControlProps=_facade.loadProperties(0x7000EAA6+DATConstants.DBPROPERTIES_OFFSET);

    // Max rank for main legacy
    int maxMainLegacyRank=((Integer)itemAdvancementControlProps.getProperty("ItemAdvancement_CombatDPS_MaxRank")).intValue();
    _data.setMaxMainLegacyRank(maxMainLegacyRank); // 7

    // ItemInfoTable
    Object[] itemInfoTable=(Object[])itemAdvancementControlProps.getProperty("ItemAdvancement_ItemInfoTable_Array");
    for(Object itemInfoObj : itemInfoTable)
    {
      PropertiesSet itemInfoProps=(PropertiesSet)itemInfoObj;
      /*
        ItemAdvancement_ItemLevelInfo: 1879138014
        ItemAdvancement_LegendaryPointInfo: 1879148999
        ItemAdvancement_LevelTable: 1879163384
        ItemAdvancement_Quality: 2
        ItemAdvancement_SpecialWidgetUnlocksAtImbuement: 0
       */
      int qualityCode=((Integer)itemInfoProps.getProperty("ItemAdvancement_Quality")).intValue();
      ItemQuality quality=DatEnumsUtils.getQuality(qualityCode);
      LOGGER.debug("Quality: {}",quality);
      QualityBasedData qualityData=_data.getQualityData(quality,true);
      // - item level info
      int itemLevelInfoId=((Integer)itemInfoProps.getProperty("ItemAdvancement_ItemLevelInfo")).intValue();
      handleItemLevelInfo(itemLevelInfoId,qualityData);
      // - legendary point info
      int legendaryPointInfoId=((Integer)itemInfoProps.getProperty("ItemAdvancement_LegendaryPointInfo")).intValue();
      handleLegendaryPointInfo(legendaryPointInfoId,qualityData);
      // - level table
      int levelTableId=((Integer)itemInfoProps.getProperty("ItemAdvancement_LevelTable")).intValue();
      int[] xpTable=handleLevelTable(levelTableId);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\tFound XP table: {}",Arrays.toString(xpTable));
      }
      qualityData.setXpTable(xpTable);
    }
  }

  private void handleItemLevelInfo(int itemLevelInfoId, QualityBasedData qualityData)
  {
    PropertiesSet itemLevelInfoProps=_facade.loadProperties(itemLevelInfoId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] levelInfoTable=(Object[])itemLevelInfoProps.getProperty("ItemAdvancement_ItemLevelInfo_Array");
    for(Object levelInfoObj : levelInfoTable)
    {
      PropertiesSet levelInfoProps=(PropertiesSet)levelInfoObj;
      /*
        ItemAdvancement_ItemLevelInfo_DeconstructionCost: 1760
        ItemAdvancement_ItemLevelInfo_DeconstructionRelicCurrencyCost: 70
        ItemAdvancement_ItemLevelInfo_IdentificationCost: 17600
        ItemAdvancement_ItemLevelInfo_IdentificationRelicCurrencyCost: 301
        ItemAdvancement_ItemLevelInfo_ItemLevel: 165
        ItemAdvancement_ItemLevelInfo_LegacyRankCap: 9
        ItemAdvancement_ItemLevelInfo_MaxLevel: 60
        ItemAdvancement_ItemLevelInfo_ReforgeCost: 17600
        ItemAdvancement_ItemLevelInfo_ReforgeRelicCurrencyCost: 301
        ItemAdvancement_ItemLevelInfo_RunicLootTable: 1879249065
        ItemAdvancement_ItemLevelInfo_StartingProgressionLevel: 150
       */
      Integer itemLevel=(Integer)levelInfoProps.getProperty("ItemAdvancement_ItemLevelInfo_ItemLevel");
      Integer startProgressionLevel=(Integer)levelInfoProps.getProperty("ItemAdvancement_ItemLevelInfo_StartingProgressionLevel");
      qualityData.addStartProgressionLevel(itemLevel.intValue(),startProgressionLevel.intValue());
    }
  }

  private void handleLegendaryPointInfo(int legendaryPointInfoId, QualityBasedData qualityData)
  {
    PropertiesSet legendaryPointInfoProps=_facade.loadProperties(legendaryPointInfoId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] legendaryPointSlotInfoTable=(Object[])legendaryPointInfoProps.getProperty("ItemAdvancement_LegendaryPointSlotInfo_Array");
    for(Object legendaryPointSlotInfoObj : legendaryPointSlotInfoTable)
    {
      /*
        ItemAdvancement_LegendaryPointsTable: 1879149190
        ItemAdvancement_Slot: 65536 (Main Hand)
       */
      PropertiesSet legendaryPointSlotInfoProps=(PropertiesSet)legendaryPointSlotInfoObj;
      // Slot
      int slotBits=((Integer)legendaryPointSlotInfoProps.getProperty("ItemAdvancement_Slot")).intValue();
      BitSet slotsSet=BitSetUtils.getBitSetFromFlags(slotBits);
      String slots=BitSetUtils.getStringFromBitSet(slotsSet,_slotMapper,",");
      LOGGER.debug("\t\tSlots: {}",slots);
      // Legendary points table
      int legendaryPointsTableId=((Integer)legendaryPointSlotInfoProps.getProperty("ItemAdvancement_LegendaryPointsTable")).intValue();
      int[] pointsByLevel=getPointsByLevel(legendaryPointsTableId);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\t\tPoints by level: {}",Arrays.toString(pointsByLevel));
      }
      EquipmentLocation slot=DatEnumsUtils.getSlot(slotBits);
      qualityData.setPointsTable(slot,pointsByLevel);
    }
  }

  private int[] getPointsByLevel(int legendaryPointsTableId)
  {
    // See: https://lotro-wiki.com/index.php/Legendary_Items for confirmation
    // The wstate below contains the 'increase of legacy points' for each level, for 3rd age items Nothing in the properties.
    WStateDataSet data=_facade.loadWState(legendaryPointsTableId);
    int[] ret=new int[LegendaryConstants.MAX_LEVEL+1];
    @SuppressWarnings("unchecked")
    Map<Integer,Long> pointsMap=(Map<Integer,Long>)data.getValue(1);
    for(Map.Entry<Integer,Long> entry : pointsMap.entrySet())
    {
      int level=entry.getKey().intValue();
      int points=entry.getValue().intValue();
      ret[level]=points;
    }
    // Compute totals
    int total=0;
    for(int i=0;i<=LegendaryConstants.MAX_LEVEL;i++)
    {
      total+=ret[i];
      ret[i]=total;
    }
    return ret;
  }

  private int[] handleLevelTable(int levelTableId)
  {
    // Load XP values for each legendary level
    // See: https://lotro-wiki.com/index.php/Legendary_Items for confirmation
    WStateDataSet data=_facade.loadWState(levelTableId);
    int[] ret=new int[LegendaryConstants.MAX_LEVEL+1];
    long[] data1=(long[])data.getValue(1);
    for(int i=0;i<=LegendaryConstants.MAX_LEVEL;i++)
    {
      ret[i]=(int)data1[i];
    }
    return ret;
  }

  private void save()
  {
    LegendaryDataXMLWriter.write(GeneratedFiles.LEGENDARY_DATA,_data);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MainDatLegendarySystemLoader loader=new MainDatLegendarySystemLoader(facade);
    loader.doIt();
    facade.dispose();
  }
}
