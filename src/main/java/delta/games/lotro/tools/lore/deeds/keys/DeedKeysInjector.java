package delta.games.lotro.tools.lore.deeds.keys;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedsSaxParser;

/**
 * Tool for injection of legacy deed keys into the deeds database.
 * @author DAM
 */
public class DeedKeysInjector
{
  private static final Logger LOGGER=Logger.getLogger(DeedKeysInjector.class);

  private static final File OLD_FILE=new File("../lotro-deeds-db/deeds.xml").getAbsoluteFile();
  //private static final File OLD_FILE_NO_ID=new File("../lotro-deeds-db/deeds_no_id.xml").getAbsoluteFile();

  private DeedsBundle _old;
  private DeedsBundle _new;
  private int _nbFailures;

  /**
   * Constructor.
   */
  public DeedKeysInjector()
  {
    _old=new DeedsBundle();
    _new=new DeedsBundle();
  }

  private void loadOldDeeds()
  {
    List<DeedDescription> oldDeeds=DeedsSaxParser.parseDeedsFile(OLD_FILE);
    _old.setDeeds(oldDeeds);
  }

  private void resolveDeeds()
  {
    manualResolution();
    for(DeedDescription deed : _old.getAll())
    {
      resolveDeed(deed);
    }
  }

  private void resolveDeed(DeedDescription deed)
  {
    String key=deed.getKey();
    int oldId=deed.getIdentifier();
    if (oldId!=0)
    {
      return;
    }
    String name=deed.getName();
    //System.out.println("Handling deed: "+key+" -- "+name);
    List<DeedDescription> oldDeedsWithName=_old.getDeedsByName(name);
    if (oldDeedsWithName.size()==1)
    {
      DeedDescription newDeed=fetchByName(name);
      if (newDeed==null)
      {
        newDeed=handleBadName(deed);
      }
      if (newDeed==null)
      {
        newDeed=handleClassDeed(deed);
      }
      if (newDeed==null)
      {
        newDeed=handleRegionSuffix(deed);
      }
      if (newDeed==null)
      {
        newDeed=handleRaceSuffix(deed);
      }
      if (newDeed==null)
      {
        //System.out.println("manualResolution(\""+key+"\",1234567);");
        LOGGER.warn("Deed not resolved: key=["+key+"], name=["+name+"]");
        _nbFailures++;
      }
      else
      {
        resolution(deed,newDeed,key,newDeed.getIdentifier());
      }
    }
    else
    {
      //System.out.println("manualResolution(\""+key+"\",1234567);");
      LOGGER.warn("Several old deeds with that name: "+name);
      _nbFailures++;
    }
  }

  private DeedDescription fetchByName(String name)
  {
    DeedDescription ret=fetchByNamePrivate(name);
    if (ret==null)
    {
      String lowerCaseName=name.toLowerCase();
      ret=fetchByNamePrivate(lowerCaseName);
    }
    return ret;
  }

  private DeedDescription fetchByNamePrivate(String name)
  {
    List<DeedDescription> newDeeds=_new.getDeedsByName(name);
    int nbNewDeeds=newDeeds.size();
    if (nbNewDeeds==1)
    {
      return newDeeds.get(0);
    }
    if (nbNewDeeds>1)
    {
      // TODO Handle these
      LOGGER.warn("Ambiguous name: (x"+nbNewDeeds+") "+name);
      //return null;
      return newDeeds.get(0);
    }
    return null;
  }

  private DeedDescription handleBadName(DeedDescription deed)
  {
    String name=deed.getName();
    if (name.startsWith("War-leader Slayer - T"))
    {
      name=name.replace("War-leader Slayer - T","Warleader-slayer -- T");
    }
    if (name.contains(" - Tier"))
    {
      name=name.replace(" - Tier"," -- Tier");
    }
    return fetchByName(name);
  }

  private DeedDescription handleClassDeed(DeedDescription deed)
  {
    String name=deed.getName();
    if (name.startsWith("Class Deeds"))
    {
      int index=name.indexOf("(");
      int index2=name.indexOf(")");
      if ((index!=-1) && (index2!=-1))
      {
        String className=name.substring(index+1,index2);
        String newDeedName=name.substring(0,index-1)+name.substring(index2+1);
        List<DeedDescription> candidateDeeds=_new.getDeedsByName(newDeedName);
        for(DeedDescription candidateDeed : candidateDeeds)
        {
          CharacterClass requiredClass=candidateDeed.getUsageRequirement().getRequiredClass();
          if ((requiredClass!=null) && (requiredClass.getLabel().equals(className)))
          {
            return candidateDeed;
          }
        }
      }
    }
    return null;
  }

  private String[] REGIONS = { "Angmar", "Bree-land", "Dunland", "Enedwaith", "Ered Luin", "Eregion", "Evendim", 
      "Forochel", "Great River", "Lone-lands", "Lothlórien", "Misty Mountains",
      "Moria", "North Downs", "Southern Mirkwood", "The Shire", "The Trollshaws" };

  private DeedDescription handleRegionSuffix(DeedDescription deed)
  {
    String name=deed.getName();
    for(String region : REGIONS)
    {
      String suffix="("+region+")";
      if (name.endsWith(suffix))
      {
        String fixedRegionName=region;
        if ("The Trollshaws".equals(region)) fixedRegionName="Trollshaws";
        if ("The Shire".equals(region)) fixedRegionName="Shire";

        String newDeedName=name.substring(0,name.length()-suffix.length()).trim();
        List<DeedDescription> candidateDeeds=_new.getDeedsByName(newDeedName);
        for(DeedDescription candidateDeed : candidateDeeds)
        {
          String category=candidateDeed.getCategory();
          if (fixedRegionName.equals(category))
          {
            return candidateDeed;
          }
        }
      }
    }
    return null;
  }

  private String[] RACES = { "Dwarf", "Elf", "High Elf", "Hobbit", "Man" };

  private DeedDescription handleRaceSuffix(DeedDescription deed)
  {
    String name=deed.getName();
    for(String race : RACES)
    {
      String suffix="("+race+")";
      if (name.endsWith(suffix))
      {
        String fixedRace=race;
        //if ("The Trollshaws".equals(race)) fixedRegionName="Trollshaws";
        //if ("The Shire".equals(race)) fixedRegionName="Shire";

        String newDeedName=name.substring(0,name.length()-suffix.length()).trim();
        List<DeedDescription> candidateDeeds=_new.getDeedsByName(newDeedName);
        for(DeedDescription candidateDeed : candidateDeeds)
        {
          Race requiredRace=candidateDeed.getUsageRequirement().getRequiredRace();
          if ((requiredRace!=null) && (requiredRace.getLabel().equals(fixedRace)))
          {
            return candidateDeed;
          }
        }
      }
    }
    return null;
  }

  private void manualResolution()
  {
    manualResolution("Ally_to_the_Council_of_the_North",1879190367);
    manualResolution("Alternate_Ending_(Deed)",1879278971);
    // This one is the same as the previous one
    //manualResolution("Alternative_Ending_(Deed)",1879278971);
    manualResolution("Ann%C3%BAminas_--_Glinghant",1879188607);
    manualResolution("Ann%C3%BAminas_--_Haudh_Valandil",1879188619);
    manualResolution("Ann%C3%BAminas_--_Ost_Elendil",1879188613);

    // Not found
    manualResolution("Battle_Royale",0);

    manualResolution("Beast_Slayer_of_Ud%C3%BBn",1879354317);
    manualResolution("Beast_Slayer_of_Ud%C3%BBn_(Advanced)",1879354320);
    manualResolution("Commanders_of_Isengard_--_Tier_1",1879227844);
    manualResolution("Commanders_of_Isengard_--_Tier_2",1879227846);
    manualResolution("Commanders_of_the_Foundry_--_Tier_1",1879226083);
    manualResolution("Commanders_of_the_Foundry_--_Tier_2",1879226101);
    manualResolution("Dead-slayer_(Southern_Mirkwood)",1879155758);
    manualResolution("Deed:_The_Vanished_Rider",1879229954);
    manualResolution("Discovery:_Northcotton_Farms",1879205935);
    manualResolution("Dual-slayer_of_North_Ithilien",1879338673);
    manualResolution("Dual-slayer_of_North_Ithilien_(Advanced)",1879338672);
    manualResolution("Eglan",1879180379);
    manualResolution("Enraged_Stone-crusher_Slayer",1879161132);
    manualResolution("Enraged_Stone-crusher_Slayer_(Advanced)",1879161056);
    manualResolution("Ered_Mithrin_Beast-slayer_(Advanced)",1879378528);
    manualResolution("Explorer_of_Dor_Amarth",1879354878);
    manualResolution("Explorer_of_Talath_%C3%9Arui",1879354859);
    manualResolution("Foe-slayer_of_Towers_of_the_Teeth",1879342491);
    manualResolution("Foe-slayer_of_Towers_of_the_Teeth_(Advanced)",1879342482);
    manualResolution("Forgeworker_Slayer_of_Ud%C3%BBn",1879354251);
    manualResolution("Forgeworker_Slayer_of_Ud%C3%BBn_(Advanced)",1879354253);
    manualResolution("Forgeworks_of_Ud%C3%BBn",1879354281);
    manualResolution("Friend_to_the_Council_of_the_North",1879190366);
    manualResolution("Geneology_of_the_Beornings_(Beorning_Deed)",1879316233);
    manualResolution("Gredbyg-slayer_of_Lhingris",1879356159);
    manualResolution("Gredbyg-slayer_of_Lhingris_(Advanced)",1879356160);
    manualResolution("Host_of_the_West_Armourer_(Faction)_(Deed)",1879341965);
    manualResolution("Host_of_the_West_Armourer_(Advanced)(Faction)_(Deed)",1879341970);
    manualResolution("Host_of_the_West_Armourer_(Final)(Faction)_(Deed)",1879341967);
    manualResolution("Host_of_the_West_Armourer_(Intermediate)_(Faction)_(Deed)",1879341964);
    manualResolution("Host_of_the_West_Provisioner_(Faction)_(Deed)",1879341968);
    manualResolution("Host_of_the_West_Provisioner_(Advanced)(Faction)_(Deed)",1879341974);
    manualResolution("Host_of_the_West_Provisioner_(Final)(Faction)_(Deed)",1879341959);
    manualResolution("Host_of_the_West_Provisioner_(Intermediate)_(Faction)_(Deed)",1879341972);
    manualResolution("Host_of_the_West_Weaponist_(Faction)_(Deed)",1879341969);
    manualResolution("Host_of_the_West_Weaponist_(Advanced)_(Faction)_(Deed)",1879341973);
    manualResolution("Host_of_the_West_Weaponist_(Final)_(Faction)_(Deed)",1879341971);
    manualResolution("Host_of_the_West_Weaponist_(Intermediate)_(Faction)_(Deed)",1879341966);
    manualResolution("Ironfold_Beast-slayer_(Advanced)",1879378537);
    manualResolution("Kindred_to_the_Entwash_Vale",1879246628);
    manualResolution("Kindred_with_the_Council_of_the_North",1879190368);
    manualResolution("Known_to_the_Council_of_the_North",1879190365);
    manualResolution("L%C3%B3rien_Lookout",1879152530);
    manualResolution("Master_of_Beasts_(Advanced)_(Sarn%C3%BAr)",1879093966);
    manualResolution("Master_of_Beasts_(Sarn%C3%BAr)",1879093965);
    manualResolution("Master_of_the_Forgotten_Lore",1879114138);
    manualResolution("Maze_Wing",1879190318);
    manualResolution("Not_the_Bees_(Beorning_Deed)",1879317518);
    manualResolution("Quests_in_Nan_Curun%C3%ADr",1879220135);
    manualResolution("Quests_of_Limlight_Gorge",1879231052);
    manualResolution("Quests_of_Pelennor_(After_Battle)",1879338695);
    manualResolution("Quests_to_Restore_the_Three_Kingdoms",1879366147);
    // This one is the same as the previous one
    //manualResolution("Quests_to_Restore_the_Three_Kingdoms",1879366147);
    manualResolution("Quick_Wrist_(Captain)",1879277396);
    manualResolution("Quick_Wrist_(Guardian)",1879277271);
    manualResolution("Rune-keeper_Slayer3",1879145087);
    manualResolution("Rune-keeper_Slayer4",1879145088);
    manualResolution("Sambrog_Wing",1879190349);
    manualResolution("Slay_Enemies_of_Angmar1",1879084394);
    manualResolution("Slay_Enemies_of_Angmar2",1879084396);
    manualResolution("Slay_Enemies_of_Angmar3",1879084398);
    manualResolution("Slay_Enemies_of_Angmar4",1879084400);
    manualResolution("Slay_Enemies_of_Angmar5",1879084402);
    manualResolution("Slay_Enemies_of_Angmar6",1879084404);
    manualResolution("Slay_Enemies_of_Angmar7",1879084406);
    manualResolution("Strong_Voice_(Warden_Deed)",1879139064); // or 1879277308
    manualResolution("Surveyor_of_the_Dwarvish_Markers",1879366160);
    // Not found, marked as obsolete in Lotro-wiki
    manualResolution("Subtle_Command_(deed)",0);
    manualResolution("The_Blighted_Ones_(Advanced)",1879147184);
    manualResolution("The_Gate_to_Sambrog",1879190166);
    manualResolution("The_Line_of_Beorn:_Part_Four_(Beorning_Deed)",1879316455);
    manualResolution("The_Line_of_Beorn:_Part_One_(Beorning_Deed)",1879316458);
    manualResolution("The_Line_of_Beorn:_Part_Three_(Beorning_Deed)",1879316456);
    manualResolution("The_Line_of_Beorn:_Part_Two_(Beorning_Deed)",1879316457);
    manualResolution("The_Mead_Hall:_Inhabitants",1879238995);
    manualResolution("The_Mead_Hall:_Interior_Enhancements",1879238988);
    manualResolution("The_Mead_Hall:_Outdoor_Enhancements",1879238987);
    manualResolution("The_Mines_of_Moria_(Burglar)",1879139434);
    manualResolution("The_Mines_of_Moria_(Hunter)",1879139446);
    manualResolution("The_Mines_of_Moria_(Quests_Deed)",1879152682);
    manualResolution("The_Mines_of_Moria_(Rune-keeper)",1879139455);
    manualResolution("The_Mines_of_Moria_(Warden)",1879139458);
    // Those 5 are not found in the current deeds database
    manualResolution("The_Ranger%27s_Offensive:_Tier_Five",0);
    manualResolution("The_Ranger%27s_Offensive:_Tier_Four",0);
    manualResolution("The_Ranger%27s_Offensive:_Tier_One",0);
    manualResolution("The_Ranger%27s_Offensive:_Tier_Three",0);
    manualResolution("The_Ranger%27s_Offensive:_Tier_Two",0);
    manualResolution("The_Ruins_of_Breeland",1879071672);
    manualResolution("The_Townsfolk",1879239040);
    manualResolution("Throne_of_the_Dread_Terror:_Enslaved_of_Minas_Morgul_--_Tier_1",1879334071);
    manualResolution("Throne_of_the_Dread_Terror:_Enslaved_of_Minas_Morgul_--_Tier_2",1879334072);
    manualResolution("Treating_With_Scoundrels_-_Distraction",1879332082);
    manualResolution("Water_Wing",1879190348);
    manualResolution("Wordsmith_(Deed)",1879278975);
    // Old deeds with duplicate names
    manualResolution("Captain%27s_Victory_(deed)",1879051858);
    manualResolution("Captain%27s_Victory",1879277411);
    manualResolution("Enmity_of_the_Dourhands",1879073586);
    manualResolution("Enmity_of_the_Dourhands_II",1879073587);
    manualResolution("Enmity_of_the_Goblins_II_(Beorning_Deed)",1879317098);
    manualResolution("Enmity_of_the_Goblins_III_(Beorning_Deed)",1879317097);
    manualResolution("Enmity_of_the_Orcs",1879073552);
    manualResolution("Enmity_of_the_Orcs_(Beorning_Deed)",1879317091);
    manualResolution("Enmity_of_the_Orcs_II_(Beorning_Deed)",1879317096);
    manualResolution("Enmity_of_the_Orcs_II",1879073553);
    manualResolution("Enmity_of_the_Orcs_III",1879073554);
    manualResolution("Enmity_of_the_Spiders_(Beorning_Deed)",1879317092);
    manualResolution("Enmity_of_the_Spiders",1879073463);
    manualResolution("Enmity_of_the_Spiders_II_(Beorning_Deed)",1879317093);
    manualResolution("Enmity_of_the_Spiders_II",1879073464);
    manualResolution("Enmity_of_the_Spiders_III",1879073465);
    manualResolution("Enmity_of_the_Spiders_III_(Beorning_Deed)",1879317095);
    manualResolution("Expert_Attacks",1879277415);
    manualResolution("Expert_Attacks_(deed)",1879051855);
    manualResolution("Friend_to_the_Galadhrim",1879190386);
    manualResolution("Nameless-slayer",1879147187);
    manualResolution("Nameless-slayer_(Advanced)",1879147188);
    manualResolution("Now_for_Wrath",1879277409);
    manualResolution("Now_for_Wrath_(deed)",1879051860);
    manualResolution("Of_Elendilmir",1879326531);
    manualResolution("Renewed_Voice",1879277397);
    manualResolution("Renewed_Voice_(deed)",1879051847);
    manualResolution("Skillful_Blocking_(Deed)",1879139084);
    manualResolution("Strong_Voice_(Deed)",1879277413);
    manualResolution("Strong_Voice_(deed)",1879051852);
    manualResolution("The_Best_Defence",1879060151);
    manualResolution("The_Best_Defence_(Deed)",1879277264);

    // Force some resolutions otherwise wrong with auto resolution
    manualResolution("Enmity_of_the_Goblins_(Hobbit)",1879073466);
    manualResolution("Enmity_of_the_Goblins_(Beorning_Deed)",1879317099);

    // Some more manual resolutions after an update of the deed label
    manualResolution("Enmity_of_the_Goblins_(Beorning_Deed)",1879317099);
    manualResolution("Enmity_of_the_Goblins_(Beorning_Deed)",1879317099);
    manualResolution("Enmity_of_the_Goblins_(Beorning_Deed)",1879317099);
    manualResolution("Enmity_of_the_Goblins_(Beorning_Deed)",1879317099);
    manualResolution("Uruk_Slayer_of_the_Westemnet",1879287745);
    manualResolution("Dunlending_Slayer_of_the_Westemnet_(Advanced)",1879287747);
    manualResolution("Dunlending_Slayer_of_the_Westemnet",1879287748);
    manualResolution("Uruk_Slayer_of_the_Westemnet_(Advanced)",1879287749);
    manualResolution("Craban_Slayer_of_the_Westemnet",1879287752);
    manualResolution("Craban_Slayer_of_the_Westemnet_(Advanced)",1879287753);
    manualResolution("Orc_Slayer_of_the_Westemnet_(Advanced)",1879287754);
    manualResolution("Warg_Slayer_of_the_Westemnet_(Advanced)",1879287755);
    manualResolution("Orc_Slayer_of_the_Westemnet",1879287756);
    manualResolution("Warg_Slayer_of_the_Westemnet",1879287757);
    manualResolution("Wolf_Slayer_of_the_Westemnet_(Advanced)",1879287760);
    manualResolution("Wolf_Slayer_of_the_Westemnet",1879287761);
    manualResolution("Troll_Slayer_of_the_Westemnet_(Advanced)",1879287762);
    manualResolution("Troll_Slayer_of_the_Westemnet",1879287763);
    manualResolution("Goblin_Slayer_of_the_Westemnet_(Advanced)",1879287764);
    manualResolution("Goblin_Slayer_of_the_Westemnet",1879287765);
  }

  private void manualResolution(String key, int identifier)
  {
    if (identifier==0)
    {
      return;
    }
    DeedDescription oldDeed=_old.getDeedByKey(key);
    DeedDescription newDeed=_new.getDeedById(identifier);
    resolution(oldDeed,newDeed,key,identifier);
    if (newDeed!=null)
    {
      _new.removeDeed(newDeed);
    }
  }

  private void resolution(DeedDescription oldDeed, DeedDescription newDeed, String key, int identifier)
  {
    if (oldDeed!=null)
    {
      oldDeed.setIdentifier(identifier);
    }
    if (newDeed!=null)
    {
      newDeed.setKey(key);
    }
  }

  /**
   * Perform deed keys injection.
   * @param newDeeds Deeds to update.
   */
  public void doIt(List<DeedDescription> newDeeds)
  {
    _new.setDeeds(newDeeds);
    loadOldDeeds();
    resolveDeeds();
    System.out.println("Number of failures: "+_nbFailures);
  }
}
