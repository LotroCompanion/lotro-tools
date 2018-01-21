package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;

/**
 * Tool to merge deeds databases (lorebook+lotro compendium databases).
 * @author DAM
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
    mergeMatchingDeeds();
    // Write results
    writeMergeResult();
  }

  private void writeMergeResult()
  {
    // Lorebook
    DeedXMLWriter writer=new DeedXMLWriter();
    File toLorebook=new File(_lorebook.getFile().getParentFile(),"merged_lorebook.xml");
    writer.writeDeeds(toLorebook,_lorebook.getAll(),EncodingNames.UTF_8);
    // Lotro Compendium
    File toLotroCompendium=new File(_lotroCompendium.getFile().getParentFile(),"merged_lotrocompendium.xml");
    writer.writeDeeds(toLotroCompendium,_lotroCompendium.getAll(),EncodingNames.UTF_8);
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

  private void mergeMatchingDeeds()
  {
    List<DeedDescription> matchingLotroCompendiumDeeds=findMatchingDeeds(_lorebook,_lotroCompendium);
    List<DeedDescription> lorebookDeeds=_lorebook.getAll();
    int nbDeeds=lorebookDeeds.size();
    for(int i=0;i<nbDeeds;i++)
    {
      DeedDescription lorebookDeed=lorebookDeeds.get(i);
      DeedDescription lotroCompendiumDeed=matchingLotroCompendiumDeeds.get(i);
      if (lotroCompendiumDeed!=null)
      {
        // Categories
        String category=lotroCompendiumDeed.getCategory();
        lorebookDeed.setCategory(category);
        // IDs
        lotroCompendiumDeed.setIdentifier(lorebookDeed.getIdentifier());
        // Objectives
        String lorebookObjectives=lorebookDeed.getObjectives();
        if ((lorebookObjectives==null) || (lorebookObjectives.isEmpty()))
        {
          lorebookDeed.setObjectives(lotroCompendiumDeed.getObjectives());
        }
        // Item XP
        lotroCompendiumDeed.getRewards().setHasItemXP(lorebookDeed.getRewards().hasItemXP());
      }
    }
  }

  private List<DeedDescription> findMatchingDeeds(DeedsContainer toMatch, DeedsContainer matchCandidates)
  {
    List<DeedDescription> matchingDeeds=new ArrayList<DeedDescription>();
    int nbMatches=0;
    List<DeedDescription> sourceDeeds=toMatch.getAll();
    for(DeedDescription sourceDeed : sourceDeeds)
    {
      DeedDescription matchingDeed=findMatchingDeedInContainer(matchCandidates,sourceDeed);
      if (matchingDeed!=null)
      {
        matchingDeeds.add(matchingDeed);
        nbMatches++;
      }
      else
      {
        matchingDeeds.add(null);
      }
    }
    System.out.println("Nb matches="+nbMatches+" / "+sourceDeeds.size());
    return matchingDeeds;
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new MergeDeedsDatabases().doIt();
  }
}
