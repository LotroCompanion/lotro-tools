package delta.games.lotro.tools.lore.maps.linkEditor;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import delta.common.ui.swing.GuiFactory;
import delta.common.ui.swing.combobox.ComboBoxController;
import delta.common.ui.swing.windows.DefaultFormDialogController;
import delta.common.ui.swing.windows.WindowController;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;

/**
 * Map chooser dialog controller.
 * @author DAM
 */
public class MapChooserDialogController extends DefaultFormDialogController<MapBundle>
{
  private ComboBoxController<MapBundle> _controller;
  private MapsManager _manager;

  /**
   * Constructor.
   * @param parentController Parent controller.
   * @param mapsManager Maps manager.
   */
  public MapChooserDialogController(WindowController parentController, MapsManager mapsManager)
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
