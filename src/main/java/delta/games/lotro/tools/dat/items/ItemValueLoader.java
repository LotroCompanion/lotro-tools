package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.money.QualityBasedValueLookupTable;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;

/**
 * Loader for item value tables.
 * @author DAM
 */
public class ItemValueLoader
{
  private static final Logger LOGGER=Logger.getLogger(ItemValueLoader.class);

  private DataFacade _facade;
  private Map<Integer,QualityBasedValueLookupTable> _valueTables;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ItemValueLoader(DataFacade facade)
  {
    _facade=facade;
    _valueTables=new HashMap<Integer,QualityBasedValueLookupTable>();
  }

  /**
   * Get all the loaded tables.
   * @return an unsorted list of tables.
   */
  public List<QualityBasedValueLookupTable> getTables()
  {
    return new ArrayList<QualityBasedValueLookupTable>(_valueTables.values());
  }

  /**
   * Get an item value table.
   * @param tableId Table identifier.
   * @return the targeted table or <code>null</code> if not found.
   */
  public QualityBasedValueLookupTable getTable(int tableId)
  {
    Integer key=Integer.valueOf(tableId);
    QualityBasedValueLookupTable ret=_valueTables.get(key);
    if (ret==null)
    {
      ret=loadTable(tableId);
      if (ret!=null)
      {
        _valueTables.put(key,ret);
      }
      else
      {
        LOGGER.warn("Failed to load value table: "+tableId);
      }
    }
    return ret;
  }

  private QualityBasedValueLookupTable loadTable(int tableId)
  {
    QualityBasedValueLookupTable ret=null;
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new QualityBasedValueLookupTable();
      ret.setIdentifier(tableId);
      // Qualities
      Object[] qualityArray=(Object[])properties.getProperty("Item_QualityArray");
      for(Object qualityObj : qualityArray)
      {
        PropertiesSet qualityProps=(PropertiesSet)qualityObj;
        int qualityCode=((Integer)qualityProps.getProperty("Item_Quality")).intValue();
        ItemQuality quality=DatEnumsUtils.getQuality(qualityCode);
        float factor=((Float)qualityProps.getProperty("Item_QualityModVal")).floatValue();
        ret.addQualityFactor(quality,factor);
      }
      // Base values
      Object[] baseValueArray=(Object[])properties.getProperty("Item_BaseValArray");
      int level=1;
      for(Object baseValueObj : baseValueArray)
      {
        PropertiesSet baseValueProps=(PropertiesSet)baseValueObj;
        float baseValue=((Float)baseValueProps.getProperty("Item_BaseVal")).floatValue();
        ret.addBaseValue(level,baseValue);
        level++;
      }
    }
    return ret;
  }
}
