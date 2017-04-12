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
import delta.games.lotro.lore.items.stats.ItemStatsProvider;
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
    HashMap<Integer,HashMap<Integer,Item>> essencesByTier=sortByTier(essences);
    List<Integer> tiers=new ArrayList<Integer>(essencesByTier.keySet());
    Collections.sort(tiers);
    for(Integer tier : tiers)
    {
      System.out.println("Tier: "+tier);
      HashMap<Integer,Item> essencesOfTier=essencesByTier.get(tier);
      handleTier(tier.intValue(),essencesOfTier);
    }
    System.out.println("Tier: 8");
    handleTier(8,essencesByTier.get(Integer.valueOf(-1)));
    System.out.println("Nb essences: "+essences.size());
  }

  private void handleTier(int tier, HashMap<Integer,Item> essences)
  {
    URL url=URLTools.getFromClassPath("tier"+tier+".txt",EssenceStatsInjector.class.getPackage());
    if (url!=null)
    {
      TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
      List<String> lines=TextUtils.readAsLines(reader);
      lines.remove(0);
      HashSet<Integer> unmanaged=new HashSet<Integer>();
      unmanaged.addAll(essences.keySet());
      LotroPlanTable table=new LotroPlanTable();
      for(String line : lines)
      {
        String[] fields=StringSplitter.split(line,'\t');
        if (fields.length>2)
        {
          // Name
          String name=fields[LotroPlanTable.NAME_INDEX];
          if (name.startsWith("-"))
          {
            name=name.substring(1).trim();
          }
          List<Item> items=findEssencesByName(essences,name);
          if (items!=null)
          {
            for(Item item : items)
            {
              unmanaged.remove(Integer.valueOf(item.getIdentifier()));
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
              // Tier
              item.setSubCategory("Essence:Tier"+tier);
              // Stats
              ItemStatsProvider provider=table.loadStats(fields);
              BasicStatsSet stats=item.getStats();
              stats.setStats(provider.getStats(itemLevel));
            }
          }
          else
          {
            System.out.println("Essence not found: "+name);
          }
        }
      }
      if (unmanaged.size()>0)
      {
        System.out.println(unmanaged+" unmanaged essences:");
        for(Integer id : unmanaged)
        {
          Item essence=essences.get(id);
          System.out.println("\t"+essence);
        }
      }
    }
  }

  private List<Item> findEssencesByName(HashMap<Integer,Item> essences, String name)
  {
    List<Item> ret=null;
    for(Item essence : essences.values())
    {
      if (name.equals(essence.getName()))
      {
        if (ret==null)
        {
          ret=new ArrayList<Item>();
        }
        ret.add(essence);
      }
    }
    return ret;
  }

  private HashMap<Integer,HashMap<Integer,Item>> sortByTier(List<Item> essences)
  {
    List<Integer> tiers=loadTiers();
    HashMap<Integer,HashMap<Integer,Item>> essencesByTier=new HashMap<Integer,HashMap<Integer,Item>>();
    int nbEssences=essences.size();
    int nbTierSpecifications=tiers.size();
    if (nbEssences!=nbTierSpecifications)
    {
      System.err.println("Warning! Bad tier specifications: nbEssences="+nbEssences+"!=nbTierSpecifications"+nbTierSpecifications);
    }
    for(int i=0;i<nbEssences;i++)
    {
      Item item=essences.get(i);
      Integer tier=(i<nbTierSpecifications)?tiers.get(i):null;
      //System.out.println(item.getName()+"  =>  "+tier);
      if (tier!=null)
      {
        String category="Essence:Tier"+tier;
        item.setSubCategory(category);
      }
      else
      {
        tier=Integer.valueOf(-1); // Unspecified
      }
      HashMap<Integer,Item> essencesOfTier=essencesByTier.get(tier);
      if (essencesOfTier==null)
      {
        essencesOfTier=new HashMap<Integer,Item>();
        essencesByTier.put(tier,essencesOfTier);
      }
      essencesOfTier.put(Integer.valueOf(item.getIdentifier()),item);
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
