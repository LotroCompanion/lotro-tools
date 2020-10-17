package delta.games.lotro.tools.dat.instances;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.instances.InstanceCategories;
import delta.games.lotro.lore.instances.InstanceCategory;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;

/**
 * Loads instance categories (similar to the ones in the Instance Finder)
 * @author DAM
 */
public class MainDatInstancesTreeLoader
{
  /**
   * "Seasonal" category name.
   */
  private static final String SEASONAL="Seasonal";

  private static final Logger LOGGER=Logger.getLogger(MainDatInstancesTreeLoader.class);

  private DataFacade _facade;
  private EnumMapper _instanceGroups;
  private EnumMapper _encounterCategory;
  private InstanceCategories _categories;
  private Set<String> _seasonalCategories;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatInstancesTreeLoader(DataFacade facade)
  {
    _facade=facade;
    _categories=new InstanceCategories();
    _instanceGroups=facade.getEnumsManager().getEnumMapper(0x230003D1);
    _encounterCategory=facade.getEnumsManager().getEnumMapper(0x23000350);
    _seasonalCategories=new HashSet<String>();
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
  private void doIt()
  {
    // WorldJoinControl
    PropertiesSet props=_facade.loadProperties(0x7001B7CE+DATConstants.DBPROPERTIES_OFFSET);
    loadCategories(props);
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    // Featured instances
    /*
    Object[] featuredInstancesArray=(Object[])props.getProperty("WorldJoinControl_FeaturedInstance_Array");
    for(Object featuredInstanceObj : featuredInstancesArray)
    {
      int instanceId=((Integer)featuredInstanceObj).intValue();
      PrivateEncounter pe=peMgr.getPrivateEncounterById(instanceId);
      if (pe!=null)
      {
        String name=pe.getName();
        boolean isSkirmish=(pe instanceof SkirmishPrivateEncounter);
        System.out.println("\tID="+instanceId+" => "+name+" (skirmish="+isSkirmish);
      }
      else
      {
        LOGGER.warn("Private encounter not found: "+instanceId);
      }
    }
    */
    // Available instances
    //System.out.println("Available instances:");
    String[] path={"",""};
    Object[] availablePEsArray=(Object[])props.getProperty("WorldJoin_AvailablePETemplates_Array");
    for(Object availablePEObj : availablePEsArray)
    {
      int privateEncounterId=((Integer)availablePEObj).intValue();
      PrivateEncounter pe=peMgr.getPrivateEncounterById(privateEncounterId);
      if (pe!=null)
      {
        boolean isSkirmish=(pe instanceof SkirmishPrivateEncounter);
        //String name=pe.getName();
        //System.out.println("\tID="+privateEncounterId+" => "+name+" (skirmish="+isSkirmish+")");
        if (!isSkirmish)
        {
          continue;
        }
        SkirmishPrivateEncounter skirmishPE=(SkirmishPrivateEncounter)pe;
        String categoryName=skirmishPE.getCategory();
        path[1]=categoryName;
        boolean scalable=skirmishPE.isScalable();
        if (!scalable)
        {
          path[0]="Non-scaling Instance";
        }
        else if (_seasonalCategories.contains(categoryName))
        {
          path[0]=SEASONAL;
        }
        else
        {
          // "Instance" or "Skimirsh" ATM
          path[0]=skirmishPE.getType();
        }
        InstanceCategory category=_categories.getFromPath(path);
        category.addPrivateEncounter(skirmishPE);
      }
      else
      {
        LOGGER.warn("Private encounter not found: "+privateEncounterId);
      }
    }
    _categories.dump();
  }

  private void loadCategories(PropertiesSet props)
  {
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();

    // Epic battles
    {
      String[] path={"Epic Battles","",""};
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
        String label=_instanceGroups.getLabel(labelCode);
        path[2]=label;
        Object[] instanceIdsArray=(Object[])instanceGroupProps.getProperty("WorldJoinControl_Grouped_Instance_List");
        for(Object instanceIdObj : instanceIdsArray)
        {
          int instanceId=((Integer)instanceIdObj).intValue();
          SkirmishPrivateEncounter skirmishPE=(SkirmishPrivateEncounter)peMgr.getPrivateEncounterById(instanceId);
          String categoryName=skirmishPE.getCategory();
          path[1]=categoryName;
          InstanceCategory category=_categories.getFromPath(path);
          category.addPrivateEncounter(skirmishPE);
        }
      }
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
      String[] path={SEASONAL,""};
      Object[] seasonalCategoryArray=(Object[])props.getProperty("WorldJoin_Seasonal_EncounterCategory_Array");
      for(Object seasonalCategoryObj : seasonalCategoryArray)
      {
        int categoryId=((Integer)seasonalCategoryObj).intValue();
        String categoryName=_encounterCategory.getLabel(categoryId);
        path[1]=categoryName;
        _categories.getFromPath(path);
        _seasonalCategories.add(categoryName);
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
