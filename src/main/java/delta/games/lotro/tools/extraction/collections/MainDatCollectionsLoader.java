package delta.games.lotro.tools.extraction.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.CollectionCategory;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.requirements.RaceRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.collections.Collectable;
import delta.games.lotro.lore.collections.CollectionDescription;
import delta.games.lotro.lore.collections.io.xml.CollectionsXMLWriter;
import delta.games.lotro.lore.collections.mounts.MountsManager;
import delta.games.lotro.lore.collections.pets.CosmeticPetsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.achievables.rewards.DatRewardsLoader;
import delta.games.lotro.tools.extraction.requirements.RequirementsLoadingUtils;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get definition of collections from DAT files.
 * @author DAM
 */
public class MainDatCollectionsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatCollectionsLoader.class);

  private DataFacade _facade;
  private DatRewardsLoader _rewardsLoader;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param rewardsLoader Rewards loader.
   */
  public MainDatCollectionsLoader(DataFacade facade, DatRewardsLoader rewardsLoader)
  {
    _facade=facade;
    _rewardsLoader=rewardsLoader;
    _i18n=new I18nUtils("collections",facade.getGlobalStringsManager());
  }

  private CollectionDescription loadCollection(int collectionId)
  {
    CollectionDescription ret=null;
    PropertiesSet properties=_facade.loadProperties(collectionId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new CollectionDescription(collectionId);
      // Name
      String name=_i18n.getNameStringProperty(properties,"Collection_Name",collectionId,0);
      ret.setName(name);
      // Category
      int collectionCategory=((Integer)properties.getProperty("Collection_Category")).intValue();
      LotroEnum<CollectionCategory> categoryEnum=LotroEnumsRegistry.getInstance().get(CollectionCategory.class);
      CollectionCategory category=categoryEnum.getEntry(collectionCategory);
      ret.setCategory(category);
      // Pieces
      Object[] piecesList=(Object[])properties.getProperty("Collection_DID_PieceList");
      for(Object pieceObj : piecesList)
      {
        int pieceId=((Integer)pieceObj).intValue();
        Collectable element=loadCollectionItem(pieceId,collectionCategory);
        if (element!=null)
        {
          ret.addElement(element);
        }
      }
      // Treasure
      int treasureId=((Integer)properties.getProperty("Collection_TreasureDID")).intValue();
      Rewards rewards=ret.getRewards();
      _rewardsLoader.loadRewards(rewards,treasureId,null);
      // Requirements
      UsageRequirement usageRequirements=new UsageRequirement();
      RequirementsLoadingUtils.loadRequiredRaces(properties,usageRequirements);
      RaceRequirement raceRequirement=usageRequirements.getRaceRequirement();
      ret.setRaceRequirement(raceRequirement);
    }
    else
    {
      LOGGER.warn("Could not handle collection ID="+collectionId);
    }
    return ret;
  }

  private Collectable loadCollectionItem(int collectionItemId, int category)
  {
    Collectable ret=null;
    if (category==2)
    {
      ret=CosmeticPetsManager.getInstance().getPet(collectionItemId);
    }
    else if (category==1)
    {
      ret=MountsManager.getInstance().getMount(collectionItemId);
    }
    else
    {
      LOGGER.warn("Could not handle collection item ID="+collectionItemId);
    }
    return ret;
  }

  /**
   * Load collections.
   */
  public void doIt()
  {
    List<CollectionDescription> collections=new ArrayList<CollectionDescription>();
    PropertiesSet collectionDirectoryProps=WeenieContentDirectory.loadWeenieContentProps(_facade,"CollectionControl");
    Object[] collectionsList=(Object[])collectionDirectoryProps.getProperty("CollectionControl_CollectionList");
    for(Object collectionObj : collectionsList)
    {
      int collectionId=((Integer)collectionObj).intValue();
      CollectionDescription collection=loadCollection(collectionId);
      if (collection!=null)
      {
        collections.add(collection);
      }
    }
    LOGGER.info("Loaded "+collections.size()+" collections.");
    // Save
    // - data
    saveCollections(collections);
    // - labels
    _i18n.save();
  }

  /**
   * Save the loaded collections to a file.
   * @param collections Collections to save.
   */
  private void saveCollections(List<CollectionDescription> collections)
  {
    Collections.sort(collections,new IdentifiableComparator<CollectionDescription>());
    boolean ok=CollectionsXMLWriter.write(GeneratedFiles.COLLECTIONS,collections);
    if (ok)
    {
      LOGGER.info("Wrote collections file: "+GeneratedFiles.COLLECTIONS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    DatRewardsLoader rewardsLoader=new DatRewardsLoader(facade);
    new MainDatCollectionsLoader(facade,rewardsLoader).doIt();
    facade.dispose();
  }
}
