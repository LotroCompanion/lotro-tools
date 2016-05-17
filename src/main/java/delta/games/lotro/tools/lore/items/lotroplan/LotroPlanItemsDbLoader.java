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
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Loads starter stats from a raw data file.
 * @author DAM
 */
public class LotroPlanItemsDbLoader
{

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
    URL url=URLTools.getFromClassPath("itemsdb.txt",LotroPlanItemsDbLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    List<Item> items=new ArrayList<Item>();
    //_fields=StringSplitter.split(lines.get(0),'\t');
    lines.remove(0);
    LotroPlanTable table=new LotroPlanTable();
    for(String line : lines)
    {
      Item item=buildItemFromLine(table, line);
      items.add(item);
    }
    ItemsManager mgr=ItemsManager.getInstance();
    File toFile=new File("itemsdb.xml").getAbsoluteFile();
    mgr.writeItemsFile(toFile,items);
    //System.out.println(_map);
  }

  private Item buildItemFromLine(LotroPlanTable table, String line)
  {
    String[] fields=StringSplitter.split(line,'\t');
    Integer armor=null;
    if (fields[LotroPlanTable.ARMOUR_INDEX].length()>0)
    {
      armor=NumericTools.parseInteger(fields[LotroPlanTable.ARMOUR_INDEX]);
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
    String idStr=fields[LotroPlanTable.NOTES].trim();
    if (idStr.startsWith("ID:"))
    {
      idStr=idStr.substring(3).trim();
    }
    int id=NumericTools.parseInt(idStr,-1);
    item.setIdentifier(id);
    // Name
    String name=fields[LotroPlanTable.NAME_INDEX];
    item.setName(name);
    // Item level
    int itemLevel=NumericTools.parseInt(fields[LotroPlanTable.ITEM_LEVEL_INDEX],-1);
    if (itemLevel!=-1)
    {
      item.setItemLevel(Integer.valueOf(itemLevel));
    }
    // Stats
    BasicStatsSet itemStats=table.loadStats(fields);
    BasicStatsSet stats=item.getStats();
    stats.setStats(itemStats);
    return item;
  }
}
