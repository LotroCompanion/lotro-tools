package delta.games.lotro.tools.lore.items.scalables;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileReader;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.StringSplitter;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.lore.items.StatValueParser;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Loads scalable items samples.
 * @author DAM
 */
public class ScalableItemsSamplesLoader
{
  private static final int NAME_INDEX=0;
  private static final int ITEM_LEVEL_INDEX=1;
  private static final int REQUIRED_LEVEL_INDEX=2;
  private static final int SLOTS_INDEX=25;
  private static final STAT[] STATS = {
    null,null,null,STAT.ARMOUR,
    STAT.MIGHT,STAT.AGILITY,STAT.VITALITY,STAT.WILL,STAT.FATE,
    STAT.MORALE,STAT.POWER,STAT.ICMR,STAT.OCMR,STAT.ICPR,STAT.OCPR,
    STAT.CRITICAL_RATING,STAT.FINESSE,STAT.PHYSICAL_MASTERY,STAT.TACTICAL_MASTERY,
    STAT.RESISTANCE,STAT.CRITICAL_DEFENCE,STAT.INCOMING_HEALING,
    STAT.BLOCK,STAT.PARRY,STAT.EVADE,null,null,null,STAT.PARRY_PERCENTAGE
  };

  /**
   * Load items from a table.
   * @param filename Name of file to read.
   * @return A list of items.
   */
  public List<Item> loadTable(String filename)
  {
    URL url=URLTools.getFromClassPath(filename,ScalableItemsSamplesLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    List<Item> items=new ArrayList<Item>();
    lines.remove(0);
    for(String line : lines)
    {
      Item item=buildItemFromLine(line);
      if (item!=null)
      {
        items.add(item);
      }
    }
    return items;
  }

  private Item buildItemFromLine(String line)
  {
    String[] fields=StringSplitter.split(line,'\t');
    // Stats
    BasicStatsSet stats=new BasicStatsSet();
    int nbStats=STATS.length;
    for(int i=0;i<nbStats;i++)
    {
      if ((STATS[i]!=null) && (fields.length>i))
      {
        String valueStr=fields[i];
        FixedDecimalsInteger value=StatValueParser.parseStatValue(valueStr);
        if (value!=null)
        {
          stats.addStat(STATS[i],value);
        }
      }
    }
    // Item level
    Integer itemLevel=NumericTools.parseInteger(fields[ITEM_LEVEL_INDEX]);

    // Build item
    FixedDecimalsInteger armorStat=stats.getStat(STAT.ARMOUR);
    Item item=null;
    Armour armour=null;
    if (armorStat!=null)
    {
      armour=new Armour();
      item=armour;
      armour.setArmourValue(armorStat.intValue());
      stats.removeStat(STAT.ARMOUR);
    }
    else
    {
      item=new Item();
    }
    // Item level
    item.setItemLevel(itemLevel);
    // Name
    String name=fields[NAME_INDEX];
    name=name.trim();
    item.setName(name);
    // Slots
    String slotStr=fields[SLOTS_INDEX];
    int nbSlots=NumericTools.parseInt(slotStr,0);
    item.setEssenceSlots(nbSlots);
    // Min level
    String requiredLevelStr=fields[REQUIRED_LEVEL_INDEX];
    Integer requiredLevel=NumericTools.parseInteger(requiredLevelStr);
    if (requiredLevelStr!=null)
    {
      item.setMinLevel(requiredLevel);
    }
    // Stats
    item.getStats().setStats(stats);
    return item;
  }
}
