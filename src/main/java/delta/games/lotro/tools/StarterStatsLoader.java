package delta.games.lotro.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileReader;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.character.CharacterStat;
import delta.games.lotro.character.stats.base.StarterStatsManager;
import delta.games.lotro.character.stats.base.io.xml.StarterStatsWriter;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;

/**
 * Loads starter stats from a raw data file.
 * @author DAM
 */
public class StarterStatsLoader
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    URL url=URLTools.getFromClassPath("stats.txt",StarterStatsLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.ISO8859_1);
    List<String> lines=TextUtils.readAsLines(reader);
    HashMap<String,List<String>> values=new HashMap<String,List<String>>(); 
    for(String line : lines)
    {
      line=removeDuplicateSpaces(line.trim());
      String[] items=line.split(" ");
      //System.out.println(Arrays.toString(items));
      String key=items[0].replace('_',' ');
      List<String> tokens=values.get(key);
      if (tokens==null)
      {
        tokens=new ArrayList<String>();
        values.put(key,tokens);
      }
      for(int i=1;i<items.length;i++) tokens.add(items[i]);
    }
    StarterStatsManager mgr=new StarterStatsManager();
    for(String key : values.keySet())
    {
      List<String> list=values.get(key);
      CharacterStat.STAT stat=CharacterStat.STAT.getByName(key);
      if (list.size()!=25) System.out.println("Bad size: "+list.size());
      int index=0;
      int[][][] STARTER_STATS = new int[CharacterClass.ALL_CLASSES.length][][];
      int classIndex=0;
      for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
      {
        if (cClass==CharacterClass.BEORNING) continue;
        STARTER_STATS[classIndex]=new int[Race.ALL_RACES.length][];
        int raceIndex=0;
        for(Race r : Race.ALL_RACES)
        {
          if (r==Race.BEORNING) continue;
          raceIndex++;
          if ((cClass==CharacterClass.BURGLAR) && (r==Race.DWARF)) continue;
          if ((cClass==CharacterClass.BURGLAR) && (r==Race.ELF)) continue;
          if ((cClass==CharacterClass.CAPTAIN) && (r==Race.DWARF)) continue;
          if ((cClass==CharacterClass.CAPTAIN) && (r==Race.ELF)) continue;
          if ((cClass==CharacterClass.CAPTAIN) && (r==Race.HOBBIT)) continue;
          if ((cClass==CharacterClass.CHAMPION) && (r==Race.HOBBIT)) continue;
          if ((cClass==CharacterClass.LORE_MASTER) && (r==Race.DWARF)) continue;
          if ((cClass==CharacterClass.LORE_MASTER) && (r==Race.HOBBIT)) continue;
          if ((cClass==CharacterClass.RUNE_KEEPER) && (r==Race.MAN)) continue;
          if ((cClass==CharacterClass.RUNE_KEEPER) && (r==Race.HOBBIT)) continue;
          if ((cClass==CharacterClass.WARDEN) && (r==Race.DWARF)) continue;
          STARTER_STATS[classIndex][raceIndex-1]=new int[CharacterStat.STAT.values().length];
          // Parse value
          String value=list.get(index).trim();
          Integer intValue=("N/A".equals(value))?null:NumericTools.parseInteger(value);
          CharacterStat characterStat=new CharacterStat(stat,intValue);
          mgr.setStat(r,cClass,characterStat);
          //System.out.println(cClass+"    "+r+"    "+stat+" => "+intValue);
          index++;
        }
        classIndex++;
      }
    }
    //BasicStatsSet set=mgr.getStartingStats(Race.ELF,CharacterClass.WARDEN);
    //System.out.println(set);
    StarterStatsWriter writer=new StarterStatsWriter();
    File to=new File("starter.xml");
    writer.write(to,mgr,EncodingNames.UTF_8);
    System.out.println("Wrote file " + to.getAbsolutePath());
  }

  private static String removeDuplicateSpaces(String line)
  {
    String oldLine=line;
    while(true)
    {
      line=line.replace("  ", " ");
      if (line.equals(oldLine)) break;
      oldLine=line;
    }
    return line;
  }
}
