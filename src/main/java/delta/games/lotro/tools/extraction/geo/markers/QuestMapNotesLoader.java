package delta.games.lotro.tools.extraction.geo.markers;

import java.io.ByteArrayInputStream;
import java.io.File;

import delta.common.utils.io.Console;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.tools.extraction.geo.maps.MapConstants;

/**
 * Loader for quest map notes.
 * @author DAM
 */
public class QuestMapNotesLoader
{
  private static final boolean VERBOSE=false;
  private static final int QUEST_MAP_NOTES_DID=0x0E400000;

  private DataFacade _facade;
  private DBPropertiesLoader _propsLoader;
  private MarkersLoadingUtils _markersUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param markersUtils Markers utils.
   */
  public QuestMapNotesLoader(DataFacade facade, MarkersLoadingUtils markersUtils)
  {
    _facade=facade;
    _markersUtils=markersUtils;
    _propsLoader=new DBPropertiesLoader(_facade);
  }

  private void loadQuestMapNote(ByteArrayInputStream bis)
  {
    // Position
    DatPosition position=GeoLoader.readPosition(bis);
    // Area ID
    int areaDID=BufferUtils.readUInt32(bis);
    // Dungeon ID
    int dungeonDID=BufferUtils.readUInt32(bis);

    // Associated data ID
    int noteDID=BufferUtils.readUInt32(bis);

    // Properties
    // Get content layers properties (Object[] with Integers)
    Object[] contentLayersArray=null;
    PropertyValue contentLayersProperties=_propsLoader.decodeProperty(bis,false);
    if (contentLayersProperties!=null)
    {
      contentLayersArray=(Object[])contentLayersProperties.getValue();
    }

    PropertiesSet questDispenserInfo=new PropertiesSet();
    _propsLoader.decodeProperties(bis,questDispenserInfo);

    if (VERBOSE)
    {
      Console.println("****** Quest map note:");
      _markersUtils.log(position,areaDID,dungeonDID,noteDID,contentLayersArray,null,0);
      if (questDispenserInfo.getPropertyNames().size()>0)
      {
        Console.println("Quest dispenser info: "+questDispenserInfo.dump());
      }
    }
    _markersUtils.buildMarker(position,areaDID,dungeonDID,noteDID,contentLayersArray,null,0);
  }

  /**
   * Load quest map notes.
   */
  private void loadQuestMapNotes()
  {
    byte[] data=_facade.loadData(QUEST_MAP_NOTES_DID);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=QUEST_MAP_NOTES_DID)
    {
      throw new IllegalArgumentException("Expected DID for quest map notes: "+QUEST_MAP_NOTES_DID);
    }
    int count=BufferUtils.readUInt32(bis);
    System.out.println("Number of quest map notes: "+count);
    for(int i=0;i<count;i++)
    {
      loadQuestMapNote(bis);
    }
  }

  /**
   * Do load quest map notes.
   */
  public void doIt()
  {
    loadQuestMapNotes();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    File rootDir=MapConstants.getRootDir();
    MapsManager mapsManager=new MapsManager(rootDir,false);
    MarkersDataManager markersDataManager=new MarkersDataManager(facade,mapsManager);
    MarkersLoadingUtils markersUtils=new MarkersLoadingUtils(facade,markersDataManager);
    QuestMapNotesLoader loader=new QuestMapNotesLoader(facade,markersUtils);
    loader.doIt();
    markersDataManager.write();
  }
}
