package delta.games.lotro.tools.lore.items;

import java.util.HashMap;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemPropertyNames;

/**
 * Injector for factory comments.
 * @author DAM
 */
public class FactoryCommentsInjector
{
  private static final Logger _logger=Logger.getLogger(FactoryCommentsInjector.class);

  private HashMap<Integer,Item> _items;

  /**
   * Constructor.
   * @param items Items to use.
   */
  public FactoryCommentsInjector(HashMap<Integer,Item> items)
  {
    _items=items;
  }

  private void injectComment(String comment, int[] ids)
  {
    for(int id : ids)
    {
      Item item=_items.get(Integer.valueOf(id));
      if (item!=null)
      {
        item.setProperty(ItemPropertyNames.FACTORY_COMMENT,comment);
      }
      else
      {
        _logger.warn("Item not found: ID="+id);
      }
    }
  }

  private void injectBinding(ItemBinding binding, int[] ids)
  {
    for(int id : ids)
    {
      Item item=_items.get(Integer.valueOf(id));
      if (item!=null)
      {
        item.setBinding(binding);
      }
      else
      {
        _logger.warn("Item not found: ID="+id);
      }
    }
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    //String comment="Recipe Kindred Erebor: single use, 40 tokens of lake and river";
    doDale();
    doFelegothJewels();
    doFelegothArmor();
  }

  private void doFelegothJewels()
  {
    String comment=null;
    // Barter Felegoth
    // - Kindred
    {
      comment="Barter Kindred Felegoth: 90 tokens of lake and river";
      // - ears (agility)
      // 1879347747 Opal Gold Earring of Thranduil's Power
      // 1879347644 Rhodalite Silver Earring of Thranduil's Power
      // 1879347651 Opal Gold Earring of Thranduil's Cunning
      // 1879347741 Rhodalite Silver Earring of Thranduil's Cunning
      int[] kindredAgility=new int[]{ 1879347747, 1879347644, 1879347651, 1879347741 };
      injectComment(comment,kindredAgility);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindredAgility);
      // - ears (might)
      // 1879347690 Amethyst Iron Earring of Thranduil's Cunning
      // 1879347710 Fire Opal Steel Earring of Thranduil's Power
      // 1879347740 Fire Opal Steel Earring of Thranduil's Cunning
      // 1879347743 Amethyst Iron Earring of Thranduil's Power
      int[] kindredMight=new int[]{ 1879347690, 1879347710, 1879347740, 1879347743 };
      injectComment(comment,kindredMight);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindredMight);
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
      injectComment(comment,allyAgility);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyAgility);
      // - neck/pocket might
      // 1879347626 Heavy Choker of Thranduil's Cunning
      // 1879347750 Heavy Choker of Thranduil's Power
      // 1879347633 Antique Brooch of Thranduil's Cunning
      // 1879347726 Antique Brooch of Thranduil's Power
      int[] allyMight=new int[]{ 1879347626, 1879347750, 1879347633, 1879347726 };
      injectComment(comment,allyMight);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyMight);
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
        injectComment(comment,friendWrist);
        injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
        // - ear
        // 1879347635 Tourmaline Silver Earring of Thranduil's Cunning
        // 1879347663 Moonstone Gold Earring of Thranduil's Cunning
        // 1879347673 Moonstone Gold Earring of Thranduil's Power
        // 1879347717 Tourmaline Silver Earring of Thranduil's Power
        int[] friendEar=new int[]{ 1879347635, 1879347663, 1879347673, 1879347717 };
        injectComment(comment,friendEar);
        injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
      }
      // Might
      {
        // -wrist
        // 1879347667 Strong Steel Bracelet of Thranduil's Power
        // 1879347669 Strong Steel Bracelet of Thranduil's Cunning
        // 1879347730 Tough Iron Bracelet of Thranduil's Power
        // 1879347734 Tough Iron Bracelet of Thranduil's Cunning
        int[] friendWrist=new int[]{ 1879347667, 1879347669, 1879347730, 1879347734 };
        injectComment(comment,friendWrist);
        injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
        // -ear
        // 1879347625 Topaz Steel Earring of Thranduil's Cunning
        // 1879347630 Topaz Steel Earring of Thranduil's Power
        // 1879347672 Lapis Iron Earring of Thranduil's Power
        // 1879347724 Lapis Iron Earring of Thranduil's Cunning
        int[] friendEar=new int[]{ 1879347625, 1879347630, 1879347672, 1879347724 };
        injectComment(comment,friendEar);
        injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
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
        injectComment(comment,acquaintanceRing);
        injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
      }
      // Might
      {
        // - ring
        // 1879347656 Black Granite Ring of Thranduil's Power
        // 1879347660 Chrysolite Oak Ring of Thranduil's Cunning
        // 1879347697 Black Granite Ring of Thranduil's Cunning
        // 1879347735 Chrysolite Oak Ring of Thranduil's Power
        int[] acquaintanceRing=new int[]{ 1879347656, 1879347660, 1879347697, 1879347735 };
        injectComment(comment,acquaintanceRing);
        injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
      }
    }
  }

  private void doFelegothArmor()
  {
    String comment=null;
    // Barter Felegoth
    // - Kindred (shoulders)
    {
      comment="Barter Kindred Felegoth: 45 tokens of lake and river";
      // - head
      // 1879365553 Rivetted Camail of Thranduil's Cunning
      // 1879365556 Rivetted Camail of Thranduil's Cunning
      // 1879365581 Rivetted Camail of Thranduil's Power
      // 1879365598 Rivetted Camail of Thranduil's Power
      int[] kindred=new int[]{ 1879365553, 1879365556, 1879365581, 1879365598 };
      injectComment(comment,kindred);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
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
      injectComment(comment,allyHands);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyHands);
      // - Boots
      // 1879365540 Nimble Boots of Thranduil's Cunning
      // 1879365566 Forceful Boots of Thranduil's Cunning
      // 1879365567 Forceful Boots of Thranduil's Power
      // 1879365586 Nimble Boots of Thranduil's Power
      int[] allyBoots=new int[]{ 1879365540, 1879365566, 1879365567, 1879365586 };
      injectComment(comment,allyBoots);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,allyBoots);
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
      injectComment(comment,friendHead);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendHead);
      // - Shoulder
      // 1879365462 Fine Camail of Thranduil's Power
      // 1879365474 Fine Camail of Thranduil's Cunning
      // 1879365492 Fine Camail of Thranduil's Cunning
      // 1879365506 Fine Camail of Thranduil's Power
      int[] friendShoulder=new int[]{ 1879365462, 1879365474, 1879365492, 1879365506 };
      injectComment(comment,friendShoulder);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendShoulder);
      // - Chest
      // 1879365455 Heavy Hauberk of Thranduil's Cunning
      // 1879365468 Heavy Hauberk of Thranduil's Cunning
      // 1879365485 Heavy Hauberk of Thranduil's Power
      // 1879365499 Heavy Hauberk of Thranduil's Power
      int[] friendChest=new int[]{ 1879365455, 1879365468, 1879365485, 1879365499 };
      injectComment(comment,friendChest);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendChest);
      // - Cloak
      // 1879365473 Woven Wool Cloak of Thranduil's Power
      // 1879365478 Woven Silk Cloak of Thranduil's Power
      // 1879365479 Woven Silk Cloak of Thranduil's Cunning
      // 1879365493 Woven Wool Cloak of Thranduil's Cunning
      int[] friendCloak=new int[]{ 1879365473, 1879365478, 1879365479, 1879365493 };
      injectComment(comment,friendCloak);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendCloak);
    }
  }

  private void doDale()
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
      injectComment(comment,kindred);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,kindred);
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
      injectComment(comment,ally);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,ally);
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
      injectComment(comment,friendWrist);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendWrist);
      // - ear
      // 1879347687 Topaz Gold Ear Cuff of Bard's Will
      // 1879347720 Lapis Silver Ear Cuff of Bard's Will
      // 1879347639 Topaz Gold Ear Cuff of Bard's Honour
      // 1879347705 Lapis Silver Ear Cuff of Bard's Honour
      int[] friendEar=new int[]{ 1879347687, 1879347720, 1879347639, 1879347705 };
      injectComment(comment,friendEar);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,friendEar);
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
      injectComment(comment,acquaintanceRing);
      injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,acquaintanceRing);
    }
  }
}
