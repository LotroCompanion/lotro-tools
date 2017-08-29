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
import delta.games.lotro.character.stats.STAT;
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
   * Index of the 'name' column.
   */
  public static final int NAME_INDEX=0;
  /**
   * Index of the 'item level' column.
   */
  public static final int ITEM_LEVEL_INDEX=1;
  /**
   * Index of the 'notes' column.
   */
  public static final int NOTES_INDEX=28;

  private static final STAT[] STATS={ null, null, // 1
    STAT.ARMOUR, STAT.MIGHT, STAT.AGILITY, STAT.VITALITY, STAT.WILL, STAT.FATE, STAT.MORALE, STAT.POWER, // 9
    STAT.ICMR, STAT.OCMR, STAT.ICPR, STAT.OCPR, STAT.CRITICAL_RATING, STAT.FINESSE, STAT.PHYSICAL_MASTERY, STAT.TACTICAL_MASTERY, // 17
    STAT.RESISTANCE, STAT.CRITICAL_DEFENCE, STAT.INCOMING_HEALING, STAT.BLOCK, STAT.PARRY, STAT.EVADE, // 23
    STAT.PHYSICAL_MITIGATION, STAT.TACTICAL_MITIGATION, STAT.AUDACITY, STAT.HOPE, null, // 28
    STAT.LIGHT_OF_EARENDIL
  };

  /**
   * Default cell values.
   */
  public static final String[] CELLS={
    "ID",
    "Name",
    "ItemLevel",
    STAT.ARMOUR.getKey(),
    STAT.MIGHT.getKey(),
    STAT.AGILITY.getKey(),
    STAT.VITALITY.getKey(),
    STAT.WILL.getKey(),
    STAT.FATE.getKey(),
    STAT.MORALE.getKey(),
    STAT.POWER.getKey(),
    STAT.ICMR.getKey(),
    STAT.OCMR.getKey(),
    STAT.ICPR.getKey(),
    STAT.OCPR.getKey(),
    STAT.CRITICAL_RATING.getKey(),
    STAT.FINESSE.getKey(),
    STAT.PHYSICAL_MASTERY.getKey(),
    STAT.TACTICAL_MASTERY.getKey(),
    STAT.RESISTANCE.getKey(),
    STAT.CRITICAL_DEFENCE.getKey(),
    STAT.INCOMING_HEALING.getKey(),
    STAT.BLOCK.getKey(),
    STAT.PARRY.getKey(),
    STAT.EVADE.getKey(),
    STAT.PHYSICAL_MITIGATION.getKey(),
    STAT.TACTICAL_MITIGATION.getKey(),
    STAT.AUDACITY.getKey(),
    STAT.HOPE.getKey(),
    "Notes",
    STAT.LIGHT_OF_EARENDIL.getKey()
  };

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
    System.out.println("Nb essences: "+essences.size());
  }

  private void handleTier(int tier, HashMap<Integer,Item> essences)
  {
    URL url=URLTools.getFromClassPath("tier"+tier+".txt",EssenceStatsInjector.class.getPackage());
    if (url!=null)
    {
      TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
      List<String> lines=TextUtils.readAsLines(reader);
      boolean hasId=lines.get(0).startsWith("ID");
      lines.remove(0);
      HashSet<Integer> unmanaged=new HashSet<Integer>();
      unmanaged.addAll(essences.keySet());
      LotroPlanTable table=new LotroPlanTable(STATS);
      for(String line : lines)
      {
        String[] fields=StringSplitter.split(line,'\t');
        if (fields.length>2)
        {
          List<Item> items=findEssences(fields,hasId,essences);
          for(Item item : items)
          {
            if (hasId)
            {
              String[] newFields=new String[fields.length-1];
              for(int i=1;i<fields.length;i++)
              {
                newFields[i-1]=fields[i];
                if (newFields[i-1].equals(CELLS[i]))
                {
                  newFields[i-1]="";
                }
              }
              fields=newFields;
            }
            unmanaged.remove(Integer.valueOf(item.getIdentifier()));
            // Item level
            int itemLevel=NumericTools.parseInt(fields[ITEM_LEVEL_INDEX],-1);
            if (itemLevel!=-1)
            {
              item.setItemLevel(Integer.valueOf(itemLevel));
            }
            // Notes
            if (fields.length>NOTES_INDEX)
            {
              String notes=fields[NOTES_INDEX].trim();
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
          if (items.size()==0)
          {
            System.out.println("Essence not found: "+fields[0]);
          }
        }
        else
        {
          if (!line.startsWith("#"))
          {
            System.err.println("Bad fields count: "+line);
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

  private List<Item> findEssences(String[] fields, boolean hasId, HashMap<Integer,Item> essences)
  {
    List<Item> ret=new ArrayList<Item>();
    Integer id=null;
    if (hasId)
    {
      id=NumericTools.parseInteger(fields[0]);
    }
    int nameIndex=NAME_INDEX+(hasId?1:0);
    String name=fields[nameIndex];
    if (name.startsWith("-"))
    {
      name=name.substring(1).trim();
    }
    for(Item essence : essences.values())
    {
      if (id!=null)
      {
        if (id.intValue()==essence.getIdentifier())
        {
          ret.add(essence);
          return ret;
        }
      }
      if (name.equals(essence.getName()))
      {
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
          int tier=0;
          if ((tierChar>='0') && (tierChar<='9')) tier=tierChar-'0';
          else if ((tierChar>='A') && (tierChar<='F')) tier=tierChar-'A'+10;
          ret.add(Integer.valueOf(tier));
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
        String name=item.getName();
        // Ignore Mordor Essences boxes
        if (name.contains("Mordor - Essences"))
        {
          item.setSubCategory("Box of Essences");
          continue;
        }
        ret.add(item);
      }
    }
    return ret;
  }
}
