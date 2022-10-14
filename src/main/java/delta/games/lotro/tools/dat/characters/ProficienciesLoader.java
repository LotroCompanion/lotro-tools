package delta.games.lotro.tools.dat.characters;

import java.util.List;
import java.util.Set;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassTrait;
import delta.games.lotro.character.classes.proficiencies.ClassProficiencies;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.tools.dat.utils.ArmourTypesUtils;
import delta.games.lotro.tools.dat.utils.WeaponTypesUtils;

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
    //System.out.println("Class: "+classDescription.getCharacterClass().getLabel());
    List<ClassTrait> classTraits=classDescription.getTraits();
    for(ClassTrait classTrait : classTraits)
    {
      handleClassTrait(classDescription.getProficiencies(),classTrait);
    }
  }

  private void handleClassTrait(ClassProficiencies proficiencies, ClassTrait classTrait)
  {
    TraitDescription trait=classTrait.getTrait();
    int level=classTrait.getRequiredLevel();
    PropertiesSet props=_facade.loadProperties(trait.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
    Long category=(Long)props.getProperty("Trait_Granted_Category");
    if (category!=null)
    {
      //System.out.println("\tTrait: "+trait.getName()+" at level "+level+", Category: "+category);
      Set<WeaponType> weaponTypes=_weaponUtils.getAllowedEquipment(category.longValue());
      if (!weaponTypes.isEmpty())
      {
        //System.out.println("\t\t=> "+weaponTypes);
        for(WeaponType weaponType : weaponTypes)
        {
          proficiencies.getWeaponProficiencies().addEntry(weaponType,level);
        }
      }
      Set<ArmourType> armourTypes=ArmourTypesUtils.getArmourTypes(category.longValue());
      if (!armourTypes.isEmpty())
      {
        //System.out.println("\t\t=> "+armourTypes);
        for(ArmourType armourType : armourTypes)
        {
          proficiencies.getArmourProficiencies().addEntry(armourType,level);
        }
      }
    }
  }
}
