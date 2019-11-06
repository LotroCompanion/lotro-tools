package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;

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
    if (id==214) return CharacterClass.BEORNING;
    if (id==40) return CharacterClass.BURGLAR;
    if (id==24) return CharacterClass.CAPTAIN;
    if (id==172) return CharacterClass.CHAMPION;
    if (id==23) return CharacterClass.GUARDIAN;
    if (id==162) return CharacterClass.HUNTER;
    if (id==185) return CharacterClass.LORE_MASTER;
    if (id==31) return CharacterClass.MINSTREL;
    if (id==193) return CharacterClass.RUNE_KEEPER;
    if (id==194) return CharacterClass.WARDEN;
    // Monster Play
    if (id==71) return null; // Reaver
    if (id==128) return null; // Defiler
    if (id==127) return null; // Weaver
    if (id==179) return null; // Blackarrow
    if (id==52) return null; // Warleader
    if (id==126) return null; // Stalker
    return null;
  }

  /**
   * Get a race from a DAT enum code.
   * @param raceId Input code.
   * @return A race or <code>null</code> if not supported.
   */
  public static Race getRaceFromRaceId(int raceId)
  {
    if (raceId==23) return Race.MAN;
    if (raceId==65) return Race.ELF;
    if (raceId==73) return Race.DWARF;
    if (raceId==81) return Race.HOBBIT;
    if (raceId==114) return Race.BEORNING;
    if (raceId==117) return Race.HIGH_ELF;
    if (raceId==120) return Race.STOUT_AXE_DWARF;
    return null;
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
}
