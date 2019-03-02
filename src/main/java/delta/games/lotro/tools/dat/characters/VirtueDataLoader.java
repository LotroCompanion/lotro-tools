package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Get virtues definitions from DAT files.
 * @author DAM
 */
public class VirtueDataLoader
{
  private DataFacade _facade;
  private TraitsManager _traitsManager;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param traitsManager Traits manager.
   */
  public VirtueDataLoader(DataFacade facade, TraitsManager traitsManager)
  {
    _facade=facade;
    _traitsManager=traitsManager;
  }

  private void loadVirtues()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900025B);
    //System.out.println(properties.dump());
    Object[] freepTraitsArray=(Object[])properties.getProperty("Trait_Control_FreepTraits");
    for(Object freepTraitObj : freepTraitsArray)
    {
      int traitId=((Integer)freepTraitObj).intValue();
      TraitDescription trait=TraitLoader.loadTrait(_facade,traitId);
      _traitsManager.registerTrait(trait);
      //System.out.println("Virtue: "+traitId+" - "+trait.getName());
    }
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    loadVirtues();
  }
}
