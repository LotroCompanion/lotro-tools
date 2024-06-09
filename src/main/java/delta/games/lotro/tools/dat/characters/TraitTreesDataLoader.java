package delta.games.lotro.tools.dat.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
import delta.games.lotro.character.classes.traitTree.TraitTree;
import delta.games.lotro.character.classes.traitTree.TraitTreeBranch;
import delta.games.lotro.character.classes.traitTree.TraitTreeCell;
import delta.games.lotro.character.classes.traitTree.TraitTreeCellDependency;
import delta.games.lotro.character.classes.traitTree.TraitTreeProgression;
import delta.games.lotro.character.classes.traitTree.io.xml.TraitTreeXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.TraitTreeBranchType;
import delta.games.lotro.common.enums.TraitTreeType;
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
  private EnumMapper _traitCell;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public TraitTreesDataLoader(DataFacade facade)
  {
    _facade=facade;
    _traitCell=_facade.getEnumsManager().getEnumMapper(0x2300036E);
  }

  private TraitTree handleTraitTree(int traitTreeId)
  {
    PropertiesSet properties=_facade.loadProperties(traitTreeId+DATConstants.DBPROPERTIES_OFFSET);
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
    int traitTreeTypeCode=((Integer)properties.getProperty("Trait_TraitTree_TreeType")).intValue();
    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    LotroEnum<TraitTreeType> traitTreeTypeEnum=registry.get(TraitTreeType.class);
    TraitTreeType traiTreeType=traitTreeTypeEnum.getEntry(traitTreeTypeCode);
    TraitTree tree=new TraitTree(traitTreeId,traiTreeType);
    LOGGER.info("Loading trait tree with type: "+traiTreeType);
    Map<Integer,TraitTreeBranch> branchesById=new HashMap<Integer,TraitTreeBranch>();
    // Specializations
    LotroEnum<TraitTreeBranchType> traitTreeBranchTypeEnum=registry.get(TraitTreeBranchType.class);
    Object[] specializations=(Object[])properties.getProperty("Trait_TraitTree_SpecializationsArray");
    if (specializations!=null)
    {
      for(Object specializationObj : specializations)
      {
        PropertiesSet specializationProps=(PropertiesSet)specializationObj;
        int branchId=((Integer)specializationProps.getProperty("Trait_TraitTree_SpecializationBranch")).intValue();
        TraitTreeBranchType branchType=traitTreeBranchTypeEnum.getEntry(branchId);
        TraitTreeBranch branch=new TraitTreeBranch(branchType);
        branchesById.put(Integer.valueOf(branchId),branch);
        tree.addBranch(branch);
        // Progression
        TraitTreeProgression progression=branch.getProgression();
        int progressionId=((Integer)specializationProps.getProperty("Trait_TraitTree_SpecializationProgression")).intValue();
        PropertiesSet progressionProperties=_facade.loadProperties(progressionId+DATConstants.DBPROPERTIES_OFFSET);
        handleSpecializationProgression(progression,progressionProperties);
        // Enabled?
        boolean enabled=isEnabled(branchId);
        branch.setEnabled(enabled);
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
        TraitTreeBranchType branchType=traitTreeBranchTypeEnum.getEntry(branchId);
        branch=new TraitTreeBranch(branchType);
        branchesById.put(Integer.valueOf(branchId),branch);
        tree.addBranch(branch);
      }
      int traitId=((Integer)traitProps.getProperty("Trait_TraitTree_Trait")).intValue();
      int traitLocation=((Integer)traitProps.getProperty("Trait_TraitTree_TraitLocation")).intValue();
      String cellId=_traitCell.getString(traitLocation);
      TraitDescription trait=TraitUtils.getTrait(traitId);
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
    Object[] progressionSteps=(Object[])progressionProperties.getProperty("SparseDIDProgression_Array");
    for(Object progressionStepObj : progressionSteps)
    {
      PropertiesSet progressionStepProps=(PropertiesSet)progressionStepObj;
      Number nbPointsValue=(Number)progressionStepProps.getProperty("SparseDIDProgressionEntry_Key");
      int nbPoints=nbPointsValue.intValue();
      int traitId=((Integer)progressionStepProps.getProperty("SparseDIDProgressionEntry_DID")).intValue();
      TraitDescription description=TraitUtils.getTrait(traitId);
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
    Map<Integer,TraitTreeBranch> branchByCode=new HashMap<Integer,TraitTreeBranch>();
    Object[] traitNatures=(Object[])properties.getProperty("Trait_Control_PointBasedTraitNature_Array");
    if (traitNatures==null)
    {
      return;
    }
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
        String classKey=getCharacterClassKeyFromTraitNatureKey(traitNatureKey);
        if (classKey!=null)
        {
          for(ClassDescription description : classes)
          {
            if (classKey.equals(description.getKey()))
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
    Object[] traitTreeArray=(Object[])traitNatureProps.getProperty("Trait_Control_PointBasedTrait_TraitTreeArray");
    for(Object traitTreeIdObj : traitTreeArray)
    {
      int traitTreeId=((Integer)traitTreeIdObj).intValue();
      PropertiesSet properties=_facade.loadProperties(traitTreeId+DATConstants.DBPROPERTIES_OFFSET);
      // Traits
      Object[] traits=(Object[])properties.getProperty("Trait_TraitTree_TraitArray");
      for(Object traitObj : traits)
      {
        PropertiesSet traitProps=(PropertiesSet)traitObj;
        int branchId=((Integer)traitProps.getProperty("Trait_TraitTree_Branch")).intValue();
        TraitTreeBranch branch=branchByCode.get(Integer.valueOf(branchId));
        int traitId=((Integer)traitProps.getProperty("Trait_TraitTree_Trait")).intValue();
        TraitDescription trait=TraitUtils.getTrait(traitId);
        branch.setMainTrait(trait);
      }
    }
  }

  private String getCharacterClassKeyFromTraitNatureKey(int traitNatureKey)
  {
    if (traitNatureKey==19) return WellKnownCharacterClassKeys.BURGLAR;
    if (traitNatureKey==20) return WellKnownCharacterClassKeys.LORE_MASTER;
    if (traitNatureKey==21) return WellKnownCharacterClassKeys.HUNTER;
    if (traitNatureKey==22) return WellKnownCharacterClassKeys.MINSTREL;
    if (traitNatureKey==23) return WellKnownCharacterClassKeys.CHAMPION;
    if (traitNatureKey==24) return WellKnownCharacterClassKeys.RUNE_KEEPER;
    if (traitNatureKey==25) return WellKnownCharacterClassKeys.WARDEN;
    if (traitNatureKey==26) return WellKnownCharacterClassKeys.CAPTAIN;
    if (traitNatureKey==27) return WellKnownCharacterClassKeys.GUARDIAN;
    if (traitNatureKey==31) return WellKnownCharacterClassKeys.BEORNING;
    if (traitNatureKey==32) return WellKnownCharacterClassKeys.BRAWLER;
    if (traitNatureKey==33) return WellKnownCharacterClassKeys.CORSAIR;
    return null;
  }

  private boolean isEnabled(int branchCode)
  {
    if (branchCode==27) return false; // Fighter of Shadows (Guardian's yellow branch)
    if (branchCode==28) return false; // The Protector of Song (Minstrel's yellow branch)
    if (branchCode==59) return false; // The Fundaments (Brawler's yellow branch)
    if (branchCode==35) return false; // Assailment (Warden's yellow branch)
    return true;
  }
}
