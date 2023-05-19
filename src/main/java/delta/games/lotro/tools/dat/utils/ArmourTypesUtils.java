package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ArmourTypes;
import delta.games.lotro.lore.items.ShieldTypes;

/**
 * Utility methods related to armour types.
 * @author DAM
 */
public class ArmourTypesUtils
{
  /**
   * Get the armour types from the given equipment category.
   * @param equipmentCategory Equipment category.
   * @return A possibly empty but never <code>null</code> set of armour types.
   */
  public static Set<ArmourType> getArmourTypes(long equipmentCategory)
  {
    BitSet equipementBitSet=BitSetUtils.getBitSetFromFlags(equipmentCategory);
    Set<ArmourType> ret=new HashSet<ArmourType>();
    for(int i=0;i<equipementBitSet.size();i++)
    {
      if (equipementBitSet.get(i))
      {
        ArmourType armourType=getArmourType(i+1);
        if (armourType!=null)
        {
          ret.add(armourType);
        }
      }
    }
    return ret;
  }

  /**
   * Get an armour type from an equipment category code.
   * @param code Code to use.
   * @return An armour type or <code>null</code>.
   */
  public static ArmourType getArmourType(int code)
  {
    // Shields
    if (code==11) return ShieldTypes.HEAVY_SHIELD;
    if (code==17) return ShieldTypes.SHIELD;
    if (code==40) return ShieldTypes.WARDEN_SHIELD;
    // Armour
    if (code==9) return ArmourTypes.MEDIUM;
    if (code==10) return ArmourTypes.HEAVY;
    if (code==18) return ArmourTypes.LIGHT;
    if (code==31) return ArmourTypes.LIGHT; // Cloak
    return null;
  }
}
