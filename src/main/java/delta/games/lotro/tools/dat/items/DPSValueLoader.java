package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.utils.valueTables.QualityBasedValuesTable;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;

/**
 * Loader for DPS tables.
 * @author DAM
 */
public class DPSValueLoader
{
  private static final Logger LOGGER=Logger.getLogger(DPSValueLoader.class);

  private DataFacade _facade;
  private Map<Integer,QualityBasedValuesTable> _valueTables;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DPSValueLoader(DataFacade facade)
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
        LOGGER.warn("Failed to load value table: "+tableId);
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
      Object[] qualityArray=(Object[])properties.getProperty("Combat_QualityModArray");
      for(Object qualityObj : qualityArray)
      {
        PropertiesSet qualityProps=(PropertiesSet)qualityObj;
        int qualityCode=((Integer)qualityProps.getProperty("Combat_Quality")).intValue();
        ItemQuality quality=DatEnumsUtils.getQuality(qualityCode);
        float factor=((Float)qualityProps.getProperty("Combat_DPSMod")).floatValue();
        ret.addQualityFactor(quality,factor);
      }
      // Base values
      Object[] baseValueArray=(Object[])properties.getProperty("Combat_BaseDPSArray");
      int level=1;
      for(Object baseValueObj : baseValueArray)
      {
        float baseValue=((Float)baseValueObj).floatValue();
        ret.addBaseValue(level,baseValue);
        level++;
      }
    }
    return ret;
  }
}
