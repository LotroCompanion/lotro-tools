package delta.games.lotro.tools.extraction.instances;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.WJEncounterCategory;
import delta.games.lotro.common.enums.WJEncounterType;
import delta.games.lotro.common.enums.WJInstanceGroup;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.instances.InstanceCategory;
import delta.games.lotro.lore.instances.InstancesTree;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;
import delta.games.lotro.lore.instances.io.xml.InstancesTreeXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Loads instance categories (similar to the ones in the Instance Finder)
 * @author DAM
 */
public class MainDatInstancesTreeLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatInstancesTreeLoader.class);

  /**
   * "Seasonal" category name.
   */
  private static final String SEASONAL="Seasonal";
  /**
   * "Epic Battles" category name.
   */
  private static final String EPIC_BATTLES="Epic Battles";
  /**
   * "Non-scaling Instance" category name.
   */
  private static final String NON_SCALING_INSTANCES="Non-scaling Instance";
  /**
   * "(Scaling)Instance" category name.
   */
  private static final String SCALING_INSTANCES="Instance";
  /**
   * "Skirmish" category name.
   */
  private static final String SKIRMISH="Skirmish";

  private DataFacade _facade;
  private LotroEnum<WJInstanceGroup> _instanceGroups;
  private LotroEnum<WJEncounterCategory> _encounterCategory;
  private InstancesTree _tree;
  private Set<WJEncounterCategory> _seasonalCategories;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatInstancesTreeLoader(DataFacade facade)
  {
    _facade=facade;
    _tree=new InstancesTree();
    _instanceGroups=LotroEnumsRegistry.getInstance().get(WJInstanceGroup.class);
    _encounterCategory=LotroEnumsRegistry.getInstance().get(WJEncounterCategory.class);
    _seasonalCategories=new HashSet<WJEncounterCategory>();
  }

  /*
   * Categories are:
   * - Epic Battles: the ones in WorldJoinControl_InstanceGroups, sorted by their own category.
   * one item covers 1 or several instances (depending on size). See PE property: Skirmish_Template_GroupSize_Override: 2 (Duo)
   * - Instance: scaling ones, sorted by their own categories
   * - Non-scaling instances: non scaling ones, sorted by their own categories
   * - Seasonal: the ones whose category is in WorldJoin_Seasonal_EncounterCategory_Array
   * - Skirmish
   * - Classic
   */

  /**
   * Load instances tree.
   */
  public void doIt()
  {
    // WorldJoinControl
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"WorldJoinControl"); // 0x7001B7CE
    loadCategories(props);
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    // Featured instances
    LOGGER.debug("Featured instances");
    Object[] featuredInstancesArray=(Object[])props.getProperty("WorldJoinControl_FeaturedInstance_Array");
    for(Object featuredInstanceObj : featuredInstancesArray)
    {
      int instanceId=((Integer)featuredInstanceObj).intValue();
      PrivateEncounter pe=peMgr.getPrivateEncounterById(instanceId);
      if (pe!=null)
      {
        String name=pe.getName();
        boolean isSkirmish=(pe instanceof SkirmishPrivateEncounter);
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("\tID="+instanceId+" => "+name+" (skirmish="+isSkirmish);
        }
      }
      else
      {
        LOGGER.warn("Private encounter not found: "+instanceId);
      }
    }
    InstanceCategory seasonal=new InstanceCategory(SEASONAL);
    _tree.addCategory(seasonal);
    InstanceCategory nonScalingInstances=new InstanceCategory(NON_SCALING_INSTANCES);
    _tree.addCategory(nonScalingInstances);
    InstanceCategory instances=new InstanceCategory(SCALING_INSTANCES);
    _tree.addCategory(instances);
    InstanceCategory skirmishes=new InstanceCategory(SKIRMISH);
    _tree.addCategory(skirmishes);
    // Available instances
    Object[] availablePEsArray=(Object[])props.getProperty("WorldJoin_AvailablePETemplates_Array");
    for(Object availablePEObj : availablePEsArray)
    {
      int privateEncounterId=((Integer)availablePEObj).intValue();
      PrivateEncounter pe=peMgr.getPrivateEncounterById(privateEncounterId);
      if (pe!=null)
      {
        boolean isSkirmish=(pe instanceof SkirmishPrivateEncounter);
        if (!isSkirmish)
        {
          continue;
        }
        SkirmishPrivateEncounter skirmishPE=(SkirmishPrivateEncounter)pe;
        WJEncounterCategory encounterCategory=skirmishPE.getCategory();
        WJEncounterType encounterType=skirmishPE.getType();
        InstanceCategory categoryToUse=instances;
        boolean scalable=skirmishPE.isScalable();
        if (!scalable)
        {
          categoryToUse=nonScalingInstances;
        }
        else if (_seasonalCategories.contains(encounterCategory))
        {
          categoryToUse=seasonal;
        }
        else if (encounterType.getCode()==3)
        {
          categoryToUse=skirmishes;
        }
        categoryToUse.addPrivateEncounter(skirmishPE);
      }
      else
      {
        LOGGER.warn("Private encounter not found: "+privateEncounterId);
      }
    }
    _tree.dump();
    // Save instances tree
    boolean ok=InstancesTreeXMLWriter.writeInstancesTreeFile(GeneratedFiles.INSTANCES_TREE,_tree);
    if (ok)
    {
      LOGGER.info("Wrote instances tree file: "+GeneratedFiles.INSTANCES_TREE);
    }
  }

  private void loadCategories(PropertiesSet props)
  {
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();

    // Epic battles
    {
      InstanceCategory category=new InstanceCategory(EPIC_BATTLES);
      /*
      WorldJoinControl_InstanceGroups:
        #1:
          WorldJoinControl_Grouped_Instance_List:
            #1: 1879269583
          WorldJoinControl_Instance_Group_Label: 3 (Deeping-coomb)
      */
      Object[] instanceGroupsArray=(Object[])props.getProperty("WorldJoinControl_InstanceGroups");
      for(Object instanceGroupObj : instanceGroupsArray)
      {
        PropertiesSet instanceGroupProps=(PropertiesSet)instanceGroupObj;
        int labelCode=((Integer)instanceGroupProps.getProperty("WorldJoinControl_Instance_Group_Label")).intValue();
        @SuppressWarnings("unused")
        WJInstanceGroup label=_instanceGroups.getEntry(labelCode);
        Object[] instanceIdsArray=(Object[])instanceGroupProps.getProperty("WorldJoinControl_Grouped_Instance_List");
        for(Object instanceIdObj : instanceIdsArray)
        {
          int instanceId=((Integer)instanceIdObj).intValue();
          SkirmishPrivateEncounter skirmishPE=(SkirmishPrivateEncounter)peMgr.getPrivateEncounterById(instanceId);
          category.addPrivateEncounter(skirmishPE);
        }
      }
      _tree.addCategory(category);
    }
    //Unused: WorldJoin_Capped_EncounterCategory: 29 (Minas Morgul)
    // Seasonal categories
    {
      /*
      WorldJoin_Seasonal_EncounterCategory_Array:
        #1: 25 (Spring)
        #2: 23 (Summer)
        #3: 27 (Harvestmath)
        #4: 24 (Yule)
        */
      Object[] seasonalCategoryArray=(Object[])props.getProperty("WorldJoin_Seasonal_EncounterCategory_Array");
      for(Object seasonalCategoryObj : seasonalCategoryArray)
      {
        int categoryId=((Integer)seasonalCategoryObj).intValue();
        WJEncounterCategory category=_encounterCategory.getEntry(categoryId);
        _seasonalCategories.add(category);
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
    new MainDatInstancesTreeLoader(facade).doIt();
    facade.dispose();
  }
}
