package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get trait definitions from DAT files.
 * @author DAM
 */
public class MainTraitDataLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainTraitDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();

    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        //System.out.println(classDefIndex);
        if ((classDefIndex==1477) || (classDefIndex==1478) || (classDefIndex==1483) ||
            (classDefIndex==1494) || (classDefIndex==2525) || (classDefIndex==3438) || (classDefIndex==3509))
        {
          // Traits
          TraitDescription trait=TraitLoader.loadTrait(_facade,did);
          if (trait!=null)
          {
            traitsMgr.registerTrait(trait);
          }
        }
      }
    }
    TraitLoader.saveTraits();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainTraitDataLoader(facade).doIt();
    facade.dispose();
  }
}
