package delta.games.lotro.tools.lore.maps.linkEditor;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import delta.common.ui.swing.GuiFactory;
import delta.common.ui.swing.combobox.ComboBoxController;
import delta.common.ui.swing.windows.DefaultFormDialogController;
import delta.common.ui.swing.windows.WindowController;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;

/**
 * Map chooser dialog controller.
 * @author DAM
 */
public class MapChooserDialogController extends DefaultFormDialogController<GeoreferencedBasemap>
{
  private ComboBoxController<GeoreferencedBasemap> _controller;
  private GeoreferencedBasemapsManager _manager;

  /**
   * Constructor.
   * @param parentController Parent controller.
   * @param mapsManager Maps manager.
   */
  public MapChooserDialogController(WindowController parentController, GeoreferencedBasemapsManager mapsManager)
  {
    super(parentController,null);
    _manager=mapsManager;
  }

  @Override
  protected JPanel buildFormPanel()
  {
    JPanel map=GuiFactory.buildPanel(new FlowLayout());
    JLabel label=GuiFactory.buildLabel("Choose map:");
    map.add(label);
    _controller=buildMapCombo();
    map.add(_controller.getComboBox());
    return map;
  }

  private ComboBoxController<GeoreferencedBasemap> buildMapCombo()
  {
    ComboBoxController<GeoreferencedBasemap> controller=new ComboBoxController<GeoreferencedBasemap>();
    List<GeoreferencedBasemap> basemaps=_manager.getBasemaps();
    for(GeoreferencedBasemap basemap : basemaps)
    {
      controller.addItem(basemap,basemap.getName());
    }
    return controller;
  }

  @Override
  protected void okImpl()
  {
    _data=_controller.getSelectedItem();
  }

  @Override
  public void dispose()
  {
    super.dispose();
    if (_controller!=null)
    {
      _controller.dispose();
      _controller=null;
    }
    _manager=null;
  }
}
