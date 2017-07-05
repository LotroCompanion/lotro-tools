package delta.games.lotro.tools.lore.maps.linkEditor;

import java.io.File;

import javax.swing.JFrame;

import delta.common.utils.collections.filters.Filter;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.ui.MapCanvas;
import delta.games.lotro.maps.ui.NavigationListener;
import delta.games.lotro.maps.ui.NavigationManager;

/**
 * Link editor for maps.
 * @author DAM
 */
public class MainLinkEditor
{
  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    File rootDir=new File("../lotro-maps-db");
    final MapsManager mapsManager=new MapsManager(rootDir);
    mapsManager.load();

    Filter<Marker> filter=new Filter<Marker>()
    {
      public boolean accept(Marker item)
      {
        return false;
      }
    };

    MapBundle bundle=mapsManager.getMapByKey("breeland");
    MapCanvas canvas=new MapCanvas(mapsManager);
    final NavigationManager navigationManager=new NavigationManager(canvas);
    NavigationListener listener=new NavigationListener()
    {
      public void mapChangeRequest(String key)
      {
        navigationManager.setMap(mapsManager.getMapByKey(key).getMap());
      }
    };
    navigationManager.setNavigationListener(listener);
    /*LinkCreationInterator interactor=*/new LinkCreationInterator(mapsManager,canvas);
    canvas.setFilter(filter);
    String key=bundle.getKey();
    canvas.setMap(key);
    navigationManager.setMap(bundle.getMap());
    JFrame f=new JFrame();
    String title=bundle.getLabel();
    f.setTitle(title);
    f.getContentPane().add(canvas);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
}
