package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on armour sets from the 'Master of Mordor Lore'.
 * @author DAM
 */
public class MasterOfMordorLore
{
  private static final String ARMOR_COMMENT="Master of Mordor Lore ; Abyss of Mordath Tier 2 ; 2500 ash";
  private static final Integer MIN_LEVEL=Integer.valueOf(115);

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MasterOfMordorLore(FactoryCommentsInjector injector)
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
    // Beast-master's armour
    {
      // 1879362633 Beast-master's Trousers of the Abyss
      // 1879362640 Beast-master's Gloves of the Abyss
      // 1879362720 Beast-master's Mantle of the Abyss
      // 1879362722 Beast-master's Shoes of the Abyss
      // 1879362725 Beast-master's Waistcoat of the Abyss
      // 1879364638 Beast-master's Cap of the Abyss
      int[] items=new int[]{ 1879362633, 1879362640, 1879362720, 1879362722, 1879362725, 1879364638 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.LORE_MASTER,items);
    }
    // Storm-caller's armour
    {
      // 1879362593 Storm-caller's Trousers of the Abyss
      // 1879362634 Storm-caller's Shoes of the Abyss
      // 1879362647 Storm-caller's Mantle of the Abyss
      // 1879362653 Storm-caller's Gloves of the Abyss
      // 1879362743 Storm-caller's Waistcoat of the Abyss
      // 1879364662 Storm-caller's Cap of the Abyss
      int[] items=new int[]{ 1879362593, 1879362634, 1879362647, 1879362653, 1879362743, 1879364662 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.LORE_MASTER,items);
    }
    // Librarian's armour
    {
      // 1879362600 Librarian's Waistcoat of the Abyss
      // 1879362622 Librarian's Shoes of the Abyss
      // 1879362682 Librarian's Trousers of the Abyss
      // 1879362703 Librarian's Gloves of the Abyss
      // 1879362746 Librarian's Mantle of the Abyss
      // 1879364652 Librarian's Cap of the Abyss
      int[] items=new int[]{ 1879362600, 1879362622, 1879362682, 1879362703, 1879362746, 1879364652 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.LORE_MASTER,items);
    }
  }

  private void doMinstrel()
  {
    // Troubador's armour
    {
      // 1879362599 Troubador's Waistcoat of the Abyss
      // 1879362619 Troubador's Shoes of the Abyss
      // 1879362672 Troubador's Gloves of the Abyss
      // 1879362714 Troubador's Trousers of the Abyss
      // 1879362751 Troubador's Mantle of the Abyss
      // 1879364650 Troubador's Cap of the Abyss
      int[] items=new int[]{ 1879362599, 1879362619, 1879362672, 1879362714, 1879362751, 1879364650 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.MINSTREL,items);
    }
    // Chanter's armour
    {
      // 1879362595 Chanter's Waistcoat of the Abyss
      // 1879362620 Chanter's Trousers of the Abyss
      // 1879362626 Chanter's Shoes of the Abyss
      // 1879362691 Chanter's Gloves of the Abyss
      // 1879362719 Chanter's Mantle of the Abyss
      // 1879364642 Chanter's Cap of the Abyss
      int[] items=new int[]{ 1879362595, 1879362620, 1879362626, 1879362691, 1879362719, 1879364642 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.MINSTREL,items);
    }

    // Storyteller's armour
    {
      // 1879362659 Storyteller's Trousers of the Abyss
      // 1879362669 Storyteller's Shoes of the Abyss
      // 1879362686 Storyteller's Gloves of the Abyss
      // 1879362690 Storyteller's Waistcoat of the Abyss
      // 1879362732 Storyteller's Mantle of the Abyss
      // 1879364631 Storyteller's Cap of the Abyss
      int[] items=new int[]{ 1879362659, 1879362669, 1879362686, 1879362690, 1879362732, 1879364631 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.MINSTREL,items);
    }
  }

  private void doRuneKeeper()
  {
    // Enlightened armour
    {
      // 1879362642 Enlightened Trousers of the Abyss
      // 1879362648 Enlightened Waistcoat of the Abyss
      // 1879362656 Enlightened Mantle of the Abyss
      // 1879362662 Enlightened Shoes of the Abyss
      // 1879362692 Enlightened Gloves of the Abyss
      // 1879364649 Enlightened Cap of the Abyss
      int[] items=new int[]{ 1879362642, 1879362648, 1879362656, 1879362662, 1879362692, 1879364649 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.RUNE_KEEPER,items);
    }
    // Infernal armour
    {
      // 1879362590 Infernal Gloves of the Abyss
      // 1879362673 Infernal Trousers of the Abyss
      // 1879362678 Infernal Mantle of the Abyss
      // 1879362693 Infernal Waistcoat of the Abyss
      // 1879362716 Infernal Shoes of the Abyss
      // 1879364648 Infernal Cap of the Abyss
      int[] items=new int[]{ 1879362590, 1879362673, 1879362678, 1879362693, 1879362716, 1879364648 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.RUNE_KEEPER,items);
    }

    // Striking armour
    {
      // 1879362637 Striking Mantle of the Abyss
      // 1879362702 Striking Trousers of the Abyss
      // 1879362726 Striking Shoes of the Abyss
      // 1879362740 Striking Gloves of the Abyss
      // 1879362750 Striking Waistcoat of the Abyss
      // 1879364660 Striking Cap of the Abyss
      int[] items=new int[]{ 1879362637, 1879362702, 1879362726, 1879362740, 1879362750, 1879364660 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.LIGHT,items);
      //_injector.injectClass(CharacterClass.RUNE_KEEPER,items);
    }
  }
}
