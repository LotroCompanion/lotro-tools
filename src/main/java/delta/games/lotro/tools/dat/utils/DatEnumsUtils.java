package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.items.DamageType;

/**
 * Misc enum utils.
 * @author DAM
 */
public class DatEnumsUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatEnumsUtils.class);

  /**
   * Get a damage type from a DAT enum code.
   * @param damageTypeEnum Input code.
   * @return A damage type or <code>null</code> if not supported.
   */
  public static DamageType getDamageType(int damageTypeEnum)
  {
    // 0 Undef
    if (damageTypeEnum==1) return DamageType.COMMON;
    if (damageTypeEnum==2) return DamageType.WESTERNESSE;
    if (damageTypeEnum==4) return DamageType.ANCIENT_DWARF;
    if (damageTypeEnum==8) return DamageType.BELERIAND;
    if (damageTypeEnum==16) return DamageType.FIRE;
    if (damageTypeEnum==32) return DamageType.SHADOW;
    if (damageTypeEnum==64) return DamageType.LIGHT;
    // 128 ImplementInherited
    if (damageTypeEnum==256) return DamageType.FROST;
    if (damageTypeEnum==512) return DamageType.LIGHTNING;
    // 1024  Acid
    // 2048  Morgul-forged
    // 4096  Orc-craft
    // 8192  Fell-wrought
    // 16384 Physical
    // 32768 Tactical
    // 49152 PvP
    // 65407 ALL
    LOGGER.warn("Unmanaged damage type: "+damageTypeEnum);
    return null;
  }
}
