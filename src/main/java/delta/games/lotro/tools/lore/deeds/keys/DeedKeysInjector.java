package delta.games.lotro.tools.lore.deeds.keys;

import java.io.File;
import java.util.List;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Tool for injection of legacy deed keys into the deeds database.
 * @author DAM
 */
public class DeedKeysInjector
{
  private static final File OLD_FILE=new File("../lotro-deeds-db/deeds.xml").getAbsoluteFile();
  private static final File NEW_FILE=GeneratedFiles.DEEDS;

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

  private void loadDeeds()
  {
    DeedXMLParser parser=new DeedXMLParser();
    List<DeedDescription> oldDeeds=parser.parseXML(OLD_FILE);
    _old.setDeeds(oldDeeds);
    List<DeedDescription> newDeeds=parser.parseXML(NEW_FILE);
    _new.setDeeds(newDeeds);
  }

  private void resolveDeeds()
  {
    for(DeedDescription deed : _old.getAll())
    {
      resolveDeed(deed);
    }
  }

  private void resolveDeed(DeedDescription deed)
  {
    //String key=deed.getKey();
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
        System.out.println("\tDeed not resolved: name=["+name+"]");
        _nbFailures++;
      }
    }
    else
    {
      System.out.println("\tSeveral old deeds with that name: "+name);
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
      //System.out.println("Ambiguous name: (x"+nbNewDeeds+") "+name);
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

  private void doIt()
  {
    loadDeeds();
    resolveDeeds();
    System.out.println("Number of failures: "+_nbFailures);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new DeedKeysInjector().doIt();
  }
}
