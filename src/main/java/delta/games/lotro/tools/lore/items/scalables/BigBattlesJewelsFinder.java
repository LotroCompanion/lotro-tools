package delta.games.lotro.tools.lore.items.scalables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;

/**
 * Find some scalable items using their name.
 * @author DAM
 */
public class BigBattlesJewelsFinder
{
  private static final String[] PREFIXES = {
    "Officer's", "Engineer's", "Vanguard's",
    "Veteran Officer's", "Veteran Engineer's", "Veteran Vanguard's",
    "Adept Officer's", "Master Engineer's", "Skilled Vanguard's"
  };
  private static final String[] ADJECTIVES = {
    "Resolute", "Valourous", "Dextrous", "Resilient", "Enduring", "Persevering", "Steadfast"
  };

  private static final String[] JEWELS = {
    "Token", "Necklace", "Bracelet", "Ring", "Earring"
  };
  private static final EquipmentLocation[] JEWEL_LOCATIONS = {
    EquipmentLocation.POCKET, EquipmentLocation.NECK, EquipmentLocation.WRIST, EquipmentLocation.FINGER, EquipmentLocation.EAR
  };

  private static final String[] PREFIXES_PELARGIR = {
    "", "Elite"
  };
  private static final String[] TYPE = { "Scout's", "Soldier's", "Advisor's" };
  private static final String[] ADJECTIVES_PELARGIR = {
    "Sturdy", "Fateful"
  };

  private static final Integer ITEM_LEVEL=Integer.valueOf(217);
  /**
   * Find some scalable items.
   * @param items Items to search.
   * @return A list of selected items.
   */
  public List<Item> findScalableItems(List<Item> items)
  {
    List<Item> ret=new ArrayList<Item>();
    Map<String,Item> bbJewels=generateBigBattleJewels();
    Map<String,Item> pelargirRings=generatePelargirRings();
    for(Item item : items)
    {
      Integer level=item.getItemLevel();
      if (ITEM_LEVEL.equals(level))
      {
        Item bbJewel=bbJewels.get(item.getName());
        if (bbJewel!=null)
        {
          ret.add(item);
          //System.out.println("Added:"+item);
        }
        Item pelargirRing=pelargirRings.get(item.getName());
        if (pelargirRing!=null)
        {
          ret.add(item);
          //System.out.println("Added:"+item);
        }
      }
    }
    return ret;
  }

  private Map<String,Item> generatePelargirRings()
  {
    Map<String,Item> ret=new HashMap<String,Item>();
    for(int i=0;i<PREFIXES_PELARGIR.length;i++)
    {
      String prefix=PREFIXES_PELARGIR[i];
      for(String type : TYPE)
      {
        for(String adjective : ADJECTIVES_PELARGIR)
        {
          String name=generatePelargirRingName(prefix,type,adjective);
          Item item=new Item();
          item.setName(name);
          item.setEquipmentLocation(EquipmentLocation.FINGER);
          ret.put(name,item);
        }
      }
    }
    return ret;
  }

  private Map<String,Item> generateBigBattleJewels()
  {
    Map<String,Item> ret=new HashMap<String,Item>();
    for(String prefix : PREFIXES)
    {
      for(String adjective : ADJECTIVES)
      {
        for(int i=0;i<JEWELS.length;i++)
        {
          String jewel=JEWELS[i];
          String name=generateBigBattleJewelName(prefix,adjective,jewel);
          Item item=new Item();
          item.setName(name);
          item.setEquipmentLocation(JEWEL_LOCATIONS[i]);
          ret.put(name,item);
        }
      }
    }
    return ret;
  }

  private String generateBigBattleJewelName(String prefix, String adjective, String jewel)
  {
    return prefix+" "+adjective+" "+jewel;
  }

  private String generatePelargirRingName(String prefix, String type, String adjective)
  {
    return ((prefix.length()>0)?prefix+" ":"")+type+" "+adjective+" Ring";
  }
}
