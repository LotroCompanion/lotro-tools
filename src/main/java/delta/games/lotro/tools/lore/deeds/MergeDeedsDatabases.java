package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.lore.deeds.DeedDescription;

/**
 * @author dm
 */
public class MergeDeedsDatabases
{
  private DeedsContainer _lotroCompendium;
  private DeedsContainer _lorebook;

  private void doIt()
  {
    load();
    merge();
  }

  private void load()
  {
    File loreDir=LotroCoreConfig.getInstance().getLoreDir();
    File lotroCompendiumFile=new File(loreDir,"deeds_lc.xml");
    _lotroCompendium=new DeedsContainer(lotroCompendiumFile);
    System.out.println("Lotro Compendium: "+_lotroCompendium.getCount());
    File lorebookDeedDir=new File(loreDir,"../../../lotro-companion/data/lore");
    File lorebookFile=new File(lorebookDeedDir,"deeds_by_name.xml");
    _lorebook=new DeedsContainer(lorebookFile);
    System.out.println("Lorebook: "+_lorebook.getCount());
  }

  private void merge()
  {
    findMatchingDeeds();
  }

  private DeedDescription findMatchingDeedInContainer(DeedsContainer container, DeedDescription deed)
  {
    List<DeedDescription> matches=container.getDeedByName(deed.getName());
    if (matches.size()==1)
    {
      // Single match: good!
      return matches.get(0);
    }
    // Multiple matches
    // - look at description
    List<DeedDescription> matches2=new ArrayList<DeedDescription>();
    for(DeedDescription match : matches)
    {
      String description=match.getDescription();
      if (description.equals(deed.getDescription()))
      {
        matches2.add(match);
      }
    }
    int nbNameAndDescriptionMatches=matches2.size();
    if (nbNameAndDescriptionMatches==1)
    {
      return matches2.get(0);
    }
    if (nbNameAndDescriptionMatches!=1)
    {
      List<DeedDescription> matches3=new ArrayList<DeedDescription>();
      for(DeedDescription match : matches)
      {
        if (Objects.equals(match.getObjectives(),deed.getObjectives()))
        {
          matches3.add(match);
        }
      }
      int count=matches3.size();
      if (count==1)
      {
        return matches3.get(0);
      }
    }
    if (nbNameAndDescriptionMatches>1)
    {
      // Still multiple matches, look at class
      List<DeedDescription> matches3=new ArrayList<DeedDescription>();
      for(DeedDescription match : matches2)
      {
        if (Objects.equals(match.getClassName(),deed.getClassName()))
        {
          matches3.add(match);
        }
      }
      int nbNameDescriptionAndClassMatches=matches3.size();
      if (nbNameDescriptionAndClassMatches==1)
      {
        return matches3.get(0);
      }
    }
    System.out.println("No match for: "+deed+"nb matches="+nbNameAndDescriptionMatches);
    return null;
  }

  private void findMatchingDeeds()
  {
    List<String> matches=new ArrayList<String>();
    int nbMatches=0;
    List<DeedDescription> lotroCompendiumDeeds=_lorebook.getAll();
    for(DeedDescription lotroCompendiumDeed : lotroCompendiumDeeds)
    {
      DeedDescription lorebookDeed=findMatchingDeedInContainer(_lotroCompendium,lotroCompendiumDeed);
      if (lorebookDeed!=null)
      {
        matches.add(lotroCompendiumDeed.getIdentifier()+"=>"+lorebookDeed.getIdentifier());
        nbMatches++;
      }
    }
    System.out.println("Nb matches="+nbMatches+" / "+lotroCompendiumDeeds.size());
    Collections.sort(matches);
    for(String match : matches)
    {
      System.out.println(match);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new MergeDeedsDatabases().doIt();
  }
}
