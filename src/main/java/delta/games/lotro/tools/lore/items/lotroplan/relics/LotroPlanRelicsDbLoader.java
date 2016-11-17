package delta.games.lotro.tools.lore.items.lotroplan.relics;

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
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.tools.lore.items.lotroplan.LotroPlanTable;

/**
 * Loads relics definitions from raw data files.
 * @author DAM
 */
public class LotroPlanRelicsDbLoader
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LotroPlanRelicsDbLoader().doIt();
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    for(RelicType type : RelicType.values())
    {
      System.out.println("******" + type);
      List<Relic> relics=loadTable(type);
      for(Relic relic : relics)
      {
        System.out.println(relic.toString());
      }
    }
  }

  private List<Relic> loadTable(RelicType type)
  {
    String filename=type.name()+".txt";
    URL url=URLTools.getFromClassPath(filename,LotroPlanRelicsDbLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    List<Relic> items=new ArrayList<Relic>();
    lines.remove(0);
    LotroPlanTable table=new LotroPlanTable();
    for(String line : lines)
    {
      Relic relic=buildRelicFromLine(table, line, type);
      if (relic!=null)
      {
        items.add(relic);
      }
    }
    return items;
  }

  private Relic buildRelicFromLine(LotroPlanTable table, String line, RelicType type)
  {
    String[] fieldsTrimmed=StringSplitter.split(line.trim(),'\t');
    if (fieldsTrimmed.length<2)
    {
      System.out.println("Ignored: "+line);
      return null;
    }
    String[] fields=StringSplitter.split(line,'\t');

    // Name
    String name=fields[LotroPlanTable.NAME_INDEX];
    // Item level
    Integer itemLevel=NumericTools.parseInteger(fields[LotroPlanTable.ITEM_LEVEL_INDEX]);
    Relic relic=new Relic(name,type,itemLevel);
    // Stats
    BasicStatsSet relicStats=table.loadStats(fields);
    BasicStatsSet stats=relic.getStats();
    stats.setStats(relicStats);
    return relic;
  }
}
