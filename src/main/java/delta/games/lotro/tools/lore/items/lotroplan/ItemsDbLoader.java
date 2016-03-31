package delta.games.lotro.tools.lore.items.lotroplan;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileReader;
import delta.common.utils.misc.IntegerHolder;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.StringSplitter;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.character.CharacterStat.STAT;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Loads starter stats from a raw data file.
 * @author DAM
 */
public class ItemsDbLoader
{
  private static final int NAME_INDEX=0;
  private static final int ITEM_LEVEL_INDEX=1;
  private static final int ARMOUR_INDEX=2;
  private static final int FIRST_STAT_INDEX=3;
  private static final int Notes=28;
  // TODO add support for these stats
  /*
  private static final int Audacity=26;
  private static final int Hope=27;
  private static final int ParryP=53;
  private static final int RngRedP=62;
  */
  // All other stats are unused
  //Name  iLvl  Armour  Might Agility Vitality  Will  Fate  Morale  Power ICMR  NCMR  ICPR  NCPR  CritHit Finesse PhyMas  TacMas  Resist  CritDef InHeal  Block Parry Evade PhyMit  TacMit  Audacity  Hope  Notes ArmourP MoraleP PowerP  MelCritP  RngCritP  TacCritP  HealCritP MelMagnP  RngMagnP  TacMagnP  HealMagnP MelDmgP RngDmgP TacDmgP OutHealP  MelIndP RngIndP TacIndP HealIndP  AttDurP RunSpdP CritDefP  InHealP BlockP  ParryP  EvadeP  PblkP PparP PevaP PblkMitP  PparMitP  PevaMitP  MelRedP RngRedP TacRedP PhyMitP TacMitP

  private static final STAT[] STATS={ STAT.MIGHT, STAT.AGILITY, STAT.VITALITY, STAT.WILL, STAT.FATE, STAT.MORALE, STAT.POWER,
    STAT.ICMR, STAT.OCMR, STAT.ICPR, STAT.OCPR, STAT.CRITICAL_RATING, STAT.FINESSE, STAT.PHYSICAL_MASTERY, STAT.TACTICAL_MASTERY,
    STAT.RESISTANCE, STAT.CRITICAL_DEFENCE, STAT.INCOMING_HEALING, STAT.BLOCK, STAT.PARRY, STAT.EVADE,
    STAT.PHYSICAL_MITIGATION, STAT.TACTICAL_MITIGATION
  };

  private HashMap<String,IntegerHolder> _map=new HashMap<String,IntegerHolder>();
  private String[] _fields;
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemsDbLoader().doIt();
  }

  private void doIt()
  {
    URL url=URLTools.getFromClassPath("itemsdb.txt",ItemsDbLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    List<Item> items=new ArrayList<Item>();
    _fields=StringSplitter.split(lines.get(0),'\t');
    lines.remove(0);
    for(String line : lines)
    {
      Item item=buildItemFromLine(line);
      items.add(item);
    }
    ItemsManager mgr=ItemsManager.getInstance();
    File toFile=new File("itemsdb.xml").getAbsoluteFile();
    mgr.writeItemsFile(toFile,items);
    System.out.println(_map);
  }

  private Item buildItemFromLine(String line)
  {
    String[] fields=StringSplitter.split(line,'\t');
    Integer armor=null;
    if (fields[ARMOUR_INDEX].length()>0)
    {
      armor=NumericTools.parseInteger(fields[ARMOUR_INDEX]);
    }
    Item item=null;
    Armour armour=null;
    if (armor!=null)
    {
      armour=new Armour();
      item=armour;
      armour.setArmourValue(armor.intValue());
    }
    else
    {
      item=new Item();
    }
    // ID
    String idStr=fields[Notes].trim();
    if (idStr.startsWith("ID:"))
    {
      idStr=idStr.substring(3).trim();
    }
    int id=NumericTools.parseInt(idStr,-1);
    item.setIdentifier(id);
    // Name
    String name=fields[NAME_INDEX];
    item.setName(name);
    // Item level
    int itemLevel=NumericTools.parseInt(fields[ITEM_LEVEL_INDEX],-1);
    if (itemLevel!=-1)
    {
      item.setItemLevel(Integer.valueOf(itemLevel));
    }
    // Stats
    BasicStatsSet stats=item.getStats();
    for(int i=0;i<STATS.length;i++)
    {
      Integer statValue=NumericTools.parseInteger(fields[i+FIRST_STAT_INDEX]);
      if (statValue!=null)
      {
        stats.setStat(STATS[i],statValue.intValue());
      }
    }
    for(int i=FIRST_STAT_INDEX+STATS.length;i<fields.length;i++)
    {
      if (fields[i].length()>0) {
        IntegerHolder holder=_map.get(_fields[i]);
        if (holder==null)
        {
          holder=new IntegerHolder();
          _map.put(_fields[i],holder);
        }
        holder.increment();
      }
    }
    return item;
  }
}
