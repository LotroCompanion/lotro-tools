package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemCategory;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;
import delta.games.lotro.lore.items.legendary.LegendaryWeapon;
import delta.games.lotro.tools.lore.items.lotroplan.essences.EssenceStatsInjector;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Normalize items database "release candidate".
 * @author DAM
 */
public class ItemNormalization
{
  private HashMap<String,List<Item>> _byCategory;
  private HashMap<Integer,Item> loadItemsFile(File file)
  {
    ItemXMLParser parser=new ItemXMLParser();
    List<Item> items=parser.parseItemsFile(file);
    HashMap<Integer,Item> ret=new HashMap<Integer,Item>();
    for(Item item : items)
    {
      ret.put(Integer.valueOf(item.getIdentifier()),item);
    }
    return ret;
  }

  /**
   * Constructor.
   */
  public ItemNormalization()
  {
    _byCategory=new HashMap<String,List<Item>>();
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    File file1=new File("data/items/tmp/items-rc.xml");
    HashMap<Integer,Item> sourceItems=loadItemsFile(file1);
    System.out.println(sourceItems.size());

    List<Integer> ids=new ArrayList<Integer>(sourceItems.keySet());
    for(Integer id : ids)
    {
      Item sourceItem=sourceItems.get(id);
      sourceItem=normalizeItem(sourceItem);
      if (sourceItem!=null)
      {
        sourceItems.put(id,sourceItem);
      }
      else
      {
        sourceItems.remove(id);
      }
    }
    // Essences stats injection
    new EssenceStatsInjector().doIt(sourceItems.values());

    // Build final items list
    List<Item> items=new ArrayList<Item>(sourceItems.values());

    // Consistency checks
    consistencyChecks(items);

    // Write result file
    File toFile=new File("data/items/items.xml").getAbsoluteFile();
    ItemsManager.getInstance().writeItemsFile(toFile,items);
    // Dump unmanaged items
    if (_byCategory.size()>0)
    {
      System.out.println("There are unmanaged item categories:");
      List<String> categories=new ArrayList<String>(_byCategory.keySet());
      Collections.sort(categories);
      int totalSize=0;
      for(String category : categories)
      {
        int size=_byCategory.get(category).size();
        System.out.println(category+ "  =>  " + size + _byCategory.get(category));
        totalSize+=size;
      }
      System.out.println(totalSize);
    }

    /*
    ItemsSorter sorter=new ItemsSorter();
    sorter.sortItems(items);
    File rootDir=new File("sorted");
    rootDir.mkdirs();
    sorter.writeToFiles(rootDir);
    */
  }

  private void consistencyChecks(List<Item> items)
  {
    int nbArmours=0;
    int nbWeapons=0;
    for(Item item : items)
    {
      int id=item.getIdentifier();
      String name=item.getName();
      // Armours
      if (item instanceof Armour)
      {
        Armour armour=(Armour)item;
        ArmourType type=armour.getArmourType();
        if (type==null)
        {
          nbArmours++;
          //System.out.println("No armour type for: " + name + " (" + id + ')');
        }
      }
      // Weapons
      if (item instanceof Weapon)
      {
        Weapon weapon=(Weapon)item;
        WeaponType type=weapon.getWeaponType();
        if (type==null)
        {
          nbWeapons++;
          System.out.println("No weapon type for: " + name + " (" + id + ')');
        }
      }
    }
    if (nbArmours>0)
    {
      System.out.println("Nb failed armours: " + nbArmours);
    }
    if (nbWeapons>0)
    {
      System.out.println("Nb failed weapons: " + nbWeapons);
    }
  }

  private Item normalizeItem(Item item)
  {
    Item ret=removeTestItems(item);
    if (ret==null)
    {
      return null;
    }
    ret=normalizeSpecifics(ret);
    ret=normalizeArmours(ret);
    ret=normalizeWeapons(ret);
    ret=normalizeCrafting(ret);
    ret=normalizeJewels(ret);
    ret=normalizeRecipes(ret);
    ret=normalizeLegendaryItem(ret);
    ret=normalizeCraftingTool(ret);
    ret=normalizeInstrument(ret);
    ret=normalizeBeorningItems(ret);
    ret=normalizeBurglarItems(ret);
    ret=normalizeCaptainItems(ret);
    ret=normalizeChampionItems(ret);
    ret=normalizeGuardianItems(ret);
    ret=normalizeHunterItems(ret);
    ret=normalizeLoreMasterItems(ret);
    ret=normalizeMinstrelItems(ret);
    ret=normalizeRuneKeeperItems(ret);
    ret=normalizeWardenItems(ret);
    ret=normalizePotions(ret);
    ret=normalizeByCategory(ret);
    ret=normalizeBelt(ret);
    ret=normalizeThrownWeapon(ret);
    ret=normalizeMiscWeapons(ret);
    ret=normalizeCategory0(ret);
    String prop=ret.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if (prop!=null)
    {
      List<Item> items=_byCategory.get(prop);
      if (items==null)
      {
        items=new ArrayList<Item>();
        _byCategory.put(prop,items);
      }
      items.add(item);
    }
    return ret;
  }

  private Item removeTestItems(Item item)
  {
    Item ret=item;
    String name=item.getName();
    if (name!=null)
    {
      if ((name.startsWith("DNT")) || (name.contains("TBD")))
      {
        ret=null;
      }
      if (name.contains("Tester"))
      {
        ret=null;
      }
      if (name.contains("Barter Test"))
      {
        ret=null;
      }
    }
    String tulkasCategory=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    // Test armours  (medium, heavy, light)
    if ("230".equals(tulkasCategory)) return null;
    if ("231".equals(tulkasCategory)) return null;
    if ("232".equals(tulkasCategory)) return null;
    return ret;
  }

  private Item normalizeSpecifics(Item item)
  {
    int id=item.getIdentifier();
    // Sword of Thrâng
    if (id==1879097298)
    {
      item.setSubCategory("Misc:Quest Item");
      item.setEquipmentLocation(null);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    // Inscribed Riffler of the Veteran Rune-keeper
    if (id==1879335200)
    {
      item.setSubCategory("Rune-keeper:Riffler");
      item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
      item.setEssenceSlots(1);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      BasicStatsSet stats=item.getStats();
      stats.setStat(STAT.TACTICAL_MASTERY,2952);
      stats.setStat(STAT.MORALE,1472);
      stats.setStat(STAT.WILL,180);
    }
    // Ring of Dryad Loveliness
    if (id==1879337970)
    {
      item.setEssenceSlots(3);
      BasicStatsSet stats=item.getStats();
      stats.setStat(STAT.MORALE,1401);
      stats.setStat(STAT.WILL,237);
    }
    return item;
  }

  private Item normalizePotions(Item item)
  {
    String itemTulkasCategory=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("28".equals(itemTulkasCategory))
    {
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      String name=item.getName();
      if ((name.contains("Edhelharn Token")) || (name.contains("Hope Token")))
      {
        item.setSubCategory("Misc:Hope Token");
      }
      else
      {
        item.setSubCategory("Misc:Potion");
      }
      item.setEquipmentLocation(null);
    }
    return item;
  }

  
  private Item normalizeByCategory(Item item)
  {
    normalizeByCategory(item,"27","Misc:Quest Item");
    normalizeByCategory(item,"31","Misc:Key");
    normalizeByCategory(item,"50","Misc:Device");
    normalizeByCategory(item,"55","Misc:Food");
    normalizeByCategory(item,"57","Misc:Scroll");
    normalizeByCategory(item,"172","Misc:Trophy:Special");
    normalizeByCategory(item,"47","Misc:Trophy");
    normalizeByCategory(item,"8","Misc:Mount");
    normalizeByCategory(item,"91","Misc:Implement");
    normalizeByCategory(item,"53","Misc:Non-Inventory");
    normalizeByCategory(item,"52","Misc:Emote");
    normalizeByCategory(item,"16","Misc:Smoking");
    normalizeByCategory(item,"174","Misc:Emote");
    normalizeByCategory(item,"205","Misc:Event Item");
    // Cosmetic
    normalizeByCategory(item,"97","Cosmetic:Back");
    normalizeByCategory(item,"182","Cosmetic:Chest");
    normalizeByCategory(item,"192","Cosmetic:Class");
    normalizeByCategory(item,"180","Cosmetic:Feet");
    normalizeByCategory(item,"184","Cosmetic:Hands");
    normalizeByCategory(item,"183","Cosmetic:Head");
    normalizeByCategory(item,"96","Cosmetic:Held");
    normalizeByCategory(item,"185","Cosmetic:Legs");
    normalizeByCategory(item,"181","Cosmetic:Shoulders");
    // Housing
    normalizeByCategory(item,"82","Housing:Music");
    normalizeByCategory(item,"83","Housing:SurfacePaint");
    normalizeByCategory(item,"84","Housing:Trophy");
    normalizeByCategory(item,"85","Housing:Furniture");
    normalizeByCategory(item,"86","Housing:Floor");
    normalizeByCategory(item,"87","Housing:Yard");
    normalizeByCategory(item,"88","Housing:Wall");
    normalizeByCategory(item,"98","Housing:Special");
    normalizeByCategory(item,"99","Housing:Ceiling");
    // Fishing
    normalizeByCategory(item,"102","Fishing:Fish");
    normalizeByCategory(item,"100","Fishing:Bait");
    normalizeByCategory(item,"103","Fishing:Pole",null,EquipmentLocation.MAIN_HAND);
    normalizeByCategory(item,"101","Misc");
    // Legendary
    normalizeByCategory(item,"206","Legendary:Crystal of Remembrance");
    normalizeByCategory(item,"176","Legendary:Star-lit crystal");
    normalizeByCategory(item,"107","Legendary:XP");
    normalizeByCategory(item,"166","Legendary:Legacy Scroll");
    normalizeByCategory(item,"167","Legendary:Delving Scroll");
    normalizeByCategory(item,"168","Legendary:Enpowerment Scroll");
    normalizeByCategory(item,"108","Legendary:Reset Scroll");
    normalizeByCategory(item,"194","Legendary:Relic Removal Scroll");
    normalizeByCategory(item,"109","Legendary:Deconstructable Relics");

    // Misc
    normalizeByCategory(item,"41","Dye");
    normalizeByCategory(item,"14","Oil");
    normalizeByCategory(item,"20","Trap");
    normalizeByCategory(item,"235","Essence");
    normalizeByCategory(item,"89","Faction Item");
    normalizeByCategory(item,"177","Skirmish Mark");
    normalizeByCategory(item,"178","Barter Item");
    normalizeByCategory(item,"186","Skill Item");
    normalizeByCategory(item,"207","Task Item");
    normalizeByCategory(item,"189","Perk");
    normalizeByCategory(item,"191","Travel");
    normalizeByCategory(item,"190","Tome");
    normalizeByCategory(item,"173","Misc");
    normalizeByCategory(item,"164","Misc");
    normalizeByCategory(item,"170","Crafting Trophy");
    normalizeByCategory(item,"43","Charter");
    normalizeByCategory(item,"187","Horn",CharacterClass.CHAMPION,null);
    normalizeByCategory(item,"171","Pet Food",CharacterClass.LORE_MASTER,null);
    return item;
  }

  private Item normalizeByCategory(Item item, String tulkasCategory, String category)
  {
    return normalizeByCategory(item,tulkasCategory,category,null,null);
  }

  private Item normalizeByCategory(Item item, String tulkasCategory, String category, CharacterClass cClass, EquipmentLocation loc)
  {
    String itemTulkasCategory=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if (itemTulkasCategory!=null)
    {
      if (tulkasCategory.equals(itemTulkasCategory))
      {
        item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        item.setSubCategory(category);
        if (cClass!=null)
        {
          item.setRequiredClass(cClass);
        }
        item.setEquipmentLocation(loc);
      }
    }
    return item;
  }

  private Item normalizeCraftingTool(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("32".equals(category))
    {
      String oldCategory=item.getSubCategory();
      if ((!"32".equals(oldCategory)) && (!"Craft Tool".equals(oldCategory)))
      {
        System.out.println("Warn: expected 'Craft Tool', got '"+oldCategory+"'");
      }
      item.setSubCategory("Crafting Tool");
      item.setEquipmentLocation(EquipmentLocation.TOOL);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeThrownWeapon(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("54".equals(category))
    {
      item=setWeaponTypeFromCategory(item,null,WeaponType.THROWN_WEAPON);
      item.setEquipmentLocation(null);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      item.setSubCategory(null);
    }
    return item;
  }


  private Item normalizeBelt(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("111".equals(category))
    {
      item.setSubCategory(CharacterClass.GUARDIAN.getLabel()+":Belt");
      item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
      item.setRequiredClass(CharacterClass.GUARDIAN);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeCategory0(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("0".equals(category))
    {
      String name=item.getName().toLowerCase();
      if (name.indexOf("tracking")!=-1)
      {
        item.setSubCategory("Misc:Tracking");
        item.setEquipmentLocation(null);
      }
      else if (name.indexOf("experience disabler")!=-1)
      {
        item.setSubCategory("Misc:XP disabler");
        item.setEquipmentLocation(null);
      }
      else
      {
        item.setSubCategory("Misc");
        item.setEquipmentLocation(null);
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeInstrument(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("11".equals(category))
    {
      String oldCategory=item.getSubCategory();
      if ((oldCategory!=null) && (!"11".equals(oldCategory)) && (!oldCategory.startsWith("Instrument")))
      {
        System.out.println("Warn: expected 'Instrument', got '"+oldCategory+"'");
      }
      item.setSubCategory("Instrument");
      item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeBeorningItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("233".equals(category))
    {
      item.setRequiredClass(CharacterClass.BEORNING);
      String name=item.getName().toLowerCase();
      if (name.indexOf("carving")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.BEORNING.getLabel()+":Carving");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BEORNING.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeBurglarItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("48".equals(category))
    {
      item.setRequiredClass(CharacterClass.BURGLAR);
      String name=item.getName().toLowerCase();
      if (name.indexOf("signal")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Signal");
      }
      else if (name.indexOf("caltrops")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Clever Devices:Caltrops");
      }
      else if (name.indexOf("marbles")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Clever Devices:Marbles");
      }
      else if (name.indexOf("stun dust")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Clever Devices:Stun Dust");
      }
      else if (name.indexOf("knife")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Knife");
      }
      else if (name.indexOf("hatchet")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Throwing Hatchet");
      }
      else if (name.indexOf("tools")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Tools");
      }
      else if (name.indexOf("implements")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Implements");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.BURGLAR.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeCaptainItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("13".equals(category))
    {
      item.setRequiredClass(CharacterClass.CAPTAIN);
      String name=item.getName().toLowerCase();
      if (name.indexOf("crest")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.CAPTAIN.getLabel()+":Crest");
      }
      else if (name.indexOf("armaments")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.CAPTAIN.getLabel()+":Armaments");
      }
      else if (name.indexOf("battle tonic")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.CAPTAIN.getLabel()+":Battle Tonic");
      }
      else if (name.indexOf("standard")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.CAPTAIN.getLabel()+":Standard");
      }
      else if (name.indexOf("emblem")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.CAPTAIN.getLabel()+":Emblem");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.CAPTAIN.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeChampionItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("22".equals(category))
    {
      item.setRequiredClass(CharacterClass.CHAMPION);
      String name=item.getName().toLowerCase();
      if (name.indexOf("rune")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.CHAMPION.getLabel()+":Rune");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.CHAMPION.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeGuardianItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("26".equals(category))
    {
      item.setRequiredClass(CharacterClass.GUARDIAN);
      String name=item.getName().toLowerCase();
      if (name.indexOf("overpower")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.GUARDIAN.getLabel()+":Overpower Tactics");
      }
      else if (name.indexOf("spike")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.GUARDIAN.getLabel()+":Shield-spike Kit"); // TODO and warden
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.GUARDIAN.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    if ("179".equals(category))
    {
      item.setRequiredClass(CharacterClass.GUARDIAN); // TODO and warden
      item.setEquipmentLocation(null);
      item.setSubCategory(CharacterClass.GUARDIAN.getLabel()+":Shield-spike Kit");
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeHunterItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("17".equals(category))
    {
      item.setRequiredClass(CharacterClass.HUNTER);
      String name=item.getName().toLowerCase();
      if (name.indexOf("bow chant")!=-1)
      {
        // Use bow chant before 'whisper-draw' and 'wind-rider'
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.HUNTER.getLabel()+":Bow Chant");
      }
      else if (name.indexOf("whisper-draw")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.HUNTER.getLabel()+":Whisper-draw");
      }
      else if (name.indexOf("wind-rider")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.HUNTER.getLabel()+":Wind-rider");
      }
      else if (name.indexOf("oil")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.HUNTER.getLabel()+":Oil");
      }
      else if (name.indexOf("guide")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.HUNTER.getLabel()+":Guide");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.HUNTER.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private static final String[] LOREMASTER_PETS = {
    "cat", "dog", "fox", "frog", "hare", "snake", "sparrow", "squirrel", "turtle"
  };

  private Item normalizeLoreMasterItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("19".equals(category))
    {
      item.setRequiredClass(CharacterClass.LORE_MASTER);
      String name=item.getName().toLowerCase();
      if (name.indexOf("brooch")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Brooch");
      }
      else if (name.indexOf("parable")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Parable");
      }
      else if (name.indexOf("book")!=-1)
      {
        if (name.startsWith("the"))
        {
          item.setEquipmentLocation(null);
          item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Other");
        }
        else
        {
          item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
          item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Book");
        }
      }
      else if (name.indexOf("tome")!=-1)
      {
        boolean isPet=false;
        for(String petName : LOREMASTER_PETS)
        {
          if (name.indexOf(petName)!=-1)
          {
            isPet=true;
          }
        }
        if (isPet)
        {
          item.setEquipmentLocation(null);
          item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Pet Skill");
        } else {
          item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
          item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Tome");
        }
      }
      else if (name.indexOf("stickpin")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Stickpin");
      }
      else if (name.indexOf("talisman")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Talisman");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeMinstrelItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("4".equals(category))
    {
      item.setRequiredClass(CharacterClass.MINSTREL);
      String name=item.getName().toLowerCase();
      if (name.indexOf("sheet")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Music Sheet");
      }
      else if (name.indexOf("parable")!=-1)
      {
        item.setRequiredClass(CharacterClass.LORE_MASTER); // !!
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Parable");
      }
      else if (name.indexOf("mentor")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Mentoring");
      }
      else if (name.indexOf("strings")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Strings");
      }
      else if (name.indexOf("manual")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Manual");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    if ("163".equals(category))
    {
      item.setRequiredClass(CharacterClass.MINSTREL);
      String name=item.getName().toLowerCase();
      if (name.indexOf("songbook")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Songbook");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.MINSTREL.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeRuneKeeperItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("106".equals(category))
    {
      item.setRequiredClass(CharacterClass.RUNE_KEEPER);
      String name=item.getName().toLowerCase();
      if (name.indexOf("satchel")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Satchel");
      }
      else if (name.indexOf("rune-bag")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Satchel");
      }
      else if (name.indexOf("chisel")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Chisel");
      }
      else if (name.indexOf("riffler")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Riffler");
      }
      else if (name.indexOf("inlay")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Inlay");
      }
      else if (name.indexOf("enamel")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Enamel");
      }
      else if (name.indexOf("parchment")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Parchment");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.RUNE_KEEPER.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeWardenItems(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("105".equals(category))
    {
      item.setRequiredClass(CharacterClass.WARDEN);
      String name=item.getName().toLowerCase();
      if (name.indexOf("carving")!=-1)
      {
        item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        item.setSubCategory(CharacterClass.WARDEN.getLabel()+":Carving");
      }
      else if (name.indexOf("hymn")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.WARDEN.getLabel()+":Hymn");
      }
      else if (name.indexOf("muster")!=-1)
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.WARDEN.getLabel()+":Muster");
      }
      else
      {
        item.setEquipmentLocation(null);
        item.setSubCategory(CharacterClass.WARDEN.getLabel()+":Other");
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeWeapons(Item item)
  {
    item=setWeaponTypeFromCategory(item,"10",WeaponType.DAGGER);
    item=setWeaponTypeFromCategory(item,"36",WeaponType.HALBERD);
    item=setWeaponTypeFromCategory(item,"110",WeaponType.JAVELIN);
    item=setWeaponTypeFromCategory(item,"29",WeaponType.CROSSBOW);
    item=setWeaponTypeFromCategory(item,"46",WeaponType.SPEAR);
    //item=setWeaponTypeFromCategory(item,"12",WeaponType.ONE_HANDED_AXE);
    item=setWeaponTypeFromCategory(item,"One-handed Axe",WeaponType.ONE_HANDED_AXE);
    item=setWeaponTypeFromCategory(item,"Two-handed Axe",WeaponType.TWO_HANDED_AXE);
    item=setWeaponTypeFromCategory(item,"One-handed Sword",WeaponType.ONE_HANDED_SWORD);
    item=setWeaponTypeFromCategory(item,"Two-handed Sword",WeaponType.TWO_HANDED_SWORD);
    item=setWeaponTypeFromCategory(item,"One-handed Club",WeaponType.ONE_HANDED_CLUB);
    item=setWeaponTypeFromCategory(item,"Rune-stone",WeaponType.RUNE_STONE);
    item=setWeaponTypeFromCategory(item,"Staff",WeaponType.STAFF);
    item=setWeaponTypeFromCategory(item,"Dagger",WeaponType.DAGGER);
    if (item instanceof Weapon) {
      Weapon weapon=(Weapon)item;
      WeaponType type=weapon.getWeaponType();
      if (type!=null)
      {
        EquipmentLocation loc=type.isRanged()?EquipmentLocation.RANGED_ITEM:EquipmentLocation.MAIN_HAND;
        weapon.setEquipmentLocation(loc);
        item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        item.setSubCategory(null);
      }
    }
    
    return item;
  }

  private Item setWeaponTypeFromCategory(Item item, String category, WeaponType type)
  {
    Weapon ret=null;
    if ((category==null) || (category.equals(item.getSubCategory())))
    {
      if (item.getClass()==Item.class)
      {
        ret=new Weapon();
        ret.copyFrom(item);
      }
      else
      {
        ret=(Weapon)item;
      }
      WeaponType oldType=ret.getWeaponType();
      if (oldType==null)
      {
        ret.setWeaponType(type);
      }
      else
      {
        if (type!=oldType)
        {
          System.out.println("Conflict weapon type for ID:"+item.getIdentifier()+", name:"+item.getName()+": from:"+oldType+" to:"+type);
        }
      }
    }
    if (ret!=null)
    {
      if (type.isRanged())
      {
        ret.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
      }
      else
      {
        ret.setEquipmentLocation(EquipmentLocation.MAIN_HAND);
      }
    }
    return (ret!=null)?ret:item;
  }

  private Item normalizeMiscWeapons(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    String name=item.getName();
    if (name==null) return item;
    name=name.toLowerCase();
    WeaponType type=null;
    if (("1".equals(category)) || ("12".equals(category)) || ("24".equals(category))
        || ("30".equals(category)) || ("40".equals(category)) || ("44".equals(category)))
    {
      if (item instanceof Weapon)
      {
        type=((Weapon)item).getWeaponType();
      }
      if (type==null)
      {
        if ("12".equals(category))
        {
          if (name.indexOf("great axe")!=-1) type=WeaponType.TWO_HANDED_AXE;
          else type=WeaponType.ONE_HANDED_AXE;
        }
        else if ("1".equals(category))
        {
          if (name.indexOf("bow")!=-1) type=WeaponType.BOW;
          else if (name.indexOf("javelin")!=-1) type=WeaponType.JAVELIN;
          else type=null;
        }
        else if ("24".equals(category))
        {
          if (name.indexOf("mallet")!=-1) type=WeaponType.TWO_HANDED_HAMMER;
          else type=WeaponType.ONE_HANDED_HAMMER;
        }
        else if ("40".equals(category))
        {
          type=WeaponType.ONE_HANDED_CLUB;
        }
        else if ("30".equals(category))
        {
          if (name.indexOf("mace")!=-1) type=WeaponType.ONE_HANDED_MACE;
          else if (name.indexOf("hammer")!=-1) type=WeaponType.ONE_HANDED_HAMMER;
          else if (name.indexOf("club")!=-1) type=WeaponType.ONE_HANDED_CLUB;
          else if (name.indexOf("old reliable")!=-1) type=WeaponType.ONE_HANDED_CLUB;
        }
        else if ("44".equals(category))
        {
          if (name.indexOf("mace")!=-1) type=WeaponType.ONE_HANDED_MACE;
          else if (name.indexOf("hammer")!=-1) type=WeaponType.ONE_HANDED_HAMMER;
          else if (name.indexOf("club")!=-1) type=WeaponType.ONE_HANDED_CLUB;
          else if (name.indexOf("sword")!=-1) type=WeaponType.ONE_HANDED_SWORD;
          else if (name.indexOf("blade")!=-1) type=WeaponType.ONE_HANDED_SWORD;
          else if (name.indexOf("great axe")!=-1) type=WeaponType.TWO_HANDED_AXE;
          else if (name.indexOf("oathbreaker's bane")!=-1) type=WeaponType.ONE_HANDED_SWORD;
        }
      }
      if (type==null)
      {
        System.out.println("Weapon type not found: category:"+category+", name:"+name);
        type=WeaponType.OTHER;
      }
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      item=setWeaponTypeFromCategory(item,null,type);
    }
    return item;
  }

  private Item normalizeArmours(Item item)
  {
    int id=item.getIdentifier();
    String category=item.getSubCategory();
    Armour armour=null;
    if ("Heavy Armour".equals(category))
    {
      armour=checkArmour(item);
      ArmourType previous=armour.getArmourType();
      if ((previous!=null) && (previous!=ArmourType.HEAVY))
      {
        System.out.println("ID: " + id+": armour type conflict: was=" + previous + ", should be=" + ArmourType.HEAVY);
      }
      armour.setArmourType(ArmourType.HEAVY);
      armour.setSubCategory(null);
    }
    else if ("Medium Armour".equals(category))
    {
      armour=checkArmour(item);
      ArmourType previous=armour.getArmourType();
      if ((previous!=null) && (previous!=ArmourType.MEDIUM))
      {
        System.out.println("ID: " + id+": armour type conflict: was=" + previous + ", should be=" + ArmourType.MEDIUM);
      }
      armour.setArmourType(ArmourType.MEDIUM);
      armour.setSubCategory(null);
    }
    else if ("Light Armour".equals(category))
    {
      armour=checkArmour(item);
      ArmourType previous=armour.getArmourType();
      if ((previous!=null) && (previous!=ArmourType.LIGHT))
      {
        System.out.println("ID: " + id+": armour type conflict: was=" + previous + ", should be=" + ArmourType.LIGHT);
      }
      armour.setArmourType(ArmourType.LIGHT);
      armour.setSubCategory(null);
    }
    else if ("Shield".equals(category))
    {
      armour=checkArmour(item);
      ArmourType previous=armour.getArmourType();
      if ((previous==ArmourType.LIGHT) || (previous==null))
      {
        armour.setArmourType(ArmourType.SHIELD);
      }
      armour.setSubCategory(null);
      armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
      armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
    }
    else if ("Heavy Shield".equals(category))
    {
      armour=checkArmour(item);
      armour.setArmourType(ArmourType.HEAVY_SHIELD);
      armour.setSubCategory(null);
      armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
      armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
    }
    else if ("Warden's Shield".equals(category))
    {
      armour=checkArmour(item);
      armour.setArmourType(ArmourType.WARDEN_SHIELD);
      armour.setSubCategory(null);
      armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
      armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
    }
    else if ("33".equals(category))
    {
      armour=checkArmour(item);
      ArmourType type=armour.getArmourType();
      if (type==null)
      {
        String name=armour.getName();
        if (name.toLowerCase().contains("warden"))
        {
          armour.setArmourType(ArmourType.WARDEN_SHIELD);
        }
        else if ((name.indexOf("Heavy Shield")!=-1) || (name.indexOf("Bulwark")!=-1)
            || (name.indexOf("Battle-shield")!=-1))
        {
          armour.setArmourType(ArmourType.HEAVY_SHIELD);
        }
        else
        {
          // Default as shield
          armour.setArmourType(ArmourType.SHIELD);
        }
      }
      type=armour.getArmourType();
      if (type!=null)
      {
        armour.setSubCategory(null);
        armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
        armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        if (type==ArmourType.WARDEN_SHIELD)
        {
          armour.setRequiredClass(CharacterClass.WARDEN);
        }
      }
    }
    Item ret=(armour!=null)?armour:item;
    ret=normalizeArmour(ret, "3", "Chest", EquipmentLocation.CHEST);
    ret=normalizeArmour(ret, "5", "Hands", EquipmentLocation.HAND);
    ret=normalizeArmour(ret, "6", "Shoulders", EquipmentLocation.SHOULDER);
    ret=normalizeArmour(ret, "7", "Head", EquipmentLocation.HEAD);
    ret=normalizeArmour(ret, "15", "Leggings", EquipmentLocation.LEGS);
    ret=normalizeArmour(ret, "23", "Boots", EquipmentLocation.FEET);
    ret=normalizeArmour(ret, "45", "Cloak", EquipmentLocation.BACK);

    // Check shields
    //33 => {Warden's Shield=99, Shield=107, Heavy Shield=75}
    //=> Shield (any type)
    return ret;
  }

  private Armour checkArmour(Item item)
  {
    Armour ret=null;
    if (item instanceof Armour)
    {
      ret=(Armour)item;
    }
    else
    {
      ret=new Armour();
      ret.copyFrom(item);
    }
    return ret;
  }

  private Item normalizeArmour(Item item, String categoryInt, String expectedCategoryStr, EquipmentLocation loc)
  {
    Item ret=item;
    int id=item.getIdentifier();
    String categoryProp=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if (categoryInt.equals(categoryProp)) {
      Armour armour=checkArmour(item);
      ret=armour;
      // Location
      {
        EquipmentLocation previousLoc=ret.getEquipmentLocation();
        if (previousLoc==null)
        {
          armour.setEquipmentLocation(loc);
        }
        /* Previous value is probably good, so keep it...
        if ((previousLoc!=null) && (previousLoc!=loc))
        {
          String name=ret.getName();
          System.out.println("ID: " + id+"("+name+"): loc conflict: was=" + previousLoc + ", should be=" + loc);
        }
        armour.setEquipmentLocation(loc);
        */
      }
      // Sub category
      {
        String previousSubCategory=ret.getSubCategory();
        if ((previousSubCategory!=null) && (previousSubCategory.length()>0) && 
            (!previousSubCategory.equals(categoryInt)) && (!previousSubCategory.equalsIgnoreCase(expectedCategoryStr)))
        {
          System.out.println("ID: " + id+": armour category conflict: was=" + previousSubCategory + ", should be=" + expectedCategoryStr);
        }
        armour.setSubCategory(null);
      }
      armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return ret;
  }

  private Item normalizeCrafting(Item item)
  {
    Item ret=normalizeCrafting(item, "37");
    ret=normalizeCrafting(ret, "38");
    ret=normalizeCrafting(ret, "56");
    ret=normalizeCrafting(ret, "188");
    return ret;
  }

  private Item normalizeCrafting(Item item, String categoryInt)
  {
    Item ret=item;
    //int id=item.getIdentifier();
    String categoryProp=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if (categoryInt.equals(categoryProp)) {
      String previousSubCategory=ret.getSubCategory();
      if ((previousSubCategory!=null) && (previousSubCategory.length()>0) && 
          (!previousSubCategory.equals(categoryInt)))
      {
        //System.out.println("ID: " + id+": crafting category conflict: was=" + previousSubCategory);
      }
      ret.setSubCategory("Crafting Item");
      ret.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      ret.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return ret;
  }

  private Item normalizeJewels(Item item)
  {
    Item ret=item;
    //int id=item.getIdentifier();
    String categoryProp=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    String subCategory=item.getSubCategory();
    if (("49".equals(categoryProp)) || ("49".equals(subCategory))) {
      String previousSubCategory=ret.getSubCategory();
      //Pocket=103, Wrist=211, Ear=211, Neck=170, Finger=198
      if ("Pocket".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.POCKET);
        ret.setSubCategory(null);
      }
      else if ("Wrist".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.WRIST);
        ret.setSubCategory(null);
      }
      else if ("Ear".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.EAR);
        ret.setSubCategory(null);
      }
      else if ("Neck".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.NECK);
        ret.setSubCategory(null);
      }
      else if ("Finger".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.FINGER);
        ret.setSubCategory(null);
      }
      else
      {
        EquipmentLocation loc=ret.getEquipmentLocation();
        if (loc!=null)
        {
          ret.setSubCategory(null);
        }
        else
        {
          ret.setSubCategory("Jewelry");
          normalizeJewelByName(ret,"Token",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Earring",EquipmentLocation.EAR);
          normalizeJewelByName(ret,"Bauble",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Necklace",EquipmentLocation.NECK);
          normalizeJewelByName(ret,"Ring",EquipmentLocation.FINGER);
          normalizeJewelByName(ret,"Bracelet",EquipmentLocation.WRIST);
          normalizeJewelByName(ret,"Cuff",EquipmentLocation.WRIST);
          normalizeJewelByName(ret,"Barrow-brie",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Stone",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Pendant",EquipmentLocation.NECK);
          normalizeJewelByName(ret,"Choker",EquipmentLocation.NECK);
          normalizeJewelByName(ret,"Band ",EquipmentLocation.FINGER);
          normalizeJewelByName(ret,"Armlet",EquipmentLocation.WRIST);
          //normalizeJewelByName(ret,"Bangle of Deep Waters",EquipmentLocation.WRIST);
          normalizeJewelByName(ret,"Mountain-stone",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Scroll",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Phial",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Pocket-square",EquipmentLocation.POCKET);
          // Specific names for pocket items
          normalizeJewelByName(ret,"Carved Jewellery Box",EquipmentLocation.POCKET);
          String name=ret.getName();
          if ("Plains Walker's Symbol".equals(name))
          {
            ret.setRequiredClass(CharacterClass.BEORNING);
            ret.setEquipmentLocation(EquipmentLocation.POCKET);
            ret.setSubCategory(null);
          }
          else if ("Talisman of the Tundra Cub".equals(name))
          {
            ret.setRequiredClass(CharacterClass.LORE_MASTER);
            ret.setEquipmentLocation(null);
            ret.setSubCategory(CharacterClass.LORE_MASTER.getLabel()+":Talisman");
          }
        }
      }
      ret.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      ret.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
      if (ret instanceof Armour)
      {
        Armour armour=(Armour)ret;
        int armourValue=armour.getArmourValue();
        ret.getStats().setStat(STAT.ARMOUR,new FixedDecimalsInteger(armourValue));
        ret.setCategory(ItemCategory.ITEM);
      }
    }
    return ret;
  }

  private void normalizeJewelByName(Item item, String pattern, EquipmentLocation loc)
  {
    String name=item.getName();
    if (name.contains(pattern))
    {
      item.setEquipmentLocation(loc);
      item.setSubCategory(null);
    }
  }

  private static final String[] PROFESSIONS = {"Prospector", "Farmer", "Forester", "Woodworker", "Cook", "Jeweller", "Metalsmith", "Weaponsmith", "Tailor", "Scholar" };

  private static int[][] CRAFTING_CATEGORIES = {
    { 60, 61, 63, 67, 65, 59, 64, 66, 58, 62},
    { 134, 162, 122, 147, 113, 143, 141, 148, 155, 139},
    { 135, 121, 127, 152, 114, 146, 144, 153, 160, 140},
    { 136, 126, 130, 157, 115, 151, 149, 158, 120, 142},
    { 137, 129, 132, 118, 116, 156, 154, 119, 125, 145},
    { 138, 131, 133, 123, 117, 161, 159, 124, 128, 150},
    { 202, 200, 201, 197, 203, 196, 195, 198, 199, 204},
    { 215, 213, 214, 210, 216, 209, 208, 211, 212, 217},
    { 228, 226, 227, 223, 229, 222, 221, 224, 225, 220}
  };

  private Item normalizeRecipes(Item item)
  {
    boolean found=false;
    for(int i=0;i<CRAFTING_CATEGORIES.length;i++)
    {
      for(int j=0;j<CRAFTING_CATEGORIES[0].length;j++)
      {
        String craftingCategory=String.valueOf(CRAFTING_CATEGORIES[i][j]);
        String subCategory=item.getSubCategory();
        if (craftingCategory.equals(subCategory))
        {
          String newCategory="Recipe:"+PROFESSIONS[j]+":Tier"+(i+1);
          item.setSubCategory(newCategory);
          item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
          item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
          // Patch for badly named beorning carving recipes
          if ("Woodworker".equals(PROFESSIONS[j]))
          {
            String name=item.getName();
            if (name.indexOf("Beorning")!=-1)
            {
              if (!name.endsWith("Recipe"))
              {
                name=name+" Recipe";
                item.setName(name);
              }
            }
          }
          // Patch for badly named bridle recipes
          if ("Tailor".equals(PROFESSIONS[j]))
          {
            String name=item.getName();
            if (name.indexOf("Bridle")!=-1)
            {
              if (!name.endsWith("Recipe"))
              {
                name=name+" Recipe";
                item.setName(name);
              }
            }
          }
          found=true;
          break;
        }
      }
      if (found)
      {
        break;
      }
    }
    return item;
  }

  private Item normalizeLegendaryItem(Item item)
  {
    String name=item.getName();
    ItemQuality quality=null;
    if (name==null) return item;
    if (name.endsWith("of the First Age")) quality=ItemQuality.LEGENDARY;
    else if (name.endsWith("of the Second Age")) quality=ItemQuality.INCOMPARABLE;
    else if (name.endsWith("of the Third Age")) quality=ItemQuality.RARE;
    if (quality!=null)
    {
      CharacterClass cClass=null;
      if (name.indexOf("Champion")!=-1) cClass=CharacterClass.CHAMPION;
      else if (name.indexOf("Captain")!=-1) cClass=CharacterClass.CAPTAIN;
      else if (name.indexOf("Beorning")!=-1) cClass=CharacterClass.BEORNING;
      else if (name.indexOf("Burglar")!=-1) cClass=CharacterClass.BURGLAR;
      else if (name.indexOf("Guardian")!=-1) cClass=CharacterClass.GUARDIAN;
      else if (name.indexOf("Hunter")!=-1) cClass=CharacterClass.HUNTER;
      else if (name.indexOf("Lore-master")!=-1) cClass=CharacterClass.LORE_MASTER;
      else if (name.indexOf("Minstrel")!=-1) cClass=CharacterClass.MINSTREL;
      else if (name.indexOf("Rune-keeper")!=-1) cClass=CharacterClass.RUNE_KEEPER;
      else if (name.indexOf("Warden")!=-1) cClass=CharacterClass.WARDEN;

      WeaponType weaponType=null;
      String classItemType=null;
      boolean bridle=false;
      if (name.indexOf("Great Club")!=-1) weaponType=WeaponType.TWO_HANDED_CLUB;
      else if (name.indexOf("Great Axe")!=-1) weaponType=WeaponType.TWO_HANDED_AXE;
      else if (name.indexOf("Great Sword")!=-1) weaponType=WeaponType.TWO_HANDED_SWORD;
      else if (name.indexOf("Greatsword")!=-1) weaponType=WeaponType.TWO_HANDED_SWORD;
      else if (name.indexOf("Great Hammer")!=-1) weaponType=WeaponType.TWO_HANDED_HAMMER;
      else if (name.indexOf("Halberd")!=-1) weaponType=WeaponType.HALBERD;
      else if (name.indexOf("Club")!=-1) weaponType=WeaponType.ONE_HANDED_CLUB;
      else if (name.indexOf("Axe")!=-1) weaponType=WeaponType.ONE_HANDED_AXE;
      else if (name.indexOf("Sword")!=-1) weaponType=WeaponType.ONE_HANDED_SWORD;
      else if (name.indexOf("Hammer")!=-1) weaponType=WeaponType.ONE_HANDED_HAMMER;
      else if (name.indexOf("Dagger")!=-1) weaponType=WeaponType.DAGGER;
      else if (name.indexOf("Spear")!=-1) weaponType=WeaponType.SPEAR;
      else if (name.indexOf("Mace")!=-1) weaponType=WeaponType.ONE_HANDED_MACE;
      else if (name.indexOf("Rune-stone")!=-1)
      {
        weaponType=WeaponType.RUNE_STONE;
        cClass=CharacterClass.RUNE_KEEPER;
      }
      else if (name.indexOf("Stone")!=-1)
      {
        weaponType=WeaponType.RUNE_STONE;
        cClass=CharacterClass.RUNE_KEEPER;
      }
      else if (name.indexOf("Staff")!=-1) weaponType=WeaponType.STAFF;
      else if (name.indexOf("Carving")!=-1) classItemType="Carving";
      else if (name.indexOf("Tools")!=-1) classItemType="Tools";
      else if (name.indexOf("Emblem")!=-1) classItemType="Emblem";
      else if (name.indexOf("Rune-satchel")!=-1) classItemType="Rune-satchel";
      else if (name.indexOf("Rune")!=-1) classItemType="Rune";
      else if (name.indexOf("Belt")!=-1) classItemType="Belt";
      else if (name.indexOf("Crossbow")!=-1) weaponType=WeaponType.CROSSBOW;
      else if (name.indexOf("Bow")!=-1) weaponType=WeaponType.BOW;
      else if (name.indexOf("Book")!=-1) classItemType="Book";
      else if (name.indexOf("Songbook")!=-1) classItemType="Songbook";
      else if (name.indexOf("Javelin")!=-1) weaponType=WeaponType.JAVELIN;
      else if (name.indexOf("Bridle")!=-1) bridle=true;

      if ((weaponType!=null) || (classItemType!=null) || (bridle))
      {
        item.setQuality(quality);
        item.setRequiredClass(cClass);
        String category=null;
        if (bridle)
        {
          category="Bridle";
          item.setCategory(ItemCategory.LEGENDARY_ITEM);
          item.setEquipmentLocation(EquipmentLocation.BRIDLE);
        }
        else if (classItemType!=null)
        {
          category=cClass.getLabel()+":"+classItemType;
          item.setCategory(ItemCategory.LEGENDARY_ITEM);
          item.setEquipmentLocation(EquipmentLocation.CLASS_SLOT);
        }
        else if (weaponType!=null)
        {
          LegendaryWeapon weapon;
          if (item instanceof LegendaryWeapon)
          {
            weapon=(LegendaryWeapon)item;
          }
          else
          {
            weapon=new LegendaryWeapon();
            weapon.copyFrom(item);
            item=weapon;
          }
          EquipmentLocation location=weaponType.isRanged()?EquipmentLocation.RANGED_ITEM:EquipmentLocation.MAIN_HAND;
          weapon.setEquipmentLocation(location);
          weapon.setWeaponType(weaponType);
        }
        item.setSubCategory(category);
        item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        //System.out.println(name+", class="+cClass+", category="+category+", weapon type="+type);
      }
    }
    return item;
  }

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemNormalization().doIt();
  }
}
