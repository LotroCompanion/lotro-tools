package delta.games.lotro.tools.dat.misc.ui;

import java.util.List;

/**
 * Interface of a UI component that has children.
 * @author DAM
 */
public interface UIContainer
{
  /**
   * Get child elements.
   * @return a list of child elements.
   */
  List<UIElement> getChildElements();
}
