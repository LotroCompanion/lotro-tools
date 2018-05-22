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
      // 1879347670" Delicate Gold Armlet of Bard's Will
      // 1879347745" Woven Silver Armlet of Bard's Will
      // 1879347676" Delicate Gold Armlet of Bard's Honour
      // 1879347648" Woven Silver Armlet of Bard's Honour
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
