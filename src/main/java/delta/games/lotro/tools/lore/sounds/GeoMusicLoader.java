package delta.games.lotro.tools.lore.sounds;

import java.util.List;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;

/**
 * Loads music data for areas/dungeons. 
 * @author DAM
 */
public class GeoMusicLoader
{
  private DataFacade _facade;
  private EnumMapper _musicMapper;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public GeoMusicLoader(DataFacade facade)
  {
    _facade=facade;
    _musicMapper=facade.getEnumsManager().getEnumMapper(587202880);
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    handleAreas();
    handleDungeons();
  }

  private void handleAreas()
  {
    GeoAreasManager areasMgr=GeoAreasManager.getInstance();
    List<Area> areas=areasMgr.getAreas();
    for(Area area : areas)
    {
      handleArea(area);
    }
  }

  private void handleArea(Area area)
  {
    int areaId=area.getIdentifier();
    PropertiesSet itemProps=_facade.loadProperties(areaId+DATConstants.DBPROPERTIES_OFFSET);
    int musicType=((Integer)itemProps.getProperty("Ambient_MusicType")).intValue();
    int areaType=((Integer)itemProps.getProperty("Area_VisitedAreaType")).intValue();
    String musicLabel=_musicMapper.getLabel(musicType);
    System.out.println(area+" => Ambient_MusicType="+musicLabel+", area type="+areaType);
    // No Area_Music
  }

  private void handleDungeons()
  {
    DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
    List<Dungeon> dungeons=dungeonsMgr.getDungeons();
    for(Dungeon dungeon : dungeons)
    {
      handleDungeon(dungeon);
    }
  }

  private void handleDungeon(Dungeon dungeon)
  {
    int dungeonId=dungeon.getIdentifier();
    PropertiesSet itemProps=_facade.loadProperties(dungeonId+DATConstants.DBPROPERTIES_OFFSET);
    int musicType=((Integer)itemProps.getProperty("Dungeon_Music")).intValue();
    String musicLabel=_musicMapper.getLabel(musicType);
    System.out.println(dungeon+" => Dungeon_Music="+musicLabel);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    GeoMusicLoader loader=new GeoMusicLoader(facade);
    loader.doIt();
    facade.dispose();
  }
}
