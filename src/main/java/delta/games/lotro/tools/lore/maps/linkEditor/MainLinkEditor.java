package delta.games.lotro.tools.lore.maps.linkEditor;

import java.io.File;

import javax.swing.JFrame;

import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.ui.MapCanvas;
import delta.games.lotro.maps.ui.navigation.NavigationListener;
import delta.games.lotro.maps.ui.navigation.NavigationSupport;

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

    MapBundle bundle=mapsManager.getMapByKey("268437653");
    final MapCanvas canvas=new MapCanvas(mapsManager);
    NavigationSupport navSupport=new NavigationSupport(canvas);
    NavigationListener listener=new NavigationListener()
    {
      public void mapChangeRequest(String key)
      {
        MapBundle map=mapsManager.getMapByKey(key);
        if (map==null)
        {
          return;
        }
        canvas.setMap(key);
        String title=map.getName();
        _frame.setTitle(title);
      }
    };
    navSupport.getNavigationListeners().addListener(listener);
    /*LinkCreationInterator interactor=*/new LinkCreationInterator(mapsManager,canvas);
    String key=bundle.getKey();
    JFrame f=new JFrame();
    _frame=f;
    String title=bundle.getName();
    f.setTitle(title);
    f.getContentPane().add(canvas);
    navSupport.requestMap(key);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
}
