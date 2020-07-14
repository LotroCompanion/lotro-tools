package delta.games.lotro.tools.dat.misc;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get private encounters (instances) from DAT files.
 * @author DAM
 */
public class MainDatPrivateEncountersLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatPrivateEncountersLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void load(int privateEncounterId)
  {
    System.out.println("*********** "+privateEncounterId+" ***********");
    PropertiesSet props=_facade.loadProperties(privateEncounterId+DATConstants.DBPROPERTIES_OFFSET);
    System.out.println(props.dump());
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return classDefIndex==2651;
    }
    return false;
  }

  private void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        load(id);
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
    new MainDatPrivateEncountersLoader(facade).doIt();
    facade.dispose();
  }
}
