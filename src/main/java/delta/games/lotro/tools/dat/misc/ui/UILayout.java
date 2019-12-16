package delta.games.lotro.tools.dat.misc.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * UI layout.
 * @author DAM
 */
public class UILayout
{
  private int _id;
  private List<UIElement> _uiElements;

  /**
   * Constructor.
   * @param id Identifier.
   */
  public UILayout(int id)
  {
    _id=id;
    _uiElements=new ArrayList<UIElement>();
  }

  /**
   * Get the UI element identifier.
   * @return an identifier.
   */
  public int getIdentifier()
  {
    return _id;
  }

  /**
   * Get the associated properties.
   * @return a properties set.
   */
  public List<UIElement> getUIElements()
  {
    return _uiElements;
  }

  /**
   * Add a UI element.
   * @param uiElement UI element to add.
   */
  public void addUIElement(UIElement uiElement)
  {
    _uiElements.add(uiElement);
  }
}
