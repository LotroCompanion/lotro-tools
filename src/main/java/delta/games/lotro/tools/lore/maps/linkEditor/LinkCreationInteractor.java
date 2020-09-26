package delta.games.lotro.tools.lore.maps.linkEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.maps.ui.MapCanvas;

/**
 * Manages link creation interactions.
 * @author DAM
 */
public class LinkCreationInteractor
{
  private MapsManager _manager;
  private MapCanvas _canvas;

  private MouseListener _listener;

  /**
   * Constructor.
   * @param mapsManager Maps manager.
   * @param canvas Decorated canvas.
   */
  public LinkCreationInteractor(MapsManager mapsManager, MapCanvas canvas)
  {
    _manager=mapsManager;
    _canvas=canvas;
    _listener=new LinkCreationMouseListener();
    _canvas.addMouseListener(_listener);
  }

  /**
   * Release all managed resources.
   */
  public void dispose()
  {
    if (_listener!=null)
    {
      _canvas.removeMouseListener(_listener);
      _listener=null;
    }
  }

  private void doLink(GeoreferencedBasemap target, int x, int y)
  {
    GeoreferencedBasemap currentMap=_canvas.getCurrentBasemap();
    int sourceMapId=currentMap.getIdentifier();
    GeoPoint hotPoint=currentMap.getGeoReference().pixel2geo(new Dimension(x,y));
    int targetMapId=target.getIdentifier();
    MapLink link=new MapLink(sourceMapId,0,targetMapId,hotPoint);
    LinksManager linksManager=_manager.getLinksManager();
    linksManager.addLink(link);
    linksManager.write();
    _canvas.repaint();
  }

  private class LinkCreationMouseListener extends MouseAdapter
  {
    private int _x;
    private int _y;

    @Override
    public void mouseClicked(MouseEvent event)
    {
      int button=event.getButton();
      int modifiers=event.getModifiers();
      if ((button==MouseEvent.BUTTON1) && ((modifiers&MouseEvent.SHIFT_MASK)!=0))
      {
        _x=event.getX();
        _y=event.getY();
        GeoreferencedBasemapsManager basemapsManager=_manager.getBasemapsManager();
        MapChooserDialogController chooser=new MapChooserDialogController(null,basemapsManager);
        chooser.setTitle("Choose...");
        chooser.getDialog().setLocationRelativeTo((Component)event.getSource());
        GeoreferencedBasemap selected=chooser.editModal();
        if (selected!=null)
        {
          doLink(selected,_x,_y);
        }
      }
    }
  }
}
