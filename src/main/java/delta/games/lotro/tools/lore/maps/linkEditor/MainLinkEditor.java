package delta.games.lotro.tools.lore.maps.linkEditor;

import java.io.File;

import javax.swing.JFrame;

import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.ui.MapCanvas;
import delta.games.lotro.maps.ui.NavigationListener;
import delta.games.lotro.maps.ui.NavigationManager;

/**
 * Link editor for maps.
 * @author DAM
 */
public class MainLinkEditor
{
  private static JFrame _frame;

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    File rootDir=new File("../lotro-maps-db");
    final MapsManager mapsManager=new MapsManager(rootDir);
    mapsManager.load();

    MapBundle bundle=mapsManager.getMapByKey("breeland");
    MapCanvas canvas=new MapCanvas(mapsManager);
    final NavigationManager navigationManager=new NavigationManager(canvas);
    NavigationListener listener=new NavigationListener()
    {
      public void mapChangeRequest(String key)
      {
        MapBundle map=mapsManager.getMapByKey(key);
        navigationManager.setMap(map);
        String title=map.getLabel();
        _frame.setTitle(title);
      }
    };
    navigationManager.getNavigationListeners().addListener(listener);
    /*LinkCreationInterator interactor=*/new LinkCreationInterator(mapsManager,canvas);
    String key=bundle.getKey();
    canvas.setMap(key);
    navigationManager.setMap(bundle);
    JFrame f=new JFrame();
    _frame=f;
    String title=bundle.getLabel();
    f.setTitle(title);
    f.getContentPane().add(canvas);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
}
