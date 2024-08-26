package delta.games.lotro.tools.extraction.items;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.weapons.WeaponSpeedEntry;
import delta.games.lotro.lore.items.weapons.WeaponSpeedTable;
import delta.games.lotro.lore.items.weapons.WeaponSpeedTables;
import delta.games.lotro.tools.extraction.utils.DatEnumsUtils;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Loader for speed tables.
 * @author DAM
 */
public class SpeedValuesLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SpeedValuesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load weapon speed data.
   * @return the loaded speed tables.
   */
  public WeaponSpeedTables loadData()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"WeaponActionDurationTable");
    WeaponSpeedTables ret=new WeaponSpeedTables();
    /*
Item_WeaponActionDurationTable:
  #1: Item_WeaponActionDurationData 1879049507
  ...
  #19: Item_WeaponActionDurationData 1879049525
     */
    if (props!=null)
    {
      Object[] idObjs=(Object[])props.getProperty("Item_WeaponActionDurationTable");
      for(Object idObj : idObjs)
      {
        int id=((Integer)idObj).intValue();
        WeaponSpeedTable weaponSpeed=loadTable(id);
        if (weaponSpeed!=null)
        {
          ret.addWeaponSpeed(weaponSpeed);
        }
      }
    }
    return ret;
  }

  private WeaponSpeedTable loadTable(int tableId)
  {
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    /*
Item_EquipmentCategory: 8192 (Crossbow[E])
Item_WeaponActionDurationDataArray:
  #1: Item_WeaponActionDurationDataEntry
    Item_BaseActionDuration: 2.7
    Item_BaseAnimDurationMultiplierMod: 0.1
    Item_WeaponSpeed: 5 (ExtremelySlow)
  #2: Item_WeaponActionDurationDataEntry
    Item_BaseActionDuration: 2.6
    Item_BaseAnimDurationMultiplierMod: 0.05
    Item_WeaponSpeed: 4 (VerySlow)
    ...
     */
    Long equipmentCategoryValue=(Long)properties.getProperty("Item_EquipmentCategory");
    int equipmentCategoryCode=DatEnumsUtils.getEquipmentCategoryCode(equipmentCategoryValue);
    WeaponType weaponType=DatEnumsUtils.getWeaponTypeFromEquipmentCategory(equipmentCategoryCode);
    if (weaponType==null)
    {
      return null;
    }
    WeaponSpeedTable ret=new WeaponSpeedTable(weaponType);
    // Duration data
    Object[] dataArray=(Object[])properties.getProperty("Item_WeaponActionDurationDataArray");
    for(Object entryObj : dataArray)
    {
      PropertiesSet durationProps=(PropertiesSet)entryObj;
      int weaponSpeed=((Integer)durationProps.getProperty("Item_WeaponSpeed")).intValue();
      float baseDuration=((Float)durationProps.getProperty("Item_BaseActionDuration")).floatValue();
      float animDurationMod=((Float)durationProps.getProperty("Item_BaseAnimDurationMultiplierMod")).floatValue();
      WeaponSpeedEntry entry=new WeaponSpeedEntry(weaponSpeed,baseDuration,animDurationMod);
      ret.addSpeedEntry(entry);
    }
    return ret;
  }
}
