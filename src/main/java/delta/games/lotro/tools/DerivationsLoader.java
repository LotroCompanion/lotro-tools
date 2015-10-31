package delta.games.lotro.tools;

import java.io.File;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.text.TextUtils;
import delta.games.lotro.character.CharacterStat.STAT;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.DerivatedStatsContributionsMgr;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Loads derivated stats contributions from a file.
 * @author DAM
 */
public class DerivationsLoader
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DerivatedStatsContributionsMgr mgr=new DerivatedStatsContributionsMgr();
    File f=new File("d:\\dam\\tmp\\derivations.txt");
    List<String> lines=TextUtils.readAsLines(f);
    CharacterClass[] classes = CharacterClass.ALL_CLASSES;
    for(String line : lines)
    {
      line=removeDuplicateSpaces(line.trim());
      String[] items=line.split(" ");
      //System.out.println(Arrays.toString(items));
      String primaryStatStr=items[0].replace('_',' ');
      STAT primaryStat=STAT.getByName(primaryStatStr);
      String impactedStatStr=items[1].replace('_',' ').trim();
      STAT impactedStat=STAT.getByName(impactedStatStr);
      if (impactedStat==null)
      {
        if (impactedStatStr.endsWith("Rating"))
        {
          impactedStatStr=impactedStatStr.substring(0,impactedStatStr.length()-6).trim();
        }
        impactedStat=STAT.getByName(impactedStatStr);
      }
      System.out.println(primaryStat+"  =>  "+impactedStat);
      int index=0;
      for(CharacterClass cClass : classes)
      {
        String factorStr = items[2 + index];
        System.out.println("\t"+cClass+": "+factorStr);
        index++;
        FixedDecimalsInteger factor=getFactor(factorStr);
        if (factor!=null)
        {
          mgr.setFactor(primaryStat,impactedStat,cClass,factor);
        }
      }
    }
    // Test
    BasicStatsSet stats=new BasicStatsSet();
    stats.addStat(STAT.MIGHT,new FixedDecimalsInteger(100));
    stats.addStat(STAT.FATE,new FixedDecimalsInteger(10));
    System.out.println(stats);
    System.out.println(mgr.getContribution(CharacterClass.CHAMPION,stats));
  }

  private static FixedDecimalsInteger getFactor(String factorStr)
  {
    FixedDecimalsInteger ret=null;
    Integer factor=NumericTools.parseInteger(factorStr,false);
    if (factor!=null)
    {
      ret=new FixedDecimalsInteger(factor.intValue());
    }
    else
    {
      Float fFactor=NumericTools.parseFloat(factorStr,false);
      if (fFactor!=null)
      {
        ret=new FixedDecimalsInteger(fFactor.floatValue());
      }
    }
    return ret;
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
