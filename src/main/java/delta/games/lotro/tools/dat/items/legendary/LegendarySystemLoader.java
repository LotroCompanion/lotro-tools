package delta.games.lotro.tools.dat.items.legendary;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.legendary.LegendaryConstants;
import delta.games.lotro.lore.items.legendary.global.LegendaryData;
import delta.games.lotro.lore.items.legendary.global.QualityBasedData;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;

/**
 * Loader for data related to the legendary items system.
 * @author DAM
 */
public class LegendarySystemLoader
{
  private static final Logger LOGGER=Logger.getLogger(LegendarySystemLoader.class);

  private DataFacade _facade;
  private EnumMapper _slotMapper;
  private LegendaryData _data;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LegendarySystemLoader(DataFacade facade)
  {
    _facade=facade;
    _slotMapper=facade.getEnumsManager().getEnumMapper(587202798);
    _data=new LegendaryData();
    loadLegendaryData();
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
    // Load properties for ItemAdvancementControl (0x7900EAA6)
    PropertiesSet itemAdvancementControlProps=_facade.loadProperties(1879108262+DATConstants.DBPROPERTIES_OFFSET);
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
      LOGGER.info("Quality: "+quality);
      QualityBasedData qualityData=_data.getQualityData(quality);
      // - item level info
      int itemLevelInfoId=((Integer)itemInfoProps.getProperty("ItemAdvancement_ItemLevelInfo")).intValue();
      handleItemLevelInfo(itemLevelInfoId,qualityData);
      // - legendary point info
      int legendaryPointInfoId=((Integer)itemInfoProps.getProperty("ItemAdvancement_LegendaryPointInfo")).intValue();
      handleLegendaryPointInfo(legendaryPointInfoId,qualityData);
      // - level table
      int levelTableId=((Integer)itemInfoProps.getProperty("ItemAdvancement_LevelTable")).intValue();
      int[] xpTable=handleLevelTable(levelTableId);
      LOGGER.info("\tFound XP table: "+Arrays.toString(xpTable));
      qualityData.setXpTable(xpTable);
    }
  }

  private void handleItemLevelInfo(int itemLevelInfoId, QualityBasedData qualityData)
  {
    PropertiesSet itemLevelInfoProps=_facade.loadProperties(itemLevelInfoId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(itemLevelInfoProps.dump());
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
    //System.out.println(legendaryPointInfoProps.dump());
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
      LOGGER.info("\t\tSlots: "+slots);
      // Legendary points table
      int legendaryPointsTableId=((Integer)legendaryPointSlotInfoProps.getProperty("ItemAdvancement_LegendaryPointsTable")).intValue();
      int[] pointsByLevel=getPointsByLevel(legendaryPointsTableId);
      LOGGER.info("\t\tPoints by level: "+Arrays.toString(pointsByLevel));
      EquipmentLocation slot=DatEnumsUtils.getSlot(slotBits);
      qualityData.setPointsTable(slot,pointsByLevel);
    }
  }

  private int[] getPointsByLevel(int legendaryPointsTableId)
  {
    // See: https://lotro-wiki.com/index.php/Legendary_Items for confirmation
    // The wstate below contains the 'increase of legacy points' for each level, for 3rd age items
    //PropertiesSet legendaryPointsTableProps=showProperties(_facade,legendaryPointsTableId+OFFSET); // Nothing!
    List<Object> data=_facade.loadWState(legendaryPointsTableId);
    //WStateLoader.showDecodedData(data);
    //Number of read classes: 2
    //#0: Buffer of size: 4
    //#1: {1=0, 2=10, 3=10, 4=10, 5=10, 6=10, 7=10, 8=10, 9=10, 10=10, 11=12, 12=12, 13=12, 14=12, 15=12, 17=12, 16=12, 19=12, 18=12, 21=14, 20=12, 23=14, 22=14, 25=14, 24=14, 27=14, 26=14, 29=14, 28=14, 31=8, 30=14, 34=8, 35=8, 32=8, 33=8, 38=8, 39=8, 36=8, 37=8, 42=8, 43=8, 40=8, 41=8, 46=8, 47=8, 44=8, 45=8, 51=8, 50=8, 49=8, 48=8, 55=8, 54=8, 53=8, 52=8, 59=8, 58=8, 57=8, 56=8, 63=8, 62=8, 61=8, 60=8, 68=8, 69=8, 70=8, 64=8, 65=8, 66=8, 67=8}
    int[] ret=new int[LegendaryConstants.MAX_LEVEL+1];
    @SuppressWarnings("unchecked")
    Map<Integer,Long> pointsMap=(Map<Integer,Long>)data.get(1);
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
    // See: https://lotro-wiki.com/index.php/Legendary_Items for confirmation
    //showProperties(_facade,levelTableId+OFFSET); // Nothing!
    // The wstate below contains the Item XP value for levels 1-70 of 3rd age items
    List<Object> data=_facade.loadWState(levelTableId);
    //WStateLoader.showDecodedData(xpData);
    //Number of read classes: 2
    //#0: Buffer of size: 4
    //#1: (71): [0, 0, 100, 350, 850, 1700, 3000, 4850, 7718, 11130, 15435, 20738, 27143, 34755, 43680, 54023, 65888, 79380, 94605, 111668, 130673, 151725, 174930, 200393, 228218, 258510, 291375, 330031, 372200, 418068, 467828, 507828, 547828, 587828, 627828, 667828, 707828, 747828, 787828, 827828, 867828, 907828, 947828, 987828, 1027828, 1067828, 1107828, 1147828, 1187828, 1227828, 1267828, 1307828, 1347828, 1387828, 1427828, 1467828, 1507828, 1547828, 1587828, 1627828, 1667828, 1707828, 1747828, 1787828, 1827828, 1867828, 1907828, 1947828, 1987828, 2027828, 2067828]
    int[] ret=new int[LegendaryConstants.MAX_LEVEL+1];
    long[] data1=(long[])data.get(1);
    for(int i=0;i<=LegendaryConstants.MAX_LEVEL;i++)
    {
      ret[i]=(int)data1[i];
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
    LegendarySystemLoader loader=new LegendarySystemLoader(facade);
    LegendaryData data=loader.getData();
    ItemQuality[] qualities={ItemQuality.RARE, ItemQuality.INCOMPARABLE, ItemQuality.LEGENDARY};
    for(ItemQuality quality : qualities)
    {
      System.out.println("Quality: "+quality);
      QualityBasedData legendaryData=data.getQualityData(quality);
      int[] xpTable=legendaryData.getXpTable();
      System.out.println(Arrays.toString(xpTable));
    }
    facade.dispose();
  }
}
