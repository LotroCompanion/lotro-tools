package delta.games.lotro.tools.dat.misc.ui;

import delta.games.lotro.dat.data.PropertiesSet;

/**
 * UI element.
 * @author DAM
 */
public class UIElement
{
  private int _id;
  private PropertiesSet _properties;

  /**
   * Constructor.
   * @param id Identifier.
   */
  public UIElement(int id)
  {
    _id=id;
    _properties=new PropertiesSet();
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
  public PropertiesSet getProperties()
  {
    return _properties;
  }
}
