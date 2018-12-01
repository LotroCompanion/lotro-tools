package delta.games.lotro.tools.lore.items.complements;

/**
 * Adds complements on items from the 'Keeper of Mysteries' of Mordor.
 * @author DAM
 */
public class MordorKeeperOfMysteriesItems
{
  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MordorKeeperOfMysteriesItems(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doHeavy();
    doMedium();
    doMediumMight();
    doLight();
  }

  private void doHeavy()
  {
    String comment="Barter Mordor Keeper of Mysteries: 1750 Ash";
    // Barter Incomparable Armor (Abyss)
    {
      // 1879349980 Engraved Pauldrons of the Abyss
      // 1879350062 Buttressed Chestplate of the Abyss
      // 1879350277 Articulated Gauntlets of the Abyss
      // 1879350187 Burnished Greaves of the Abyss
      // 1879350189 Hardened Sabatons of the Abyss
      int[] abyss=new int[]{ 1879349980, 1879350062, 1879350277, 1879350187, 1879350189 };
      _injector.injectComment(comment,abyss);
    }
    // Barter Incomparable Armor (Wyrm)
    {
      // 1879350024 Engraved Pauldrons of the Wyrm
      // 1879349900 Buttressed Chestplate of the Wyrm
      // 1879350128 Burnished Greaves of the Wyrm
      // 1879350039 Articulated Gauntlets of the Wyrm
      // 1879350199 Hardened Sabatons of the Wyrm
      int[] wyrm=new int[]{ 1879350024, 1879349900, 1879350128, 1879350039, 1879350199 };
      _injector.injectComment(comment,wyrm);
    }
  }

  private void doMedium()
  {
    String comment="Barter Mordor Keeper of Mysteries: 1750 Ash";
    // Barter Incomparable Armor (Abyss)
    {
      // 1879349997 Rivetted Camail of the Abyss
      // 1879350067 Reinforced Hauberk of the Abyss
      // 1879350173 Quick Gages of the Abyss
      // 1879350226 Agile Leggings of the Abyss
      // 1879350192 Nimble Boots of the Abyss
      int[] abyss=new int[]{ 1879349997, 1879350067, 1879350173, 1879350226, 1879350192 };
      _injector.injectComment(comment,abyss);
    }
    // Barter Incomparable Armor (Wyrm)
    {
      // 1879350045 Rivetted Camail of the Wyrm
      // 1879350267 Reinforced Hauberk of the Wyrm
      // 1879350089 Quick Gages of the Wyrm
      // 1879349968 Agile Leggings of the Wyrm
      // 1879350270 Nimble Boots of the Wyrm
      int[] wyrm=new int[]{ 1879350045, 1879350267, 1879350089, 1879349968, 1879350270 };
      _injector.injectComment(comment,wyrm);
    }
  }

  private void doMediumMight()
  {
    String comment="Barter Mordor Keeper of Mysteries: 1750 Ash";
    // Barter Incomparable Armor (Abyss)
    {
      // 1879350279 Rivetted Camail of the Abyss
      // 1879350142 Double-mail Hauberk of the Abyss
      // 1879350230 Mighty Gages of the Abyss
      // 1879350112 Hardened Leggings of the Abyss
      // 1879350054 Forceful Boots of the Abyss
      int[] abyss=new int[]{ 1879350279, 1879350142, 1879350230, 1879350112, 1879350054 };
      _injector.injectComment(comment,abyss);
    }
    // Barter Incomparable Armor (Wyrm)
    {
      // 1879350083 Rivetted Camail of the Wyrm
      // 1879350135 Double-mail Hauberk of the Wyrm
      // 1879350080 Mighty Gages of the Wyrm
      // 1879350274 Hardened Leggings of the Wyrm
      // 1879350198 Forceful Boots of the Wyrm
      int[] wyrm=new int[]{ 1879350083, 1879350135, 1879350080, 1879350274, 1879350198 };
      _injector.injectComment(comment,wyrm);
    }
  }

  private void doLight()
  {
    String comment="Barter Mordor Keeper of Mysteries: 1750 Ash";
    // Barter Incomparable Armor (Abyss)
    {
      // 1879350261 Embossed Mantle of the Abyss
      // 1879350180 Woven Vest of the Abyss
      // 1879350159 Runed Gloves of the Abyss
      // 1879349986 Belted Trousers of the Abyss
      // 1879350005 Rugged Shoes of the Abyss
      int[] abyss=new int[]{ 1879350261, 1879350180, 1879350159, 1879349986, 1879350005 };
      _injector.injectComment(comment,abyss);
    }
    // Barter Incomparable Armor (Wyrm)
    {
      // 1879349974 Embossed Mantle of the Wyrm
      // 1879349941 Woven Vest of the Wyrm
      // 1879350265 Runed Gloves of the Wyrm
      // 1879350049 Belted Trousers of the Wyrm
      // 1879350118 Rugged Shoes of the Wyrm
      int[] wyrm=new int[]{ 1879349974, 1879349941, 1879350265, 1879350049, 1879350118 };
      _injector.injectComment(comment,wyrm);
    }
  }
}
