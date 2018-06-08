package delta.games.lotro.tools.lore.maps.linkEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Map;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapLink;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.ui.MapCanvas;

/**
 * Manages link creation interactions.
 * @author DAM
 */
public class LinkCreationInterator
{
  private MapsManager _manager;
  private MapCanvas _canvas;

  private MouseListener _listener;

  /**
   * Constructor.
   * @param mapsManager Maps manager.
   * @param canvas Decorated canvas.
   */
  public LinkCreationInterator(MapsManager mapsManager, MapCanvas canvas)
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

  private void doLink(MapBundle bundle, int x, int y)
  {
    MapBundle currentMap=_canvas.getCurrentMap();
    Map map=currentMap.getMap();
    String target=bundle.getKey();
    GeoPoint hotPoint=map.getGeoReference().pixel2geo(new Dimension(x,y));
    MapLink link=new MapLink(target,hotPoint);
    currentMap.getLinks().add(link);
    _manager.saveMap(map.getKey());
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
        MapChooserDialogController chooser=new MapChooserDialogController(null,_manager);
        chooser.setTitle("Choose...");
        chooser.getDialog().setLocationRelativeTo((Component)event.getSource());
        MapBundle selected=chooser.editModal();
        if (selected!=null)
        {
          doLink(selected,_x,_y);
        }
      }
    }
  }
}
