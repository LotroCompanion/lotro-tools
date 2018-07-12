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
    doLoreMaster();
    doMinstrel();
    doRuneKeeper();
  }

  private void doLoreMaster()
  {
    // 1879364638 Beast-master's Cap of the Abyss
    // 1879364662 Storm-caller's Cap of the Abyss
    // 1879364652 Librarian's Cap of the Abyss
    int[] items=new int[]{ 1879364638, 1879364662, 1879364652 };
    _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Lore-master Set Helms Recipe"),items);
    _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
    _injector.injectMinLevel(MIN_LEVEL,items);
    _injector.injectArmourType(ArmourType.LIGHT,items);
  }

  private void doMinstrel()
  {
    // 1879364650 Troubador's Cap of the Abyss
    // 1879364642 Chanter's Cap of the Abyss
    // 1879364631 Storyteller's Cap of the Abyss
    int[] items=new int[]{ 1879364650, 1879364642, 1879364631 };
    _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Minstrel Set Helms Recipe"),items);
    _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
    _injector.injectMinLevel(MIN_LEVEL,items);
    _injector.injectArmourType(ArmourType.LIGHT,items);
  }

  private void doRuneKeeper()
  {
    // 1879364649 Enlightened Cap of the Abyss
    // 1879364648 Infernal Cap of the Abyss
    // 1879364660 Striking Cap of the Abyss
    int[] items=new int[]{ 1879364649, 1879364648, 1879364660 };
    _injector.injectComment(ARMOR_COMMENT.replace(RECIPE_NAME,"Rune-keeper Set Helms Recipe"),items);
    _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,items);
    _injector.injectMinLevel(MIN_LEVEL,items);
    _injector.injectArmourType(ArmourType.LIGHT,items);
  }
}
