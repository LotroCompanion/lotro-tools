package delta.games.lotro.tools.extraction.loot;

import org.apache.log4j.Logger;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.MobDivision;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.LandDivision;
import delta.games.lotro.lore.maps.Territory;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Loader for the quest items directory.
 * @author DAM
 */
public class QuestItemDirectoryLoader
{
  private static final Logger LOGGER=Logger.getLogger(QuestItemDirectoryLoader.class);

  private DataFacade _facade;
  private LootProbabilities _probabilities;
  private LotroEnum<Species> _species;
  private LotroEnum<SubSpecies> _subSpecies;
  private LotroEnum<MobDivision> _mobDivision;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public QuestItemDirectoryLoader(DataFacade facade)
  {
    _facade=facade;
    _probabilities=new LootProbabilities(facade);
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _species=enumsRegistry.get(Species.class);
    _subSpecies=enumsRegistry.get(SubSpecies.class);
    _mobDivision=enumsRegistry.get(MobDivision.class);
  }

  private void handleQuestItemDirectory(int id)
  {
    PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(props.dump());
    }
    Object[] array=(Object[])props.getProperty("Quest_QuestItemDirectory_Array");
    if (array==null)
    {
      return;
    }
    for(Object entryObj : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      handleQuestItemEntry(entryProps);
    }
  }

  private void handleQuestItemEntry(PropertiesSet props)
  {
    /*
    Agent_Species: 95 (Dunlending)
    Agent_Subspecies: 0 (Undef)
    Quest_DropFrequency: 10 (Frequent)
    Quest_ItemDID: 1879137762
    Quest_MonsterDivision: 200 (Eregion_PendEregion)
     */
    // Species
    int speciesCode=((Integer)props.getProperty("Agent_Species")).intValue();
    Species species=_species.getEntry(speciesCode);
    // Sub-species
    int subSpeciesCode=((Integer)props.getProperty("Agent_Subspecies")).intValue();
    SubSpecies subSpecies=_subSpecies.getEntry(subSpeciesCode);
    // Probability
    int frequencyCode=((Integer)props.getProperty("Quest_DropFrequency")).intValue();
    float probability=_probabilities.getProbability(frequencyCode);
    // Item
    int itemID=((Integer)props.getProperty("Quest_ItemDID")).intValue();
    Item item=ItemsManager.getInstance().getItem(itemID);
    // Mob division
    int mobDivisionID=((Integer)props.getProperty("Quest_MonsterDivision")).intValue();
    MobDivision mobDivision=_mobDivision.getEntry(mobDivisionID);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Species="+species+", subSpecies="+subSpecies+", probability="+probability+", item="+item+", division="+mobDivision);
    }
  }

  private LandDivision getRegion(int id)
  {
    GeoAreasManager geoAreasManager=GeoAreasManager.getInstance();
    Area area=geoAreasManager.getAreaById(id);
    if (area!=null)
    {
      return area;
    }
    Territory territory=geoAreasManager.getTerritoryById(id);
    if (territory!=null)
    {
      return territory;
    }
    return null;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"QuestItemRegionDirectory");
    Object[] array=(Object[])props.getProperty("Quest_QuestItemRegionDirectory_Array");
    for(Object entryObj : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      int regionID=((Integer)entryProps.getProperty("Quest_MonsterRegion")).intValue();
      LandDivision region=getRegion(regionID);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Region ID="+regionID+" => "+region);
      }
      int directoryID=((Integer)entryProps.getProperty("Quest_QuestItemDirectory")).intValue();
      handleQuestItemDirectory(directoryID);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new QuestItemDirectoryLoader(facade).doIt();
  }
}
