package delta.games.lotro.tools.lore.sounds;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.enums.AbstractMapper;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.enums.MapperUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Loads music data for items. 
 * @author DAM
 */
public class ItemsMusicLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ItemsMusicLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<Item> items=findMusicItems();
    for(Item item : items)
    {
      handleItem(item);
    }
  }

  private List<Item> findMusicItems()
  {
    List<Item> ret=new ArrayList<Item>();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      ItemClass itemClass=item.getItemClass();
      if ((itemClass!=null) && (itemClass.getCode()==82))
      {
        // Home: Music
        ret.add(item);
      }
    }
    return ret;
  }

  private void handleItem(Item item)
  {
    int itemId=item.getIdentifier();
    PropertiesSet itemProps=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    long category=((Long)itemProps.getProperty("Item_Decoration_Category")).longValue();
    int propertyId=((Integer)itemProps.getProperty("Item_Decoration_PropertyHook_Name")).intValue();
    int propertyValue=((Integer)itemProps.getProperty("Item_Decoration_PropertyHook_Value")).intValue();
    EnumMapper enumMapper=findEnumForProperty(propertyId);
    String valueLabel=(enumMapper!=null)?enumMapper.getLabel(propertyValue):"?";
    System.out.println("Item: "+item+" => category="+category+", ID="+propertyId+", value="+propertyValue+" ("+valueLabel+")");
  }

  private EnumMapper findEnumForProperty(int propertyId)
  {
    PropertiesRegistry registry=_facade.getPropertiesRegistry();
    PropertyDefinition propertyDef=registry.getPropertyDef(propertyId);
    if (propertyDef==null)
    {
      return null;
    }
    PropertyType type=propertyDef.getPropertyType();
    if (type!=PropertyType.ENUM_MAPPER)
    {
      return null;
    }
    int enumId=propertyDef.getData();
    AbstractMapper mapper=MapperUtils.getEnum(_facade,enumId);
    if (mapper instanceof EnumMapper)
    {
      return (EnumMapper)mapper;
    }
    return null;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    ItemsMusicLoader loader=new ItemsMusicLoader(facade);
    loader.doIt();
    facade.dispose();
  }
}
