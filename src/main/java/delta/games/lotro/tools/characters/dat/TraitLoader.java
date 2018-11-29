package delta.games.lotro.tools.characters.dat;

import java.io.File;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatStatUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;

/**
 * Traits loader.
 * @author DAM
 */
public class TraitLoader
{
  /**
   * Load a trait.
   * @param facade Data facade.
   * @param traitId Trait identifier.
   * @return the loaded trait description.
   */
  public static TraitDescription loadTrait(DataFacade facade, int traitId)
  {
    TraitDescription ret=null;
    PropertiesSet traitProperties=facade.loadProperties(0x9000000+traitId);
    if (traitProperties!=null)
    {
      //System.out.println("*********** Trait: "+traitId+" ****************");
      ret=new TraitDescription();
      ret.setIdentifier(traitId);
      // Name
      String traitName=DatUtils.getStringProperty(traitProperties,"Trait_Name");
      ret.setName(traitName);
      // Description
      String description=DatUtils.getStringProperty(traitProperties,"Trait_Description");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)traitProperties.getProperty("Trait_Icon")).intValue();
      ret.setIconId(iconId);
      // Min level
      int minLevel=((Integer)traitProperties.getProperty("Trait_Minimum_Level")).intValue();
      ret.setMinLevel(minLevel);
      // Tier
      //int traitTier=((Integer)traitProperties.getProperty("Trait_Tier")).intValue();
      Integer maxTier=(Integer)traitProperties.getProperty("Trait_Virtue_Maximum_Rank");
      if ((maxTier!=null) && (maxTier.intValue()>1))
      {
        ret.setTiersCount(maxTier.intValue());
      }
      //System.out.println("Trait name: "+traitName+" (min level="+minLevel+")");

      // Stats
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,traitProperties);
      ret.setStatsProvider(statsProvider);
      // Build icon file
      File to=new File("data/icons/traits/"+iconId+".png").getAbsoluteFile();
      if (!to.exists())
      {
        DatIconsUtils.buildImageFile(facade,iconId,to);
      }
    }
    return ret;
  }
}
