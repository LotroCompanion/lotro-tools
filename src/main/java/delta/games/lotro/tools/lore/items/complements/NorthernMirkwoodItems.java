package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on Northern Mirkwood items.
 * @author DAM
 */
public class NorthernMirkwoodItems
{
  private static final Integer MIN_LEVEL=Integer.valueOf(115);

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public NorthernMirkwoodItems(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doDale();
    doFelegoth();
    doErebor();
    doNorthernMirkwoodCrafting();
    doQuestItems();
  }

  private void doFelegoth()
  {
    doFelegothJewels();
    doFelegothArmor();
  }

  private void doFelegothJewels()
  {
    String comment=null;
    // Barter Felegoth (Jewels)
    // - Kindred
    {
      comment="Barter Kindred Felegoth: 90 tokens of lake and river";
      // - ears (agility)
      // 1879347747 Opal Gold Earring of Thranduil's Power
      // 1879347644 Rhodalite Silver Earring of Thranduil's Power
      // 1879347651 Opal Gold Earring of Thranduil's Cunning
      // 1879347741 Rhodalite Silver Earring of Thranduil's Cunning
      int[] kindredAgility=new int[]{ 1879347747, 1879347644, 1879347651, 1879347741 };
      _injector.injectComment(comment,kindredAgility);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindredAgility);
      _injector.injectMinLevel(MIN_LEVEL,kindredAgility);
      // - ears (might)
      // 1879347690 Amethyst Iron Earring of Thranduil's Cunning
      // 1879347710 Fire Opal Steel Earring of Thranduil's Power
      // 1879347740 Fire Opal Steel Earring of Thranduil's Cunning
      // 1879347743 Amethyst Iron Earring of Thranduil's Power
      int[] kindredMight=new int[]{ 1879347690, 1879347710, 1879347740, 1879347743 };
      _injector.injectComment(comment,kindredMight);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindredMight);
      _injector.injectMinLevel(MIN_LEVEL,kindredMight);
    }
    // - Ally
    {
      comment="Barter Ally Felegoth: 60 tokens of lake and river";
      // - neck/pocket agi
      // 1879347655 Stylish Pendant of Thranduil's Power
      // 1879347751 Stylish Pendant of Thranduil's Cunning
      // 1879347698 Meaningful Locket of Thranduil's Cunning
      // 1879347738 Meaningful Locket of Thranduil's Power
      int[] allyAgility=new int[]{ 1879347655, 1879347751, 1879347698, 1879347738 };
      _injector.injectComment(comment,allyAgility);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyAgility);
      _injector.injectMinLevel(MIN_LEVEL,allyAgility);
      // - neck/pocket might
      // 1879347626 Heavy Choker of Thranduil's Cunning
      // 1879347750 Heavy Choker of Thranduil's Power
      // 1879347633 Antique Brooch of Thranduil's Cunning
      // 1879347726 Antique Brooch of Thranduil's Power
      int[] allyMight=new int[]{ 1879347626, 1879347750, 1879347633, 1879347726 };
      _injector.injectComment(comment,allyMight);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyMight);
      _injector.injectMinLevel(MIN_LEVEL,allyMight);
    }
    // - Friend
    {
      comment="Barter Friend Felegoth: 55 tokens of lake and river";
      // Agility
      {
        // - wrist
        // 1879347689 Agile Silver Bracelet of Thranduil's Power
        // 1879347709 Agile Silver Bracelet of Thranduil's Cunning
        // 1879347739 Dextrous Gold Bracelet of Thranduil's Power
        // 1879347744 Dextrous Gold Bracelet of Thranduil's Cunning
        int[] friendWrist=new int[]{ 1879347689, 1879347709, 1879347739, 1879347744 };
        _injector.injectComment(comment,friendWrist);
        _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
        _injector.injectMinLevel(MIN_LEVEL,friendWrist);
        // - ear
        // 1879347635 Tourmaline Silver Earring of Thranduil's Cunning
        // 1879347663 Moonstone Gold Earring of Thranduil's Cunning
        // 1879347673 Moonstone Gold Earring of Thranduil's Power
        // 1879347717 Tourmaline Silver Earring of Thranduil's Power
        int[] friendEar=new int[]{ 1879347635, 1879347663, 1879347673, 1879347717 };
        _injector.injectComment(comment,friendEar);
        _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
        _injector.injectMinLevel(MIN_LEVEL,friendEar);
      }
      // Might
      {
        // -wrist
        // 1879347667 Strong Steel Bracelet of Thranduil's Power
        // 1879347669 Strong Steel Bracelet of Thranduil's Cunning
        // 1879347730 Tough Iron Bracelet of Thranduil's Power
        // 1879347734 Tough Iron Bracelet of Thranduil's Cunning
        int[] friendWrist=new int[]{ 1879347667, 1879347669, 1879347730, 1879347734 };
        _injector.injectComment(comment,friendWrist);
        _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
        _injector.injectMinLevel(MIN_LEVEL,friendWrist);
        // -ear
        // 1879347625 Topaz Steel Earring of Thranduil's Cunning
        // 1879347630 Topaz Steel Earring of Thranduil's Power
        // 1879347672 Lapis Iron Earring of Thranduil's Power
        // 1879347724 Lapis Iron Earring of Thranduil's Cunning
        int[] friendEar=new int[]{ 1879347625, 1879347630, 1879347672, 1879347724 };
        _injector.injectComment(comment,friendEar);
        _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
        _injector.injectMinLevel(MIN_LEVEL,friendEar);
      }
    }
    // - Acquaintance
    {
      comment="Barter Acquaintance Felegoth: 55 tokens of lake and river";
      // Agility
      {
        // - ring
        // 1879347627 Lapis Silver Ring of Thranduil's Cunning
        // 1879347654 Lapis Silver Ring of Thranduil's Power
        // 1879347731 Topaz Gold Ring of Thranduil's Power
        // 1879347733 Topaz Gold Ring of Thranduil's Cunning
        int[] acquaintanceRing=new int[]{ 1879347627, 1879347654, 1879347731, 1879347733 };
        _injector.injectComment(comment,acquaintanceRing);
        _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
        _injector.injectMinLevel(MIN_LEVEL,acquaintanceRing);
      }
      // Might
      {
        // - ring
        // 1879347656 Black Granite Ring of Thranduil's Power
        // 1879347660 Chrysolite Oak Ring of Thranduil's Cunning
        // 1879347697 Black Granite Ring of Thranduil's Cunning
        // 1879347735 Chrysolite Oak Ring of Thranduil's Power
        int[] acquaintanceRing=new int[]{ 1879347656, 1879347660, 1879347697, 1879347735 };
        _injector.injectComment(comment,acquaintanceRing);
        _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
        _injector.injectMinLevel(MIN_LEVEL,acquaintanceRing);
      }
    }
  }

  private void doFelegothArmor()
  {
    String comment=null;
    // Barter Felegoth (Armor)
    // - Kindred (shoulders)
    {
      comment="Barter Kindred Felegoth: 45 tokens of lake and river";
      // - head
      // 1879365553 Rivetted Camail of Thranduil's Cunning
      // 1879365556 Rivetted Camail of Thranduil's Cunning
      // 1879365581 Rivetted Camail of Thranduil's Power
      // 1879365598 Rivetted Camail of Thranduil's Power
      int[] kindred=new int[]{ 1879365553, 1879365556, 1879365581, 1879365598 };
      _injector.injectComment(comment,kindred);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
      _injector.injectArmourType(ArmourType.MEDIUM,kindred);
      _injector.injectMinLevel(MIN_LEVEL,kindred);
    }
    // - Ally
    {
      comment="Barter Ally Felegoth: 45 tokens of lake and river";
      // - Hands
      // 1879365547 Mighty Gages of Thranduil's Power
      // 1879365551 Quick Gages of Thranduil's Power
      // 1879365560 Quick Gages of Thranduil's Cunning
      // 1879365577 Mighty Gages of Thranduil's Cunning
      int[] allyHands=new int[]{ 1879365547, 1879365551, 1879365560, 1879365577 };
      _injector.injectComment(comment,allyHands);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyHands);
      _injector.injectArmourType(ArmourType.MEDIUM,allyHands);
      _injector.injectMinLevel(MIN_LEVEL,allyHands);
      // - Boots
      // 1879365540 Nimble Boots of Thranduil's Cunning
      // 1879365566 Forceful Boots of Thranduil's Cunning
      // 1879365567 Forceful Boots of Thranduil's Power
      // 1879365586 Nimble Boots of Thranduil's Power
      int[] allyBoots=new int[]{ 1879365540, 1879365566, 1879365567, 1879365586 };
      _injector.injectComment(comment,allyBoots);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyBoots);
      _injector.injectArmourType(ArmourType.MEDIUM,allyBoots);
      _injector.injectMinLevel(MIN_LEVEL,allyBoots);
    }
    // - Friend
    {
      comment="Barter Friend Felegoth: 20 tokens of lake and river";
      // - Head
      // 1879365463 Padded Coif of Thranduil's Power
      // 1879365465 Padded Coif of Thranduil's Power
      // 1879365467 Padded Coif of Thranduil's Cunning
      // 1879365481 Padded Coif of Thranduil's Cunning
      int[] friendHead=new int[]{ 1879365463, 1879365465, 1879365467, 1879365481 };
      _injector.injectComment(comment,friendHead);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendHead);
      _injector.injectArmourType(ArmourType.MEDIUM,friendHead);
      _injector.injectMinLevel(MIN_LEVEL,friendHead);
      // - Shoulder
      // 1879365462 Fine Camail of Thranduil's Power
      // 1879365474 Fine Camail of Thranduil's Cunning
      // 1879365492 Fine Camail of Thranduil's Cunning
      // 1879365506 Fine Camail of Thranduil's Power
      int[] friendShoulder=new int[]{ 1879365462, 1879365474, 1879365492, 1879365506 };
      _injector.injectComment(comment,friendShoulder);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendShoulder);
      _injector.injectArmourType(ArmourType.MEDIUM,friendShoulder);
      _injector.injectMinLevel(MIN_LEVEL,friendShoulder);
      // - Chest
      // 1879365455 Heavy Hauberk of Thranduil's Cunning
      // 1879365468 Heavy Hauberk of Thranduil's Cunning
      // 1879365485 Heavy Hauberk of Thranduil's Power
      // 1879365499 Heavy Hauberk of Thranduil's Power
      int[] friendChest=new int[]{ 1879365455, 1879365468, 1879365485, 1879365499 };
      _injector.injectComment(comment,friendChest);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendChest);
      _injector.injectArmourType(ArmourType.MEDIUM,friendChest);
      _injector.injectMinLevel(MIN_LEVEL,friendChest);
      // - Cloak
      // 1879365473 Woven Wool Cloak of Thranduil's Power
      // 1879365478 Woven Silk Cloak of Thranduil's Power
      // 1879365479 Woven Silk Cloak of Thranduil's Cunning
      // 1879365493 Woven Wool Cloak of Thranduil's Cunning
      int[] friendCloak=new int[]{ 1879365473, 1879365478, 1879365479, 1879365493 };
      _injector.injectComment(comment,friendCloak);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendCloak);
      _injector.injectArmourType(ArmourType.LIGHT,friendCloak);
      _injector.injectMinLevel(MIN_LEVEL,friendCloak);
    }
    // - Acquaintance
    {
      comment="Barter Acquaintance Felegoth: 20 tokens of lake and river";
      // - Hands
      // 1879365461 Dextrous Gages of Thranduil's Cunning
      // 1879365491 Dextrous Gages of Thranduil's Power
      // 1879365498 Strong Gages of Thranduil's Cunning
      // 1879365504 Strong Gages of Thranduil's Power
      int[] acquaintanceHands=new int[]{ 1879365461, 1879365491, 1879365498, 1879365504 };
      _injector.injectComment(comment,acquaintanceHands);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceHands);
      _injector.injectArmourType(ArmourType.MEDIUM,acquaintanceHands);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceHands);
      // - Legs
      // 1879365454 Flexible Leggings of Thranduil's Cunning
      // 1879365482 Flexible Leggings of Thranduil's Power
      // 1879365483 Enduring Leggings of Thranduil's Power
      // 1879365507 Enduring Leggings of Thranduil's Cunning
      int[] acquaintanceLegs=new int[]{ 1879365454, 1879365482, 1879365483, 1879365507 };
      _injector.injectComment(comment,acquaintanceLegs);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceLegs);
      _injector.injectArmourType(ArmourType.MEDIUM,acquaintanceLegs);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceLegs);
      // - Boots
      // 1879365484 Tough Boots of Thranduil's Cunning
      // 1879365486 Lithe Boots of Thranduil's Power
      // 1879365489 Tough Boots of Thranduil's Power
      // 1879365509 Lithe Boots of Thranduil's Cunning
      int[] acquaintanceBoots=new int[]{ 1879365484, 1879365486, 1879365489, 1879365509 };
      _injector.injectComment(comment,acquaintanceBoots);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceBoots);
      _injector.injectArmourType(ArmourType.MEDIUM,acquaintanceBoots);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceBoots);
    }
  }

  private void doNorthernMirkwoodCrafting()
  {
    // Crafting Felegoth/Dale/Erebor
    // - Kindred
    String comment="Recipe Kindred Felegoth or Dale or Erebor: single use, 40 tokens of lake and river";
    // - Head
    {
      // 1879366087 Master-crafted Coif of Thranduil's Power
      // 1879366083 Master-crafted Nimble Coif of Thranduil's Power
      // 1879366085 Master-crafted Coif of Thranduil's Cunning
      // 1879366086 Master-crafted Nimble Coif of Thranduil's Cunning
      // 1879366081 Master-crafted Helm of Thorin's Memory
      // 1879366082 Master-crafted Helm of Thorin's Strength
      // 1879366084 Master-crafted Cap of Bard's Will
      // 1879366080 Master-crafted Cap of Bard's Honour
      int[] head=new int[]{ 1879366087, 1879366083, 1879366085, 1879366086, 1879366081, 1879366082, 1879366084, 1879366080 };
      _injector.injectComment(comment,head);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,head);
      _injector.injectMinLevel(MIN_LEVEL,head);

      int[] medium=new int[]{ 1879366087, 1879366083, 1879366085, 1879366086 };
      _injector.injectArmourType(ArmourType.MEDIUM,medium);
      int[] heavy=new int[]{ 1879366081, 1879366082 };
      _injector.injectArmourType(ArmourType.HEAVY,heavy);
      int[] light=new int[]{ 1879366084, 1879366080 };
      _injector.injectArmourType(ArmourType.LIGHT,light);
    }
    // - Pocket
    {
      // 1879366101 Noble Emblem of Thorin's Memory (evade)
      // 1879366104 Noble Emblem of Thorin's Strength (critical rating)
      // 1879366112 Momentous Locket of Thranduil's Power (critical rating)
      // 1879366103 Momentous Locket of Thranduil's Cunning (evade)
      // 1879366099 Glowing Phial of Bard's Will (critical rating)
      // 1879366109 Glowing Phial of Bard's Honour (outgoing healing)
      int[] pocket=new int[]{ 1879366101, 1879366104, 1879366112, 1879366103, 1879366099, 1879366109};
      _injector.injectComment(comment,pocket);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,pocket);
      _injector.injectMinLevel(MIN_LEVEL,pocket);
    }
    // - Neck
    {
      // 1879366110 Polished Torc of Thorin's Strength
      // 1879366105 Polished Torc of Thorin's Memory
      // 1879366107 Graceful Pendant of Thranduil's Power
      // 1879366113 Graceful Pendant of Thranduil's Cunning
      // 1879366106 Filigree Necklace of Bard's Will
      // 1879366100 Filigree Necklace of Bard's Honour
      int[] neck=new int[]{ 1879366110, 1879366105, 1879366107, 1879366113, 1879366106, 1879366100};
      _injector.injectComment(comment,neck);
      _injector.injectBinding(ItemBinding.BIND_ON_EQUIP,neck);
      _injector.injectMinLevel(MIN_LEVEL,neck);
    }
  }

  private void doDale()
  {
    doDaleJewels();
    doDaleArmor();
  }

  private void doDaleJewels()
  {
    String comment=null;
    // Barter Dale
    // - Kindred
    {
      comment="Barter Kindred Dale: 90 tokens of lake and river";
      // - ears
      // 1879347674 Fire Opal Gold Ear Cuff of Bard's Honour
      // 1879347728 Fire Opal Gold Ear Cuff of Bard's Will
      // 1879347643 Amethyst Silver Ear Cuff of Bard's Will
      // 1879347700 Amethyst Silver Ear Cuff of Bard's Honour
      int[] kindred=new int[]{ 1879347674, 1879347728, 1879347643, 1879347700 };
      _injector.injectComment(comment,kindred);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
      _injector.injectMinLevel(MIN_LEVEL,kindred);
    }
    // - Ally
    {
      comment="Barter Ally Dale: 60 tokens of lake and river";
      // - neck
      // 1879347634 Fancy Necklace of Bard's Will
      // 1879347699 Fancy Necklace of Bard's Honour
      // - pocket
      // 1879347708 Glimmering Phial of Bard's Will
      // 1879347636 Glimmering Phial of Bard's Honour
      int[] ally=new int[]{ 1879347634, 1879347699, 1879347708, 1879347636 };
      _injector.injectComment(comment,ally);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,ally);
      _injector.injectMinLevel(MIN_LEVEL,ally);
    }
    // - Friend
    {
      comment="Barter Friend Dale: 55 tokens of lake and river";
      // - wrist
      // 1879347670 Delicate Gold Armlet of Bard's Will
      // 1879347745 Woven Silver Armlet of Bard's Will
      // 1879347676 Delicate Gold Armlet of Bard's Honour
      // 1879347648 Woven Silver Armlet of Bard's Honour
      int[] friendWrist=new int[]{ 1879347670, 1879347745, 1879347676, 1879347648 };
      _injector.injectComment(comment,friendWrist);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
      _injector.injectMinLevel(MIN_LEVEL,friendWrist);
      // - ear
      // 1879347687 Topaz Gold Ear Cuff of Bard's Will
      // 1879347720 Lapis Silver Ear Cuff of Bard's Will
      // 1879347639 Topaz Gold Ear Cuff of Bard's Honour
      // 1879347705 Lapis Silver Ear Cuff of Bard's Honour
      int[] friendEar=new int[]{ 1879347687, 1879347720, 1879347639, 1879347705 };
      _injector.injectComment(comment,friendEar);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
      _injector.injectMinLevel(MIN_LEVEL,friendEar);
    }
    // - Acquaintance
    {
      comment="Barter Acquaintance Dale: 55 tokens of lake and river";
      // - ring
      // 1879347732 Carved Jade Ring of Bard's Honour
      // 1879347711 Curious Onyx Ring of Bard's Honour
      // 1879347652 Carved Jade Ring of Bard's Will
      // 1879347681 Curious Onyx Ring of Bard's Will
      int[] acquaintanceRing=new int[]{ 1879347732, 1879347711, 1879347652, 1879347681 };
      _injector.injectComment(comment,acquaintanceRing);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceRing);
    }
  }

  private void doDaleArmor()
  {
    String comment=null;
    // Barter Dale (Armor)
    // - Kindred (shoulders)
    {
      comment="Barter Kindred Dale: 45 tokens of lake and river";
      // - head
      // 1879365555 Embossed Mantle of Bard's Honour
      // 1879365562 Embossed Mantle of Bard's Will
      int[] kindred=new int[]{ 1879365555, 1879365562 };
      _injector.injectComment(comment,kindred);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
      _injector.injectArmourType(ArmourType.LIGHT,kindred);
      _injector.injectMinLevel(MIN_LEVEL,kindred);
    }
    // - Ally
    {
      comment="Barter Ally Dale: 45 tokens of lake and river";
      // - Hands
      // 1879365565 Runed Gloves of Bard's Will
      // 1879365573 Runed Gloves of Bard's Honour
      int[] allyHands=new int[]{ 1879365565, 1879365573 };
      _injector.injectComment(comment,allyHands);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyHands);
      _injector.injectArmourType(ArmourType.LIGHT,allyHands);
      _injector.injectMinLevel(MIN_LEVEL,allyHands);
      // - Boots
      // 1879365554 Rugged Shoes of Bard's Honour
      // 1879365596 Rugged Shoes of Bard's Will
      int[] allyBoots=new int[]{ 1879365554, 1879365596 };
      _injector.injectComment(comment,allyBoots);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyBoots);
      _injector.injectArmourType(ArmourType.LIGHT,allyBoots);
      _injector.injectMinLevel(MIN_LEVEL,allyBoots);
    }
    // - Friend
    {
      comment="Barter Friend Dale: 20 tokens of lake and river";
      // - Head
      // 1879365469 Fancy Cap of Bard's Will
      // 1879365488 Fancy Cap of Bard's Honour
      int[] friendHead=new int[]{ 1879365469, 1879365488 };
      _injector.injectComment(comment,friendHead);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendHead);
      _injector.injectArmourType(ArmourType.LIGHT,friendHead);
      _injector.injectMinLevel(MIN_LEVEL,friendHead);
      // - Shoulder
      // 1879365505 Embroidered Mantle of Bard's Will
      // 1879365508 Embroidered Mantle of Bard's Honour
      int[] friendShoulder=new int[]{ 1879365505, 1879365508 };
      _injector.injectComment(comment,friendShoulder);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendShoulder);
      _injector.injectArmourType(ArmourType.LIGHT,friendShoulder);
      _injector.injectMinLevel(MIN_LEVEL,friendShoulder);
      // - Chest
      // 1879365480 Padded Vest of Bard's Honour
      // 1879365490 Padded Vest of Bard's Will
      int[] friendChest=new int[]{ 1879365480, 1879365490 };
      _injector.injectComment(comment,friendChest);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendChest);
      _injector.injectArmourType(ArmourType.LIGHT,friendChest);
      _injector.injectMinLevel(MIN_LEVEL,friendChest);
      // - Cloak
      // 1879365464 Woven Light Cloak of Bard's Will
      // 1879365494 Woven Light Cloak of Bard's Honour
      int[] friendCloak=new int[]{ 1879365464, 1879365494 };
      _injector.injectComment(comment,friendCloak);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendCloak);
      _injector.injectArmourType(ArmourType.LIGHT,friendCloak);
      _injector.injectMinLevel(MIN_LEVEL,friendCloak);
    }
    // - Acquaintance
    {
      comment="Barter Acquaintance Dale: 20 tokens of lake and river";
      // - Hands
      // 1879365497 Supple Gloves of Bard's Honour
      // 1879365501 Supple Gloves of Bard's Will
      int[] acquaintanceHands=new int[]{ 1879365497, 1879365501 };
      _injector.injectComment(comment,acquaintanceHands);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceHands);
      _injector.injectArmourType(ArmourType.LIGHT,acquaintanceHands);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceHands);
      // - Legs
      // 1879365495 Corded Trousers of Bard's Honour
      // 1879365502 Corded Trousers of Bard's Will
      int[] acquaintanceLegs=new int[]{ 1879365495, 1879365502 };
      _injector.injectComment(comment,acquaintanceLegs);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceLegs);
      _injector.injectArmourType(ArmourType.LIGHT,acquaintanceLegs);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceLegs);
      // - Boots
      // 1879365475 Superior Shoes of Bard's Honour
      // 1879365487 Superior Shoes of Bard's Will
      int[] acquaintanceBoots=new int[]{ 1879365475, 1879365487 };
      _injector.injectComment(comment,acquaintanceBoots);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceBoots);
      _injector.injectArmourType(ArmourType.LIGHT,acquaintanceBoots);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceBoots);
    }
  }

  private void doErebor()
  {
    doEreborJewels();
    doEreborArmor();
  }

  private void doEreborJewels()
  {
    String comment=null;
    // Barter Erebor (Jewels)
    // - Kindred
    {
      comment="Barter Kindred Erebor: 90 tokens of lake and river";
      // - ears
      // 1879347682 Opal Gold Stud of Thorin's Strength
      // 1879347742 Opal Gold Stud of Thorin's Memory
      // 1879347694 Rhodalite Silver Stud of Thorin's Memory
      // 1879347729 Rhodalite Silver Stud of Thorin's Strength
      int[] kindred=new int[]{ 1879347682, 1879347742, 1879347694, 1879347729 };
      _injector.injectComment(comment,kindred);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
      _injector.injectMinLevel(MIN_LEVEL,kindred);
    }
    // - Ally
    {
      comment="Barter Ally Erebor: 60 tokens of lake and river";
      // - neck
      // 1879347748 Heavy Torc of Thorin's Strength
      // 1879347716 Heavy Torc of Thorin's Memory
      // - pocket
      // 1879347680 Proud Emblem of Thorin's Strength
      // 1879347727 Proud Emblem of Thorin's Memory
      int[] ally=new int[]{ 1879347748, 1879347716, 1879347680, 1879347727 };
      _injector.injectComment(comment,ally);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,ally);
      _injector.injectMinLevel(MIN_LEVEL,ally);
    }
    // - Friend
    {
      comment="Barter Friend Erebor: 55 tokens of lake and river";
      // - wrist
      // 1879347702 Studded Gold Bracer of Thorin's Strength
      // 1879347641 Studded Gold Bracer of Thorin's Memory
      // 1879347662 Burnished Silver Bracer of Thorin's Strength
      // 1879347706 Burnished Silver Bracer of Thorin's Memory
      int[] friendWrist=new int[]{ 1879347702, 1879347641, 1879347662, 1879347706 };
      _injector.injectComment(comment,friendWrist);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
      _injector.injectMinLevel(MIN_LEVEL,friendWrist);
      // - ear
      // 1879347665 Moonstone Gold Stud of Thorin's Memory
      // 1879347684 Moonstone Gold Stud of Thorin's Strength
      // 1879347678 Tourmaline Silver Stud of Thorin's Memory
      // 1879347713 Tourmaline Silver Stud of Thorin's Strength
      int[] friendEar=new int[]{ 1879347665, 1879347684, 1879347678, 1879347713 };
      _injector.injectComment(comment,friendEar);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
      _injector.injectMinLevel(MIN_LEVEL,friendEar);
    }
    // - Acquaintance
    {
      comment="Barter Acquaintance Erebor: 55 tokens of lake and river";
      // - ring
      // 1879347628 Topaz Steel Ring of Thorin's Memory
      // 1879347688 Topaz Steel Ring of Thorin's Strength
      // 1879347658 Lapis Mithril Ring of Thorin's Memory
      // 1879347722 Lapis Mithril Ring of Thorin's Strength
      int[] acquaintanceRing=new int[]{ 1879347628, 1879347688, 1879347658, 1879347722 };
      _injector.injectComment(comment,acquaintanceRing);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceRing);
    }
  }

  private void doEreborArmor()
  {
    String comment=null;
    // Barter Erebor (Armor)
    // - Kindred (shoulders)
    {
      comment="Barter Kindred Erebor: 45 tokens of lake and river";
      // - head
      // 1879365546 Engraved Pauldrons of Thorin's Memory
      // 1879365558 Engraved Pauldrons of Thorin's Strength
      int[] kindred=new int[]{ 1879365546, 1879365558 };
      _injector.injectComment(comment,kindred);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
      _injector.injectArmourType(ArmourType.HEAVY,kindred);
      _injector.injectMinLevel(MIN_LEVEL,kindred);
    }
    // - Ally
    {
      comment="Barter Ally Erebor: 45 tokens of lake and river";
      // - Hands
      // 1879365590 Articulated Gauntlets of Thorin's Strength
      // 1879365592 Articulated Gauntlets of Thorin's Memory
      int[] allyHands=new int[]{ 1879365590, 1879365592 };
      _injector.injectComment(comment,allyHands);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyHands);
      _injector.injectArmourType(ArmourType.HEAVY,allyHands);
      _injector.injectMinLevel(MIN_LEVEL,allyHands);
      // - Boots
      // 1879365563 Hardened Sabatons of Thorin's Strength
      // 1879365595 Hardened Sabatons of Thorin's Memory
      int[] allyBoots=new int[]{ 1879365563, 1879365595 };
      _injector.injectComment(comment,allyBoots);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyBoots);
      _injector.injectArmourType(ArmourType.HEAVY,allyBoots);
      _injector.injectMinLevel(MIN_LEVEL,allyBoots);
    }
    // - Friend
    {
      comment="Barter Friend Erebor: 20 tokens of lake and river";
      // - Head
      // 1879365457 Hardened Helm of Thorin's Strength
      // 1879365470 Hardened Helm of Thorin's Memory
      int[] friendHead=new int[]{ 1879365457, 1879365470 };
      _injector.injectComment(comment,friendHead);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendHead);
      _injector.injectArmourType(ArmourType.HEAVY,friendHead);
      _injector.injectMinLevel(MIN_LEVEL,friendHead);
      // - Shoulder
      // 1879365471 Bolstered Pauldrons of Thorin's Memory
      // 1879365476 Bolstered Pauldrons of Thorin's Strength
      int[] friendShoulder=new int[]{ 1879365471, 1879365476 };
      _injector.injectComment(comment,friendShoulder);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendShoulder);
      _injector.injectArmourType(ArmourType.HEAVY,friendShoulder);
      _injector.injectMinLevel(MIN_LEVEL,friendShoulder);
      // - Chest
      // 1879365456 Reinforced Chestplate of Thorin's Memory
      // 1879365472 Reinforced Chestplate of Thorin's Strength
      int[] friendChest=new int[]{ 1879365456, 1879365472 };
      _injector.injectComment(comment,friendChest);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendChest);
      _injector.injectArmourType(ArmourType.HEAVY,friendChest);
      _injector.injectMinLevel(MIN_LEVEL,friendChest);
      // - Cloak
      // 1879365458 Woven Heavy Cloak of Thorin's Memory
      // 1879365460 Woven Heavy Cloak of Thorin's Strength
      int[] friendCloak=new int[]{ 1879365458, 1879365460 };
      _injector.injectComment(comment,friendCloak);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendCloak);
      _injector.injectArmourType(ArmourType.LIGHT,friendCloak);
      _injector.injectMinLevel(MIN_LEVEL,friendCloak);
    }
    // - Acquaintance
    {
      comment="Barter Acquaintance Erebor: 20 tokens of lake and river";
      // - Hands
      // 1879365459 Strong Gauntlets of Thorin's Memory
      // 1879365503 Strong Gauntlets of Thorin's Strength
      int[] acquaintanceHands=new int[]{ 1879365459, 1879365503 };
      _injector.injectComment(comment,acquaintanceHands);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceHands);
      _injector.injectArmourType(ArmourType.HEAVY,acquaintanceHands);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceHands);
      // - Legs
      // 1879365466 Tough Greaves of Thorin's Memory
      // 1879365477 Tough Greaves of Thorin's Strength
      int[] acquaintanceLegs=new int[]{ 1879365466, 1879365477 };
      _injector.injectComment(comment,acquaintanceLegs);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceLegs);
      _injector.injectArmourType(ArmourType.HEAVY,acquaintanceLegs);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceLegs);
      // - Boots
      // 1879365496 Thick Sabatons of Thorin's Memory
      // 1879365500 Thick Sabatons of Thorin's Strength
      int[] acquaintanceBoots=new int[]{ 1879365496, 1879365500 };
      _injector.injectComment(comment,acquaintanceBoots);
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceBoots);
      _injector.injectArmourType(ArmourType.HEAVY,acquaintanceBoots);
      _injector.injectMinLevel(MIN_LEVEL,acquaintanceBoots);
    }
  }

  private void doQuestItems()
  {
    doChests();
    doCloaks();
    doLeggings();
  }

  private void doChests()
  {
    // Heavy
    // 1879365594 Buttressed Chestplate of Thorin's Strength
    // 1879365539 Buttressed Chestplate of Thorin's Memory
    // Medium (agility)
    // 1879365543 Reinforced Hauberk of Thranduil's Power
    // 1879365597 Reinforced Hauberk of Thranduil's Cunning
    // Medium (might)
    // 1879365541 Double-mail Hauberk of Thranduil's Power
    // 1879365564 Double-mail Hauberk of Thranduil's Cunning
    // Light
    // 1879365591 Woven Vest of Bard's Will" level="340
    // 1879365548 Woven Vest of Bard's Honour" level="340
    int[] chests=new int[]{ 1879365594, 1879365539, 1879365543, 1879365597, 1879365541, 1879365564, 1879365591, 1879365548 };
    _injector.injectComment("Black Book of Mordor, Volume 1, 5.5: The Walls Brought Down",chests);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,chests);
    _injector.injectArmourType(ArmourType.HEAVY,new int[]{ 1879365594, 1879365539 });
    _injector.injectArmourType(ArmourType.MEDIUM,new int[]{ 1879365543, 1879365597, 1879365541, 1879365564 });
    _injector.injectArmourType(ArmourType.LIGHT,new int[]{ 1879365591, 1879365548 });
    _injector.injectMinLevel(MIN_LEVEL,chests);
  }

  private void doCloaks()
  {
    // 1879365572 Fantastic Heavy Cloak of Thorin's Strength
    // 1879365582 Fantastic Heavy Cloak of Thorin's Memory
    // 1879365552 Fantastic Silk Cloak of Thranduil's Power
    // 1879365593 Fantastic Silk Cloak of Thranduil's Cunning
    // 1879365570 Fantastic Wool Cloak of Thranduil's Power
    // 1879365568 Fantastic Wool Cloak of Thranduil's Cunning
    // 1879365569 Fantastic Light Cloak of Bard's Will
    // 1879365587 Fantastic Light Cloak of Bard's Honour
    int[] cloaks=new int[]{ 1879365572, 1879365582, 1879365552, 1879365593, 1879365570, 1879365568, 1879365569, 1879365587 };
    _injector.injectComment("Black Book of Mordor, Volume 1, Chapter 6.7: Wood, Lake, Mountain, and Stone",cloaks);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,cloaks);
    _injector.injectArmourType(ArmourType.LIGHT,cloaks);
    _injector.injectMinLevel(MIN_LEVEL,cloaks);
  }

  private void doLeggings()
  {
    // Heavy
    // 1879365544 Burnished Greaves of Thorin's Strength
    // 1879365574 Burnished Greaves of Thorin's Memory
    // Medium (agility)
    // 1879365575 Agile Leggings of Thranduil's Power
    // 1879365545 Agile Leggings of Thranduil's Cunning
    // Medium (might)
    // 1879365557 Hardened Leggings of Thranduil's Power
    // 1879365549 Hardened Leggings of Thranduil's Cunning
    // Light
    // 1879365542 Belted Trousers of Bard's Will
    // 1879365578 Belted Trousers of Bard's Honour
    int[] chests=new int[]{ 1879365544, 1879365574, 1879365575, 1879365545, 1879365557, 1879365549, 1879365542, 1879365578 };
    _injector.injectComment("Northern Mirkwood Epilogue: Trail of Rust",chests);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,chests);
    _injector.injectArmourType(ArmourType.HEAVY,new int[]{ 1879365544, 1879365574 });
    _injector.injectArmourType(ArmourType.MEDIUM,new int[]{ 1879365575, 1879365545, 1879365557, 1879365549 });
    _injector.injectArmourType(ArmourType.LIGHT,new int[]{ 1879365542, 1879365578 });
    _injector.injectMinLevel(MIN_LEVEL,chests);
  }
}
