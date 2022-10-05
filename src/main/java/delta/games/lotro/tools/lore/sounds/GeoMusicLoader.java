package delta.games.lotro.tools.lore.sounds;

import java.io.File;
import java.util.List;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.lotro.jukebox.core.model.SoundContext;
import delta.lotro.jukebox.core.model.SoundContextsManager;
import delta.lotro.jukebox.core.model.SoundDescription;
import delta.lotro.jukebox.core.model.SoundReference;
import delta.lotro.jukebox.core.model.SoundReferences;
import delta.lotro.jukebox.core.model.io.xml.SoundContextsXMLConstants;
import delta.lotro.jukebox.core.model.io.xml.SoundContextsXMLWriter;

/**
 * Loads music data for areas/dungeons. 
 * @author DAM
 */
public class GeoMusicLoader
{
  private DataFacade _facade;
  private EnumMapper _musicMapper;
  private SoundContextManager _contextMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param contextMgr Context manager.
   */
  public GeoMusicLoader(DataFacade facade, SoundContextManager contextMgr)
  {
    _facade=facade;
    _contextMgr=contextMgr;
    _musicMapper=facade.getEnumsManager().getEnumMapper(587202880);
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    SoundContextsManager areaContexts=handleAreas();
    saveContextFile(GeneratedFiles.AREA_CONTEXTS,areaContexts,SoundContextsXMLConstants.AREA_CONTEXT_TAG);
    handleDungeons();
  }

  private void saveContextFile(File toFile, SoundContextsManager mgr, String contextTag)
  {
    SoundContextsXMLWriter writer=new SoundContextsXMLWriter(contextTag);
    writer.writeSoundContextsFile(toFile,mgr.getAllSoundContexts());
  }

  private SoundContextsManager handleAreas()
  {
    SoundContextsManager ret=new SoundContextsManager();
    GeoAreasManager areasMgr=GeoAreasManager.getInstance();
    List<Area> areas=areasMgr.getAreas();
    for(Area area : areas)
    {
      SoundContext context=handleArea(area);
      ret.registerSoundContext(context);
    }
    return ret;
  }

  private SoundContext handleArea(Area area)
  {
    int areaId=area.getIdentifier();
    PropertiesSet areaProps=_facade.loadProperties(areaId+DATConstants.DBPROPERTIES_OFFSET);
    int musicType=((Integer)areaProps.getProperty("Ambient_MusicType")).intValue();
    //int areaType=((Integer)areaProps.getProperty("Area_VisitedAreaType")).intValue();
    //String musicLabel=_musicMapper.getLabel(musicType);
    //System.out.println(area+" => Ambient_MusicType="+musicLabel+", area type="+areaType);
    // Name
    String areaName=DatUtils.getStringProperty(areaProps,"Area_Name");
    // Icon
    Integer imageId=(Integer)areaProps.getProperty("Area_Icon");
    SoundContext ret=new SoundContext(areaId,areaName,imageId.toString());
    SoundReferences soundRefs=ret.getSounds();
    PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDefByName("Area_Music");
    PropertySoundsRegistry registry=_contextMgr.getProperty(propertyDef);
    List<SoundDescription> sounds=registry.getSoundsForValue(musicType);
    for(SoundDescription sound : sounds)
    {
      SoundReference ref=new SoundReference(sound.getIdentifier());
      ref.setSound(sound);
      soundRefs.addSoundReference(ref);
    }
    // No Area_Music
    return ret;
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
    PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDefByName("Area_Music");
    PropertySoundsRegistry registry=_contextMgr.getProperty(propertyDef);
    List<SoundDescription> sounds=registry.getSoundsForValue(musicType);
    for(SoundDescription sound : sounds)
    {
      System.out.println("\t"+sound);
    }
  }
}
