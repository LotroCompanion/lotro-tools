package delta.games.lotro.tools.lore.items.complements;

/**
 * Adds complements on armors from the 'High Enchanter' of Mordor.
 * @author DAM
 */
public class MordorHighEnchanterArmors
{
  private static final String COMMENT="Barter Mordor High Enchanter: 420 Ash";

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MordorHighEnchanterArmors(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doHeavyArmor();
    doMediumArmor();
    doLightArmor();
  }

  private void doHeavyArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361726 Polished Helm of the Expedition's Vanguard - BonA
      // 1879361647 Engraved Pauldrons of the Expedition's Vanguard - BonA
      // 1879361723 Articulated Gauntlets of the Expedition's Vanguard - BonA
      // 1879361653 Hardened Sabatons of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361726, 1879361647, 1879361723, 1879361653 };
      _injector.injectComment(COMMENT,vanguard1);
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361672 Polished Helm of Mordor's Bane - BonA
      // 1879361745 Engraved Pauldrons of Mordor's Bane - BonA
      // 1879361703 Hardened Sabatons of Mordor's Bane - BonA
      // 1879361660 Articulated Gauntlets of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361672, 1879361745, 1879361703, 1879361660 };
      _injector.injectComment(COMMENT,bane1);
    }
    // Cloaks
    {
      // 1879361648 Embroidered Heavy Cloak of the Expedition's Vanguard - BonA
      // 1879361708 Embroidered Heavy Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361648, 1879361708 };
      _injector.injectComment(COMMENT,cloaks1);
    }
  }

  private void doMediumArmor()
  {
    doMediumMightArmor();
    doMediumAgilityArmor();
  }

  private void doMediumMightArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361750 Reinforced Coif of the Expedition's Vanguard - BonA
      // 1879361674 Rivetted Camail of the Expedition's Vanguard - BonA
      // 1879361732 Mighty Gages of the Expedition's Vanguard - BonA
      // 1879361680 Forceful Boots of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361750, 1879361674, 1879361732, 1879361680 };
      _injector.injectComment(COMMENT,vanguard1);
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361749 Reinforced Coif of Mordor's Bane - BonA
      // 1879361665 Rivetted Camail of Mordor's Bane - BonA
      // 1879361686 Mighty Gages of Mordor's Bane - BonA
      // 1879361704 Forceful Boots of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361749, 1879361665, 1879361686, 1879361704 };
      _injector.injectComment(COMMENT,bane1);
    }
    // Cloaks
    {
      // 1879361685 Embroidered Wool Cloak of the Expedition's Vanguard - BonA
      // 1879361737 Embroidered Wool Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361685, 1879361737 };
      _injector.injectComment(COMMENT,cloaks1);
    }
  }

  private void doMediumAgilityArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361649 Reinforced Coif of the Expedition's Vanguard - BonA
      // 1879361709 Rivetted Camail of the Expedition's Vanguard - BonA
      // 1879361690 Quick Gages of the Expedition's Vanguard - BonA
      // 1879361661 Nimble Boots of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361649, 1879361709, 1879361690, 1879361661 };
      _injector.injectComment(COMMENT,vanguard1);
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361691 Reinforced Coif of Mordor's Bane - BonA
      // 1879361734 Rivetted Camail of Mordor's Bane - BonA
      // 1879361657 Quick Gages of Mordor's Bane - BonA
      // 1879361755 Nimble Boots of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361691, 1879361734, 1879361657, 1879361755 };
      _injector.injectComment(COMMENT,bane1);
    }
    // Cloaks
    {
      // 1879361700 Embroidered Silk Cloak of the Expedition's Vanguard - BonA
      // 1879361677 Embroidered Silk Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361700, 1879361677 };
      _injector.injectComment(COMMENT,cloaks1);
    }
  }

  private void doLightArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361668 Gilded Cap of the Expedition's Vanguard - BonA
      // 1879361646 Embossed Mantle of the Expedition's Vanguard - BonA
      // 1879361662 Runed Gloves of the Expedition's Vanguard - BonA
      // 1879361733 Rugged Shoes of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361668, 1879361646, 1879361662, 1879361733 };
      _injector.injectComment(COMMENT,vanguard1);
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361675 Gilded Cap of Mordor's Bane - BonA
      // 1879361714 Embossed Mantle of Mordor's Bane - BonA
      // 1879361710 Runed Gloves of Mordor's Bane - BonA
      // 1879361650 Rugged Shoes of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361675, 1879361714, 1879361710, 1879361650 };
      _injector.injectComment(COMMENT,bane1);
    }
    // Cloaks
    {
      // 1879361738 Embroidered Light Cloak of the Expedition's Vanguard - BonA
      // 1879361689 Embroidered Light Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361738, 1879361689 };
      _injector.injectComment(COMMENT,cloaks1);
    }
  }
}
