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
  private static final String INCOMPARABLE_WEAPONS_COMMENT="Master of Mordor Lore (Gorgoroth Warrior's Gear Rewards Vendor) ; Abyss of Mordath Tier 2 ; 1750 ash";
  private static final String RARE_COMMENT="Master of Mordor Lore (Gorgoroth Warrior's Gear Rewards Vendor) ; Abyss of Mordath Tier 1 ; 700 ash";
  private static final String JEWELS_COMMENT="Master of Mordor Lore (Gorgoroth Warrior's/Scout's/Sage's Gear Rewards Vendor) ; Abyss of Mordath Tier 1 ; 2500 ash";
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
    doJewels();
    doArmours();
    doWeapons();
  }

  private void doJewels()
  {
    // 1879365756 Earring of Spring's Arrival
    // 1879365757 Earring of Spring's Promise
    // 1879365755 Earring of Spring's Rite
    int[] items=new int[]{ 1879365756, 1879365757, 1879365755 };
    _injector.injectComment(JEWELS_COMMENT,items);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
    _injector.injectMinLevel(MIN_LEVEL,items);
  }

  private void doArmours()
  {
    doCaptainArmour();
    doChampionArmour();
    doGuardianArmour();
    doRareArmour();
  }

  private void doCaptainArmour()
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

  private void doChampionArmour()
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

  private void doGuardianArmour()
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

  private void doRareArmour()
  {
    // 1879364269 Reinforced Chestplate of the Wyrm
    // 1879364258 Bolstered Pauldrons of the Wyrm
    // 1879364189 Hardened Helm of the Wyrm
    // 1879364255 Strong Gauntlets of the Wyrm
    // 1879364254 Tough Greaves of the Wyrm
    // 1879364292 Thick Sabatons of the Wyrm
    // 1879364268 Reinforced Chestplate of the Abyss
    // 1879364218 Bolstered Pauldrons of the Abyss
    // 1879364264 Hardened Helm of the Abyss
    // 1879364213 Strong Gauntlets of the Abyss
    // 1879364238 Tough Greaves of the Abyss
    // 1879364243 Thick Sabatons of the Abyss
    int[] items=new int[]{ 1879364269, 1879364258, 1879364189, 1879364255, 1879364254, 1879364292,
        1879364268, 1879364218, 1879364264, 1879364213, 1879364238, 1879364243 };
    _injector.injectComment(RARE_COMMENT,items);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
    _injector.injectMinLevel(MIN_LEVEL,items);
    _injector.injectArmourType(ArmourType.HEAVY,items);
    for(int i=0;i<items.length;i++)
    {
      _injector.shareStats(items[i]);
    }
  }

  private void doWeapons()
  {
    doIncomparableWeapons();
    doRareWeapons();
  }

  private void doIncomparableWeapons()
  {
    // Abyss
    {
      // 1879360481 Powerful Greatbow of the Abyss
      // 1879360434 Powerful Bow of the Abyss
      // 1879360421 Sharp Dagger of the Abyss
      // 1879360519 Sharp Skean of the Abyss
      // 1879360499 Brutal Axe of the Abyss
      // 1879360417 Brutal Double Axe of the Abyss
      // 1879360477 Forceful Bludgeon of the Abyss
      // 1879360412 Forceful Club of the Abyss
      // 1879360452 Alloyed War Hammer of the Abyss
      // 1879360465 Alloyed Mallet of the Abyss
      // 1879360432 Steel Heavy Mace of the Abyss
      // 1879360400 Steel Mace of the Abyss
      // 1879360526 Sharp Falchion of the Abyss
      // 1879360524 Serrated Sword of the Abyss
      // 1879360472 Powerful Lance of the Abyss
      // 1879360487 Powerful Spear of the Abyss
      int[] items=new int[]{ 1879360481, 1879360434, 1879360421, 1879360519, 1879360499, 1879360417, 1879360477, 1879360412,
          1879360452, 1879360465, 1879360432, 1879360400, 1879360526, 1879360524, 1879360472, 1879360487 };
      _injector.injectComment(INCOMPARABLE_WEAPONS_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
    }

    // Wyrm
    {
      // 1879360461 Powerful Greatbow of the Wyrm
      // 1879360484 Powerful Bow of the Wyrm
      // 1879360431 Sharp Dagger of the Wyrm
      // 1879360429 Sharp Skean of the Wyrm
      // 1879360413 Brutal Axe of the Wyrm
      // 1879360425 Brutal Double Axe of the Wyrm
      // 1879360405 Forceful Bludgeon of the Wyrm
      // 1879360423 Forceful Club of the Wyrm
      // 1879360464 Alloyed War Hammer of the Wyrm
      // 1879360527 Alloyed Mallet of the Wyrm
      // 1879360470 Steel Heavy Mace of the Wyrm
      // 1879360509 Steel Mace of the Wyrm
      // 1879360494 Sharp Falchion of the Wyrm
      // 1879360512 Serrated Sword of the Wyrm
      // 1879360523 Powerful Lance of the Wyrm
      // 1879360426 Powerful Spear of the Wyrm
      int[] items=new int[]{ 1879360461, 1879360484, 1879360431, 1879360429, 1879360413, 1879360425, 1879360405, 1879360423,
          1879360464, 1879360527, 1879360470, 1879360509, 1879360494, 1879360512, 1879360523, 1879360426 };
      _injector.injectComment(INCOMPARABLE_WEAPONS_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
    }
  }

  private void doRareWeapons()
  {
    // Abyss
    {
      // 1879360485 Keen Falchion of the Abyss
      // 1879360436 Cruel Double Axe of the Abyss
      // 1879360497 Iron Heavy Mace of the Abyss
      // 1879360449 Stout Lance of the Abyss
      // 1879360522 Keen Skean of the Abyss
      // 1879360450 Stout War Hammer of the Abyss
      // 1879360407 Taut Greatbow of the Abyss
      // 1879360474 Weighted Bludgeon of the Abyss
      // 1879360490 Heavy Sword of the Abyss
      // 1879360463 Cruel Axe of the Abyss
      // 1879360416 Iron Mace of the Abyss
      // 1879360444 Stout Spear of the Abyss
      // 1879360491 Keen Dagger of the Abyss
      // 1879360435 Stout Mallet of the Abyss
      // 1879360408 Taut Bow of the Abyss
      // 1879360445 Weighted Club of the Abyss
      int[] items=new int[]{ 1879360485, 1879360436, 1879360497, 1879360449, 1879360522, 1879360450, 1879360407, 1879360474,
          1879360490, 1879360463, 1879360416, 1879360444, 1879360491, 1879360435, 1879360408, 1879360445 };
      _injector.injectComment(RARE_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
    }

    // Wyrm
    {
      // 1879360506 Keen Falchion of the Wyrm
      // 1879360440 Cruel Double Axe of the Wyrm
      // 1879360513 Iron Heavy Mace of the Wyrm
      // 1879360437 Stout Lance of the Wyrm
      // 1879360510 Keen Skean of the Wyrm
      // 1879360419 Stout War Hammer of the Wyrm
      // 1879360505 Taut Greatbow of the Wyrm
      // 1879360482 Weighted Bludgeon of the Wyrm
      // 1879360483 Heavy Sword of the Wyrm
      // 1879360518 Cruel Axe of the Wyrm
      // 1879360466 Iron Mace of the Wyrm
      // 1879360415 Stout Spear of the Wyrm
      // 1879360528 Keen Dagger of the Wyrm
      // 1879360411 Stout Mallet of the Wyrm
      // 1879360467 Taut Bow of the Wyrm
      // 1879360455 Weighted Club of the Wyrm
      int[] items=new int[]{ 1879360506, 1879360440, 1879360513, 1879360437, 1879360510, 1879360419, 1879360505, 1879360482,
          1879360483, 1879360518, 1879360466, 1879360415, 1879360528, 1879360411, 1879360467, 1879360455 };
      _injector.injectComment(RARE_COMMENT,items);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,items);
      _injector.injectMinLevel(MIN_LEVEL,items);
    }
  }
}
