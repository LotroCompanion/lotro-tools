package delta.games.lotro.tools.lore.items.lotroplan;

import java.io.File;
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
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.items.stats.ItemStatsProvider;
import delta.games.lotro.lore.items.stats.ScalingRulesNames;
import delta.games.lotro.lore.items.stats.SlicesBasedItemStatsProvider;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Loads starter stats from a raw data file.
 * @author DAM
 */
public class LotroPlanItemsDbLoader
{
  //private static final String[] NAMES={};
  private static final String[] NAMES={"jewels.txt", "hd_jewels.txt", "heavy.txt", "medium.txt", "light.txt", "weapons.txt", "misc.txt", "mordor.txt", "northern_mirkwood.txt"};

  private String _section;
  private ItemsMerger _merger;

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LotroPlanItemsDbLoader().doIt();
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    _merger=new ItemsMerger();
    List<Item> items=loadTable("itemsdb.txt");
    _merger.registerItems(items);
    for(String tableName : NAMES)
    {
      handleAdditionalTable(tableName);
    }
    File toFile=new File("data/items/tmp/itemsdb.xml").getAbsoluteFile();
    items=_merger.getItems();
    setScalingRules(items);
    ItemXMLWriter.writeItemsFile(toFile,items);
    //List<Integer> ids=new ArrayList<Integer>(_failedItems.keySet());
    //new BuildItemsDbForIcons().buildDb(_failedItems,ids);
  }

  private void setScalingRules(List<Item> items)
  {
    for(Item item : items)
    {
      String scalingRule=null;
      String itemLevels=item.getProperty(ItemPropertyNames.LEVELS);
      if (itemLevels!=null)
      {
        if (itemLevels.endsWith("207]")) scalingRule=ScalingRulesNames.TEAL_ARMOR_SETS;
        if ("[201, 221]".equals(itemLevels)) scalingRule=ScalingRulesNames.OSGILIATH;
        if (scalingRule!=null)
        {
          item.setProperty(ItemPropertyNames.SCALING,scalingRule);
        }
        item.removeProperty(ItemPropertyNames.LEVELS);
      }
    }
  }

  private void handleAdditionalTable(String tableName)
  {
    ArmourType armourType=null;
    if ("heavy.txt".equals(tableName)) armourType=ArmourType.HEAVY;
    else if ("medium.txt".equals(tableName)) armourType=ArmourType.MEDIUM;
    else if ("light.txt".equals(tableName)) armourType=ArmourType.LIGHT;
    List<Item> items=loadTable(tableName);
    for(Item item : items)
    {
      _merger.newItem(item,armourType);
    }
  }

  /**
   * Load the items in a table.
   * @param filename Table name.
   * @return A list of items.
   */
  public List<Item> loadTable(String filename)
  {
    _section=null;
    URL url=URLTools.getFromClassPath(filename,LotroPlanItemsDbLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    List<Item> items=new ArrayList<Item>();
    //_fields=StringSplitter.split(lines.get(0),'\t');
    lines.remove(0);
    boolean mordor=useMordorFormat(filename);
    LotroPlanTable table=new LotroPlanTable(mordor);
    for(String line : lines)
    {
      Item item=buildItemFromLine(table, line);
      if (item!=null)
      {
        items.add(item);
      }
    }
    return items;
  }

  private boolean useMordorFormat(String filename)
  {
    if (filename.contains("mordor")) return true;
    if (filename.contains("northern_mirkwood")) return true;
    return false;
  }

  private Item buildItemFromLine(LotroPlanTable table, String line)
  {
    String[] fieldsTrimmed=StringSplitter.split(line.trim(),'\t');
    if (line.startsWith("#"))
    {
      System.out.println("Ignored: "+line);
      return null;
    }
    if (fieldsTrimmed.length<2)
    {
      _section=line.trim();
      System.out.println("Section: "+_section);
      return null;
    }
    String[] fields=StringSplitter.split(line,'\t');

    // Item level
    int itemLevel=NumericTools.parseInt(fields[LotroPlanTable.ITEM_LEVEL_INDEX],-1);
    // Stats
    ItemStatsProvider provider=table.loadStats(fields);
    BasicStatsSet stats=provider.getStats(itemLevel);

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
    // ID
    String idStr="";
    int notesIndex=table.getNotesIndex();
    if (fields.length>=notesIndex)
    {
      idStr=fields[notesIndex].trim();
    }
    int id=0;
    if (idStr.startsWith("ID:"))
    {
      idStr=idStr.substring(3).trim();
    }
    id=NumericTools.parseInt(idStr,-1);
    if (id!=-1)
    {
      item.setIdentifier(id);
    }
    // Name
    String name=fields[LotroPlanTable.NAME_INDEX];
    if (name.startsWith("("))
    {
      int index=name.indexOf(')');
      idStr=name.substring(1,index).trim();
      id=NumericTools.parseInt(idStr,-1);
      item.setIdentifier(id);
      name=name.substring(index+1).trim();
    }
    if (name.endsWith(":"))
    {
      name=name.substring(0,name.length()-1);
    }
    name=name.replace(' ',' ');
    if (name.endsWith("s)"))
    {
      name=name.substring(0,name.length()-2);
      int index=name.lastIndexOf('(');
      int nbSlots=NumericTools.parseInt(name.substring(index+1),0);
      name=name.substring(0,index).trim();
      item.setEssenceSlots(nbSlots);
    }
    if (name.endsWith(")"))
    {
      String newName=name.substring(0,name.length()-1);
      int index=newName.lastIndexOf('(');
      String valueStr=newName.substring(index+1);
      int minLevel;
      if ("TBD".equals(valueStr))
      {
        minLevel=-1;
      }
      else
      {
        minLevel=NumericTools.parseInt(valueStr,-1);
        name=newName.substring(0,index).trim();
      }
      if (minLevel>0)
      {
        item.setMinLevel(Integer.valueOf(minLevel));
      }
    }
    name=name.trim();
    item.setName(name);
    // Item level
    if (itemLevel!=-1)
    {
      item.setItemLevel(Integer.valueOf(itemLevel));
    }
    // Stats
    item.getStats().setStats(stats);
    String slices=provider.toPersistableString();
    int nbSlices=((SlicesBasedItemStatsProvider)provider).getSlices();
    if (nbSlices>0)
    {
      item.setProperty(ItemPropertyNames.FORMULAS,slices);
    }
    // Slot
    EquipmentLocation slot=null;
    if ("Head".equals(_section)) slot=EquipmentLocation.HEAD;
    else if ("Shoulders".equals(_section)) slot=EquipmentLocation.SHOULDER;
    else if ("Chest".equals(_section)) slot=EquipmentLocation.CHEST;
    else if ("Hands".equals(_section)) slot=EquipmentLocation.HAND;
    else if ("Legs".equals(_section)) slot=EquipmentLocation.LEGS;
    else if ("Feet".equals(_section)) slot=EquipmentLocation.FEET;
    else if ("Shields".equals(_section)) slot=EquipmentLocation.OFF_HAND;
    else if ("Ears".equals(_section)) slot=EquipmentLocation.EAR;
    else if ("Neck".equals(_section)) slot=EquipmentLocation.NECK;
    else if ("Wrists".equals(_section)) slot=EquipmentLocation.WRIST;
    else if ("Fingers".equals(_section)) slot=EquipmentLocation.FINGER;
    else if ("Pockets".equals(_section)) slot=EquipmentLocation.POCKET;
    if (slot==null)
    {
      int categoryIndex=table.getCategoryIndex();
      if ((categoryIndex>0) && (fields.length>=categoryIndex))
      {
        String category=fields[categoryIndex];
        slot=getSlotFromCategory(category);
      }
    }

    // Class requirement
    String classRequirementStr="";
    int classesIndex=table.getClassesIndex();
    if ((classesIndex!=-1) && (fields.length>=classesIndex))
    {
      classRequirementStr=fields[classesIndex].trim();
    }
    CharacterClass classRequirement=getClassRequirement(classRequirementStr);
    if (classRequirement!=null)
    {
      item.setRequiredClass(classRequirement);
    }

    if ("Burglar Signals".equals(_section))
    {
      item.setSubCategory("Burglar:Signal");
      slot=EquipmentLocation.RANGED_ITEM;
      item.setRequiredClass(CharacterClass.BURGLAR);
    }
    else if ("Captain Standards".equals(_section))
    {
      item.setSubCategory("Captain:Standard");
      slot=EquipmentLocation.RANGED_ITEM;
      item.setRequiredClass(CharacterClass.CAPTAIN);
    }
    else if ("Hunter Tomes".equals(_section))
    {
      String subCategory="Tome";
      if (name.contains("Wind-rider")) subCategory="Wind-rider";
      if (name.contains("Whisper-draw")) subCategory="Whisper-draw";
      item.setSubCategory("Hunter:"+subCategory);
      slot=EquipmentLocation.CLASS_SLOT;
      item.setRequiredClass(CharacterClass.HUNTER);
    }
    else if ("Lore-master Brooches".equals(_section))
    {
      item.setSubCategory("Lore-master:Stickpin");
      slot=EquipmentLocation.RANGED_ITEM;
      item.setRequiredClass(CharacterClass.LORE_MASTER);
    }
    else if ("Minstrel Instruments".equals(_section))
    {
      item.setSubCategory("Instrument");
      slot=EquipmentLocation.RANGED_ITEM;
      item.setRequiredClass(CharacterClass.MINSTREL);
    }
    else if ("Rune-keeper Chisels".equals(_section))
    {
      String subCategory="Other";
      if (name.contains("Riffler")) subCategory="Riffler";
      if (name.contains("Chisel")) subCategory="Chisel";
      item.setSubCategory("Rune-keeper:"+subCategory);
      slot=EquipmentLocation.RANGED_ITEM;
      item.setRequiredClass(CharacterClass.RUNE_KEEPER);
    }
    else if ("Warden Carvings".equals(_section))
    {
      item.setSubCategory("Warden:Carving");
      slot=EquipmentLocation.CLASS_SLOT;
      item.setRequiredClass(CharacterClass.WARDEN);
    }
    if (slot!=null)
    {
      item.setEquipmentLocation(slot);
    }
    return item;
  }

  private CharacterClass getClassRequirement(String classRequirement)
  {
    if ("Be".equals(classRequirement)) return CharacterClass.BEORNING;
    if ("Bu".equals(classRequirement)) return CharacterClass.BURGLAR;
    if ("Ca".equals(classRequirement)) return CharacterClass.CAPTAIN;
    if ("Ch".equals(classRequirement)) return CharacterClass.CHAMPION;
    if ("Gu".equals(classRequirement)) return CharacterClass.GUARDIAN;
    if ("Hu".equals(classRequirement)) return CharacterClass.HUNTER;
    if ("Lo".equals(classRequirement)) return CharacterClass.LORE_MASTER;
    if ("Mi".equals(classRequirement)) return CharacterClass.MINSTREL;
    if ("Ru".equals(classRequirement)) return CharacterClass.RUNE_KEEPER;
    if ("Wa".equals(classRequirement)) return CharacterClass.WARDEN;
    return null;
  }

  private EquipmentLocation getSlotFromCategory(String category)
  {
    EquipmentLocation slot=null;
    // Armor
    if ("Head".equals(category)) slot=EquipmentLocation.HEAD;
    else if ("Shoulders".equals(category)) slot=EquipmentLocation.SHOULDER;
    else if ("Chest".equals(category)) slot=EquipmentLocation.CHEST;
    else if ("Hands".equals(category)) slot=EquipmentLocation.HAND;
    else if ("Back".equals(category)) slot=EquipmentLocation.BACK;
    else if ("Legs".equals(category)) slot=EquipmentLocation.LEGS;
    else if ("Feet".equals(category)) slot=EquipmentLocation.FEET;
    // Jewels
    else if ("Ears".equals(category)) slot=EquipmentLocation.EAR;
    else if ("Neck".equals(category)) slot=EquipmentLocation.NECK;
    else if ("Wrists".equals(category)) slot=EquipmentLocation.WRIST;
    else if ("Fingers".equals(category)) slot=EquipmentLocation.FINGER;
    else if ("Pocket".equals(category)) slot=EquipmentLocation.POCKET;
    // Others
    else if ("Melee".equals(category)) slot=EquipmentLocation.MAIN_HAND;
    else if ("Ranged".equals(category)) slot=EquipmentLocation.RANGED_ITEM;
    else if ("Shield".equals(category)) slot=EquipmentLocation.OFF_HAND;
    else if ("Class".equals(category)) slot=EquipmentLocation.CLASS_SLOT;
    else if ("Instrument".equals(category)) slot=EquipmentLocation.RANGED_ITEM;
    return slot;
  }
}
