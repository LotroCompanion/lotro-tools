package delta.games.lotro.tools.dat.characters;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get trait definitions from DAT files.
 * @author DAM
 */
public class MainTraitDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainTraitDataLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainTraitDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void loadTrait(int indexDataId)
  {
    System.out.println(indexDataId);
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      String name=DatUtils.getStringProperty(properties,"Trait_Name");
      System.out.println(indexDataId+": "+name);
      //String description=DatUtils.getStringProperty(properties,"Trait_Description");
      //System.out.println(properties.dump());
    }
    else
    {
      LOGGER.warn("Could not handle trait ID="+indexDataId);
    }
  }

  private void doIt()
  {
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        //System.out.println(classDefIndex);
        if (classDefIndex==1477)
        {
          // Traits
          loadTrait(did);
        }
      }
    }
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
