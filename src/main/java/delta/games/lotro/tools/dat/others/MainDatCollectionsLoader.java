package delta.games.lotro.tools.dat.others;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get definition of collections from DAT files.
 * @author DAM
 */
public class MainDatCollectionsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatCollectionsLoader.class);

  private DataFacade _facade;
  private MountsLoader _mountsLoader;
  private CosmeticPetLoader _cosmeticPetsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatCollectionsLoader(DataFacade facade)
  {
    _facade=facade;
    _mountsLoader=new MountsLoader(facade);
    _cosmeticPetsLoader=new CosmeticPetLoader(facade);
  }

  private Object loadCollection(int collectionId)
  {
    Object ret=null;
    PropertiesSet properties=_facade.loadProperties(collectionId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      System.out.println("************* "+collectionId+" *************");
      //System.out.println(properties.dump());

      // Name
      String name=DatUtils.getStringProperty(properties,"Collection_Name");
      System.out.println("Collection name: "+name);
      // Category
      int collectionCategory=((Integer)properties.getProperty("Collection_Category")).intValue();
      // Pieces
      Object[] piecesList=(Object[])properties.getProperty("Collection_DID_PieceList");
      for(Object pieceObj : piecesList)
      {
        int pieceId=((Integer)pieceObj).intValue();
        loadCollectionItem(pieceId,collectionCategory);
      }
      // TODO Treasure
      //int treasureId=((Integer)properties.getProperty("Collection_TreasureDID")).intValue();
    }
    else
    {
      LOGGER.warn("Could not handle collection ID="+collectionId);
    }
    return ret;
  }

  private void loadCollectionItem(int collectionItemId, int category)
  {
    if (category==2)
    {
      _cosmeticPetsLoader.load(collectionItemId);
    }
    else if (category==1)
    {
      _mountsLoader.load(collectionItemId);
    }
    else
    {
      LOGGER.warn("Could not handle collection item ID="+collectionItemId);
    }
  }

  private void doIt()
  {
    PropertiesSet collectionDirectoryProps=_facade.loadProperties(1879310505+DATConstants.DBPROPERTIES_OFFSET);
    Object[] collectionsList=(Object[])collectionDirectoryProps.getProperty("CollectionControl_CollectionList");
    for(Object collectionObj : collectionsList)
    {
      int collectionId=((Integer)collectionObj).intValue();
      loadCollection(collectionId);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatCollectionsLoader(facade).doIt();
    facade.dispose();
  }
}
