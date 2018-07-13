package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on armour sets from the 'Master of Mordor Lore (Gorgoroth Warrior's Gear Rewards Vendor)'.
 * @author DAM
 */
public class GorgorothWarriorGearRewardsVendor
{
  private static final String ARMOR_COMMENT="Master of Mordor Lore (Gorgoroth Warrior's Gear Rewards Vendor) ; Abyss of Mordath Tier 2 ; 2500 ash";
  private static final Integer MIN_LEVEL=Integer.valueOf(115);

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public GorgorothWarriorGearRewardsVendor(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doCaptain();
    doChampion();
    doGuardian();
  }

  private void doCaptain()
  {
    // Surgeon's armour
    {
      // 1879362611 Surgeon's Greaves of the Abyss
      // 1879362621 Surgeon's Gauntlets of the Abyss
      // 1879362695 Surgeon's Sabatons of the Abyss
      // 1879362706 Surgeon's Chestplate of the Abyss
      // 1879362729 Surgeon's Pauldrons of the Abyss
      int[] items=new int[]{ 1879362611, 1879362621, 1879362695, 1879362706, 1879362729 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.CAPTAIN,items);
    }
    // Charger's armour
    {
      // 1879362597 Charger's Chestplate of the Abyss
      // 1879362613 Charger's Pauldrons of the Abyss
      // 1879362676 Charger's Sabatons of the Abyss
      // 1879362733 Charger's Greaves of the Abyss
      // 1879362734 Charger's Gauntlets of the Abyss
      int[] items=new int[]{ 1879362597, 1879362613, 1879362676, 1879362733, 1879362734 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.CAPTAIN,items);
    }
    // Leader's armour
    {
      // 1879362591 Leader's Greaves of the Abyss
      // 1879362612 Leader's Chestplate of the Abyss
      // 1879362657 Leader's Gauntlets of the Abyss
      // 1879362728 Leader's Sabatons of the Abyss
      // 1879362748 Leader's Pauldrons of the Abyss
      int[] items=new int[]{ 1879362591, 1879362612, 1879362657, 1879362728, 1879362748 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.CAPTAIN,items);
    }
  }

  private void doChampion()
  {
    // Swordsman's armour
    {
      // 1879362596 Swordsman's Pauldrons of the Abyss
      // 1879362646 Swordsman's Sabatons of the Abyss
      // 1879362649 Swordsman's Greaves of the Abyss
      // 1879362650 Swordsman's Gauntlets of the Abyss
      // 1879362727 Swordsman's Chestplate of the Abyss
      int[] items=new int[]{ 1879362596, 1879362646, 1879362649, 1879362650, 1879362727 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.CHAMPION,items);
    }
    // Rampaging armour
    {
      // 1879362594 Rampaging Greaves of the Abyss
      // 1879362624 Rampaging Pauldrons of the Abyss
      // 1879362674 Rampaging Sabatons of the Abyss
      // 1879362680 Rampaging Chestplate of the Abyss
      // 1879362700 Rampaging Gauntlets of the Abyss
      int[] items=new int[]{ 1879362594, 1879362624, 1879362674, 1879362680, 1879362700 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.CHAMPION,items);
    }
    // Scything armour
    {
      // 1879362629 Scything Gauntlets of the Abyss
      // 1879362631 Scything Greaves of the Abyss
      // 1879362663 Scything Chestplate of the Abyss
      // 1879362684 Scything Sabatons of the Abyss
      // 1879362736 Scything Pauldrons of the Abyss
      int[] items=new int[]{ 1879362629, 1879362631, 1879362663, 1879362684, 1879362736 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.CHAMPION,items);
    }
  }

  private void doGuardian()
  {
    // Defender's armour
    {
      // 1879362627 Defender's Greaves of the Abyss
      // 1879362666 Defender's Pauldrons of the Abyss
      // 1879362681 Defender's Sabatons of the Abyss
      // 1879362704 Defender's Gauntlets of the Abyss
      // 1879362742 Defender's Chestplate of the Abyss
      int[] items=new int[]{ 1879362627, 1879362666, 1879362681, 1879362704, 1879362742 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.GUARDIAN,items);
    }
    // Savage armour
    {
      // 1879362598 Savage Sabatons of the Abyss
      // 1879362658 Savage Pauldrons of the Abyss
      // 1879362664 Savage Greaves of the Abyss
      // 1879362685 Savage Gauntlets of the Abyss
      // 1879362731 Savage Chestplate of the Abyss
      int[] items=new int[]{ 1879362598, 1879362658, 1879362664, 1879362685, 1879362731 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.GUARDIAN,items);
    }
    // Shining armour
    {
      // 1879362617 Shining Sabatons of the Abyss
      // 1879362671 Shining Gauntlets of the Abyss
      // 1879362709 Shining Greaves of the Abyss
      // 1879362735 Shining Pauldrons of the Abyss
      // 1879362744 Shining Chestplate of the Abyss
      int[] items=new int[]{ 1879362617, 1879362671, 1879362709, 1879362735, 1879362744 };
      _injector.injectComment(ARMOR_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
      _injector.injectArmourType(ArmourType.HEAVY,items);
      _injector.injectCharacterClass(CharacterClass.GUARDIAN,items);
    }
  }
}
