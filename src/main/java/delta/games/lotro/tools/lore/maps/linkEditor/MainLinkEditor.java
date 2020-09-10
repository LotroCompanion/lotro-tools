package delta.games.lotro.tools.lore.maps.linkEditor;

import java.io.File;

import javax.swing.JFrame;

import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.ui.MapCanvas;
import delta.games.lotro.maps.ui.NavigationListener;
import delta.games.lotro.maps.ui.NavigationManager;
import delta.games.lotro.maps.ui.controllers.NavigationController;
import delta.games.lotro.maps.ui.controllers.ViewInputsManager;
import delta.games.lotro.maps.ui.layers.LinksLayer;

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
    final NavigationManager navigationManager=new NavigationManager();
    final LinksLayer linksLayer=new LinksLayer(canvas);
    linksLayer.setLinks(bundle.getLinks());
    canvas.addLayer(linksLayer);

    ViewInputsManager inputsMgr=new ViewInputsManager(canvas);
    final NavigationController navigationController=new NavigationController(canvas,navigationManager);
    inputsMgr.addInputController(navigationController);
    navigationController.setLinks(bundle.getLinks());

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
        navigationManager.setMap(map);
        navigationController.setLinks(map.getLinks());
        linksLayer.setLinks(map.getLinks());
        String title=map.getName();
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
    String title=bundle.getName();
    f.setTitle(title);
    f.getContentPane().add(canvas);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
}
