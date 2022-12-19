package delta.games.lotro.tools.lore.maps.linkEditor;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.maps.ui.BasemapPanelController;
import delta.games.lotro.maps.ui.navigation.MapViewDefinition;
import delta.games.lotro.maps.ui.navigation.NavigationListener;
import delta.games.lotro.maps.ui.navigation.NavigationSupport;
import delta.games.lotro.tools.dat.maps.MapConstants;

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
    File rootDir=MapConstants.getRootDir();
    final MapsManager mapsManager=new MapsManager(rootDir);
    final GeoreferencedBasemapsManager basemapsManager=mapsManager.getBasemapsManager();

    GeoreferencedBasemap basemap=basemapsManager.getMapById(268437653);
    final BasemapPanelController mapPanelCtrl=new BasemapPanelController(basemapsManager);
    final NavigationSupport navSupport=new NavigationSupport(mapPanelCtrl);
    NavigationListener listener=new NavigationListener()
    {
      public void mapChangeRequest(MapViewDefinition mapView)
      {
        int key=mapView.getMapKey();
        GeoreferencedBasemap map=basemapsManager.getMapById(key);
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
    /*LinkCreationInterator interactor=*/new LinkCreationInteractor(mapsManager,mapPanelCtrl);
    int mapId=basemap.getIdentifier();
    JFrame f=new JFrame();
    _frame=f;
    String title=basemap.getName();
    f.setTitle(title);
    f.getContentPane().add(mapPanelCtrl.getComponent());
    navSupport.requestMap(mapId);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }
}
