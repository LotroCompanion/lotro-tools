package delta.games.lotro.tools.lore.maps.linkEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import delta.common.ui.swing.GuiFactory;
import delta.common.ui.swing.OKCancelPanelController;
import delta.common.ui.swing.combobox.ComboBoxController;
import delta.common.ui.swing.windows.DefaultDialogController;
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
    map.addLink(link);
    _manager.saveMap(map.getKey());
    _canvas.repaint();
  }

  private class LinkCreationMouseListener extends MouseAdapter
  {
    private int _x;
    private int _y;
    private DefaultDialogController _chooser;

    public LinkCreationMouseListener()
    {
      _chooser=builChooserWindow();
    }

    @Override
    public void mouseClicked(MouseEvent event)
    {
      int button=event.getButton();
      int modifiers=event.getModifiers();
      if ((button==MouseEvent.BUTTON1) && ((modifiers&MouseEvent.SHIFT_MASK)!=0))
      {
        _x=event.getX();
        _y=event.getY();
        _chooser.show(true);
      }
    }

    private DefaultDialogController builChooserWindow()
    {
      DefaultDialogController controller=new DefaultDialogController(null) {
        @Override
        protected JComponent buildContents()
        {
          return buildChooser();
        }
      };
      controller.setTitle("Choose...");
      controller.getWindow().pack();
      return controller;
    }

    private JPanel buildChooser()
    {
      final JPanel main=GuiFactory.buildBackgroundPanel(new BorderLayout());
      JPanel map=GuiFactory.buildBackgroundPanel(new FlowLayout());
      JLabel label=GuiFactory.buildLabel("Choose map:");
      map.add(label);
      final ComboBoxController<MapBundle> controller=buildMapCombo();
      JComboBox combo=controller.getComboBox();
      map.add(combo);
      main.add(map,BorderLayout.NORTH);
      OKCancelPanelController okCancel=new OKCancelPanelController();
      ActionListener ok=new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          MapBundle selected=controller.getSelectedItem();
          if (selected!=null)
          {
            doLink(selected,_x,_y);
            Window window=SwingUtilities.getWindowAncestor(main);
            window.dispose();
          }
        }
      };
      okCancel.getOKButton().addActionListener(ok);
      ActionListener cancel=new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          Window window=SwingUtilities.getWindowAncestor(main);
          window.dispose();
        }
      };
      okCancel.getCancelButton().addActionListener(cancel);
      main.add(okCancel.getPanel(),BorderLayout.SOUTH);
      return main;
    }

    private ComboBoxController<MapBundle> buildMapCombo()
    {
      ComboBoxController<MapBundle> controller=new ComboBoxController<MapBundle>();
      List<MapBundle> bundles=_manager.getMaps();
      for(MapBundle bundle : bundles)
      {
        controller.addItem(bundle,bundle.getLabel());
      }
      return controller;
    }
  }
}
