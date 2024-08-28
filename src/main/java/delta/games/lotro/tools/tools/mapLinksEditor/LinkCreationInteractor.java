package delta.games.lotro.tools.tools.mapLinksEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.maps.ui.BasemapPanelController;

/**
 * Manages link creation interactions.
 * @author DAM
 */
public class LinkCreationInteractor
{
  private MapsManager _manager;
  private BasemapPanelController _mapPanel;

  private MouseListener _listener;

  /**
   * Constructor.
   * @param mapsManager Maps manager.
   * @param mapPanel Map panel.
   */
  public LinkCreationInteractor(MapsManager mapsManager, BasemapPanelController mapPanel)
  {
    _manager=mapsManager;
    _mapPanel=mapPanel;
    _listener=new LinkCreationMouseListener();
    _mapPanel.getCanvas().addMouseListener(_listener);
  }

  /**
   * Release all managed resources.
   */
  public void dispose()
  {
    if (_listener!=null)
    {
      _mapPanel.getCanvas().removeMouseListener(_listener);
      _listener=null;
    }
  }

  private void doLink(GeoreferencedBasemap target, int x, int y)
  {
    GeoreferencedBasemap currentMap=_mapPanel.getCurrentBasemap();
    int sourceMapId=currentMap.getIdentifier();
    GeoPoint hotPoint=currentMap.getGeoReference().pixel2geo(new Dimension(x,y));
    int targetMapId=target.getIdentifier();
    MapLink link=new MapLink(sourceMapId,0,targetMapId,hotPoint,null);
    LinksManager linksManager=_manager.getLinksManager();
    linksManager.addLink(link);
    linksManager.write();
    _mapPanel.getCanvas().repaint();
  }

  private class LinkCreationMouseListener extends MouseAdapter
  {
    @Override
    public void mouseClicked(MouseEvent event)
    {
      int button=event.getButton();
      int modifiers=event.getModifiersEx();
      if ((button==MouseEvent.BUTTON1) && ((modifiers&InputEvent.SHIFT_DOWN_MASK)!=0))
      {
        int x=event.getX();
        int y=event.getY();
        GeoreferencedBasemapsManager basemapsManager=_manager.getBasemapsManager();
        MapChooserDialogController chooser=new MapChooserDialogController(null,basemapsManager);
        chooser.setTitle("Choose...");
        chooser.getDialog().setLocationRelativeTo((Component)event.getSource());
        GeoreferencedBasemap selected=chooser.editModal();
        if (selected!=null)
        {
          doLink(selected,x,y);
        }
      }
    }
  }
}
