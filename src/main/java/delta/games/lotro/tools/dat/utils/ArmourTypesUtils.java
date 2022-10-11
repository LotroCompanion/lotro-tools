package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.items.ArmourType;

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

  private static ArmourType getArmourType(int index)
  {
    // Shields
    if (index==11) return ArmourType.HEAVY_SHIELD;
    if (index==17) return ArmourType.SHIELD;
    if (index==40) return ArmourType.WARDEN_SHIELD;
    // Armour
    if (index==9) return ArmourType.MEDIUM;
    if (index==10) return ArmourType.HEAVY;
    if (index==18) return ArmourType.LIGHT;
    if (index==31) return ArmourType.LIGHT; // Cloak
    return null;
  }
}
