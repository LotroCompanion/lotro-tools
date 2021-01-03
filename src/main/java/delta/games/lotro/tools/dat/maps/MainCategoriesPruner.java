package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.categories.CategoriesManager;
import delta.games.lotro.maps.data.categories.Category;
import delta.games.lotro.maps.data.markers.BlockMarkersManager;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;

/**
 * Prunes unused marker categories.
 * @author DAM
 */
public class MainCategoriesPruner
{
  private MapsManager _mapsManager;

  /**
   * Constructor.
   * @param mapsManager
   */
  public MainCategoriesPruner(MapsManager mapsManager)
  {
    _mapsManager=mapsManager;
  }

  private void doIt()
  {
    Map<Integer,IntegerHolder> stats=new HashMap<Integer,IntegerHolder>();
    GlobalMarkersManager markersMgr=_mapsManager.getMarkersManager();
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          doBlock(region,blockX,blockY,markersMgr,stats);
        }
      }
    }
    CategoriesManager categoriesMgr=_mapsManager.getCategories();
    List<Category> categories=categoriesMgr.getAllSortedByCode();
    for(Category category : categories)
    {
      int code=category.getCode();
      IntegerHolder counter=stats.get(Integer.valueOf(code));
      if (counter==null)
      {
        categoriesMgr.removeCategory(code);
        File iconFile=categoriesMgr.getIconFile(category);
        iconFile.delete();
      }
    }
    categoriesMgr.save();
  }

  private void doBlock(int region, int x, int y, GlobalMarkersManager markersMgr, Map<Integer,IntegerHolder> stats)
  {
    BlockMarkersManager blockMgr=markersMgr.getBlockManager(region,x,y);
    List<Marker> markers=blockMgr.getMarkers();
    for(Marker marker : markers)
    {
      Integer code=Integer.valueOf(marker.getCategoryCode());
      IntegerHolder counter=stats.get(code);
      if (counter==null)
      {
        counter=new IntegerHolder();
        stats.put(code,counter);
      }
      counter.increment();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    new MainCategoriesPruner(mapsManager).doIt();
  }
}
