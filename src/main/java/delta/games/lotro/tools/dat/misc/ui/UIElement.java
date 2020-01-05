package delta.games.lotro.tools.dat.misc.ui;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.dat.data.PropertiesSet;

/**
 * UI element.
 * @author DAM
 */
public class UIElement implements UIContainer
{
  private int _id;
  private List<UIElement> _children;
  private List<UIData> _data;
  private PropertiesSet _properties;

  /**
   * Constructor.
   * @param id Identifier.
   */
  public UIElement(int id)
  {
    _id=id;
    _children=new ArrayList<UIElement>();
    _data=new ArrayList<UIData>();
    _properties=new PropertiesSet();
  }

  /**
   * Add a child element.
   * @param element Element to add.
   */
  public void addChild(UIElement element)
  {
    _children.add(element);
  }

  @Override
  public List<UIElement> getChildElements()
  {
    return _children;
  }

  /**
   * Add a data item.
   * @param data Data item to add.
   */
  public void addData(UIData data)
  {
    _data.add(data);
  }

  /**
   * Get the data items.
   * @return a list of data items.
   */
  public List<UIData> getData()
  {
    return _data;
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
