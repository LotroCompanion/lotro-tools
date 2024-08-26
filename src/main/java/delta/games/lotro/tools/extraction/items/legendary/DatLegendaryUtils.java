package delta.games.lotro.tools.extraction.items.legendary;

import delta.games.lotro.lore.items.legendary.LegacyType;

/**
 * Utility methods related to legendary stuff.
 * @author DAM
 */
public class DatLegendaryUtils
{
  /**
   * Get a legacy type from a combat property type code.
   * @param code Input code.
   * @return A <code>LegacyType</code> or <code>null</code> if no match.
   */
  public static LegacyType getLegacyTypeFromCombatPropertyType(int code)
  {
    if (code==3) return LegacyType.TACTICAL_DPS; // TacticalDPS
    if (code==6) return LegacyType.TACTICAL_DPS; // Minstrel_TacticalDPS
    if (code==22) return LegacyType.TACTICAL_DPS; // Loremaster_TacticalDPS
    if (code==15) return LegacyType.TACTICAL_DPS; // Guardian_TacticalDPS
    if (code==5) return LegacyType.TACTICAL_DPS; // Runekeeper_TacticalDPS
    if (code==7) return LegacyType.OUTGOING_HEALING; // Minstrel_HealingPS
    if (code==21) return LegacyType.OUTGOING_HEALING; // Loremaster_HealingPS
    if (code==8) return LegacyType.OUTGOING_HEALING; // Captain_HealingPS
    if (code==13) return LegacyType.OUTGOING_HEALING; // Runekeeper_HealingPS
    if (code==16) return LegacyType.INCOMING_HEALING; // Champion_IncomingHealing
    if (code==23) return LegacyType.INCOMING_HEALING; // Burglar_IncomingHealing
    if (code==25) return LegacyType.INCOMING_HEALING; // Champion_IncomingHealing_65
    if (code==24) return LegacyType.INCOMING_HEALING; // Burglar_IncomingHealing_65
    if (code==26) return LegacyType.FURY; // Mounted_MomentumMod
    if (code==28) return LegacyType.OUTGOING_HEALING; // Beorning_HealingPS
    return null;
  }
}
