package delta.games.lotro.tools.checks.sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.common.utils.math.Range;
import delta.common.utils.math.RangeComparator;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.legendary2.TraceriesSetsUtils;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSetsManager;
import delta.games.lotro.lore.items.sets.SetBonus;
import delta.games.lotro.lore.items.sets.ItemsSet.SetType;

/**
 * Simple tool class to investigate traceries sets.
 * @author DAM
 */
public class MainCheckTraceriesSets
{
  private void doIt()
  {
    List<ItemsSet> sets=findTraceriesSetsWithAverageItemLevel();
    for(ItemsSet set : sets)
    {
      inspectSet(set);
    }
  }

  private void inspectSet(ItemsSet set)
  {
    System.out.println("Set: "+set.getName());
    List<Tracery> traceries=TraceriesSetsUtils.getMemberTraceries(set);
    List<Range> characterLevelRanges=getCharacterLevelRanges(traceries);
    for(Range characterLevelRange : characterLevelRanges)
    {
      List<Tracery> traceriesForCharacterLevelRange=findTraceriesForCharacterLevelRange(traceries,characterLevelRange);
      int[] minMaxItemLevel=TraceriesSetsUtils.findItemLevelRange(traceriesForCharacterLevelRange);
      System.out.println("\tCharacter levels: "+characterLevelRange+" => Min/max item level: "+minMaxItemLevel[0]+"/"+minMaxItemLevel[1]);
      for(int itemLevel=minMaxItemLevel[0];itemLevel<=minMaxItemLevel[1];itemLevel++)
      {
        List<SetBonus> bonuses=set.getBonuses();
        for(SetBonus bonus : bonuses)
        {
          StatsProvider statsProvider=bonus.getStatsProvider();
          List<String> lines=StatUtils.getFullStatsForDisplay(statsProvider,itemLevel);
          System.out.println("\t\tItem Level "+itemLevel+", "+bonus.getPiecesCount()+" pieces: "+lines);
        }
      }
    }
  }

  private List<ItemsSet> findTraceriesSetsWithAverageItemLevel()
  {
    List<ItemsSet> ret=new ArrayList<ItemsSet>();
    ItemsSetsManager mgr=ItemsSetsManager.getInstance();
    List<ItemsSet> itemsSets=mgr.getAll();
    for(ItemsSet itemsSet : itemsSets)
    {
      // Filter
      boolean average=itemsSet.useAverageItemLevelForSetLevel();
      if (average)
      {
        SetType type=itemsSet.getSetType();
        if (type==SetType.TRACERIES)
        {
          ret.add(itemsSet);
        }
      }
    }
    return ret;
  }

  private List<Tracery> findTraceriesForCharacterLevelRange(List<Tracery> traceries, Range characterLevelRange)
  {
    List<Tracery> ret=new ArrayList<Tracery>();
    for(Tracery tracery : traceries)
    {
      Item item=tracery.getItem();
      Integer minLevel=item.getMinLevel();
      Integer maxLevel=item.getMaxLevel();
      Range levelRange=new Range(minLevel,maxLevel);
      if (levelRange.equals(characterLevelRange))
      {
        ret.add(tracery);
      }
    }
    return ret;
  }

  private List<Range> getCharacterLevelRanges(List<Tracery> traceries)
  {
    Set<Range> ranges=new HashSet<Range>();
    for(Tracery tracery : traceries)
    {
      Item item=tracery.getItem();
      Integer minLevel=item.getMinLevel();
      Integer maxLevel=item.getMaxLevel();
      Range r=new Range(minLevel,maxLevel);
      ranges.add(r);
    }
    List<Range> ret=new ArrayList<Range>(ranges);
    Collections.sort(ret,new RangeComparator());
    return ret;
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCheckTraceriesSets().doIt();
  }
}
