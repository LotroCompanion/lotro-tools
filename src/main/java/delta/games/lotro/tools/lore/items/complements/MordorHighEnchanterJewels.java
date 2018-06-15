package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on jewels from the 'High Enchanter' of Mordor.
 * @author DAM
 */
public class MordorHighEnchanterJewels
{
  private static final String JEWELS_COMMENT="Barter Mordor High Enchanter: 330 Ash";
  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MordorHighEnchanterJewels(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doMightVanguard();
    doMightMordorBane();
    doAgility();
    doWill();
  }

  private void doMightVanguard()
  {
   // Barter Incomparable Jewels (Vanguard)
    // Bind to Account on Acquire
    {
      // 1879361325 Polished Torc of the Expedition's Vanguard BonAonA
      // 1879361294 Fire Opal Steel Ring of the Expedition's Vanguard BonAonA
      // 1879361386 Amethyst Mithril Ring of the Expedition's Vanguard BonAonA
      // 1879361404 Polished Silver Bracer of the Expedition's Vanguard BonAonA
      // 1879361293 Rhodalite Silver Stud of the Expedition's Vanguard BonAonA
      // 1879361385 Noble Emblem of the Expedition's Vanguard BonAonA
      int[] vanguardBAonA=new int[]{ 1879361325, 1879361294, 1879361386, 1879361404, 1879361293, 1879361385 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE, vanguardBAonA);
    }
    // Bind on Acquire 1
    {
      // 1879361794 Polished Torc of the Expedition's Vanguard BonA
      // 1879361887 Fire Opal Steel Ring of the Expedition's Vanguard BonA
      // 1879361772 Amethyst Mithril Ring of the Expedition's Vanguard BonA
      // 1879362216 Alloyed Gold Bracer of the Expedition's Vanguard BonA
      // 1879361843 Polished Silver Bracer of the Expedition's Vanguard BonA
      // 1879362200 Opal Gold Stud of the Expedition's Vanguard BonA
      // 1879361870 Rhodalite Silver Stud of the Expedition's Vanguard BonA
      // 1879361834 Noble Emblem of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879361794, 1879361887, 1879361772, 1879362216,
          1879361843, 1879362200, 1879361870, 1879361834 };
      _injector.injectComment(JEWELS_COMMENT,vanguard);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
    }
    // Bind on Acquire 2
    {
      // 1879362224 Polished Torc of the Expedition's Vanguard BonA
      // 1879362189 Fire Opal Steel Ring of the Expedition's Vanguard BonA
      // 1879362197 Amethyst Mithril Ring of the Expedition's Vanguard BonA
      // 1879362205 Polished Silver Bracer of the Expedition's Vanguard BonA
      // 1879362207 Rhodalite Silver Stud of the Expedition's Vanguard BonA
      // 1879362182 Noble Emblem of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879362224, 1879362189, 1879362197, 1879362205, 1879362207, 1879362182 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
    }

    // Share stats
    int[] neck=new int[]{ 1879361325, 1879361794, 1879362224 };
    _injector.shareStats(neck);
    int[] finger1=new int[]{ 1879361294, 1879361887, 1879362189 };
    _injector.shareStats(finger1);
    int[] finger2=new int[]{ 1879361386, 1879361772, 1879362197 };
    _injector.shareStats(finger2);
    int[] wrist2=new int[]{ 1879361404, 1879361843, 1879362205 };
    _injector.shareStats(wrist2);
    int[] ear2=new int[]{ 1879361293, 1879361870, 1879362207 };
    _injector.shareStats(ear2);
    int[] pocket=new int[]{ 1879361385, 1879361834, 1879362182 };
    _injector.shareStats(pocket);

    // TODO Turca
    // 1879361965 Turca (330) BonAonA
  }

  private void doMightMordorBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
    // Bind to Account on Acquire
    {
      // 1879361364 Polished Torc of Mordor's Bane BonAonA
      // 1879361406 Fire Opal Steel Ring of Mordor's Bane BonAonA
      // 1879361327 Amethyst Mithril Ring of Mordor's Bane BonAonA
      // 1879361387 Polished Silver Bracer of Mordor's Bane BonAonA
      // 1879361405 Rhodalite Silver Stud of Mordor's Bane BonAonA
      // 1879361326 Noble Emblem of Mordor's Bane BonAonA
      int[] mordorBaneBAonA=new int[]{ 1879361364, 1879361406, 1879361327, 1879361387, 1879361405, 1879361326 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE, mordorBaneBAonA);
    }
    // Bind on Acquire 1
    {
    // 1879361818 Polished Torc of Mordor's Bane BonA
    // 1879361886 Fire Opal Steel Ring of Mordor's Bane BonA
    // 1879361868 Amethyst Mithril Ring of Mordor's Bane BonA
    // 1879362218 Alloyed Gold Bracer of Mordor's Bane BonA
    // 1879361866 Polished Silver Bracer of Mordor's Bane BonA
    // 1879362188 Opal Gold Stud of Mordor's Bane BonA
    // 1879361829 Rhodalite Silver Stud of Mordor's Bane BonA
    // 1879361847 Noble Emblem of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879361818, 1879361886, 1879361868, 1879362218,
          1879361866, 1879362188, 1879361829, 1879361847 };
      _injector.injectComment(JEWELS_COMMENT,mordorBane);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,mordorBane);
    }

    // Bind on Acquire 2
    {
      // 1879362195 Polished Torc of Mordor's Bane BonA
      // 1879362180 Fire Opal Steel Ring of Mordor's Bane BonA
      // 1879362230 Amethyst Mithril Ring of Mordor's Bane BonA
      // 1879362231 Polished Silver Bracer of Mordor's Bane BonA
      // 1879362176 Rhodalite Silver Stud of Mordor's Bane BonA
      // 1879362175 Noble Emblem of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879362195, 1879362180, 1879362230, 1879362231, 1879362176, 1879362175 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,mordorBane);
    }

    int[] neck=new int[]{ 1879361364, 1879361818, 1879362195 };
    _injector.shareStats(neck);
    int[] finger1=new int[]{ 1879361406, 1879361886, 1879362180 };
    _injector.shareStats(finger1);
    int[] finger2=new int[]{ 1879361327, 1879361868, 1879362230 };
    _injector.shareStats(finger2);
    int[] wrist2=new int[]{ 1879361387, 1879361866, 1879362205 };
    _injector.shareStats(wrist2);
    int[] ear2=new int[]{ 1879361405, 1879361829, 1879362176 };
    _injector.shareStats(ear2);
    int[] pocket=new int[]{ 1879361326, 1879361847, 1879362175 };
    _injector.shareStats(pocket);
  }

  private void doAgility()
  {
  }

  private void doWill()
  {
  }
}
