package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.WeaponType;

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

  /**
   * Get a character class from a DAT enum code.
   * @param id Input code.
   * @return A character class or <code>null</code> if not supported.
   */
  public static CharacterClass getCharacterClassFromId(int id)
  {
    return delta.games.lotro.utils.dat.DatEnumsUtils.getCharacterClassFromId(id);
  }

  /**
   * Get a race from a DAT enum code.
   * @param raceId Input code.
   * @return A race or <code>null</code> if not supported.
   */
  public static Race getRaceFromRaceId(int raceId)
  {
    return delta.games.lotro.utils.dat.DatEnumsUtils.getRaceFromRaceId(raceId);
  }

  /**
   * Get an item quality from a DAT enum code.
   * @param qualityCode Input code.
   * @return An item quality or <code>null</code> if not supported.
   */
  public static ItemQuality getQuality(int qualityCode)
  {
    if (qualityCode==1) return ItemQuality.LEGENDARY;
    if (qualityCode==2) return ItemQuality.RARE;
    if (qualityCode==3) return ItemQuality.INCOMPARABLE;
    if (qualityCode==4) return ItemQuality.UNCOMMON;
    if (qualityCode==5) return ItemQuality.COMMON;
    return null;
  }

  /**
   * Get a slot from a DAT enum code.
   * @param slotCode Input code.
   * @return A slot or <code>null</code> if not supported.
   */
  public static EquipmentLocation getSlot(int slotCode)
  {
    if ((slotCode&1L<<1)!=0) return EquipmentLocation.HEAD;
    if ((slotCode&1L<<2)!=0) return EquipmentLocation.CHEST;
    if ((slotCode&1L<<3)!=0) return EquipmentLocation.LEGS;
    if ((slotCode&1L<<4)!=0) return EquipmentLocation.HAND;
    if ((slotCode&1L<<5)!=0) return EquipmentLocation.FEET;
    if ((slotCode&1L<<6)!=0) return EquipmentLocation.SHOULDER;
    if ((slotCode&1L<<7)!=0) return EquipmentLocation.BACK;
    if (((slotCode&1L<<8)!=0) || ((slotCode&1L<<9)!=0)) return EquipmentLocation.WRIST;
    if ((slotCode&1L<<10)!=0) return EquipmentLocation.NECK;
    if (((slotCode&1L<<11)!=0) || ((slotCode&1L<<12)!=0)) return EquipmentLocation.FINGER;
    if (((slotCode&1L<<13)!=0) || ((slotCode&1L<<14)!=0)) return EquipmentLocation.EAR;
    if ((slotCode&1L<<15)!=0) return EquipmentLocation.POCKET;
    if ((slotCode&1L<<16)!=0) return EquipmentLocation.MAIN_HAND;
    if ((slotCode&1L<<17)!=0) return EquipmentLocation.OFF_HAND;
    if ((slotCode&1L<<18)!=0) return EquipmentLocation.RANGED_ITEM;
    if ((slotCode&1L<<19)!=0) return EquipmentLocation.TOOL;
    if ((slotCode&1L<<20)!=0) return EquipmentLocation.CLASS_SLOT;
    if ((slotCode&1L<<21)!=0) return EquipmentLocation.BRIDLE;
    if ((slotCode&1L<<22)!=0) return EquipmentLocation.MAIN_HAND_AURA;
    if ((slotCode&1L<<23)!=0) return EquipmentLocation.OFF_HAND_AURA;
    if ((slotCode&1L<<24)!=0) return EquipmentLocation.RANGED_AURA;

    return null;
  }

  /**
   * Get a profession from a profession identifier.
   * @param professionId Profession identifier.
   * @return A profession or <code>null</code> if not found.
   */
  public static Profession getProfessionFromId(int professionId)
  {
    CraftingData crafting=CraftingSystem.getInstance().getData();
    Professions professions=crafting.getProfessionsRegistry();
    Profession profession=professions.getProfessionById(professionId);
    return profession;
  }

  /**
   * Get a weapon type from an equipment category code.
   * @param code Code to use.
   * @return A weapon type or <code>null</code>.
   */
  public static WeaponType getWeaponTypeFromEquipmentCategory(int code)
  {
    if (code==3) return WeaponType.TWO_HANDED_SWORD;
    if (code==4) return WeaponType.TWO_HANDED_CLUB;
    //if (code==5) return WeaponType.TWO_HANDED_MACE;
    if (code==6) return WeaponType.TWO_HANDED_AXE;
    if (code==8) return WeaponType.BOW;
    if (code==12) return WeaponType.ONE_HANDED_HAMMER;
    if (code==13) return WeaponType.SPEAR;
    if (code==14) return WeaponType.CROSSBOW;
    if (code==15) return WeaponType.TWO_HANDED_HAMMER;
    if (code==16) return WeaponType.HALBERD;
    if (code==20) return WeaponType.DAGGER;
    if (code==22) return WeaponType.STAFF;
    if (code==24) return WeaponType.ONE_HANDED_AXE;
    if (code==26) return WeaponType.ONE_HANDED_CLUB;
    if (code==27) return WeaponType.ONE_HANDED_MACE;
    if (code==28) return WeaponType.ONE_HANDED_SWORD;
    if (code==39) return WeaponType.RUNE_STONE;
    if (code==41) return WeaponType.JAVELIN;
    if (code==48) return WeaponType.BATTLE_GAUNTLETS;
    return null;
  }

  /**
   * Get an armour type from an equipment category code.
   * @param code Code to use.
   * @return An armour type or <code>null</code>.
   */
  public static ArmourType getArmourTypeFromEquipmentCategory(int code)
  {
    if (code==9) return ArmourType.MEDIUM;
    if (code==10) return ArmourType.HEAVY;
    if (code==11) return ArmourType.HEAVY_SHIELD;
    if (code==17) return ArmourType.SHIELD;
    if (code==18) return ArmourType.LIGHT;
    if (code==31) return ArmourType.LIGHT; // Cloak
    if (code==40) return ArmourType.WARDEN_SHIELD;
    return null;
  }
}
