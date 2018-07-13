package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on crafted items from recipes of the 'Conquest Of Gorgoroth Crafting' quartermaster.
 * @author DAM
 */
public class ConquestOfGorgorthCrafting
{
  private static final String RECIPE_NAME = "RECIPE_NAME";
  private static final String ARMOR_COMMENT="Single use recipe \"" + RECIPE_NAME +"\" at Quartermaster (Gorgoroth Crafting Vendor) (100 Signets of Thandrim + 1 Fragment of the Abyss)"; 
  private static final Integer MIN_LEVEL=Integer.valueOf(115);

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public ConquestOfGorgorthCrafting(FactoryCommentsInjector injector)
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
    // 1879364661 Changeling's Coif of the Abyss
    // 1879364639 Berserker's Coif of the Abyss
    // 1879364632 Weald-guard's Coif of the Abyss

    // Burglar
    // 1879364635 Gambler's Coif of the Abyss
    // 1879364645 Blade's Coif of the Abyss
    // 1879364653 Troublemaker's Coif of the Abyss

    // Hunter
    // 1879364637 Stalker's Coif of the Abyss
    // 1879364629 Archer's Coif of the Abyss
    // 1879364655 Trapper's Coif of the Abyss

    // Warden
    // 1879364651 Stalwart Coif of the Abyss
    // 1879364634 Strident Coif of the Abyss
    // 1879364636 Lancer's Coif of the Abyss
  }
}
