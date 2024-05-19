package delta.games.lotro.tools.reports;

import java.io.File;
import java.util.List;

import delta.common.utils.files.TextFileWriter;
import delta.common.utils.math.Range;
import delta.games.lotro.lore.items.ItemUtils;
import delta.games.lotro.lore.items.legendary2.TraceriesSetsUtils;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSet.SetType;
import delta.games.lotro.lore.items.sets.ItemsSetsManager;
import delta.games.lotro.lore.items.sets.ItemsSetsUtils;
import delta.games.lotro.lore.items.sets.SetBonus;

/**
 * Report for items/traceries sets.
 * @author DAM
 */
public class ItemsSetsReport
{
  /**
   * Do report.
   */
  public void doIt()
  {
    File rootDir=ReportsConstants.getDataReportsRootDir();
    rootDir.mkdirs();
    File toFile=new File(rootDir,"sets.txt");
    TextFileWriter writer=new TextFileWriter(toFile);
    writer.start();
    for(ItemsSet set : ItemsSetsManager.getInstance().getAll())
    {
      showSet(set,writer);
    }
    writer.terminate();
  }

  private void showSet(ItemsSet set, TextFileWriter writer)
  {
    writer.writeNextLine("Set: "+set);
    if (set.useAverageItemLevelForSetLevel())
    {
      int[] levels=getItemLevels(set);
      for(int level : levels)
      {
        writer.writeNextLine("=> level "+level+":");
        writeBonus(set,writer,level);
      }
    }
    else
    {
      int level=set.getSetLevel();
      writeBonus(set,writer,level);
    }
  }

  private void writeBonus(ItemsSet set, TextFileWriter writer, int level)
  {
    set.setUseAverageItemLevelForSetLevel(false);
    for(SetBonus bonus : set.getBonuses())
    {
      List<String> lines=ItemUtils.buildLinesToShowItemsSetBonus(set,bonus,level);
      if (lines.isEmpty())
      {
        continue;
      }
      int nbPieces=bonus.getPiecesCount();
      writer.writeNextLine(nbPieces+" pieces");
      for(String line : lines)
      {
        writer.writeNextLine(line);
      }
    }
  }

  private int[] getItemLevels(ItemsSet set)
  {
    if (set.getSetType()==SetType.ITEMS)
    {
      return getItemLevelsForItemsSet(set);
    }
    if (set.getSetType()==SetType.TRACERIES)
    {
      return getItemLevelsForTraceriesSet(set);
    }
    return new int[0];
  }

  private int[] getItemLevelsForItemsSet(ItemsSet set)
  {
    Range range=ItemsSetsUtils.findItemLevelRange(set);
    int min=range.getMin().intValue();
    int max=range.getMax().intValue();
    if (min!=max)
    {
      return new int[] {min,max};
    }
    return new int[] {min};
  }

  private int[] getItemLevelsForTraceriesSet(ItemsSet set)
  {
    List<Tracery> allTraceries=TraceriesSetsUtils.getMemberTraceries(set);
    int[] minMaxCharacterLevel=TraceriesSetsUtils.findCharacterLevelRange(allTraceries);
    List<Tracery> minTraceries=TraceriesSetsUtils.findTraceriesForCharacterLevel(allTraceries,minMaxCharacterLevel[0]);
    int [] minMaxItemLevelMin=TraceriesSetsUtils.findItemLevelRange(minTraceries);
    List<Tracery> maxTraceries=TraceriesSetsUtils.findTraceriesForCharacterLevel(allTraceries,minMaxCharacterLevel[1]);
    int [] minMaxItemLevelMax=TraceriesSetsUtils.findItemLevelRange(maxTraceries);
    int min=minMaxItemLevelMin[0];
    int max=minMaxItemLevelMax[1];
    if (min!=max)
    {
      return new int[] {min,max};
    }
    return new int[] {min};
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemsSetsReport().doIt();
  }
}
