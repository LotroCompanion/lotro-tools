package delta.games.lotro.tools.extraction.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.utils.valueTables.QualityBasedValuesTable;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.tools.extraction.utils.DatEnumsUtils;

/**
 * Loader for item value tables.
 * @author DAM
 */
public class ItemValueLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ItemValueLoader.class);

  private DataFacade _facade;
  private Map<Integer,QualityBasedValuesTable> _valueTables;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ItemValueLoader(DataFacade facade)
  {
    _facade=facade;
    _valueTables=new HashMap<Integer,QualityBasedValuesTable>();
  }

  /**
   * Get all the loaded tables.
   * @return an unsorted list of tables.
   */
  public List<QualityBasedValuesTable> getTables()
  {
    return new ArrayList<QualityBasedValuesTable>(_valueTables.values());
  }

  /**
   * Get an item value table.
   * @param tableId Table identifier.
   * @return the targeted table or <code>null</code> if not found.
   */
  public QualityBasedValuesTable getTable(int tableId)
  {
    Integer key=Integer.valueOf(tableId);
    QualityBasedValuesTable ret=_valueTables.get(key);
    if (ret==null)
    {
      ret=loadTable(tableId);
      if (ret!=null)
      {
        _valueTables.put(key,ret);
      }
      else
      {
        LOGGER.warn("Failed to load value table: {}",Integer.valueOf(tableId));
      }
    }
    return ret;
  }

  private QualityBasedValuesTable loadTable(int tableId)
  {
    QualityBasedValuesTable ret=null;
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new QualityBasedValuesTable();
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
