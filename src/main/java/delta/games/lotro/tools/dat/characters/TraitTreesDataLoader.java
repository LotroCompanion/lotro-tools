package delta.games.lotro.tools.dat.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.traitTree.TraitTree;
import delta.games.lotro.character.classes.traitTree.TraitTreeBranch;
import delta.games.lotro.character.classes.traitTree.TraitTreeCell;
import delta.games.lotro.character.classes.traitTree.TraitTreeCellDependency;
import delta.games.lotro.character.classes.traitTree.TraitTreeProgression;
import delta.games.lotro.character.classes.traitTree.io.xml.TraitTreeXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Get trait trees definitions from DAT files.
 * @author DAM
 */
public class TraitTreesDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(TraitTreesDataLoader.class);

  private DataFacade _facade;
  private EnumMapper _traitTreeBranch;
  private EnumMapper _traitCell;
  private EnumMapper _traitTreeType;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public TraitTreesDataLoader(DataFacade facade)
  {
    _facade=facade;
    _traitTreeBranch=_facade.getEnumsManager().getEnumMapper(0x230003A1);
    _traitCell=_facade.getEnumsManager().getEnumMapper(0x2300036E);
    _traitTreeType=_facade.getEnumsManager().getEnumMapper(0x23000369);
  }

  private TraitTree handleTraitTree(int traitTreeId)
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
      System.out.println("Branch name: "+branchName);
      String branchDescription=DatUtils.getStringProperty(branchDescriptionProps,"Trait_TraitTree_Description");
      System.out.println("Branch description: "+branchDescription);
    }
    */
    TraitTree tree=new TraitTree(traitTreeId);
    int traitTreeType=((Integer)properties.getProperty("Trait_TraitTree_TreeType")).intValue();
    tree.setCode(traitTreeType);
    String traitTreeKey=_traitTreeType.getString(traitTreeType);
    tree.setKey(traitTreeKey);
    LOGGER.info("Loading trait tree for class: "+traitTreeKey);
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
        //System.out.println("Branch name: "+branchName);
        TraitTreeBranch branch=new TraitTreeBranch(branchId,branchName);
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
      Integer branchIdInt=(Integer)traitProps.getProperty("Trait_TraitTree_Branch");
      if (branchIdInt==null)
      {
        // Special case of non-branch specific traits in the mounted combat trees
        continue;
      }
      int branchId=branchIdInt.intValue();
      TraitTreeBranch branch=branchesById.get(Integer.valueOf(branchId));
      if (branch==null)
      {
        String branchName=_traitTreeBranch.getString(branchId);
        //System.out.println("Branch name: "+branchName);
        branch=new TraitTreeBranch(branchId,branchName);
        branchesById.put(Integer.valueOf(branchId),branch);
        tree.addBranch(branch);
      }
      int traitId=((Integer)traitProps.getProperty("Trait_TraitTree_Trait")).intValue();
      int traitLocation=((Integer)traitProps.getProperty("Trait_TraitTree_TraitLocation")).intValue();
      String cellId=_traitCell.getString(traitLocation);
      //System.out.println("Cell: "+cell);
      TraitDescription trait=TraitLoader.getTrait(_facade,traitId);
      TraitTreeCell cell=new TraitTreeCell(cellId,trait);
      // Dependencies
      Object[] depArray=(Object[])traitProps.getProperty("Trait_TraitTree_TraitDependencyArray");
      if (depArray!=null)
      {
        for(Object depObj : depArray)
        {
          PropertiesSet depProps=(PropertiesSet)depObj;
          int depTraitLocation=((Integer)depProps.getProperty("Trait_TraitTree_TraitDependency")).intValue();
          String depCellId=_traitCell.getString(depTraitLocation);
          int depRank=((Integer)depProps.getProperty("Trait_TraitTree_TraitDependencyRank")).intValue();
          //System.out.println("Cell "+cell+" depends on cell "+depCellId+" at rank: "+depRank);
          TraitTreeCellDependency cellDependency=new TraitTreeCellDependency(depCellId,depRank);
          cell.addDependency(cellDependency);
        }
      }
      branch.setCell(cellId,cell);
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
      TraitDescription description=TraitLoader.getTrait(_facade,traitId);
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
    List<TraitTree> traitTrees=new ArrayList<TraitTree>();
    //System.out.println(properties.dump());
    Map<Integer,TraitTreeBranch> branchByCode=new HashMap<Integer,TraitTreeBranch>();
    Object[] traitNatures=(Object[])properties.getProperty("Trait_Control_PointBasedTraitNature_Array");
    for(Object traitNatureObj : traitNatures)
    {
      PropertiesSet traitNatureProps=(PropertiesSet)traitNatureObj;
      int traitNatureKey=((Integer)traitNatureProps.getProperty("Trait_Control_TraitNature_Key")).intValue();
      Object[] traitTreeArray=(Object[])traitNatureProps.getProperty("Trait_Control_PointBasedTrait_TraitTreeArray");
      for(Object traitTreeIdObj : traitTreeArray)
      {
        int traitTreeId=((Integer)traitTreeIdObj).intValue();
        TraitTree tree=handleTraitTree(traitTreeId);
        traitTrees.add(tree);
        CharacterClass characterClass=getCharacterClassFromTraitNatureKey(traitNatureKey);
        if (characterClass!=null)
        {
          for(ClassDescription description : classes)
          {
            if (description.getCharacterClass()==characterClass)
            {
              description.setTraitTree(tree);
              for(TraitTreeBranch branch : tree.getBranches())
              {
                branchByCode.put(Integer.valueOf(branch.getCode()),branch);
              }
            }
          }
        }
      }
      if (traitNatureKey==28)
      {
        handleMainTraits(traitNatureProps,branchByCode);
      }
    }
    TraitTreeXMLWriter.write(GeneratedFiles.TRAIT_TREES,traitTrees);
  }

  private void handleMainTraits(PropertiesSet traitNatureProps, Map<Integer,TraitTreeBranch> branchByCode)
  {
    //System.out.println(traitNatureProps.dump());
    Object[] traitTreeArray=(Object[])traitNatureProps.getProperty("Trait_Control_PointBasedTrait_TraitTreeArray");
    for(Object traitTreeIdObj : traitTreeArray)
    {
      int traitTreeId=((Integer)traitTreeIdObj).intValue();
      PropertiesSet properties=_facade.loadProperties(traitTreeId+DATConstants.DBPROPERTIES_OFFSET);
      //System.out.println(properties.dump());
      // Traits
      Object[] traits=(Object[])properties.getProperty("Trait_TraitTree_TraitArray");
      for(Object traitObj : traits)
      {
        PropertiesSet traitProps=(PropertiesSet)traitObj;
        int branchId=((Integer)traitProps.getProperty("Trait_TraitTree_Branch")).intValue();
        TraitTreeBranch branch=branchByCode.get(Integer.valueOf(branchId));
        int traitId=((Integer)traitProps.getProperty("Trait_TraitTree_Trait")).intValue();
        TraitDescription trait=TraitLoader.getTrait(_facade,traitId);
        branch.setMainTrait(trait);
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
    if (traitNatureKey==32) return CharacterClass.BRAWLER;
    return null;
  }
}
