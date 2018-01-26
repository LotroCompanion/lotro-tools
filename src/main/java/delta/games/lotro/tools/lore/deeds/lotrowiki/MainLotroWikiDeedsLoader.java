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

    // Class deeds
    parser.doCategory("Beorning_Deeds");
    parser.doCategory("Burglar_Deeds");
    parser.doCategory("Captain_Deeds");
    parser.doCategory("Champion_Deeds");
    parser.doCategory("Guardian_Deeds");
    parser.doCategory("Hunter_Deeds");
    parser.doCategory("Lore-master_Deeds");
    parser.doCategory("Minstrel_Deeds");
    parser.doCategory("Rune-keeper_Deeds");
    parser.doCategory("Warden_Deeds");

    parser.doCategory("Angmar_Deeds");
    parser.doCategory("The_Shire_Deeds");
    parser.doCategory("The_North_Downs_Deeds");
    parser.doCategory("The_Misty_Mountains_Deeds");
    parser.doCategory("The_Lone-lands_Deeds");
    parser.doCategory("Forochel_Deeds");
    parser.doCategory("Evendim_Deeds");
    parser.doCategory("Eregion_Deeds");
    parser.doCategory("Ered_Luin_Deeds");
    parser.doCategory("Enedwaith_Deeds");
    parser.doCategory("Dunland_Deeds");
    parser.doCategory("Bree-land_Deeds");

    parser.doCategory("Freep_Deeds");
    parser.doCategory("Creep_Quest_Deeds");
    parser.doCategory("Creep_Slayer_Deeds");

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

    // Shadows of Angmar
    parser.doCategory("Barad_G%FAlaran_Deeds");
    parser.doCategory("Carn_D%FBm_Deeds");
    parser.doCategory("The_Halls_of_Night_Deeds");
    parser.doCategory("Urugarth_Deeds");
    parser.doCategory("Ann%FAminas_Instance_Deeds");
    parser.doCategory("Fornost_Slayer_Deeds");
    parser.doCategory("Fornost_Deeds");
    parser.doCategory("Inn_of_the_Forsaken_Deeds");
    parser.doCategory("The_School_at_Tham_M%EDrdain_Deeds");
    parser.doCategory("The_Library_at_Tham_M%EDrdain_Deeds");
    parser.doCategory("Tham_M%EDrdain_Deeds");
    parser.doCategory("The_Great_Barrow_Deeds");
    parser.doCategory("Shadows_of_Angmar_Instance_Meta_Deeds");

    parser.doCategory("Moria_Instance_Deeds");
    //parser.doCategory("Mines_of_Moria_Instance_Deeds");
    //parser.doCategory("Scourge_of_Khazad-dûm_Instance_Deeds");
    parser.doCategory("Tower_of_Dol_Guldur_Deeds");
    parser.doCategory("In_Their_Absence_Deeds");

    // Rise of Isengard
    parser.doCategory("Dargn%E1kh_Unleashed_Deeds");
    parser.doCategory("Fangorn's_Edge_Deeds");
    parser.doCategory("Pits_of_Isengard_Deeds");
    parser.doCategory("Pits_of_Isengard_Slayer_Deeds");
    parser.doCategory("The_Foundry_Deeds");
    parser.doCategory("The_Tower_of_Orthanc_Deeds");
    parser.doCategory("Draigoch's_Lair_Deeds");
    parser.doCategory("Rise_of_Isengard_Deeds");

    parser.doCategory("The_Road_to_Erebor_Deeds");
    parser.doCategory("Ashes_of_Osgiliath_Deeds");

    // Hobby
    parser.doCategory("Hobby_Deeds");

    // Reputation
    parser.doCategory("Reputation_Deeds");

    // Events
    parser.doCategory("Frostbluff_Event_Deeds");
    parser.doCategory("Yuletide_Festival_Deeds");
    parser.doCategory("Summer_Festival_Deeds");
    parser.doCategory("Harvest_Festival_Deeds");
    parser.doCategory("Bree-land_Event_Deeds");
    parser.doCategory("Event_Deeds");

    // Racial deeds
    parser.doCategory("Beorning_(Race)_Deeds");
    parser.doCategory("Dwarf_Deeds");
    parser.doCategory("Elf_Deeds");
    parser.doCategory("High_Elf_Deeds");
    parser.doCategory("Hobbit_Deeds");
    parser.doCategory("Race_of_Man_Deeds");

    // Skirmish
    parser.doCategory("Skirmish_Deeds");
    parser.doCategory("Skirmish_Instances_Deeds");
    parser.doCategory("Skirmish_Lieutenants_Deeds");

    // Misc
    parser.doCategory("Social_Deeds");
    parser.doCategory("Epic_Deeds");
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
