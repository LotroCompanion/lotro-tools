package delta.games.lotro.tools.dat.characters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.TraitTree;
import delta.games.lotro.character.classes.TraitTreeBranch;
import delta.games.lotro.character.classes.TraitTreeProgression;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;

/**
 * Get trait trees definitions from DAT files.
 * @author DAM
 */
public class TraitTreesDataLoader
{
  //private static final Logger LOGGER=Logger.getLogger(MainTraitTreesDataLoader.class);

  private DataFacade _facade;
  private EnumMapper _traitTreeBranch;
  private EnumMapper _traitCell;
  private TraitsManager _traitsManager;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param traitsManager Traits manager.
   */
  public TraitTreesDataLoader(DataFacade facade, TraitsManager traitsManager)
  {
    _facade=facade;
    _traitTreeBranch=_facade.getEnumsManager().getEnumMapper(0x230003A1);
    _traitCell=_facade.getEnumsManager().getEnumMapper(0x2300036E);
    _traitsManager=traitsManager;
  }

  private TraitTree handleTraitTree(CharacterClass characterClass, int traitTreeId)
  {
    PropertiesSet properties=_facade.loadProperties(traitTreeId+DATConstants.DBPROPERTIES_OFFSET);
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
    TraitTree tree=new TraitTree();
    //System.out.println("Got trait tree for: "+characterClass);
    Map<Integer,TraitTreeBranch> branchesById=new HashMap<Integer,TraitTreeBranch>();
    // Specializations
    Object[] specializations=(Object[])properties.getProperty("Trait_TraitTree_SpecializationsArray");
    if (specializations!=null)
    {
      for(Object specializationObj : specializations)
      {
        PropertiesSet specializationProps=(PropertiesSet)specializationObj;
        int branchId=((Integer)specializationProps.getProperty("Trait_TraitTree_SpecializationBranch")).intValue();
        String branchName=_traitTreeBranch.getString(branchId);
        //System.out.println("branch name: "+branchName);
        TraitTreeBranch branch=new TraitTreeBranch(branchName);
        branchesById.put(Integer.valueOf(branchId),branch);
        tree.addBranch(branch);
        TraitTreeProgression progression=branch.getProgression();
        int progressionId=((Integer)specializationProps.getProperty("Trait_TraitTree_SpecializationProgression")).intValue();
        PropertiesSet progressionProperties=_facade.loadProperties(progressionId+DATConstants.DBPROPERTIES_OFFSET);
        handleSpecializationProgression(progression,progressionProperties);
      }
    }
    // Traits
    Object[] traits=(Object[])properties.getProperty("Trait_TraitTree_TraitArray");
    for(Object traitObj : traits)
    {
      PropertiesSet traitProps=(PropertiesSet)traitObj;
      int branchId=((Integer)traitProps.getProperty("Trait_TraitTree_Branch")).intValue();
      TraitTreeBranch branch=branchesById.get(Integer.valueOf(branchId));
      int traitId=((Integer)traitProps.getProperty("Trait_TraitTree_Trait")).intValue();
      int traitLocation=((Integer)traitProps.getProperty("Trait_TraitTree_TraitLocation")).intValue();
      String cell=_traitCell.getString(traitLocation);
      //System.out.println("Cell: "+cell);
      TraitDescription description=TraitLoader.loadTrait(_facade,traitId);
      _traitsManager.registerTrait(description);
      branch.setCell(cell,description);
    }
    return tree;
  }

  private void handleSpecializationProgression(TraitTreeProgression progression, PropertiesSet progressionProperties)
  {
    //System.out.println(progressionProperties.dump());
    Object[] progressionSteps=(Object[])progressionProperties.getProperty("SparseDIDProgression_Array");
    for(Object progressionStepObj : progressionSteps)
    {
      PropertiesSet progressionStepProps=(PropertiesSet)progressionStepObj;
      Number nbPointsValue=(Number)progressionStepProps.getProperty("SparseDIDProgressionEntry_Key");
      int nbPoints=nbPointsValue.intValue();
      //System.out.println("Nb points: "+nbPoints);
      int traitId=((Integer)progressionStepProps.getProperty("SparseDIDProgressionEntry_DID")).intValue();
      TraitDescription description=TraitLoader.loadTrait(_facade,traitId);
      _traitsManager.registerTrait(description);
      progression.addStep(nbPoints,description);
    }
  }

  /**
   * Do it.
   * @param classes Classes to update.
   */
  public void doIt(List<ClassDescription> classes)
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
          TraitTree tree=handleTraitTree(characterClass,traitTreeId);
          for(ClassDescription description : classes)
          {
            if (description.getCharacterClass()==characterClass)
            {
              description.setTraitTree(tree);
            }
          }
        }
      }
    }
  }

  private CharacterClass getCharacterClassFromTraitNatureKey(int traitNatureKey)
  {
    if (traitNatureKey==19) return CharacterClass.BURGLAR;
    if (traitNatureKey==20) return CharacterClass.LORE_MASTER;
    if (traitNatureKey==21) return CharacterClass.HUNTER;
    if (traitNatureKey==22) return CharacterClass.MINSTREL;
    if (traitNatureKey==23) return CharacterClass.CHAMPION;
    if (traitNatureKey==24) return CharacterClass.RUNE_KEEPER;
    if (traitNatureKey==25) return CharacterClass.WARDEN;
    if (traitNatureKey==26) return CharacterClass.CAPTAIN;
    if (traitNatureKey==27) return CharacterClass.GUARDIAN;
    if (traitNatureKey==31) return CharacterClass.BEORNING;
    return null;
  }
}
