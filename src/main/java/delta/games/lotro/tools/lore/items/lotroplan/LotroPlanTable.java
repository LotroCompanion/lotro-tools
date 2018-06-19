package delta.games.lotro.tools.lore.items.lotroplan;

import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.stats.ItemStatSliceData;
import delta.games.lotro.lore.items.stats.ItemStatsProvider;
import delta.games.lotro.lore.items.stats.SlicesBasedItemStatsProvider;
import delta.games.lotro.tools.lore.items.StatValueParser;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Lotro plan stats table.
 * @author DAM
 */
public class LotroPlanTable
{
  /**
   * Index of the 'name' column.
   */
  public static final int NAME_INDEX=0;
  /**
   * Index of the 'item level' column.
   */
  public static final int ITEM_LEVEL_INDEX=1;
  /**
   * Index of the 'notes' column.
   */
  private static final int NOTES_INDEX=28;
  /**
   * Index of the 'classes' column.
   */
  private static final int CLASSES_INDEX=67;
  /**
   * Index of the 'classes' column for Mordor.
   */
  private static final int MORDOR_CLASSES_INDEX=69;
  /**
   * Index of the 'category' column for Mordor.
   */
  private static final int MORDOR_CATEGORY_INDEX=68;
  // Name iLvl
  // Armour  Might Agility Vitality  Will  Fate  Morale  Power
  // ICMR  NCMR  ICPR  NCPR  CritHit Finesse PhyMas  TacMas
  // Resist  CritDef InHeal  Block Parry Evade
  // PhyMit  TacMit  Audacity  Hope  Notes
  // ArmourP MoraleP PowerP  MelCritP  RngCritP  TacCritP  HealCritP MelMagnP  RngMagnP  TacMagnP  HealMagnP
  // MelDmgP RngDmgP TacDmgP OutHealP  MelIndP RngIndP TacIndP HealIndP  AttDurP RunSpdP CritDefP  InHealP
  // BlockP  ParryP  EvadeP  PblkP PparP PevaP PblkMitP  PparMitP  PevaMitP
  // MelRedP RngRedP TacRedP PhyMitP TacMitP
  // Class requirement

  private static final STAT[] STATS={ null, null, // 1
    STAT.ARMOUR, STAT.MIGHT, STAT.AGILITY, STAT.VITALITY, STAT.WILL, STAT.FATE, STAT.MORALE, STAT.POWER, // 9
    STAT.ICMR, STAT.OCMR, STAT.ICPR, STAT.OCPR, STAT.CRITICAL_RATING, STAT.FINESSE, STAT.PHYSICAL_MASTERY, STAT.TACTICAL_MASTERY, // 17
    STAT.RESISTANCE, STAT.CRITICAL_DEFENCE, STAT.INCOMING_HEALING, STAT.BLOCK, STAT.PARRY, STAT.EVADE, // 23
    STAT.PHYSICAL_MITIGATION, STAT.TACTICAL_MITIGATION, STAT.AUDACITY, STAT.HOPE, null, // 28
    null, null, null, null, null, null, null, null, null, null, null, // 39
    null, null, null, null, null, null, null, null, null, null, null, null, // 51
    STAT.BLOCK_PERCENTAGE, STAT.PARRY_PERCENTAGE, STAT.EVADE_PERCENTAGE, null, null, null, null, null, null, // 61
    null, STAT.RANGED_DEFENCE_PERCENTAGE, null, STAT.PHYSICAL_MITIGATION_PERCENTAGE, STAT.TACTICAL_MITIGATION_PERCENTAGE, // 66
    null // 67
  };

  // Mordor:
  // Name iLvl
  // Armour  Might Agility Vitality  Will  Fate  Morale  Power
  // ICMR  NCMR  ICPR  NCPR  CritHit Finesse PhyMas  TacMas
  // OutHeal Resist  CritDef InHeal  Block Parry Evade
  // PhyMit  TacMit  LoE Audacity  Hope  Notes
  // ArmourP MoraleP PowerP  MelCritP  RngCritP  TacCritP  HealCritP MelMagnP  RngMagnP  TacMagnP  HealMagnP
  // MelDmgP RngDmgP TacDmgP OutHealP  MelIndP RngIndP TacIndP HealIndP  AttDurP RunSpdP CritDefP  InHealP
  // BlockP  ParryP  EvadeP  PblkP PparP PevaP PblkMitP  PparMitP  PevaMitP
  // MelRedP RngRedP TacRedP PhyMitP TacMitP
  // ADD Category  Classes ADDS: MinLvl  MaxLvl  CategoryId  QualityId SlotId  ClassId EssSlots  SliceTot  SliceChk

  private static final STAT[] STATS_MORDOR={ null, null, // 1
    STAT.ARMOUR, STAT.MIGHT, STAT.AGILITY, STAT.VITALITY, STAT.WILL, STAT.FATE, STAT.MORALE, STAT.POWER, // 9
    STAT.ICMR, STAT.OCMR, STAT.ICPR, STAT.OCPR, STAT.CRITICAL_RATING, STAT.FINESSE, STAT.PHYSICAL_MASTERY, STAT.TACTICAL_MASTERY, // 17
    STAT.OUTGOING_HEALING, STAT.RESISTANCE, STAT.CRITICAL_DEFENCE, STAT.INCOMING_HEALING, STAT.BLOCK, STAT.PARRY, STAT.EVADE, // 24
    STAT.PHYSICAL_MITIGATION, STAT.TACTICAL_MITIGATION, STAT.LIGHT_OF_EARENDIL, STAT.AUDACITY, STAT.HOPE, null, // 30
    null, null, null, null, null, null, null, null, null, null, null, // 41
    null, null, null, null, null, null, null, null, null, null, null, null, // 53
    STAT.BLOCK_PERCENTAGE, STAT.PARRY_PERCENTAGE, STAT.EVADE_PERCENTAGE, null, null, null, null, null, null, // 63
    null, STAT.RANGED_DEFENCE_PERCENTAGE, null, STAT.PHYSICAL_MITIGATION_PERCENTAGE, STAT.TACTICAL_MITIGATION_PERCENTAGE, // 68
    null, null // 70 (classes)
  };

  private STAT[] _stats;
  private int _categoryIndex;
  private int _classesIndex;
  private int _notesIndex;

  /**
   * Constructor.
   * @param mordor Use mordor columns or previous ones.
   */
  public LotroPlanTable(boolean mordor)
  {
    this(mordor?STATS_MORDOR:STATS);
    if (mordor)
    {
      _classesIndex=MORDOR_CLASSES_INDEX;
      _notesIndex+=2;
      _categoryIndex=MORDOR_CATEGORY_INDEX;
    }
  }

  /**
   * Constructor.
   * @param stats Stats for each column.
   */
  public LotroPlanTable(STAT[] stats)
  {
    _stats=stats;
    _classesIndex=CLASSES_INDEX;
    _notesIndex=NOTES_INDEX;
    _categoryIndex=-1;
  }

  /**
   * Indicates if this is a Mordor table or not.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public boolean isMordor()
  {
    return _classesIndex==MORDOR_CLASSES_INDEX;
  }

  /**
   * Get the field index for category.
   * @return A field index.
   */
  public int getCategoryIndex()
  {
    return _categoryIndex;
  }

  /**
   * Get the field index for class requirement.
   * @return A field index.
   */
  public int getClassesIndex()
  {
    return _classesIndex;
  }

  /**
   * Get the field index for notes.
   * @return A field index.
   */
  public int getNotesIndex()
  {
    return _notesIndex;
  }

  /**
   * Load stats from fields.
   * @param fields Fields to read.
   * @return An item stats provider or <code>null</code>.
   */
  public ItemStatsProvider loadStats(String[] fields)
  {
    SlicesBasedItemStatsProvider provider=new SlicesBasedItemStatsProvider();
    int nbStats=_stats.length;
    for(int index=0;index<nbStats;index++)
    {
      STAT stat=_stats[index];
      if (stat==null) continue;
      if (index>=fields.length) continue;
      String valueStr=fields[index];
      if (valueStr.contains("CALCSLICE"))
      {
        ItemStatSliceData slice=SliceFormulaParser.parse(valueStr);
        if (slice!=null)
        {
          provider.addSlice(slice);
        }
      }
      else
      {
        FixedDecimalsInteger value=StatValueParser.parseStatValue(valueStr);
        if (value!=null)
        {
          provider.setStat(stat,value);
        }
      }
    }
    return provider;
  }
}
