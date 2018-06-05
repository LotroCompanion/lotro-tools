package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on items from the 'High Enchanter' of Mordor.
 * @author DAM
 */
public class MordorHighEnchanter
{
  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MordorHighEnchanter(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doHeavyArmor();
  }

  private void doHeavyArmor()
  {
    String comment="Barter Mordor High Enchanter: 420 Ash";
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879362155 Polished Helm of the Expedition's Vanguard
      // 1879362143 Engraved Pauldrons of the Expedition's Vanguard
      // 1879362138 Articulated Gauntlets of the Expedition's Vanguard
      // 1879362174 Hardened Sabatons of the Expedition's Vanguard
      int[] vanguard=new int[]{ 1879362155, 1879362143, 1879362138, 1879362174 };
      _injector.injectComment(comment,vanguard);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
      _injector.injectArmourType(ArmourType.HEAVY,vanguard);
    }
    // Cloaks
    {
      // 1879362161 Embroidered Heavy Cloak of the Expedition's Vanguard
      int[] cloaks=new int[]{ 1879362161 };
      _injector.injectComment(comment,cloaks);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks);
    }
  }
}
