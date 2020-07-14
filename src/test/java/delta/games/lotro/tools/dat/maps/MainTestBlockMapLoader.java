package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Test class for the BlockMapLoader.
 * @author DAM
 */
public class MainTestBlockMapLoader
{
  private void doIt()
  {
    DataFacade facade=new DataFacade();
    BlockMapLoader loader=new BlockMapLoader(facade);
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          PropertiesSet props=loader.loadPropertiesForMapBlock(region,blockX,blockY);
          if (props!=null)
          {
            System.out.println("*** Block map: region="+region+", blockX="+blockX+", blockY="+blockY);
            System.out.println(props.dump());
          }
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
    new MainTestBlockMapLoader().doIt();
  }
}
