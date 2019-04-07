package delta.games.lotro.tools.lore.deeds;

import java.util.List;

import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.utils.Proxy;

/**
 * Inject additional deeds.
 * @author DAM
 */
public class DeedsInjector
{
  private List<DeedDescription> _deeds;

  /**
   * Add deeds to the given list.
   * @param deeds Storage for new deeds.
   */
  public void addDeeds(List<DeedDescription> deeds)
  {
    _deeds=deeds;
    addAllegianceDeeds();
  }

  private void addAllegianceDeeds()
  {
    addAllegianceDeeds("CourtOfLothlorien","The Court of Lothlórien",30);
    addAllegianceDeeds("KingdomOfGondor","The Kingdom of Gondor",30);
    addAllegianceDeeds("DurinsFolk","Durin's Folk",30);
    addAllegianceDeeds("HobbitsOfTheCompany","Hobbits of the Company",30);
  }

  private void addAllegianceDeeds(String keySeed, String allegianceName, int tierMax)
  {
    for(int i=1;i<=tierMax;i++)
    {
      DeedDescription deed=new DeedDescription();
      // Key
      String key=keySeed+i;
      deed.setKey(key);
      // Name
      String name=allegianceName+": Allegiance Level "+i;
      deed.setName(name);
      // Description
      String description="Reach level "+i+" with the allegiance: "+allegianceName+".";
      if ("DurinsFolk".equals(keySeed))
      {
        description="Reach level "+i+" with the Durin's Folk allegiance.";
      }
      deed.setDescription(description);
      // Type
      deed.setType(DeedType.REPUTATION);
      // Category
      deed.setCategory("Allegiance:"+allegianceName);
      // Required level
      deed.setMinLevel(Integer.valueOf(110));
      // Rewards
      // Token of Service: 1879353887
      Rewards rewards=deed.getRewards();
      ObjectsSet objects=rewards.getObjects();
      Proxy<Item> itemProxy=new Proxy<Item>();
      itemProxy.setName("Token of Service");
      itemProxy.setId(1879353887);
      // Number of tokens
      int nbTokens=getNumberOfTokens(i);
      objects.addObject(itemProxy,nbTokens);
      _deeds.add(deed);
    }
  }

  private int getNumberOfTokens(int level)
  {
    int nbTokens;
    if (level<=10)
    {
      if (level%5==0) nbTokens=5; else nbTokens=2;
    }
    else if (level<=20)
    {
      if (level%5==0) nbTokens=5; else nbTokens=3;
    }
    else
    {
      nbTokens=4;
    }
    return nbTokens;
  }
}
