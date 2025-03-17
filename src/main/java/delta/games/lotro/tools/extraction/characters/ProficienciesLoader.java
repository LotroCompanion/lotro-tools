package delta.games.lotro.tools.extraction.characters;

import java.util.List;
import java.util.Set;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.proficiencies.ClassProficiencies;
import delta.games.lotro.character.classes.proficiencies.TypedClassProficiencies;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.utils.TraitAndLevel;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.tools.extraction.utils.ArmourTypesUtils;
import delta.games.lotro.tools.extraction.utils.WeaponTypesUtils;

/**
 * Character proficiencies loader.
 * @author DAM
 */
public class ProficienciesLoader
{
  private DataFacade _facade;
  private WeaponTypesUtils _weaponUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ProficienciesLoader(DataFacade facade)
  {
    _facade=facade;
    _weaponUtils=new WeaponTypesUtils(facade);
  }

  /**
   * Handle a class.
   * @param classDescription Class to use.
   */
  public void handleClass(ClassDescription classDescription)
  {
    List<TraitAndLevel> classTraits=classDescription.getTraits();
    for(TraitAndLevel classTrait : classTraits)
    {
      handleClassTrait(classDescription.getProficiencies(),classTrait);
    }
  }

  private void handleClassTrait(ClassProficiencies proficiencies, TraitAndLevel classTrait)
  {
    TraitDescription trait=classTrait.getTrait();
    int level=classTrait.getRequiredLevel();
    PropertiesSet props=_facade.loadProperties(trait.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
    // Category
    Long category=(Long)props.getProperty("Trait_Granted_Category");
    if (category!=null)
    {
      addProficiencies(category,level,proficiencies);
    }
    StatsProvider statsProvider=trait.getStatsProvider();
    // Dual-wield
    StatDescription dualWield=StatsRegistry.getInstance().getByKey("Inventory_AllowSecondaryWeapon");
    StatProvider dualWieldProvider=statsProvider.getStat(dualWield);
    if (dualWieldProvider!=null)
    {
      proficiencies.setMinLevelForDualWield(Integer.valueOf(level));
    }
    // Stats modifiers
    Object[] modArray=(Object[])props.getProperty("Mod_Array");
    if (modArray!=null)
    {
      for(Object modArrayEntry : modArray)
      {
        PropertiesSet modEntryProps=(PropertiesSet)modArrayEntry;
        Integer propertyID=(Integer)modEntryProps.getProperty("Mod_Modified");
        if ((propertyID!=null) && (propertyID.intValue()==0x1000029C)) // Inventory_AllowedEquipmentCategories
        {
          Long categories=(Long)modEntryProps.getProperty("Inventory_AllowedEquipmentCategories");
          Integer op=(Integer)modEntryProps.getProperty("Mod_Op");
          if ((categories!=null) && (op!=null))
          {
            if (op.intValue()==3) // OR
            {
              addProficiencies(categories,level,proficiencies);
            }
          }
        }
      }
    }
  }

  private void addProficiencies(Long category, int level, ClassProficiencies proficiencies)
  {
    Set<WeaponType> weaponTypes=_weaponUtils.getAllowedEquipment(category.longValue());
    if (!weaponTypes.isEmpty())
    {
      TypedClassProficiencies<WeaponType> weaponProficiencies=proficiencies.getWeaponProficiencies();
      for(WeaponType weaponType : weaponTypes)
      {
        if (!weaponProficiencies.getEntryValues().contains(weaponType))
        {
          weaponProficiencies.addEntry(weaponType,level);
        }
      }
    }
    Set<ArmourType> armourTypes=ArmourTypesUtils.getArmourTypes(category.longValue());
    if (!armourTypes.isEmpty())
    {
      TypedClassProficiencies<ArmourType> armourProficiencies=proficiencies.getArmourProficiencies();
      for(ArmourType armourType : armourTypes)
      {
        if (!armourProficiencies.getEntryValues().contains(armourType))
        {
          armourProficiencies.addEntry(armourType,level);
        }
      }
    }
  }
}
