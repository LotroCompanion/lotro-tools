package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
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
  /**
   * Get a damage type from a DAT enum code.
   * @param damageTypeEnum Input code.
   * @return A damage type or <code>null</code> if not supported.
   */
  public static DamageType getDamageType(int damageTypeEnum)
  {
    return LotroEnumsRegistry.getInstance().get(DamageType.class).getEntry(damageTypeEnum);
  }

  /**
   * Get an item quality from a DAT enum code.
   * @param qualityCode Input code.
   * @return An item quality or <code>null</code> if not supported.
   */
  public static ItemQuality getQuality(int qualityCode)
  {
    return LotroEnumsRegistry.getInstance().get(ItemQuality.class).getEntry(qualityCode);
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
    return LotroEnumsRegistry.getInstance().get(WeaponType.class).getEntry(code);
  }
}
