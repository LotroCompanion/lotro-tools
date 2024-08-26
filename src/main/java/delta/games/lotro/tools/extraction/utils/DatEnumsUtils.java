package delta.games.lotro.tools.extraction.utils;

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
    if ((slotCode&1L<<1)!=0) return EquipmentLocations.HEAD;
    if ((slotCode&1L<<2)!=0) return EquipmentLocations.CHEST;
    if ((slotCode&1L<<3)!=0) return EquipmentLocations.LEGS;
    if ((slotCode&1L<<4)!=0) return EquipmentLocations.HAND;
    if ((slotCode&1L<<5)!=0) return EquipmentLocations.FEET;
    if ((slotCode&1L<<6)!=0) return EquipmentLocations.SHOULDER;
    if ((slotCode&1L<<7)!=0) return EquipmentLocations.BACK;
    if (((slotCode&1L<<8)!=0) || ((slotCode&1L<<9)!=0)) return EquipmentLocations.WRIST;
    if ((slotCode&1L<<10)!=0) return EquipmentLocations.NECK;
    if (((slotCode&1L<<11)!=0) || ((slotCode&1L<<12)!=0)) return EquipmentLocations.FINGER;
    if (((slotCode&1L<<13)!=0) || ((slotCode&1L<<14)!=0)) return EquipmentLocations.EAR;
    if ((slotCode&1L<<15)!=0) return EquipmentLocations.POCKET;
    if ((slotCode&1L<<16)!=0) return EquipmentLocations.MAIN_HAND;
    if ((slotCode&1L<<17)!=0) return EquipmentLocations.OFF_HAND;
    if ((slotCode&1L<<18)!=0) return EquipmentLocations.RANGED_ITEM;
    if ((slotCode&1L<<19)!=0) return EquipmentLocations.TOOL;
    if ((slotCode&1L<<20)!=0) return EquipmentLocations.CLASS_SLOT;
    if ((slotCode&1L<<21)!=0) return EquipmentLocations.BRIDLE;
    if ((slotCode&1L<<22)!=0) return EquipmentLocations.MAIN_HAND_AURA;
    if ((slotCode&1L<<23)!=0) return EquipmentLocations.OFF_HAND_AURA;
    if ((slotCode&1L<<24)!=0) return EquipmentLocations.RANGED_AURA;

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
