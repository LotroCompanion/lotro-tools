package delta.games.lotro.tools.lore.items.lotroplan.essences;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileReader;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.StringSplitter;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.comparators.ItemIdComparator;
import delta.games.lotro.tools.lore.items.lotroplan.LotroPlanTable;

/**
 * Injects stats into essences.
 * @author DAM
 */
public class EssenceStatsInjector
{
  /**
   * Perform stats injection for essence items.
   * @param items Items.
   */
  public void doIt(Collection<Item> items)
  {
    List<Item> essences=getEssences(items);
    HashMap<Integer,HashMap<String,Item>> essencesByTier=sortByTier(essences);
    List<Integer> tiers=new ArrayList<Integer>(essencesByTier.keySet());
    Collections.sort(tiers);
    for(Integer tier : tiers)
    {
      System.out.println("Tier: "+tier);
      HashMap<String,Item> essencesOfTier=essencesByTier.get(tier);
      handleTier(tier.intValue(),essencesOfTier);
    }
    System.out.println("Nb essences: "+essences.size());
  }

  private void handleTier(int tier, HashMap<String,Item> essences)
  {
    URL url=URLTools.getFromClassPath("tier"+tier+".txt",EssenceStatsInjector.class.getPackage());
    if (url!=null)
    {
      TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
      List<String> lines=TextUtils.readAsLines(reader);
      lines.remove(0);
      HashSet<String> unmanaged=new HashSet<String>();
      unmanaged.addAll(essences.keySet());
      LotroPlanTable table=new LotroPlanTable();
      for(String line : lines)
      {
        String[] fields=StringSplitter.split(line,'\t');
        // Name
        String name=fields[LotroPlanTable.NAME_INDEX];
        if (name.startsWith("-"))
        {
          name=name.substring(1).trim();
          Item item=essences.get(name);
          if (item!=null)
          {
            unmanaged.remove(name);
            // Item level
            int itemLevel=NumericTools.parseInt(fields[LotroPlanTable.ITEM_LEVEL_INDEX],-1);
            if (itemLevel!=-1)
            {
              item.setItemLevel(Integer.valueOf(itemLevel));
            }
            // Notes
            if (fields.length>LotroPlanTable.NOTES)
            {
              String notes=fields[LotroPlanTable.NOTES].trim();
              if (notes.length()>0)
              {
                item.getBonus().add(notes);
              }
            }
            // Stats
            BasicStatsSet itemStats=table.loadStats(fields);
            BasicStatsSet stats=item.getStats();
            stats.setStats(itemStats);
          }
          else
          {
            System.out.println("Essence not found: "+name);
          }
        }
      }
      if (unmanaged.size()>0)
      {
        System.out.println(unmanaged);
      }
    }
  }

  private HashMap<Integer,HashMap<String,Item>> sortByTier(List<Item> essences)
  {
    List<Integer> tiers=loadTiers();
    HashMap<Integer,HashMap<String,Item>> essencesByTier=new HashMap<Integer,HashMap<String,Item>>();
    int nb=Math.min(essences.size(),tiers.size());
    for(int i=0;i<nb;i++)
    {
      Item item=essences.get(i);
      Integer tier=tiers.get(i);
      //System.out.println(item.getName()+"  =>  "+tier);
      if (tier!=null)
      {
        String category="Essence:Tier"+tier;
        item.setSubCategory(category);
        HashMap<String,Item> essencesOfTier=essencesByTier.get(tier);
        if (essencesOfTier==null)
        {
          essencesOfTier=new HashMap<String,Item>();
          essencesByTier.put(tier,essencesOfTier);
        }
        essencesOfTier.put(item.getName(),item);
      }
    }
    return essencesByTier;
  }

  private List<Integer> loadTiers()
  {
    List<Integer> ret=new ArrayList<Integer>();
    URL url=URLTools.getFromClassPath("tiers.txt",EssenceStatsInjector.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    for(String line : lines)
    {
      char[] tierChars=line.toCharArray();
      for(char tierChar : tierChars)
      {
        if (tierChar==' ')
        {
          ret.add(null);
        }
        else
        {
          ret.add(Integer.valueOf(tierChar-'0'));
        }
      }
    }
    return ret;
  }

  private List<Item> getEssences(Collection<Item> items)
  {
    List<Item> itemsSortedById=new ArrayList<Item>(items);
    Collections.sort(itemsSortedById,new ItemIdComparator());
    List<Item> ret=new ArrayList<Item>();
    for(Item item : itemsSortedById)
    {
      String category=item.getSubCategory();
      if ("Essence".equals(category))
      {
        ret.add(item);
      }
    }
    return ret;
  }
}
