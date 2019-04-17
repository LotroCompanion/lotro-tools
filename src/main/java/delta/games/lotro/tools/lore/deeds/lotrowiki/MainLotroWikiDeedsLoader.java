package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxies;
import delta.games.lotro.lore.deeds.DeedProxy;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.lore.deeds.DeedsInjector;
import delta.games.lotro.tools.lore.deeds.DeedsWriter;
import delta.games.lotro.tools.lore.deeds.checks.CheckDeedLinks;
import delta.games.lotro.tools.lore.deeds.checks.CheckItemRewardsInDeeds;
import delta.games.lotro.tools.lore.deeds.checks.NormalizeDeedNames;
import delta.games.lotro.tools.lore.deeds.checks.NormalizeDeedsText;
import delta.games.lotro.tools.lore.deeds.geo.GeoDeedsDataInjector;
import delta.games.lotro.tools.utils.lotrowiki.LotroWikiSiteInterface;

/**
 * Download deeds data from the site lotro-wiki.
 * @author DAM
 */
public class MainLotroWikiDeedsLoader
{
  private File _tmpFilesDir;

  /**
   * Constructor.
   */
  public MainLotroWikiDeedsLoader()
  {
    _tmpFilesDir=new File("data/deeds/tmp/lotrowiki").getAbsoluteFile();
  }

  private static final String INSTANCES_SEED="Instances:";
  private static final String SHADOWS_OF_ANGMAR_SEED=INSTANCES_SEED+"Shadows of Angmar:";
  private static final String MINES_OF_MORIA_SEED=INSTANCES_SEED+"Mines of Moria:";
  private static final String SCOURGE_OF_KHAZAD_DUM_SEED=INSTANCES_SEED+"Scourge of Khazad-dûm:";
  private static final String TOWER_OF_DOL_GULDUR_SEED=INSTANCES_SEED+"Tower of Dol Guldur:";
  private static final String IN_THEIR_ABSENCE=INSTANCES_SEED+"In Their Absence:";
  private static final String RISE_OF_ISENGARD=INSTANCES_SEED+"Rise of Isengard:";
  private static final String ROAD_TO_EREBOR_SEED=INSTANCES_SEED+"Road to Erebor:";
  private static final String ASHES_OF_OSGILIATH_SEED=INSTANCES_SEED+"Ashes of Osgiliath:";
  private static final String BATTLE_OF_PELENNOR_SEED=INSTANCES_SEED+"Battle of Pelennor:";
  private static final String PLATEAU_OF_GORGOROTH_SEED=INSTANCES_SEED+"Plateau of Gorgoroth:";

  private void doIt()
  {
    LotroWikiSiteInterface lotroWiki=new LotroWikiSiteInterface(_tmpFilesDir);
    // Load a sample "deed category" URL
    LotroWikiDeedCategoryPageParser parser=new LotroWikiDeedCategoryPageParser(lotroWiki,_tmpFilesDir);

    // Class deeds
    parser.doCategory("Beorning_Deeds",DeedType.CLASS,CharacterClass.BEORNING);
    parser.doCategory("Burglar_Deeds",DeedType.CLASS,CharacterClass.BURGLAR);
    parser.doCategory("Burglar_Meta_Deeds",DeedType.CLASS,CharacterClass.BURGLAR);
    parser.doCategory("Captain_Deeds",DeedType.CLASS,CharacterClass.CAPTAIN);
    parser.doCategory("Champion_Deeds",DeedType.CLASS,CharacterClass.CHAMPION);
    parser.doCategory("Guardian_Deeds",DeedType.CLASS,CharacterClass.GUARDIAN);
    parser.doCategory("Guardian_Meta_Deeds",DeedType.CLASS,CharacterClass.GUARDIAN);
    parser.doCategory("Hunter_Deeds",DeedType.CLASS,CharacterClass.HUNTER);
    parser.doCategory("Lore-master_Deeds",DeedType.CLASS,CharacterClass.LORE_MASTER);
    parser.doCategory("Lore-master_Meta_Deeds",DeedType.CLASS,CharacterClass.LORE_MASTER);
    parser.doCategory("Minstrel_Deeds",DeedType.CLASS,CharacterClass.MINSTREL);
    parser.doCategory("Rune-keeper_Deeds",DeedType.CLASS,CharacterClass.RUNE_KEEPER);
    parser.doCategory("Warden_Deeds",DeedType.CLASS,CharacterClass.WARDEN);
    parser.doCategory("Warden_Meta_Deeds",DeedType.CLASS,CharacterClass.WARDEN);

    // Regions:
    // - Eriador
    parser.doCategory("Angmar_Deeds",null,"Region:Angmar");
    parser.doCategory("The_Shire_Deeds",null,"Region:Shire");
    parser.doCategory("The_North_Downs_Deeds",null,"Region:North Downs");
    parser.doCategory("The_Misty_Mountains_Deeds",null,"Region:Misty Mountains");
    parser.doCategory("The_Lone-lands_Deeds",null,"Region:Lone-lands");
    parser.doCategory("Forochel_Deeds",null,"Region:Forochel");
    parser.doCategory("Evendim_Deeds",null,"Region:Evendim");
    parser.doCategory("Eregion_Deeds",null,"Region:Eregion");
    parser.doCategory("Ered_Luin_Deeds",null,"Region:Ered Luin");
    parser.doCategory("Enedwaith_Deeds",null,"Region:Enedwaith");
    parser.doCategory("Dunland_Deeds",null,"Region:Dunland");
    parser.doCategory("Bree-land_Deeds",null,"Region:Bree-land");
    parser.doCategory("The_Trollshaws_Deeds",null,"Region:Trollshaws");
    // - Rhovanion
    parser.doCategory("Moria_Deeds",null,"Region:Moria");
    parser.doCategory("Lothlórien_Deeds",null,"Region:Lothlórien");
    parser.doCategory("Mirkwood_Deeds",null,"Region:Mirkwood");
    parser.doCategory("The_Great_River_Deeds",null,"Region:Great River");
    parser.doCategory("East_Rohan_Deeds",null,"Region:East Rohan");
    parser.doCategory("Wildermore_Deeds",null,"Region:Wildermore");
    parser.doCategory("West_Rohan_Deeds",null,"Region:West Rohan");
    parser.doCategory("The_Helmingas_Deeds",DeedType.REPUTATION,"Region:West Rohan");
    parser.doCategory("West_Rohan_Reputation_Deeds",DeedType.REPUTATION,"Region:West Rohan");
    parser.doCategory("Eryn_Lasgalen_and_the_Dale-lands_Meta_Deeds",null,"Region:Strongholds of the North");
    parser.doCategory("Eryn_Lasgalen_and_the_Dale-lands_Quest_Deeds",DeedType.QUEST,"Region:Strongholds of the North");
    parser.doCategory("Eryn_Lasgalen_and_the_Dale-lands_Slayer_Deeds",DeedType.SLAYER,"Region:Strongholds of the North");
    parser.doCategory("Eryn_Lasgalen_Slayer_Deeds",DeedType.SLAYER,"Region:Strongholds of the North:Eryn Lasgalen");
    parser.doCategory("Eryn_Lasgalen_Quest_Deeds",DeedType.QUEST,"Region:Strongholds of the North:Eryn Lasgalen");
    parser.doCategory("Eryn_Lasgalen_Explorer_Deeds",DeedType.EXPLORER,"Region:Strongholds of the North:Eryn Lasgalen");
    parser.doCategory("Elves_of_Felegoth_Deeds",null,"Region:Strongholds of the North:Eryn Lasgalen");
    parser.doCategory("The_Dale-lands_Explorer_Deeds",DeedType.EXPLORER,"Region:Strongholds of the North:Dale-lands");
    parser.doCategory("Men_of_Dale_Deeds",null,"Region:Strongholds of the North:Dale-lands");
    parser.doCategory("Erebor_Explorer_Deeds",DeedType.EXPLORER,"Region:Strongholds of the North:Erebor");
    parser.doCategory("Erebor_Quest_Deeds",DeedType.QUEST,"Region:Strongholds of the North:Erebor");
    parser.doCategory("Dwarves_of_Erebor_Deeds",null,"Region:Strongholds of the North:Erebor");
    parser.doCategory("Ered_Mithrin_and_Withered_Heath_Slayer_Deeds",DeedType.SLAYER,"Region:Ered Mithrin and Withered Heath");
    // Type set to null because page contains more than just explorer deeds
    parser.doCategory("Ered_Mithrin_and_Withered_Heath_Explorer_Deeds",null,"Region:Ered Mithrin and Withered Heath");
    parser.doCategory("Ered_Mithrin_and_Withered_Heath_Lore_Deeds",DeedType.LORE,"Region:Ered Mithrin and Withered Heath");
    // Use slayer here because it contains only a meta-slayer deed
    parser.doCategory("Ered_Mithrin_and_Withered_Heath_Meta_Deeds",DeedType.SLAYER,"Region:Ered Mithrin and Withered Heath");
    //parser.doCategory("Grey_Mountains_Expedition_Deeds",null,"Region:Ered Mithrin and Withered Heath");

    // - Gondor
    parser.doCategory("Western_Gondor_Deeds",null,"Region:Western Gondor");
    parser.doCategory("Central_Gondor_Deeds",null,"Region:Central Gondor");
    parser.doCategory("Eastern_Gondor_Deeds",null,"Region:Eastern Gondor");
    parser.doCategory("Old_Anórien_Deeds",null,"Region:Old Anórien");
    parser.doCategory("Far_Anórien_Deeds",null,"Region:Far Anórien");
    parser.doCategory("March_of_the_King_Deeds",null,"Region:March of the King");
    parser.doCategory("The_Wastes_Deeds",null,"Region:The Wastes");
    parser.doCategory("Dol_Amroth_(Faction)_Deeds",DeedType.REPUTATION,"Region:Western Gondor");
    parser.doCategory("Pelargir_(Faction)_Deeds",DeedType.REPUTATION,"Region:Central Gondor");
    parser.doCategory("Rangers_of_Ithilien_Deeds",DeedType.REPUTATION,"Region:Eastern Gondor");
    parser.doCategory("Defenders_of_Minas_Tirith_Deeds",DeedType.REPUTATION,"Region:Old Anórien");
    parser.doCategory("Riders_of_Rohan_(Faction)_Deeds",DeedType.REPUTATION,"Region:Far Anórien");

    // - Mordor
    parser.doCategory("Udûn_Deeds",null,"Region:Gorgoroth:Udûn");
    parser.doCategory("Dor_Amarth_Deeds",null,"Region:Gorgoroth:Dor Amarth");
    parser.doCategory("Lhingris_Deeds",null,"Region:Gorgoroth:Lhingris");
    parser.doCategory("Talath_Úrui_Deeds",null,"Region:Gorgoroth:Talath Úrui");
    parser.doCategory("Agarnaith_Deeds",null,"Region:Gorgoroth:Agarnaith");
    parser.doCategory("The_Plateau_of_Gorgoroth_Quest_Deeds",DeedType.QUEST,"Region:Gorgoroth");
    parser.doCategory("The_Plateau_of_Gorgoroth_Slayer_Deeds",DeedType.SLAYER,"Region:Gorgoroth");
    parser.doCategory("The_Plateau_of_Gorgoroth_Lore_Deeds",DeedType.LORE,"Region:Gorgoroth");
    parser.doCategory("The_Plateau_of_Gorgoroth_Reputation_Deeds",DeedType.REPUTATION,"Region:Gorgoroth");

    // PVP
    parser.doCategory("Freep_Deeds",null,"PVP:Freep deeds");
    parser.doCategory("Creep_Quest_Deeds",DeedType.QUEST,"PVP:Creep deeds");
    parser.doCategory("Creep_Slayer_Deeds",DeedType.SLAYER,"PVP:Creep deeds");
    parser.doCategory("Freep_Slayer_Deeds",DeedType.SLAYER,"PVP:Freep deeds");

    // Instances:
    // - Shadows of Angmar
    parser.doCategory("Barad_Gúlaran_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Barad Gúlaran");
    parser.doCategory("Carn_Dûm_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Carn Dûm");
    parser.doCategory("The_Halls_of_Night_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"The Halls of Night");
    parser.doCategory("Urugarth_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Urugarth");
    parser.doCategory("Annúminas_Instance_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Annúminas");
    parser.doCategory("Fornost_Slayer_Deeds",DeedType.SLAYER,SHADOWS_OF_ANGMAR_SEED+"Fornost");
    parser.doCategory("Fornost_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Fornost");
    parser.doCategory("Helegrod_Slayer_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Helegrod");
    parser.doCategory("Inn_of_the_Forsaken_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Inn of the Forsaken");
    parser.doCategory("The_School_at_Tham_Mírdain_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Tham Mírdain:School");
    parser.doCategory("The_Library_at_Tham_Mírdain_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Tham Mírdain:Library");
    parser.doCategory("Tham_Mírdain_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Tham Mírdain");
    parser.doCategory("The_Great_Barrow_Deeds",null,SHADOWS_OF_ANGMAR_SEED+"Great Barrows");
    parser.doCategory("Shadows_of_Angmar_Instance_Meta_Deeds",null,SHADOWS_OF_ANGMAR_SEED);

    // - Moria
    // -- Mines of Moria
    parser.doCategory("Dark_Delvings_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"Dark Delvings");
    parser.doCategory("Dark_Delvings_Deeds",null,MINES_OF_MORIA_SEED+"Dark Delvings");
    parser.doCategory("Forgotten_Treasury_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"Forgotten Treasury");
    parser.doCategory("Forgotten_Treasury_Deeds",null,MINES_OF_MORIA_SEED+"Forgotten Treasury");
    parser.doCategory("The_Sixteenth_Hall_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"The Sixteenth Hall");
    parser.doCategory("The_Sixteenth_Hall_Deeds",null,MINES_OF_MORIA_SEED+"The Sixteenth Hall");
    parser.doCategory("The_Vile_Maw_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"The Vile Maw");
    parser.doCategory("The_Vile_Maw_Deeds",null,MINES_OF_MORIA_SEED+"The Vile Maw");
    parser.doCategory("Skûmfil_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"Skûmfil");
    parser.doCategory("Skûmfil_Deeds",null,MINES_OF_MORIA_SEED+"Skûmfil");
    parser.doCategory("Fil_Gashan_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"Fil Gashan");
    parser.doCategory("Fil_Gashan_Deeds",null,MINES_OF_MORIA_SEED+"Fil Gashan");
    parser.doCategory("Forges_of_Khazad-dûm_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"Forges of Khazad-dûm");
    parser.doCategory("Forges_of_Khazad-dûm_Deeds",null,MINES_OF_MORIA_SEED+"Forges of Khazad-dûm");
    parser.doCategory("The_Grand_Stair_Slayer_Deeds",DeedType.SLAYER,MINES_OF_MORIA_SEED+"The Grand Stair");
    parser.doCategory("The_Grand_Stair_Deeds",null,MINES_OF_MORIA_SEED+"The Grand Stair");
    parser.doCategory("Mines_of_Moria_Instance_Deeds",null,MINES_OF_MORIA_SEED);
    // -- Scourge of Khazad-dûm
    parser.doCategory("Dâr_Narbugud_Slayer_Deeds",DeedType.SLAYER,SCOURGE_OF_KHAZAD_DUM_SEED+"Dâr Narbugud");
    parser.doCategory("Dâr_Narbugud_Deeds",null,SCOURGE_OF_KHAZAD_DUM_SEED+"Dâr Narbugud");
    parser.doCategory("Halls_of_Crafting_Slayer_Deeds",DeedType.SLAYER,SCOURGE_OF_KHAZAD_DUM_SEED+"Halls of Crafting");
    parser.doCategory("Halls_of_Crafting_Deeds",null,SCOURGE_OF_KHAZAD_DUM_SEED+"Halls of Crafting");
    parser.doCategory("The_Mirror-halls_of_Lumul-nar_Slayer_Deeds",DeedType.SLAYER,SCOURGE_OF_KHAZAD_DUM_SEED+"The Mirror-halls of Lumul-nar");
    parser.doCategory("The_Mirror-halls_of_Lumul-nar_Deeds",null,SCOURGE_OF_KHAZAD_DUM_SEED+"The Mirror-halls of Lumul-nar");
    parser.doCategory("The_Water_Wheels:_Nalâ-dûm_Slayer_Deeds",DeedType.SLAYER,SCOURGE_OF_KHAZAD_DUM_SEED+"The Water Wheels of Nalâ-dûm");
    parser.doCategory("The_Water_Wheels:_Nalâ-dûm_Deeds",null,SCOURGE_OF_KHAZAD_DUM_SEED+"The Water Wheels of Nalâ-dûm");
    parser.doCategory("Scourge_of_Khazad-dûm_Instance_Deeds",null,SCOURGE_OF_KHAZAD_DUM_SEED);
    parser.doCategory("Moria_Instance_Deeds");
    // - Tower of Dol Guldur
    parser.doCategory("Barad_Guldur_Lore_Deeds",DeedType.LORE,TOWER_OF_DOL_GULDUR_SEED+"Barad Guldur");
    parser.doCategory("Barad_Guldur_Slayer_Deeds",DeedType.SLAYER,TOWER_OF_DOL_GULDUR_SEED+"Barad Guldur");
    parser.doCategory("Barad_Guldur_Deeds",null,TOWER_OF_DOL_GULDUR_SEED+"Barad Guldur");
    parser.doCategory("Dungeons_of_Dol_Guldur_Explorer_Deeds",DeedType.EXPLORER,TOWER_OF_DOL_GULDUR_SEED+"Dungeons of Dol Guldur");
    parser.doCategory("Dungeons_of_Dol_Guldur_Lore_Deeds",DeedType.LORE,TOWER_OF_DOL_GULDUR_SEED+"Dungeons of Dol Guldur");
    parser.doCategory("Dungeons_of_Dol_Guldur_Slayer_Deeds",DeedType.SLAYER,TOWER_OF_DOL_GULDUR_SEED+"Dungeons of Dol Guldur");
    parser.doCategory("Dungeons_of_Dol_Guldur_Deeds",null,TOWER_OF_DOL_GULDUR_SEED);
    parser.doCategory("Sammath_Gûl_Lore_Deeds",DeedType.LORE,TOWER_OF_DOL_GULDUR_SEED+"Sammath Gûl");
    parser.doCategory("Sammath_Gûl_Slayer_Deeds",DeedType.SLAYER,TOWER_OF_DOL_GULDUR_SEED+"Sammath Gûl");
    parser.doCategory("Sammath_Gûl_Deeds",null,TOWER_OF_DOL_GULDUR_SEED+"Sammath Gûl");
    parser.doCategory("Sword-hall_of_Dol_Guldur_Slayer_Deeds",DeedType.SLAYER,TOWER_OF_DOL_GULDUR_SEED+"Sword-hall of Dol Guldur");
    parser.doCategory("Sword-hall_of_Dol_Guldur_Deeds",null,TOWER_OF_DOL_GULDUR_SEED+"Sword-hall of Dol Guldur");
    parser.doCategory("Warg-pens_of_Dol_Guldur_Slayer_Deeds",DeedType.SLAYER,TOWER_OF_DOL_GULDUR_SEED+"Warg-pens of Dol Guldur");
    parser.doCategory("Warg-pens_of_Dol_Guldur_Deeds",null,TOWER_OF_DOL_GULDUR_SEED+"Warg-pens of Dol Guldur");
    parser.doCategory("Tower_of_Dol_Guldur_Deeds",null,TOWER_OF_DOL_GULDUR_SEED);
    // - In Their Absence
    parser.doCategory("Lost_Temple_Explorer_Deeds",DeedType.EXPLORER,IN_THEIR_ABSENCE+"Lost Temple");
    parser.doCategory("Lost_Temple_Slayer_Deeds",DeedType.SLAYER,IN_THEIR_ABSENCE+"Lost Temple");
    parser.doCategory("Lost_Temple_Deeds",null,IN_THEIR_ABSENCE+"Lost Temple");
    parser.doCategory("Northcotton_Farm_Deeds",null,IN_THEIR_ABSENCE+"Northcotton Farm");
    parser.doCategory("Ost_Dunhoth_Lore_Deeds",DeedType.LORE,IN_THEIR_ABSENCE+"Ost Dunhoth");
    parser.doCategory("Ost_Dunhoth_Meta_Deeds",null,IN_THEIR_ABSENCE+"Ost Dunhoth");
    parser.doCategory("Ost_Dunhoth_Slayer_Deeds",DeedType.SLAYER,IN_THEIR_ABSENCE+"Ost Dunhoth");
    parser.doCategory("Ost_Dunhoth_Deeds",null,IN_THEIR_ABSENCE+"Ost Dunhoth");
    parser.doCategory("Stoneheight_Deeds",null,IN_THEIR_ABSENCE+"Stoneheight");
    parser.doCategory("Sâri-surma_Slayer_Deeds",DeedType.SLAYER,IN_THEIR_ABSENCE+"Sâri-surma");
    parser.doCategory("Sâri-surma_Deeds",null,IN_THEIR_ABSENCE+"Sâri-surma");
    parser.doCategory("In_Their_Absence_Deeds",null,IN_THEIR_ABSENCE);
    // - Rise of Isengard
    parser.doCategory("Dargnákh_Unleashed_Deeds",null,RISE_OF_ISENGARD+"Dargnákh Unleashed");
    parser.doCategory("Fangorn's_Edge_Deeds",null,RISE_OF_ISENGARD+"Fangorn's Edge");
    parser.doCategory("Pits_of_Isengard_Slayer_Deeds",DeedType.SLAYER,RISE_OF_ISENGARD+"Pits of Isengard");
    parser.doCategory("Pits_of_Isengard_Deeds",null,RISE_OF_ISENGARD+"Pits of Isengard");
    parser.doCategory("The_Foundry_Deeds",null,RISE_OF_ISENGARD+"The Foundry");
    parser.doCategory("The_Tower_of_Orthanc_Deeds",null,RISE_OF_ISENGARD+"The Tower of Orthanc");
    parser.doCategory("Draigoch's_Lair_Deeds",null,RISE_OF_ISENGARD+"Draigoch's Lair");
    parser.doCategory("Rise_of_Isengard_Deeds",null,RISE_OF_ISENGARD);
    // - Road to Erebor
    parser.doCategory("Flight_to_the_Lonely_Mountain_Deeds",null,ROAD_TO_EREBOR_SEED+"Flight to the Lonely Mountain");
    parser.doCategory("Iorbar's_Peak_Deeds",null,ROAD_TO_EREBOR_SEED+"Iorbar's Peak");
    parser.doCategory("Seat_of_the_Great_Goblin_Deeds",null,ROAD_TO_EREBOR_SEED+"Seat of the Great Goblin");
    parser.doCategory("The_Fires_of_Smaug_Deeds",null,ROAD_TO_EREBOR_SEED+"The Fires of Smaug");
    parser.doCategory("The_Bells_of_Dale_Deeds",null,ROAD_TO_EREBOR_SEED+"The Bells of Dale");
    parser.doCategory("The_Battle_for_Erebor_Deeds",null,ROAD_TO_EREBOR_SEED+"The Battle for Erebor");
    parser.doCategory("Webs_of_the_Scuttledells_Deeds",null,ROAD_TO_EREBOR_SEED+"Webs of the Scuttledells");
    parser.doCategory("The_Road_to_Erebor_Deeds",null,ROAD_TO_EREBOR_SEED);
    // - Ashes of Osgiliath
    parser.doCategory("Sunken_Labyrinth_Deeds",null,ASHES_OF_OSGILIATH_SEED+"Sunken Labyrinth");
    parser.doCategory("The_Dome_of_Stars_Deeds",null,ASHES_OF_OSGILIATH_SEED+"The Dome of Stars");
    parser.doCategory("The_Ruined_City_Deeds",null,ASHES_OF_OSGILIATH_SEED+"The Ruined City");
    parser.doCategory("Ashes_of_Osgiliath_Deeds",null,ASHES_OF_OSGILIATH_SEED);
    // - Battle of Pelennor
    parser.doCategory("Blood_of_the_Black_Serpent_Deeds",null,BATTLE_OF_PELENNOR_SEED+"Blood of the Black Serpent");
    parser.doCategory("The_Quays_of_the_Harlond_Deeds",null,BATTLE_OF_PELENNOR_SEED+"The Quays of the Harlond");
    parser.doCategory("The_Silent_Street_Deeds",null,BATTLE_OF_PELENNOR_SEED+"The Silent Street");
    parser.doCategory("Throne_of_the_Dread_Terror_Deeds",null,BATTLE_OF_PELENNOR_SEED+"Throne of the Dread Terror");
    // - Plateau of Gorgoroth
    parser.doCategory("The_Court_of_Seregost_Deeds",null,PLATEAU_OF_GORGOROTH_SEED+"The Court of Seregost");
    parser.doCategory("The_Dungeons_of_Naerband_Deeds",null,PLATEAU_OF_GORGOROTH_SEED+"Dungeons of Naerband");
    parser.doCategory("The_Abyss_of_Mordath_Deeds",null,PLATEAU_OF_GORGOROTH_SEED+"The Abyss of Mordath");

    // Hobby
    parser.doCategory("Hobby_Deeds",DeedType.HOBBY,"Fishing");

    // Reputation
    parser.doCategory("Reputation_Deeds",DeedType.REPUTATION,(String)null);

    // Events
    parser.doCategory("Frostbluff_Event_Deeds",DeedType.EVENT,"Event:Yule Festival");
    parser.doCategory("Yuletide_Festival_Deeds",DeedType.EVENT,"Event:Yule Festival");
    parser.doCategory("Summer_Festival_Deeds",DeedType.EVENT,"Event:Summer Festival");
    parser.doCategory("Harvest_Festival_Deeds",DeedType.EVENT,"Event:Harvest Festival");
    parser.doCategory("Bree-land_Event_Deeds",DeedType.EVENT,"Event:Spring Festival");
    parser.doCategory("Event_Deeds",DeedType.EVENT,"Event:Festival");
    parser.doCategory("Hobnanigans_Deeds",DeedType.EVENT,"Event:Hobnanigans");
    parser.doCategory("Seasonal_Content");
    parser.doCategory("LOTRO_Anniversary_Social_Deeds");

    // Racial deeds
    parser.doCategory("Beorning_(Race)_Deeds",null,Race.BEORNING);
    parser.doCategory("Dwarf_Deeds",null,Race.DWARF);
    parser.doCategory("Elf_Deeds",null,Race.ELF);
    parser.doCategory("High_Elf_Deeds",null,Race.HIGH_ELF);
    parser.doCategory("Hobbit_Deeds",null,Race.HOBBIT);
    parser.doCategory("Race_of_Man_Deeds",null,Race.MAN);

    // Skirmish
    parser.doCategory("Skirmish_Deeds",null,"Skirmish");
    parser.doCategory("Skirmish_Instances_Deeds",null,"Skirmish:Instances");
    parser.doCategory("Skirmish_Lieutenants_Deeds",DeedType.SLAYER,"Skirmish:Lieutenants");

    // Misc
    parser.doCategory("Roving_Threat_Deeds",DeedType.SLAYER,"Roving Threats deeds");
    parser.doCategory("Undefeated_Deeds",null,"Undefeated deeds");
    parser.doCategory("Social_Deeds",null,"Social");
    parser.doCategory("Epic_Deeds",null,"Epic");
    parser.doCategory("Obsolete_Deeds",null,"Obsolete");
    parser.doCategory("Chicken_Play_Deeds",null,"Event:Chicken Play");
    parser.doCategory("Treasure_Caches");
    parser.doCategory("The_Ale_Association_Deeds");
    parser.doCategory("The_Inn_League_Deeds");

    // Caution: these may bring already existing deeds:
    parser.doCategory("Hidden_Deeds");
    parser.doCategory("Meta_Deeds");
    parser.doCategory("Elves_of_Rivendell_Reputation_Quests");
    //parser.doCategory("Regional_Deeds",null,"Region:???");
 
    // Singles
    List<String> singleDeedIds=new ArrayList<String>();
    singleDeedIds.add("Seeker_of_Truth");
    parser.handleDeeds("Singles",singleDeedIds);
    writeResultFile();
  }

  private void writeResultFile()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    DeedXMLParser parser=new DeedXMLParser();
    for(File deedFile : _tmpFilesDir.listFiles())
    {
      if (deedFile.getName().endsWith(".xml"))
      {
        List<DeedDescription> newDeeds=parser.parseXML(deedFile);
        deeds.addAll(newDeeds);
      }
    }
    // Filter deeds
    filterDeeds(deeds);
    // Additional fixes
    for(DeedDescription deed : deeds)
    {
      additionalFixes(deed);
    }
    // Additional deeds
    new DeedsInjector().addDeeds(deeds);
    // Geographic data injection
    new GeoDeedsDataInjector(deeds).doIt();
    // Resolve deed links
    new DeedLinksResolver(deeds).doIt();
    int nbDeeds=deeds.size();
    System.out.println("Found "+nbDeeds+" deeds.");
    new CheckItemRewardsInDeeds().doIt(deeds);
    new NormalizeDeedsText().doIt(deeds);
    new NormalizeDeedNames().doIt(deeds);
    new CheckDeedLinks().doIt(deeds);
    DeedsWriter.writeSortedDeeds(deeds,GeneratedFiles.DEEDS);
  }

  private void filterDeeds(List<DeedDescription> deeds)
  {
    for(Iterator<DeedDescription> it=deeds.iterator();it.hasNext();)
    {
      DeedDescription deed=it.next();
      if ("Enemies_in_Durthang".equals(deed.getKey()))
      {
        it.remove();
      }
      if ("Gorgoroth_Continued_Foothold".equals(deed.getKey()))
      {
        it.remove();
      }
    }
  }

  private void additionalFixes(DeedDescription deed)
  {
    String key=deed.getKey();
    if ("Shot_through_the_Heart".equals(key))
    {
      DeedProxies parents=deed.getParentDeedProxies();
      DeedProxy parentProxy=parents.getByKey("Class_Deeds_(Hunter)_-_Tier_8");
      if (parentProxy==null)
      {
        parentProxy=new DeedProxy();
        parentProxy.setKey("Class_Deeds_(Hunter)_-_Tier_8");
        parents.add(parentProxy);
      }
    }
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
