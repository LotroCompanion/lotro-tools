package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.tools.dat.items.legendary.LegaciesLoader;

/**
 * Utility methods related to weapon types.
 * @author DAM
 */
public class WeaponTypesUtils
{
  private static final Logger LOGGER=Logger.getLogger(LegaciesLoader.class);

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
        WeaponType weaponType=getWeaponType(i+1);
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

  private WeaponType getWeaponType(int index)
  {
    if (index==3) return WeaponType.TWO_HANDED_SWORD;
    if (index==4) return WeaponType.TWO_HANDED_CLUB;
    if (index==6) return WeaponType.TWO_HANDED_AXE;
    if (index==8) return WeaponType.BOW;
    if (index==12) return WeaponType.ONE_HANDED_HAMMER;
    if (index==13) return WeaponType.SPEAR;
    if (index==14) return WeaponType.CROSSBOW;
    if (index==15) return WeaponType.TWO_HANDED_HAMMER;
    if (index==16) return WeaponType.HALBERD;
    if (index==20) return WeaponType.DAGGER;
    if (index==22) return WeaponType.STAFF;
    if (index==24) return WeaponType.ONE_HANDED_AXE;
    if (index==26) return WeaponType.ONE_HANDED_CLUB;
    if (index==27) return WeaponType.ONE_HANDED_MACE;
    if (index==28) return WeaponType.ONE_HANDED_SWORD;
    if (index==39) return WeaponType.RUNE_STONE;
    if (index==41) return WeaponType.JAVELIN;
    if (index==48) return WeaponType.BATTLE_GAUNTLETS;
    //LOGGER.warn("Unmanaged weapon type: "+index);
    return null;
  }
}
