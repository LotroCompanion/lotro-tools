package delta.games.lotro.tools.dat.misc.ui;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;

/**
 * Finder for UI components.
 * @author DAM
 */
public class UIFinder
{
  private DataFacade _facade;
  private EnumMapper _uiElementIdMapper;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public UIFinder(DataFacade facade)
  {
    _facade=facade;
    _uiElementIdMapper=_facade.getEnumsManager().getEnumMapper(587202769);
  }

  /**
   * Find a component.
   * @param root Root component.
   * @param path Path to reach the targeted component.
   * @return A component or <code>null</code> if not found.
   */
  public UIContainer find(UILayout root, String[] path)
  {
    UIContainer current=root;
    for(String pathElement : path)
    {
      current=find(current,pathElement);
      if (current==null)
      {
        return null;
      }
    }
    return current;
  }

  private UIElement find(UIContainer root, String path)
  {
    for(UIElement uiElement : root.getChildElements())
    {
      int uiElementId=uiElement.getIdentifier();
      String uiElementIdStr=_uiElementIdMapper.getString(uiElementId);
      if (path.equals(uiElementIdStr))
      {
        return uiElement;
      }
    }
    return null;
  }
}
