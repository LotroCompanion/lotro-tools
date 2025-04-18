package delta.games.lotro.tools.extraction.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.EquipmentLocations;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.WeaponType;

/**
 * Misc enum utils.
 * @author DAM
 */
public class DatEnumsUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DatEnumsUtils.class);

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
    if ((slotCode&1L<<16)!=0) return EquipmentLocations.MAIN_HAND;
    if ((slotCode&1L<<18)!=0) return EquipmentLocations.RANGED_ITEM;
    if ((slotCode&1L<<20)!=0) return EquipmentLocations.CLASS_SLOT;
    if ((slotCode&1L<<21)!=0) return EquipmentLocations.BRIDLE;
    LOGGER.warn("Unmanaged slot code: {}",Integer.valueOf(slotCode));
    return null;
  }

  /**
   * Get a location from a DAT bit set of allowed slots.
   * @param allowedSlots Input code.
   * @return A location or <code>null</code> if not supported.
   */
  public static EquipmentLocation getLocationFromAllowedSlots(int allowedSlots)
  {
    if (allowedSlots==-1073741824) return null;
    if (allowedSlots==-536870910) return EquipmentLocations.HEAD;
    if (allowedSlots==-536870908) return EquipmentLocations.CHEST;
    if (allowedSlots==-536870904) return EquipmentLocations.LEGS;
    if (allowedSlots==-536870896) return EquipmentLocations.HAND;
    if (allowedSlots==-536870880) return EquipmentLocations.FEET;
    if (allowedSlots==-536870848) return EquipmentLocations.SHOULDER;
    if (allowedSlots==-536870784) return EquipmentLocations.BACK;
    if (allowedSlots==-536870400) return EquipmentLocations.LEFT_WRIST;
    if (allowedSlots==-536870656) return EquipmentLocations.RIGHT_WRIST;
    if (allowedSlots==-536870144) return EquipmentLocations.WRIST;
    if (allowedSlots==-536869888) return EquipmentLocations.NECK;
    if (allowedSlots==-536866816) return EquipmentLocations.LEFT_FINGER;
    if (allowedSlots==-536868864) return EquipmentLocations.RIGHT_FINGER;
    if (allowedSlots==-536864768) return EquipmentLocations.FINGER;
    if (allowedSlots==-536854528) return EquipmentLocations.LEFT_EAR;
    if (allowedSlots==-536862720) return EquipmentLocations.RIGHT_EAR;
    if (allowedSlots==-536846336) return EquipmentLocations.EAR;
    if (allowedSlots==-536838144) return EquipmentLocations.POCKET;
    if (allowedSlots==-536805376) return EquipmentLocations.MAIN_HAND;
    if (allowedSlots==-536739840) return EquipmentLocations.OFF_HAND;
    if (allowedSlots==-536674304) return EquipmentLocations.EITHER_HAND;
    if (allowedSlots==-536608768) return EquipmentLocations.RANGED_ITEM;
    if (allowedSlots==-536346624) return EquipmentLocations.TOOL;
    if (allowedSlots==-535822336) return EquipmentLocations.CLASS_SLOT;
    if (allowedSlots==-534773760) return EquipmentLocations.BRIDLE;
    if (allowedSlots==-507510784) return EquipmentLocations.AURA;
    LOGGER.warn("Unmanaged allowed slots: {}",Integer.valueOf(allowedSlots));
    return null;
  }

  /**
   * Get a precluded slot(s) from a DAT bit set of precluded slots.
   * @param precludedSlots Input code.
   * @return A location or <code>null</code> if not supported.
   */
  public static EquipmentLocation getPrecludedSlots(int precludedSlots)
  {
    if (precludedSlots==0) return null;
    if (precludedSlots==536870912) return null;
    if (precludedSlots==537001984) return EquipmentLocations.OFF_HAND;
    LOGGER.warn("Unmanaged precluded slots: {}",Integer.valueOf(precludedSlots));
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

  /**
   * Get the equipment category code from an equipment category bits field.
   * @param equipmentCategory Input value.
   * @return An equipment category code.
   */
  public static int getEquipmentCategoryCode(Long equipmentCategory)
  {
    long code=(equipmentCategory!=null)?equipmentCategory.longValue():0;
    if (code!=0)
    {
      long mask=1;
      for(int i=1;i<=64;i++)
      {
        if ((code&mask)!=0)
        {
          return i;
        }
        mask<<=1;
      }
    }
    return 0;
  }
}
