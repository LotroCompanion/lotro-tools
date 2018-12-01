package delta.games.lotro.tools.lore.items.complements;

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
    doBeorning();
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
  }

  private void doMightVanguard()
  {
   // Barter Incomparable Jewels (Vanguard)
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
    }
  }

  private void doMightMordorBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
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
    }
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
  }

  private void doAgilityVanguard()
  {
    // Barter Incomparable Jewels (Vanguard)
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
    }
  }

  private void doAgilityMordorBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
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
    }
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
  }

  private void doWillVanguard()
  {
    // Barter Incomparable Jewels (Vanguard)
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
    }
  }

  private void doWillMordorBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
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
    }
  }

  private void doBeorning()
  {
    doBeorningVanguard();
    doBeorningMorborBane();
  }

  private void doBeorningVanguard()
  {
    // Barter Incomparable Jewels (Vanguard)
    // Bind on Acquire 1
    {
      // 1879361846 Polished Choker of the Expedition's Vanguard BonA
      // 1879361860 Peridot Oak Ring of the Expedition's Vanguard BonA
      // 1879361876 Jet Granite Ring of the Expedition's Vanguard BonA
      // 1879362237 Mighty Steel Bracelet of the Expedition's Vanguard BonA
      // 1879361766 Enduring Iron Bracelet of the Expedition's Vanguard BonA
      // 1879362229 Fire Opal Steel Earring of the Expedition's Vanguard BonA
      // 1879361806 Amethyst Iron Earring of the Expedition's Vanguard BonA
      // 1879361828 Timeworn Brooch of the Expedition's Vanguard BonA
      int[] vanguard=new int[]{ 1879361846, 1879361860, 1879361876, 1879362237,
          1879361766, 1879362229, 1879361806, 1879361828 };
      _injector.injectComment(JEWELS_COMMENT,vanguard);
    }
  }

  private void doBeorningMorborBane()
  {
    // Barter Incomparable Jewels (Mordor Bane)
    // Bind on Acquire 1
    {
      // 1879361793 Polished Choker of Mordor's Bane BonA
      // 1879361845 Peridot Oak Ring of Mordor's Bane BonA
      // 1879361864 Jet Granite Ring of Mordor's Bane BonA
      // 1879362234 Mighty Steel Bracelet of Mordor's Bane BonA
      // 1879361863 Enduring Iron Bracelet of Mordor's Bane BonA
      // 1879362214 Fire Opal Steel Earring of Mordor's Bane BonA
      // 1879361810 Amethyst Iron Earring of Mordor's Bane BonA
      // 1879361816 Timeworn Brooch of Mordor's Bane BonA
      int[] mordorBane=new int[]{ 1879361793, 1879361845, 1879361864, 1879362234,
          1879361863, 1879362214, 1879361810, 1879361816 };
      _injector.injectComment(JEWELS_COMMENT,mordorBane);
    }
  }
}
