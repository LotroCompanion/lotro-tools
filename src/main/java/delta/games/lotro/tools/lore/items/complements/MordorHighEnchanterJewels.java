package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on jewels from the 'High Enchanter' of Mordor.
 * @author DAM
 */
public class MordorHighEnchanterJewels
{
  private static final String JEWELS_COMMENT="Barter Mordor High Enchanter: 330 Ash";
  private static final String YELLOW_RING_COMMENT="Barter Mordor High Enchanter: 2000 Ash or 800 in upgrade";
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
    doMight();
    doAgility();
    doWill();
  }

  private void doMight()
  {
    doMightYellowRing();
    doMightVanguard();
    doMightMordorBane();
  }

  private void doMightYellowRing()
  {
    // 1879361965 Turca (330) BonAonA
    int[] turca=new int[]{ 1879361965 };
    _injector.injectComment(YELLOW_RING_COMMENT,turca);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,turca);
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
    doAgilityYellowRing();
    doAgilityVanguard();
    doAgilityMordorBane();
  }

  private void doAgilityYellowRing()
  {
    // 1879361966 Linta (330) BonAonA
    int[] linta=new int[]{ 1879361966 };
    _injector.injectComment(YELLOW_RING_COMMENT,linta);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,linta);
  }

  private void doAgilityVanguard()
  {
    // Barter Incomparable Jewels (Vanguard)
    // Bind to Account on Acquire
    {
      // 1879361337 Graceful Pendant of the Expedition's Vanguard BonAonA
      // 1879361301 Fire Opal Gold Ring of the Expedition's Vanguard BonAonA
      // 1879361373 Amethyst Silver Ring of the Expedition's Vanguard BonAonA
      // 1879361407 Swift Silver Bracelet of the Expedition's Vanguard BonAonA
      // 1879361300 Rhodalite Silver Earring of the Expedition's Vanguard BonAonA
      // 1879361374 Momentous Locket of the Expedition's Vanguard BonAonA
      int[] vanguardBAonA=new int[]{ 1879361337, 1879361301, 1879361373, 1879361407, 1879361300, 1879361374 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE, vanguardBAonA);
    }
    // Bind on Acquire 1
    {
      // 1879361789 Graceful Pendant of the Expedition's Vanguard BonA
      // 1879361786 Fire Opal Gold Ring of the Expedition's Vanguard BonA
      // 1879361776 Amethyst Silver Ring of the Expedition's Vanguard BonA
      // 1879362212 Quick Gold Bracelet of the Expedition's Vanguard BonA
      // 1879361873 Swift Silver Bracelet of the Expedition's Vanguard BonA
      // 1879362206 Opal Gold Earring of the Expedition's Vanguard BonA
      // 1879361784 Rhodalite Silver Earring of the Expedition's Vanguard BonA
      // 1879361781 Momentous Locket of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879361789, 1879361786, 1879361776, 1879362212,
          1879361873, 1879362206, 1879361784, 1879361781 };
      _injector.injectComment(JEWELS_COMMENT,vanguard);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
    }
    // Bind on Acquire 2
    {
      // 1879362220 Graceful Pendant of the Expedition's Vanguard BonA
      // 1879362213 Fire Opal Gold Ring of the Expedition's Vanguard BonA
      // 1879362226 Amethyst Silver Ring of the Expedition's Vanguard BonA
      // 1879362203 Swift Silver Bracelet of the Expedition's Vanguard BonA
      // 1879362178 Rhodalite Silver Earring of the Expedition's Vanguard BonA
      // 1879362194 Momentous Locket of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879362220, 1879362213, 1879362226, 1879362203, 1879362178, 1879362194 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
    }

    // Share stats
    int[] neck=new int[]{ 1879361337, 1879361789, 1879362220 };
    _injector.shareStats(neck);
    int[] finger1=new int[]{ 1879361301, 1879361786, 1879362213 };
    _injector.shareStats(finger1);
    int[] finger2=new int[]{ 1879361373, 1879361776, 1879362226 };
    _injector.shareStats(finger2);
    int[] wrist2=new int[]{ 1879361407, 1879361873, 1879362203 };
    _injector.shareStats(wrist2);
    int[] ear2=new int[]{ 1879361300, 1879361784, 1879362178 };
    _injector.shareStats(ear2);
    int[] pocket=new int[]{ 1879361374, 1879361781, 1879362194 };
    _injector.shareStats(pocket);
  }

  private void doAgilityMordorBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
    // Bind to Account on Acquire
    {
      // 1879361349 Graceful Pendant of Mordor's Bane BonAonA
      // 1879361408 Fire Opal Gold Ring of Mordor's Bane BonAonA
      // 1879361339 Amethyst Silver Ring of Mordor's Bane BonAonA
      // 1879361372 Swift Silver Bracelet of Mordor's Bane BonAonA
      // 1879361302 Rhodalite Silver Earring of Mordor's Bane BonAonA
      // 1879361338 Momentous Locket of Mordor's Bane BonAonA
      int[] mordorBaneBAonA=new int[]{ 1879361349, 1879361408, 1879361339, 1879361372, 1879361302, 1879361338 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE, mordorBaneBAonA);
    }
    // Bind on Acquire 1
    {
      // 1879361871 Graceful Pendant of Mordor's Bane BonA
      // 1879361768 Fire Opal Gold Ring of Mordor's Bane BonA
      // 1879361812 Amethyst Silver Ring of Mordor's Bane BonA
      // 1879362238 Quick Gold Bracelet of Mordor's Bane BonA
      // 1879361888 Swift Silver Bracelet of Mordor's Bane BonA
      // 1879362199 Opal Gold Earring of Mordor's Bane BonA
      // 1879361831 Rhodalite Silver Earring of Mordor's Bane BonA
      // 1879361852 Momentous Locket of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879361871, 1879361768, 1879361812, 1879362238,
          1879361888, 1879362199, 1879361831, 1879361852 };
      _injector.injectComment(JEWELS_COMMENT,mordorBane);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,mordorBane);
    }
    // Bind on Acquire 2
    {
      // 1879362177 Graceful Pendant of Mordor's Bane BonA
      // 1879362202 Fire Opal Gold Ring of Mordor's Bane BonA
      // 1879362210 Amethyst Silver Ring of Mordor's Bane BonA
      // 1879362227 Swift Silver Bracelet of Mordor's Bane BonA
      // 1879362193 Rhodalite Silver Earring of Mordor's Bane BonA
      // 1879362208 Momentous Locket of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879362177, 1879362202, 1879362210, 1879362227, 1879362193, 1879362208 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,mordorBane);
    }

    int[] neck=new int[]{ 1879361349, 1879361871, 1879362177 };
    _injector.shareStats(neck);
    int[] finger1=new int[]{ 1879361408, 1879361768, 1879362202 };
    _injector.shareStats(finger1);
    int[] finger2=new int[]{ 1879361339, 1879361812, 1879362210 };
    _injector.shareStats(finger2);
    int[] wrist2=new int[]{ 1879361372, 1879361888, 1879362227 };
    _injector.shareStats(wrist2);
    int[] ear2=new int[]{ 1879361302, 1879361831, 1879362193 };
    _injector.shareStats(ear2);
    int[] pocket=new int[]{ 1879361338, 1879361852, 1879362208 };
    _injector.shareStats(pocket);
  }

  private void doWill()
  {
    doWillYellowRing();
    doWillVanguard();
    doWillMordorBane();
  }

  private void doWillYellowRing()
  {
    // 1879361964 Iswa (330) BonAonA
    int[] iswa=new int[]{ 1879361964 };
    _injector.injectComment(YELLOW_RING_COMMENT,iswa);
    _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,iswa);
  }

  private void doWillVanguard()
  {
    // Barter Incomparable Jewels (Vanguard)
    // Bind to Account on Acquire
    {
      // 1879361245 Filigree Necklace of the Expedition's Vanguard BonAonA
      // 1879361308 Runed Jade Ring of the Expedition's Vanguard BonAonA
      // 1879361388 Mysterious Onyx Ring of the Expedition's Vanguard BonAonA
      // 1879361409 Twined Silver Armlet of the Expedition's Vanguard BonAonA
      // 1879361307 Amethyst Silver Ear Cuff of the Expedition's Vanguard BonAonA
      // 1879361252 Glowing Phial of the Expedition's Vanguard BonAonA
      int[] vanguardBAonA=new int[]{ 1879361245, 1879361308, 1879361388, 1879361409, 1879361307, 1879361252 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE, vanguardBAonA);
    }
    // Bind on Acquire 1
    {
      // 1879361804 Filigree Necklace of the Expedition's Vanguard BonA
      // 1879361814 Runed Jade Ring of the Expedition's Vanguard BonA
      // 1879361867 Mysterious Onyx Ring of the Expedition's Vanguard BonA
      // 1879362211 Intricate Gold Armlet of the Expedition's Vanguard
      // 1879361798 Twined Silver Armlet of the Expedition's Vanguard BonA
      // 1879362222 Fire Opal Gold Ear Cuff of the Expedition's Vanguard BonA
      // 1879361817 Amethyst Silver Ear Cuff of the Expedition's Vanguard BonA
      // 1879361774 Glowing Phial of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879361804, 1879361814, 1879361867, 1879362211,
          1879361798, 1879362222, 1879361817, 1879361774 };
      _injector.injectComment(JEWELS_COMMENT,vanguard);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
    }
    // Bind on Acquire 2
    {
      // 1879362192 Filigree Necklace of the Expedition's Vanguard BonA
      // 1879362179 Runed Jade Ring of the Expedition's Vanguard BonA
      // 1879362191 Mysterious Onyx Ring of the Expedition's Vanguard BonA
      // 1879362223 Twined Silver Armlet of the Expedition's Vanguard BonA
      // 1879362235 Amethyst Silver Ear Cuff of the Expedition's Vanguard BonA
      // 1879362219 Glowing Phial of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879362192, 1879362179, 1879362191, 1879362223, 1879362235, 1879362219 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard);
    }

    int[] neck=new int[]{ 1879361245, 1879361804, 1879362192 };
    _injector.shareStats(neck);
    int[] finger1=new int[]{ 1879361308, 1879361814, 1879362179 };
    _injector.shareStats(finger1);
    int[] finger2=new int[]{ 1879361388, 1879361867, 1879362191 };
    _injector.shareStats(finger2);
    int[] wrist2=new int[]{ 1879361409, 1879361798, 1879362223 };
    _injector.shareStats(wrist2);
    int[] ear2=new int[]{ 1879361307, 1879361817, 1879362235 };
    _injector.shareStats(ear2);
    int[] pocket=new int[]{ 1879361252, 1879361774, 1879362219 };
    _injector.shareStats(pocket);
  }

  private void doWillMordorBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
    // Bind to Account on Acquire
    {
      // 1879361355 Filigree Necklace of Mordor's Bane BonAonA
      // 1879361411 Runed Jade Ring of Mordor's Bane BonAonA
      // 1879361331 Mysterious Onyx Ring of Mordor's Bane BonAonA
      // 1879361389 Twined Silver Armlet of Mordor's Bane BonAonA
      // 1879361410 Amethyst Silver Ear Cuff of Mordor's Bane BonAonA
      // 1879361247 Glowing Phial of Mordor's Bane BonAonA
      int[] mordorBaneBAonA=new int[]{ 1879361355, 1879361411, 1879361331, 1879361389, 1879361410, 1879361247 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE, mordorBaneBAonA);
    }
    // Bind on Acquire 1
    {
      // 1879361882 Filigree Necklace of Mordor's Bane BonA
      // 1879361761 Runed Jade Ring of Mordor's Bane BonA
      // 1879361823 Mysterious Onyx Ring of Mordor's Bane BonA
      // 1879362215 Intricate Gold Armlet of Mordor's Bane BonA
      // 1879361770 Twined Silver Armlet of Mordor's Bane BonA
      // 1879362181 Fire Opal Gold Ear Cuff of Mordor's Bane BonA
      // 1879361822 Amethyst Silver Ear Cuff of Mordor's Bane BonA
      // 1879361856 Glowing Phial of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879361882, 1879361761, 1879361823, 1879362215,
          1879361770, 1879362181, 1879361822, 1879361856 };
      _injector.injectComment(JEWELS_COMMENT,mordorBane);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,mordorBane);
    }
    // Bind on Acquire 2
    {
      // 1879362187 Filigree Necklace of Mordor's Bane BonA
      // 1879362198 Runed Jade Ring of Mordor's Bane BonA
      // 1879362190 Mysterious Onyx Ring of Mordor's Bane BonA
      // 1879362228 Twined Silver Armlet of Mordor's Bane BonA
      // 1879362232 Amethyst Silver Ear Cuff of Mordor's Bane BonA
      // 1879362201 Glowing Phial of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879362187, 1879362198, 1879362190, 1879362228, 1879362232, 1879362201 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,mordorBane);
    }

    int[] neck=new int[]{ 1879361355, 1879361882, 1879362187 };
    _injector.shareStats(neck);
    int[] finger1=new int[]{ 1879361411, 1879361761, 1879362198 };
    _injector.shareStats(finger1);
    int[] finger2=new int[]{ 1879361331, 1879361823, 1879362190 };
    _injector.shareStats(finger2);
    int[] wrist2=new int[]{ 1879361389, 1879361770, 1879362228 };
    _injector.shareStats(wrist2);
    int[] ear2=new int[]{ 1879361410, 1879361822, 1879362232 };
    _injector.shareStats(ear2);
    int[] pocket=new int[]{ 1879361247, 1879361856, 1879362201 };
    _injector.shareStats(pocket);
  }
}
