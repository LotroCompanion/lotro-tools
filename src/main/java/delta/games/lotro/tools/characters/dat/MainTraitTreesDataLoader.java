package delta.games.lotro.tools.characters.dat;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;

/**
 * Get trait trees definitions from DAT files.
 * @author DAM
 */
public class MainTraitTreesDataLoader
{
  //private static final Logger LOGGER=Logger.getLogger(MainTraitTreesDataLoader.class);

  private DataFacade _facade;
  private EnumMapper _traitTreeBranch;
  private EnumMapper _traitCell;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainTraitTreesDataLoader(DataFacade facade)
  {
    _facade=facade;
    _traitTreeBranch=_facade.getEnumsManager().getEnumMapper(0x230003A1);
    _traitCell=_facade.getEnumsManager().getEnumMapper(0x2300036E);
  }

  private void handleTraitTree(CharacterClass characterClass, int traitTreeId)
  {
    PropertiesSet properties=_facade.loadProperties(traitTreeId+0x9000000);
    //System.out.println(properties.dump());
    // Branches
    /*
    Object[] branchDescriptions=(Object[])properties.getProperty("Trait_TraitTree_BranchDescriptionArray");
    for(Object branchDescriptionObj : branchDescriptions)
    {
      PropertiesSet branchDescriptionProps=(PropertiesSet)branchDescriptionObj;
      int branchId=((Integer)branchDescriptionProps.getProperty("Trait_TraitTree_Branch")).intValue();
      String branchName=_traitTreeBranch.getString(branchId);
      System.out.println("branch name: "+branchName);
      //String branchDescription=DatUtils.getStringProperty(branchDescriptionProps,"Trait_TraitTree_Description");
      //System.out.println("branch description: "+branchDescription);
    }
    */
    // Specializations
    Object[] specializations=(Object[])properties.getProperty("Trait_TraitTree_SpecializationsArray");
    if (specializations!=null)
    {
      for(Object specializationObj : specializations)
      {
        PropertiesSet specializationProps=(PropertiesSet)specializationObj;
        int branchId=((Integer)specializationProps.getProperty("Trait_TraitTree_SpecializationBranch")).intValue();
        String branchName=_traitTreeBranch.getString(branchId);
        System.out.println("branch name: "+branchName);
        int progressionId=((Integer)specializationProps.getProperty("Trait_TraitTree_SpecializationProgression")).intValue();
        PropertiesSet progressionProperties=_facade.loadProperties(progressionId+0x9000000);
        handleSpecializationProgression(progressionProperties);
      }
    }
    // Traits
    Object[] traits=(Object[])properties.getProperty("Trait_TraitTree_TraitArray");
    for(Object traitObj : traits)
    {
      PropertiesSet traitProps=(PropertiesSet)traitObj;
      //int branchId=((Integer)traitProps.getProperty("Trait_TraitTree_Branch")).intValue();
      int traitId=((Integer)traitProps.getProperty("Trait_TraitTree_Trait")).intValue();
      int traitLocation=((Integer)traitProps.getProperty("Trait_TraitTree_TraitLocation")).intValue();
      String cell=_traitCell.getString(traitLocation);
      System.out.println("Cell: "+cell);
      TraitLoader.loadTrait(_facade,traitId);
    }
  }

  private void handleSpecializationProgression(PropertiesSet progressionProperties)
  {
    //System.out.println(progressionProperties.dump());
    Object[] progressionSteps=(Object[])progressionProperties.getProperty("SparseDIDProgression_Array");
    for(Object progressionStepObj : progressionSteps)
    {
      PropertiesSet progressionStepProps=(PropertiesSet)progressionStepObj;
      Number nbPointsValue=(Number)progressionStepProps.getProperty("SparseDIDProgressionEntry_Key");
      int nbPoints=nbPointsValue.intValue();
      System.out.println("Nb points: "+nbPoints);
      int traitId=((Integer)progressionStepProps.getProperty("SparseDIDProgressionEntry_DID")).intValue();
      TraitLoader.loadTrait(_facade,traitId);
    }
  }

  private void doIt()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900025B);
    //System.out.println(properties.dump());
    Object[] traitNatures=(Object[])properties.getProperty("Trait_Control_PointBasedTraitNature_Array");
    for(Object traitNatureObj : traitNatures)
    {
      PropertiesSet traitNatureProps=(PropertiesSet)traitNatureObj;
      int traitNatureKey=((Integer)traitNatureProps.getProperty("Trait_Control_TraitNature_Key")).intValue();
      CharacterClass characterClass=getCharacterClassFromTraitNatureKey(traitNatureKey);
      if (characterClass!=null)
      {
        Object[] traitTreeArray=(Object[])traitNatureProps.getProperty("Trait_Control_PointBasedTrait_TraitTreeArray");
        for(Object traitTreeIdObj : traitTreeArray)
        {
          int traitTreeId=((Integer)traitTreeIdObj).intValue();
          handleTraitTree(characterClass,traitTreeId);
        }
      }
    }
  }

  private CharacterClass getCharacterClassFromTraitNatureKey(int traitNatureKey)
  {
    if (traitNatureKey==23) return CharacterClass.CHAMPION;
    return null;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainTraitTreesDataLoader(facade).doIt();
    facade.dispose();
  }
}
