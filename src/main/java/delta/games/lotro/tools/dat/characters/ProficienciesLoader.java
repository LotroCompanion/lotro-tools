package delta.games.lotro.tools.dat.characters;

import java.util.List;
import java.util.Set;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassTrait;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.CharacterClass;
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
   * Load trait data.
   */
  public void doIt()
  {
    for(CharacterClass characterClass : CharacterClass.ALL_CLASSES)
    {
      ClassDescription classDescription=ClassesManager.getInstance().getClassDescription(characterClass);
      handleClass(classDescription);
    }
  }

  private void handleClass(ClassDescription classDescription)
  {
    System.out.println("Class: "+classDescription.getCharacterClass().getLabel());
    List<ClassTrait> classTraits=classDescription.getTraits();
    for(ClassTrait classTrait : classTraits)
    {
      handleClassTrait(classTrait);
    }
  }

  private void handleClassTrait(ClassTrait classTrait)
  {
    TraitDescription trait=classTrait.getTrait();
    int level=classTrait.getRequiredLevel();
    PropertiesSet props=_facade.loadProperties(trait.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
    Long category=(Long)props.getProperty("Trait_Granted_Category");
    if (category!=null)
    {
      System.out.println("\tTrait: "+trait.getName()+" at level "+level+", Category: "+category);
      Set<WeaponType> weaponTypes=_weaponUtils.getAllowedEquipment(category.longValue());
      if (!weaponTypes.isEmpty())
      {
        System.out.println("\t\t=> "+weaponTypes);
      }
      Set<ArmourType> armourTypes=ArmourTypesUtils.getArmourTypes(category.longValue());
      if (!armourTypes.isEmpty())
      {
        System.out.println("\t\t=> "+armourTypes);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new ProficienciesLoader(facade).doIt();
    facade.dispose();
  }
}
