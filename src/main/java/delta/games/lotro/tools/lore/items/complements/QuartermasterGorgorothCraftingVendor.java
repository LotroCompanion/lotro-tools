package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on crafted items from recipes of the 'Quartermaster (Gorgoroth Crafting Vendor)'.
 * @author DAM
 */
public class QuartermasterGorgorothCraftingVendor
{
  private static final String RECIPE_NAME = "RECIPE_NAME";
  private static final String ARMOR_COMMENT="Single use recipe \"" + RECIPE_NAME +"\" at Quartermaster (Gorgoroth Crafting Vendor) (100 Signets of Thandrim + 1 Fragment of the Abyss)"; 
  private static final Integer MIN_LEVEL=Integer.valueOf(115);

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public QuartermasterGorgorothCraftingVendor(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doTailor();
  }

  private void doTailor()
  {
    // Lore-master
    {
      // 1879364638 Beast-master's Cap of the Abyss
      // 1879364662 Storm-caller's Cap of the Abyss
      // 1879364652 Librarian's Cap of the Abyss
      int[] items=new int[]{ 1879364638, 1879364662, 1879364652 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Lore-master Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      // TODO scaling data (standard/crit)
    }
    // Minstrel
    {
      // 1879364650 Troubador's Cap of the Abyss
      // 1879364642 Chanter's Cap of the Abyss
      // 1879364631 Storyteller's Cap of the Abyss
      int[] items=new int[]{ 1879364650, 1879364642, 1879364631 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Minstrel Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      // TODO scaling data (standard/crit)
    }
    // Rune-keeper
    {
      // 1879364649 Enlightened Cap of the Abyss
      // 1879364648 Infernal Cap of the Abyss
      // 1879364660 Striking Cap of the Abyss
      int[] items=new int[]{ 1879364649, 1879364648, 1879364660 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Rune-keeper Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      // TODO scaling data (standard/crit)
    }
    // Beorning
    {
      // 1879364661 Changeling's Coif of the Abyss
      // 1879364639 Berserker's Coif of the Abyss
      // 1879364632 Weald-guard's Coif of the Abyss
      int[] items=new int[]{ 1879364661, 1879364639, 1879364632 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Beorning Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BEORNING,items);
      // TODO scaling data (standard/crit)
    }
    // Burglar
    {
      // 1879364635 Gambler's Coif of the Abyss
      // 1879364645 Blade's Coif of the Abyss
      // 1879364653 Troublemaker's Coif of the Abyss
      int[] items=new int[]{ 1879364635, 1879364645, 1879364653 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Burglar Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      // TODO scaling data (standard/crit)
    }
    // Hunter
    {
      // 1879364637 Stalker's Coif of the Abyss
      // 1879364629 Archer's Coif of the Abyss
      // 1879364655 Trapper's Coif of the Abyss
      int[] items=new int[]{ 1879364637, 1879364629, 1879364655 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Hunter Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      // TODO scaling data (standard/crit)
    }
    // Warden
    {
      // 1879364651 Stalwart Coif of the Abyss
      // 1879364634 Strident Coif of the Abyss
      // 1879364636 Lancer's Coif of the Abyss
      int[] items=new int[]{ 1879364651, 1879364634, 1879364636 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Warden Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      // TODO scaling data (standard/crit)
    }
    // Captain
    {
      // 1879364643 Surgeon's Helm of the Abyss
      // 1879364646 Charger's Helm of the Abyss
      // 1879364656 Leader's Helm of the Abyss
      int[] items=new int[]{ 1879364643, 1879364646, 1879364656 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Captain Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      // TODO scaling data (standard/crit)
    }
    // Champion
    {
      // 1879364633 Swordsman's Helm of the Abyss
      // 1879364657 Rampaging Helm of the Abyss
      // 1879364659 Scything Helm of the Abyss
      int[] items=new int[]{ 1879364633, 1879364657, 1879364659 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Champion Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      // TODO scaling data (standard/crit)
    }
    // Guardian
    {
      // 1879364640 Defender's Helm of the Abyss
      // 1879364644 Savage Helm of the Abyss
      // 1879364641 Shining Helm of the Abyss
      int[] items=new int[]{ 1879364640, 1879364644, 1879364641 };
      _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Guardian Set Helms Recipe"),items);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      // TODO scaling data (standard/crit)
    }
  }
}
