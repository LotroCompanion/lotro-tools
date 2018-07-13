package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on armour sets from the 'Master of Mordor Lore'.
 * @author DAM
 */
public class GorgorothScoutGearRewardsVendor
{
  private static final String ARMOR_COMMENT="Master of Mordor Lore (Gorgoroth Scout's Gear Rewards Vendor) ; Abyss of Mordath Tier 2 ; 2500 ash";
  private static final Integer MIN_LEVEL=Integer.valueOf(115);

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public GorgorothScoutGearRewardsVendor(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doBeorning();
    doBurglar();
    doHunter();
    doWarden();
  }

  private void doBeorning()
  {
    // Changeling's armour
    {
      // 1879362530 Changeling's Boots of the Abyss
      // 1879362532 Changeling's Camail of the Abyss
      // 1879362533 Changeling's Gages of the Abyss
      // 1879362534 Changeling's Leggings of the Abyss
      // 1879362536 Changeling's Hauberk of the Abyss
      int[] items=new int[]{ 1879362530, 1879362532, 1879362533, 1879362534, 1879362536 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BEORNING,items);
    }
    // Berserker's armour
    {
      // 1879362564 Berserker's Boots of the Abyss
      // 1879362566 Berserker's Leggings of the Abyss
      // 1879362569 Berserker's Camail of the Abyss
      // 1879362571 Berserker's Gages of the Abyss
      // 1879362572 Berserker's Hauberk of the Abyss
      int[] items=new int[]{ 1879362564, 1879362566, 1879362569, 1879362571, 1879362572 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BEORNING,items);
    }
    // Weald-guard's armour
    {
      // 1879362563 Weald-guard's Camail of the Abyss
      // 1879362567 Weald-guard's Gages of the Abyss
      // 1879362568 Weald-guard's Leggings of the Abyss
      // 1879362570 Weald-guard's Hauberk of the Abyss
      // 1879362573 Weald-guard's Boots of the Abyss
      int[] items=new int[]{ 1879362563, 1879362567, 1879362568, 1879362570, 1879362573 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BEORNING,items);
    }
  }

  private void doBurglar()
  {
    // Gambler's armour
    {
      // 1879362610 Gambler's Leggings of the Abyss
      // 1879362675 Gambler's Hauberk of the Abyss
      // 1879362698 Gambler's Camail of the Abyss
      // 1879362699 Gambler's Boots of the Abyss
      // 1879362723 Gambler's Gages of the Abyss
      int[] items=new int[]{ 1879362610, 1879362675, 1879362698, 1879362699, 1879362723 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BURGLAR,items);
    }
    // Blade's armour
    {
      // 1879362605 Blade's Hauberk of the Abyss
      // 1879362639 Blade's Boots of the Abyss
      // 1879362651 Blade's Leggings of the Abyss
      // 1879362667 Blade's Camail of the Abyss
      // 1879362707 Blade's Gages of the Abyss
      int[] items=new int[]{ 1879362605, 1879362639, 1879362651, 1879362667, 1879362707 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BURGLAR,items);
    }

    // Troublemaker's armour
    {
      // 1879362614 Troublemaker's Gages of the Abyss
      // 1879362618 Troublemaker's Hauberk of the Abyss
      // 1879362641 Troublemaker's Leggings of the Abyss
      // 1879362655 Troublemaker's Camail of the Abyss
      // 1879362661 Troublemaker's Boots of the Abyss
      int[] items=new int[]{ 1879362614, 1879362618, 1879362641, 1879362655, 1879362661 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.BURGLAR,items);
    }
  }

  private void doHunter()
  {
    // Stalker's armour
    {
      // 1879362625 Stalker's Hauberk of the Abyss
      // 1879362645 Stalker's Boots of the Abyss
      // 1879362689 Stalker's Gages of the Abyss
      // 1879362701 Stalker's Leggings of the Abyss
      // 1879362715 Stalker's Camail of the Abyss
      int[] items=new int[]{ 1879362625, 1879362645, 1879362689, 1879362701, 1879362715 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.HUNTER,items);
    }
    // Archer's armour
    {
      // 1879362636 Archer's Leggings of the Abyss
      // 1879362643 Archer's Hauberk of the Abyss
      // 1879362670 Archer's Camail of the Abyss
      // 1879362738 Archer's Boots of the Abyss
      // 1879362747 Archer's Gages of the Abyss
      int[] items=new int[]{ 1879362636, 1879362643, 1879362670, 1879362738, 1879362747 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.HUNTER,items);
    }

    // Trapper's armour
    {
      // 1879362592 Trapper's Boots of the Abyss
      // 1879362616 Trapper's Hauberk of the Abyss
      // 1879362638 Trapper's Gages of the Abyss
      // 1879362713 Trapper's Camail of the Abyss
      // 1879362741 Trapper's Leggings of the Abyss
      int[] items=new int[]{ 1879362592, 1879362616, 1879362638, 1879362713, 1879362741 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.HUNTER,items);
    }
  }

  private void doWarden()
  {
    // Stalwart armour
    {
      // 1879362635 Stalwart Hauberk of the Abyss
      // 1879362654 Stalwart Camail of the Abyss
      // 1879362677 Stalwart Gages of the Abyss
      // 1879362679 Stalwart Leggings of the Abyss
      // 1879362712 Stalwart Boots of the Abyss
      int[] items=new int[]{ 1879362635, 1879362654, 1879362677, 1879362679, 1879362712 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.WARDEN,items);
    }
    // Strident armour
    {
      // 1879362601 Strident Hauberk of the Abyss
      // 1879362687 Strident Boots of the Abyss
      // 1879362710 Strident Camail of the Abyss
      // 1879362718 Strident Leggings of the Abyss
      // 1879362724 Strident Gages of the Abyss
      int[] items=new int[]{ 1879362601, 1879362687, 1879362710, 1879362718, 1879362724 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.WARDEN,items);
    }

    // Lancer's armour
    {
      // 1879362603 Lancer's Camail of the Abyss
      // 1879362608 Lancer's Hauberk of the Abyss
      // 1879362632 Lancer's Boots of the Abyss
      // 1879362665 Lancer's Leggings of the Abyss
      // 1879362696 Lancer's Gages of the Abyss
      int[] items=new int[]{ 1879362603, 1879362608, 1879362632, 1879362665, 1879362696 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.MEDIUM,items);
      _injector.injectCharacterClass(CharacterClass.WARDEN,items);
    }
  }
}
