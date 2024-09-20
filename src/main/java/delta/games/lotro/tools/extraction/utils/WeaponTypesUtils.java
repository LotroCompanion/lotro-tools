package delta.games.lotro.tools.extraction.utils;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.tools.extraction.items.legendary.LegaciesLoader;

/**
 * Utility methods related to weapon types.
 * @author DAM
 */
public class WeaponTypesUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LegaciesLoader.class);

  private EnumMapper _equipmentCategory;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public WeaponTypesUtils(DataFacade facade)
  {
    _equipmentCategory=facade.getEnumsManager().getEnumMapper(587202636);
  }

  /**
   * Get the weapon types from the given equipment category.
   * @param equipmentCategory Equipment category.
   * @return A possibly empty but never <code>null</code> set of weapon types.
   */
  public Set<WeaponType> getAllowedEquipment(long equipmentCategory)
  {
    BitSet equipementBitSet=BitSetUtils.getBitSetFromFlags(equipmentCategory);
    if (LOGGER.isDebugEnabled())
    {
      String allowedEquipementTypes=BitSetUtils.getStringFromBitSet(equipementBitSet,_equipmentCategory, ",");
      LOGGER.debug("Allowed equipment types:"+allowedEquipementTypes);
    }
    Set<WeaponType> ret=new HashSet<WeaponType>();
    for(int i=0;i<equipementBitSet.size();i++)
    {
      if (equipementBitSet.get(i))
      {
        WeaponType weaponType=DatEnumsUtils.getWeaponTypeFromEquipmentCategory(i+1);
        if (weaponType!=null)
        {
          ret.add(weaponType);
        }
      }
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Decoded equipment types:"+ret);
    }
    return ret;
  }
}
