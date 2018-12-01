package delta.games.lotro.tools.lore.items.complements;

/**
 * Adds complements on the rewards of the Allegiance quests (chapter 4 and 7 for each race).
 * @author DAM
 */
public class MordorAllegianceRewards
{
  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MordorAllegianceRewards(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doDwarves();
    doElves();
    doMen();
    doHobbits();
  }

  private void doDwarves()
  {
    // Rings
    // 1879354795 Agile Ring of the Strength of Dwarves
    // 1879354797 Protective Ring of the Strength of Dwarves
    // 1879354801 Strong Ring of the Strength of Dwarves
    // 1879354803 Soft Ring of the Strength of Dwarves
    // 1879354810 Nimble Ring of the Strength of Dwarves
    // 1879354812 Clever Ring of the Strength of Dwarves
    {
      String ringComment="Dwarves Allegiance Quest: Chapter 4: Treachery in Mordor";
      int[] rings=new int[]{ 1879354795, 1879354797, 1879354801, 1879354803, 1879354810, 1879354812 };
      _injector.injectComment(ringComment,rings);
    }

    // Cloaks
    // 1879354776 Strong Cloak of the Strength of Dwarves
    // 1879354777 Clever Cloak of the Strength of Dwarves
    // 1879354781 Protective Cloak of the Strength of Dwarves
    // 1879354785 Nimble Cloak of the Strength of Dwarves
    // 1879354788 Soft Cloak of the Strength of Dwarves
    // 1879354792 Agile Cloak of the Strength of Dwarves
    {
      String cloakComment="Dwarves Allegiance Quest: Chapter 7: A True Friend of Durin's Folk";
      int[] cloaks=new int[]{ 1879354776, 1879354777, 1879354781, 1879354785, 1879354788, 1879354792 };
      _injector.injectComment(cloakComment,cloaks);
    }
  }

  private void doElves()
  {
    // Rings
    // 1879354793 Agile Ring of Enduring Grace
    // 1879354798 Strong Ring of Enduring Grace
    // 1879354799 Soft Ring of Enduring Grace
    // 1879354802 Nimble Ring of Enduring Grace
    // 1879354804 Protective Ring of Enduring Grace
    // 1879354816 Clever Ring of Enduring Grace
    {
      String ringComment="Elves Allegiance Quest: Chapter 4: The Wandering and the Lost";
      int[] rings=new int[]{ 1879354793, 1879354798, 1879354799, 1879354802, 1879354804, 1879354816 };
      _injector.injectComment(ringComment,rings);
    }

    // Cloaks
    // 1879354769 Agile Cloak of Enduring Grace
    // 1879354770 Nimble Cloak of Enduring Grace
    // 1879354771 Soft Cloak of Enduring Grace
    // 1879354784 Protective Cloak of Enduring Grace
    // 1879354787 Clever Cloak of Enduring Grace
    // 1879354790 Strong Cloak of Enduring Grace
    {
      String cloakComment="Elves Allegiance Quest: Chapter 7: Chapter 7: The Bow is Drawn";
      int[] cloaks=new int[]{ 1879354769, 1879354770, 1879354771, 1879354784, 1879354787, 1879354790 };
      _injector.injectComment(cloakComment,cloaks);
    }
  }

  private void doMen()
  {
    // Rings
    // 1879354796 Strong Ring of the Hope of Men
    // 1879354800 Nimble Ring of the Hope of Men
    // 1879354807 Protective Ring of the Hope of Men
    // 1879354808 Agile Ring of the Hope of Men
    // 1879354809 Clever Ring of the Hope of Men
    // 1879354815 Soft Ring of the Hope of Men
    {
      String ringComment="Men Allegiance Quest: Chapter 4: Judgements of the King";
      int[] rings=new int[]{ 1879354796, 1879354800, 1879354807, 1879354808, 1879354809, 1879354815 };
      _injector.injectComment(ringComment,rings);
    }

    // Cloaks
    // 1879354772 Strong Cloak of the Hope of Men
    // 1879354775 Clever Cloak of the Hope of Men
    // 1879354779 Protective Cloak of the Hope of Men
    // 1879354782 Agile Cloak of the Hope of Men
    // 1879354786 Soft Cloak of the Hope of Men
    // 1879354789 Nimble Cloak of the Hope of Men
    {
      String cloakComment="Men Allegiance Quest: Chapter 7: A True Friend of Gondor";
      int[] cloaks=new int[]{ 1879354772, 1879354775, 1879354779, 1879354782, 1879354786, 1879354789 };
      _injector.injectComment(cloakComment,cloaks);
    }
  }

  private void doHobbits()
  {
    // Rings
    // 1879354794 Protective Ring of Good Company
    // 1879354805 Clever Ring of Good Company
    // 1879354806 Agile Ring of Good Company
    // 1879354811 Nimble Ring of Good Company
    // 1879354813 Soft Ring of Good Company
    // 1879354814 Strong Ring of Good Company
    {
      String ringComment="Hobbits Allegiance Quest: Chapter 4: Deeper In";
      int[] rings=new int[]{ 1879354794, 1879354805, 1879354806, 1879354811, 1879354813, 1879354814 };
      _injector.injectComment(ringComment,rings);
    }

    // Cloaks
    // 1879354773 Nimble Cloak of Good Company
    // 1879354774 Soft Cloak of Good Company
    // 1879354778 Clever Cloak of Good Company
    // 1879354780 Strong Cloak of Good Company
    // 1879354783 Agile Cloak of Good Company
    // 1879354791 Protective Cloak of Good Company
    {
      String cloakComment="Hobbits Allegiance Quest: Chapter 7: World Behind and Home Ahead";
      int[] cloaks=new int[]{ 1879354773, 1879354774, 1879354778, 1879354780, 1879354783, 1879354791 };
      _injector.injectComment(cloakComment,cloaks);
    }
  }
}
