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

    // Regions:
    // - Eriador
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
    // - Rhovanion
    parser.doCategory("Moria_Deeds");
    parser.doCategory("Lothlórien_Deeds");
    parser.doCategory("Mirkwood_Deeds");
    parser.doCategory("The_Great_River_Deeds");
    parser.doCategory("East_Rohan_Deeds");
    parser.doCategory("Wildermore_Deeds");
    parser.doCategory("West_Rohan_Deeds");
    // - Gondor
    parser.doCategory("Western_Gondor_Deeds");
    parser.doCategory("Central_Gondor_Deeds");
    parser.doCategory("Eastern_Gondor_Deeds");
    parser.doCategory("Old_Anórien_Deeds");
    parser.doCategory("Far_Anórien_Deeds");
    parser.doCategory("March_of_the_King_Deeds");
    parser.doCategory("The_Wastes_Deeds");
    // - Mordor
    parser.doCategory("Gorgoroth_Deeds");

    // PVP
    parser.doCategory("Freep_Deeds");
    parser.doCategory("Creep_Quest_Deeds");
    parser.doCategory("Creep_Slayer_Deeds");

    // Instances:
    // - Shadows of Angmar
    parser.doCategory("Barad_Gúlaran_Deeds");
    parser.doCategory("Carn_Dûm_Deeds");
    parser.doCategory("The_Halls_of_Night_Deeds");
    parser.doCategory("Urugarth_Deeds");
    parser.doCategory("Annúminas_Instance_Deeds");
    parser.doCategory("Fornost_Slayer_Deeds");
    parser.doCategory("Fornost_Deeds");
    parser.doCategory("Inn_of_the_Forsaken_Deeds");
    parser.doCategory("The_School_at_Tham_Mírdain_Deeds");
    parser.doCategory("The_Library_at_Tham_Mírdain_Deeds");
    parser.doCategory("Tham_Mírdain_Deeds");
    parser.doCategory("The_Great_Barrow_Deeds");
    parser.doCategory("Shadows_of_Angmar_Instance_Meta_Deeds");
    // - Moria
    // -- Mines of Moria
    parser.doCategory("Dark_Delvings_Slayer_Deeds");
    parser.doCategory("Dark_Delvings_Deeds");
    parser.doCategory("Forgotten_Treasury_Slayer_Deeds");
    parser.doCategory("Forgotten_Treasury_Deeds");
    parser.doCategory("The_Sixteenth_Hall_Slayer_Deeds");
    parser.doCategory("The_Sixteenth_Hall_Deeds");
    parser.doCategory("The_Vile_Maw_Slayer_Deeds");
    parser.doCategory("The_Vile_Maw_Deeds");
    parser.doCategory("Skûmfil_Slayer_Deeds");
    parser.doCategory("Skûmfil_Deeds");
    parser.doCategory("Fil_Gashan_Slayer_Deeds");
    parser.doCategory("Fil_Gashan_Deeds");
    parser.doCategory("Forges_of_Khazad-dûm_Slayer_Deeds");
    parser.doCategory("Forges_of_Khazad-dûm_Deeds");
    parser.doCategory("The_Grand_Stair_Slayer_Deeds");
    parser.doCategory("The_Grand_Stair_Deeds");
    parser.doCategory("Mines_of_Moria_Instance_Deeds");
    // -- Scourge of Khazad-dûm
    parser.doCategory("Dâr_Narbugud_Slayer_Deeds");
    parser.doCategory("Dâr_Narbugud_Deeds");
    parser.doCategory("Halls_of_Crafting_Slayer_Deeds");
    parser.doCategory("Halls_of_Crafting_Deeds");
    parser.doCategory("The_Mirror-halls_of_Lumul-nar_Slayer_Deeds");
    parser.doCategory("The_Mirror-halls_of_Lumul-nar_Deeds");
    parser.doCategory("The_Water_Wheels:_Nalâ-dûm_Slayer_Deeds");
    parser.doCategory("The_Water_Wheels:_Nalâ-dûm_Deeds");
    parser.doCategory("Scourge_of_Khazad-dûm_Instance_Deeds");
    parser.doCategory("Moria_Instance_Deeds");
    // - Tower of Dol Guldur
    parser.doCategory("Barad_Guldur_Lore_Deeds");
    parser.doCategory("Barad_Guldur_Slayer_Deeds");
    parser.doCategory("Barad_Guldur_Deeds");
    parser.doCategory("Dungeons_of_Dol_Guldur_Explorer_Deeds");
    parser.doCategory("Dungeons_of_Dol_Guldur_Lore_Deeds");
    parser.doCategory("Dungeons_of_Dol_Guldur_Slayer_Deeds");
    parser.doCategory("Dungeons_of_Dol_Guldur_Deeds");
    parser.doCategory("Sammath_Gûl_Lore_Deeds");
    parser.doCategory("Sammath_Gûl_Slayer_Deeds");
    parser.doCategory("Sammath_Gûl_Deeds");
    parser.doCategory("Sword-hall_of_Dol_Guldur_Slayer_Deeds");
    parser.doCategory("Sword-hall_of_Dol_Guldur_Deeds");
    parser.doCategory("Warg-pens_of_Dol_Guldur_Slayer_Deeds");
    parser.doCategory("Warg-pens_of_Dol_Guldur_Deeds");
    parser.doCategory("Tower_of_Dol_Guldur_Deeds");
    // - In Their Absence
    parser.doCategory("Lost_Temple_Explorer_Deeds");
    parser.doCategory("Lost_Temple_Slayer_Deeds");
    parser.doCategory("Lost_Temple_Deeds");
    parser.doCategory("Northcotton_Farm_Deeds");
    parser.doCategory("Ost_Dunhoth_Lore_Deeds");
    parser.doCategory("Ost_Dunhoth_Meta_Deeds");
    parser.doCategory("Ost_Dunhoth_Slayer_Deeds");
    parser.doCategory("Ost_Dunhoth_Deeds");
    parser.doCategory("Stoneheight_Deeds");
    parser.doCategory("Sâri-surma_Slayer_Deeds");
    parser.doCategory("Sâri-surma_Deeds");
    parser.doCategory("In_Their_Absence_Deeds");
    // - Rise of Isengard
    parser.doCategory("Dargnákh_Unleashed_Deeds");
    parser.doCategory("Fangorn's_Edge_Deeds");
    parser.doCategory("Pits_of_Isengard_Slayer_Deeds");
    parser.doCategory("Pits_of_Isengard_Deeds");
    parser.doCategory("The_Foundry_Deeds");
    parser.doCategory("The_Tower_of_Orthanc_Deeds");
    parser.doCategory("Draigoch's_Lair_Deeds");
    parser.doCategory("Rise_of_Isengard_Deeds");
    // - Road to Erebor
    parser.doCategory("Flight_to_the_Lonely_Mountain_Deeds");
    parser.doCategory("Iorbar's_Peak_Deeds");
    parser.doCategory("Seat_of_the_Great_Goblin_Deeds");
    parser.doCategory("The_Fires_of_Smaug_Deeds");
    parser.doCategory("The_Bells_of_Dale_Deeds");
    parser.doCategory("The_Battle_for_Erebor_Deeds");
    parser.doCategory("Webs_of_the_Scuttledells_Deeds");
    parser.doCategory("The_Road_to_Erebor_Deeds");
    // - Ashes of Osgiliath
    parser.doCategory("Sunken_Labyrinth_Deeds");
    parser.doCategory("The_Dome_of_Stars_Deeds");
    parser.doCategory("The_Ruined_City_Deeds");
    parser.doCategory("Ashes_of_Osgiliath_Deeds");
    // - Battle of Pelennor
    parser.doCategory("Blood_of_the_Black_Serpent_Deeds");
    parser.doCategory("The_Quays_of_the_Harlond_Deeds");
    parser.doCategory("The_Silent_Street_Deeds");
    parser.doCategory("Throne_of_the_Dread_Terror_Deeds");
    // - Plateau of Gorgoroth
    parser.doCategory("The_Court_of_Seregost_Deeds");
    parser.doCategory("Dungeons_of_Naerband_Deeds");

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
    parser.doCategory("Hobnanigans_Deeds");

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
    parser.doCategory("Obsolete_Deeds");
    parser.doCategory("Chicken_Play_Deeds");
    parser.doCategory("Treasure_Caches");

    // Caution: these may bring already existing deeds:
    parser.doCategory("Hidden_Deeds");
    parser.doCategory("Meta_Deeds");
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
