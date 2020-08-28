package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;

/**
 * Simple test class for the landblock loader.
 * @author DAM
 */
public class MainTestLandblockLoader
{
  private LandblockInfoLoader _loader;
  private LandblockGeneratorsAnalyzer _analyzer;

  private MainTestLandblockLoader()
  {
    DataFacade facade=new DataFacade();
    _loader=new LandblockInfoLoader(facade);
    _analyzer=new LandblockGeneratorsAnalyzer(facade,null);
  }

  void handleLandBlock(LandBlockInfo lbi)
  {
    //System.out.println("Using region "+region+", block X="+blockX+",Y="+blockY);
    if (lbi!=null)
    {
      LbiInspector inspector=new LbiInspector();
      inspector.handleLbi(lbi);
    }
  }

  void dumpLandBlock(LandBlockInfo lbi)
  {
    //System.out.println("Using region "+region+", block X="+blockX+",Y="+blockY);
    if (lbi!=null)
    {
      LbiInspector inspector=new LbiInspector();
      inspector.dump(lbi);
    }
  }

  void analyzeBlock(LandBlockInfo lbi)
  {
    if (lbi!=null)
    {
      _analyzer.handleLandblock(lbi);
    }
  }

  private void doIt()
  {
    long now=System.currentTimeMillis();
    /*
    LandBlockInfo lbi=_loader.loadLandblockInfo(1,779/8,1338/8);
    analyzeBlock(lbi); // Itä-mâ (r1 lx779 ly1338 ox69.00 oy59.60 oz434.04 h296.7)
    */
    for(int region=1;region<=1;region++)
    {
      System.out.println("Region "+region);
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        System.out.println("X="+blockX);
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          LandBlockInfo lbi=_loader.loadLandblockInfo(region,blockX,blockY);
          analyzeBlock(lbi);
        }
      }
    }
    long now2=System.currentTimeMillis();
    System.out.println("Done in "+(now2-now)+"ms");
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    MainTestLandblockLoader loader=new MainTestLandblockLoader();
    loader.doIt();
  }
}
