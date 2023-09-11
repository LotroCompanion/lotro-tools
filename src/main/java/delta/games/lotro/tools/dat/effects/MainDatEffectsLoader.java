package delta.games.lotro.tools.dat.effects;

import java.io.File;

import delta.common.utils.files.TextFileWriter;
import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get effects from DAT files.
 * @author DAM
 */
public class MainDatEffectsLoader
{
  private DataFacade _facade;
  private TextFileWriter _w;
  private EffectLoader _loader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEffectsLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new EffectLoader(facade);
    _w=new TextFileWriter(new File("effects3686.txt"));
    _w.start();
  }

  private int useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      if ((classDefIndex==WStateClass.EFFECT) || (classDefIndex==WStateClass.EFFECT2)
          || (classDefIndex==WStateClass.EFFECT3) || (classDefIndex==WStateClass.EFFECT4)
          || (classDefIndex==WStateClass.EFFECT5)
          || (classDefIndex==3686))
      {
        return classDefIndex;
      }
    }
    return 0;
  }

  private void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      int classIndex=useId(id);
      if (classIndex!=0)
      {
        PropertiesSet effectProps=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
        _w.writeNextLine("Class index="+classIndex+", ID="+id);
        _w.writeSomeText(effectProps.dump());
        Effect2 effect=_loader.getEffect(id);
        //load(id);
      }
    }
    _w.terminate();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatEffectsLoader(facade).doIt();
    facade.dispose();
  }
}
