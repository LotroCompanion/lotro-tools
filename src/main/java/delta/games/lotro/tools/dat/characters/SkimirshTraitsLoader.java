package delta.games.lotro.tools.dat.characters;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.skirmish.io.xml.SkirmishTraitsXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;

/**
 * Get skirmish traits from DAT files.
 * @author DAM
 */
public class SkimirshTraitsLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SkimirshTraitsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void loadSkirmishTraits()
  {
    List<TraitDescription> traits=new ArrayList<TraitDescription>();
    TraitsManager traitsMgr=TraitsManager.getInstance();
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"TraitControl");
    Object[] skirmishTraitsArray=(Object[])properties.getProperty("Trait_Control_SkirmishTraits");
    if (skirmishTraitsArray!=null)
    {
      for(Object skirmishTraitObj : skirmishTraitsArray)
      {
        int traitId=((Integer)skirmishTraitObj).intValue();
        TraitDescription trait=traitsMgr.getTrait(traitId);
        traits.add(trait);
      }
    }
    SkirmishTraitsXMLWriter.write(GeneratedFiles.SKIRMISH_TRAITS,traits);
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    loadSkirmishTraits();
  }
}
