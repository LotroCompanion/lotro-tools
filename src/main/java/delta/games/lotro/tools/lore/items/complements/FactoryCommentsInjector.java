package delta.games.lotro.tools.lore.items.complements;

import java.util.HashMap;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.items.Item;
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

  /**
   * Perform injection.
   */
  public void doIt()
  {
    new NorthernMirkwoodItems(this).doIt();
    new MordorKeeperOfMysteriesItems(this).doIt();
    new MordorHighEnchanterArmors(this).doIt();
    new MordorHighEnchanterJewels(this).doIt();
    new MordorAllegianceRewards(this).doIt();
    new GorgorothSageGearRewardsVendor(this).doIt();
    new GorgorothScoutGearRewardsVendor(this).doIt();
    new GorgorothWarriorGearRewardsVendor(this).doIt();
    new QuartermasterGorgorothCraftingVendor(this).doIt();
  }

  /**
   * Inject a factory comment into the items with the given ids.
   * @param comment Comment to add.
   * @param ids IDs of targeted items.
   */
  public void injectComment(String comment, int[] ids)
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
}
