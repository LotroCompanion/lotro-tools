package delta.games.lotro.tools.lore.deeds.lotrowiki;

/**
 * Download deeds data from the site lotro-wiki.
 * @author DAM
 */
public class MainLotroWikiDeedsLoader
{
  //private File _outDir;

  /**
   * Constructor.
   */
  public MainLotroWikiDeedsLoader()
  {
    //_outDir=new File("data/lore/deeds").getAbsoluteFile();
  }

  private void doIt()
  {
    LotroWikiSiteInterface lotroWiki=new LotroWikiSiteInterface();
    // Load a sample "deed category" URL
    LotroWikiDeedCategoryPageParser parser=new LotroWikiDeedCategoryPageParser(lotroWiki);

    parser.doCategory("Angmar_Deeds");
    parser.doCategory("The_Shire_Deeds");
    parser.doCategory("The_North_Downs_Deeds");
    parser.doCategory("The_Misty_Mountains_Deeds");
    parser.doCategory("The_Lone-lands_Deeds");
    parser.doCategory("Forochel_Deeds");
    parser.doCategory("Evendim_Deeds");
    // Ettenmoors_Deeds
    parser.doCategory("Eregion_Deeds");
    parser.doCategory("Ered_Luin_Deeds");
    parser.doCategory("Enedwaith_Deeds");
    parser.doCategory("Dunland_Deeds");
    parser.doCategory("Bree-land_Deeds");

    parser.doCategory("Moria_Deeds");
    parser.doCategory("Lothl%F3rien_Deeds");
    parser.doCategory("Mirkwood_Deeds");
    parser.doCategory("The_Great_River_Deeds");
    parser.doCategory("East_Rohan_Deeds");
    parser.doCategory("Wildermore_Deeds");
    parser.doCategory("West_Rohan_Deeds");

    parser.doCategory("Western_Gondor_Deeds");
    parser.doCategory("Central_Gondor_Deeds");
    parser.doCategory("Eastern_Gondor_Deeds");
    parser.doCategory("Old_An%F3rien_Deeds");
    parser.doCategory("Far_An%F3rien_Deeds");
    parser.doCategory("March_of_the_King_Deeds");
    parser.doCategory("The_Wastes_Deeds");
    parser.doCategory("Gorgoroth_Deeds");

    //Shadows_of_Angmar_Deeds
    parser.doCategory("Moria_Instance_Deeds");
    //parser.doCategory("Mines_of_Moria_Instance_Deeds");
    //parser.doCategory("Scourge_of_Khazad-dûm_Instance_Deeds");
    parser.doCategory("Tower_of_Dol_Guldur_Deeds");
    parser.doCategory("In_Their_Absence_Deeds");
    //Rise_of_Isengard_Deeds
    parser.doCategory("The_Road_to_Erebor_Deeds");
    parser.doCategory("Ashes_of_Osgiliath_Deeds");

    parser.doCategory("Hobby_Deeds");

    //Skirmish_Instances_Deeds
    //Skirmish_Lieutenants_Deeds
    //Fishing_Deeds
    //Social_Deeds
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainLotroWikiDeedsLoader().doIt();
  }
}
