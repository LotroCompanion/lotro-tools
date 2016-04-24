package delta.games.lotro.tools.lore.items;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.collections.filters.CompoundFilter;
import delta.common.utils.collections.filters.Filter;
import delta.common.utils.collections.filters.Operator;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.filters.ItemFilter;
import delta.games.lotro.lore.items.filters.ItemRequiredClassFilter;
import delta.games.lotro.lore.items.filters.ItemSlotFilter;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Test item filters.
 * @author DAM
 */
public class TestItemFilters
{
  private List<Item> loadItemsFile(File file)
  {
    long now1=System.currentTimeMillis();
    ItemXMLParser parser=new ItemXMLParser();
    List<Item> items=parser.parseItemsFile(file);
    long now2=System.currentTimeMillis();
    System.out.println("Time to load items: "+(now2-now1)+"ms.");
    return items;
  }

  private void filterWeapons(List<Item> weapons)
  {
    HashMap<WeaponType,List<Item>> map=new HashMap<WeaponType,List<Item>>();
    List<Item> others=new ArrayList<Item>();
    for(Item item : weapons)
    {
      Weapon weapon=(Weapon)item;
      WeaponType type=weapon.getWeaponType();
      if (type!=null)
      {
        List<Item> list=map.get(type);
        if (list==null)
        {
          list=new ArrayList<Item>();
          map.put(type,list);
        }
        list.add(item);
      }
      else
      {
        others.add(item);
      }
    }
    for(Map.Entry<WeaponType,List<Item>> entry : map.entrySet())
    {
      WeaponType type=entry.getKey();
      List<Item> list=entry.getValue();
      System.out.println("\t"+type+": "+list.size());
    }
    System.out.println("\tOther: "+others.size());
    for(Item other : others)
    {
      System.out.println(other.dump());
    }
  }

  private void filterArmours(List<Item> armours)
  {
    List<Item> head=new ArrayList<Item>();
    List<Item> shoulders=new ArrayList<Item>();
    List<Item> back=new ArrayList<Item>();
    List<Item> chest=new ArrayList<Item>();
    List<Item> hands=new ArrayList<Item>();
    List<Item> leggings=new ArrayList<Item>();
    List<Item> feet=new ArrayList<Item>();
    List<Item> shields=new ArrayList<Item>();
    List<Item> others=new ArrayList<Item>();
    for(Item item : armours)
    {
      Armour armour=(Armour)item;
      EquipmentLocation location=armour.getEquipmentLocation();
      if (location==EquipmentLocation.HEAD) head.add(armour);
      else if (location==EquipmentLocation.SHOULDER) shoulders.add(armour);
      else if (location==EquipmentLocation.CHEST) chest.add(armour);
      else if (location==EquipmentLocation.HAND) hands.add(armour);
      else if (location==EquipmentLocation.LEGS) leggings.add(armour);
      else if (location==EquipmentLocation.FEET) feet.add(armour);
      else if (location==EquipmentLocation.BACK) back.add(armour);
      else
      {
        ArmourType type=armour.getArmourType();
        if ((type==ArmourType.SHIELD) || (type==ArmourType.HEAVY_SHIELD) || (type==ArmourType.WARDEN_SHIELD))
        {
          shields.add(armour);
        }
        else others.add(armour);
      }
    }
    System.out.println("\tHead: "+head.size());
    System.out.println("\tShoulders: "+shoulders.size());
    System.out.println("\tBack: "+back.size());
    System.out.println("\tChest: "+chest.size());
    System.out.println("\tHands: "+hands.size());
    System.out.println("\tLeggings: "+leggings.size());
    System.out.println("\tFeet: "+feet.size());
    System.out.println("\tShields: "+shields.size());
    System.out.println("\tOther: "+others.size());
    for(Item other : others)
    {
      System.out.println(other.dump());
    }
  }

  private void filterJewels(List<Item> jewels)
  {
    List<Item> ear=new ArrayList<Item>();
    List<Item> neck=new ArrayList<Item>();
    List<Item> wrist=new ArrayList<Item>();
    List<Item> finger=new ArrayList<Item>();
    List<Item> pocket=new ArrayList<Item>();
    for(Item jewel : jewels)
    {
      EquipmentLocation location=jewel.getEquipmentLocation();
      if (location==EquipmentLocation.EAR) ear.add(jewel);
      else if (location==EquipmentLocation.NECK) neck.add(jewel);
      else if (location==EquipmentLocation.WRIST) wrist.add(jewel);
      else if (location==EquipmentLocation.FINGER) finger.add(jewel);
      else if (location==EquipmentLocation.POCKET) pocket.add(jewel);
    }
    System.out.println("\tEar: "+ear.size());
    System.out.println("\tNeck: "+neck.size());
    System.out.println("\tWrist: "+wrist.size());
    System.out.println("\tFinger: "+finger.size());
    System.out.println("\tPocket: "+pocket.size());
  }

  private void filterLegendaryClassItems(List<Item> classItems)
  {
    HashMap<CharacterClass,List<Item>> map=new HashMap<CharacterClass,List<Item>>();
    List<Item> others=new ArrayList<Item>();
    for(Item item : classItems)
    {
      CharacterClass characterClass=item.getRequiredClass();
      if (characterClass!=null)
      {
        List<Item> list=map.get(characterClass);
        if (list==null)
        {
          list=new ArrayList<Item>();
          map.put(characterClass,list);
        }
        list.add(item);
      }
      else
      {
        others.add(item);
      }
    }
    for(Map.Entry<CharacterClass,List<Item>> entry : map.entrySet())
    {
      CharacterClass characterClass=entry.getKey();
      List<Item> list=entry.getValue();
      System.out.println("\t"+characterClass+": "+list.size());
    }
    System.out.println("\tOther: "+others.size());
    for(Item other : others)
    {
      System.out.println(other.dump());
    }
  }

  private void filterOthers(List<Item> items)
  {
    List<Item> tools=new ArrayList<Item>();
    List<Item> instruments=new ArrayList<Item>();
    List<Item> recipes=new ArrayList<Item>();
    List<Item> others=new ArrayList<Item>();
    for(Item item : items)
    {
      EquipmentLocation location=item.getEquipmentLocation();
      if (location==EquipmentLocation.TOOL)
      {
        tools.add(item);
      }
      else
      {
        String category=item.getSubCategory();
        if ((category!=null) && (category.startsWith("Recipe:")))
        {
          recipes.add(item);
        }
        else if ("Instrument".equals(category))
        {
          // TODO some items are considered instruments but are not (chisel, satchel...)
          instruments.add(item);
        }
        else
        {
          others.add(item);
        }
      }
    }
    System.out.println("\tTool: "+tools.size());
    System.out.println("\tRecipe: "+recipes.size());
    System.out.println("\tInstrument: "+instruments.size());
    System.out.println("\tOther: "+others.size());
    for(Item other : others)
    {
      System.out.println(other.getIdentifier()+" -> "+other.getName());
    }
  }

  private void filterItems(List<Item> items)
  {
    List<Item> weapons=new ArrayList<Item>();
    List<Item> armours=new ArrayList<Item>();
    List<Item> jewels=new ArrayList<Item>();
    List<Item> liClassItems=new ArrayList<Item>();
    List<Item> others=new ArrayList<Item>();

    for(Item item : items)
    {
      if (item instanceof Weapon)
      {
        weapons.add(item);
      }
      else
      {
        if (item instanceof Armour)
        {
          armours.add(item);
        }
        else
        {
          EquipmentLocation location=item.getEquipmentLocation();
          if ((location==EquipmentLocation.EAR) || (location==EquipmentLocation.FINGER)
              || (location==EquipmentLocation.WRIST) || (location==EquipmentLocation.POCKET)
              || (location==EquipmentLocation.NECK))
          {
            jewels.add(item);
          }
          else
          {
            String category=item.getSubCategory();
            if ((category!=null) && (category.startsWith("Legendary Class Item:")))
            {
              liClassItems.add(item);
            }
            else
            {
              others.add(item);
            }
          }
        }
      }
    }
    System.out.println("Weapons: "+weapons.size());
    filterWeapons(weapons);
    System.out.println("Armours: "+armours.size());
    filterArmours(armours);
    System.out.println("Jewels: "+jewels.size());
    filterJewels(jewels);
    System.out.println("LI class items: "+liClassItems.size());
    filterLegendaryClassItems(liClassItems);
    System.out.println("Others: "+others.size());
    filterOthers(others);
    // TODO: class specific items (brooch, chisel/riffler, ...)
  }

  private void doIt()
  {
    File from=new File("items.xml");
    List<Item> items=loadItemsFile(from);
    long now1=System.currentTimeMillis();
    List<Filter<Item>> filters=new ArrayList<Filter<Item>>();
    ItemFilter filter1=new ItemRequiredClassFilter(CharacterClass.HUNTER,true);
    filters.add(filter1);
    ItemFilter filter2=new ItemSlotFilter(EquipmentLocation.WRIST);
    filters.add(filter2);
    Filter<Item> filter=new CompoundFilter<Item>(Operator.AND,filters);
    //ItemFilter filter=new ItemNameFilter(new StringFilter("Minor Essence of Vitality",MatchType.CONTAINS,true));
    List<Item> acceptedItems=new ArrayList<Item>();
    for(Item item : items)
    {
      if (filter.accept(item))
      {
        acceptedItems.add(item);
      }
    }
    long now2=System.currentTimeMillis();
    System.out.println("Time to filter items: "+(now2-now1)+"ms.");
    System.out.println(acceptedItems.size());
    /*
    for(Item item : acceptedItems)
    {
      System.out.println(item);
    }
    */
    filterItems(items);
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new TestItemFilters().doIt();
  }
}
