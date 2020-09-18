package delta.games.lotro.tools.lore.maps.linkEditor;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;

import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.maps.ui.MapCanvas;
import delta.games.lotro.maps.ui.MapPanelController;
import delta.games.lotro.maps.ui.navigation.MapViewDefinition;
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

    MapBundle bundle=mapsManager.getMapByKey(268437653);
    final MapPanelController mapPanelCtrl=new MapPanelController(mapsManager);
    MapCanvas canvas=mapPanelCtrl.getCanvas();
    final NavigationSupport navSupport=new NavigationSupport(canvas);
    NavigationListener listener=new NavigationListener()
    {
      public void mapChangeRequest(MapViewDefinition mapView)
      {
        int key=mapView.getMapKey();
        MapBundle map=mapsManager.getMapByKey(key);
        if (map==null)
        {
          return;
        }
        // Set map
        mapPanelCtrl.setMap(mapView);
        // Set title
        String title=map.getName();
        _frame.setTitle(title);
        LinksManager linksManager=mapsManager.getLinksManager();
        List<MapLink> links=linksManager.getLinks(key,0);
        // Set links
        navSupport.setLinks(links);
      }
    };
    navSupport.getNavigationListeners().addListener(listener);
    /*LinkCreationInterator interactor=*/new LinkCreationInteractor(mapsManager,canvas);
    int key=bundle.getKey();
    JFrame f=new JFrame();
    _frame=f;
    String title=bundle.getName();
    f.setTitle(title);
    f.getContentPane().add(mapPanelCtrl.getLayers());
    navSupport.requestMap(key);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
}
