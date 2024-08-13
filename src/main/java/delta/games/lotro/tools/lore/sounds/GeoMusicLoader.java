package delta.games.lotro.tools.lore.sounds;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.tools.dat.GeneratedJukeboxFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.lotro.jukebox.core.model.base.SoundDescription;
import delta.lotro.jukebox.core.model.context.SoundContext;
import delta.lotro.jukebox.core.model.context.SoundContextsManager;
import delta.lotro.jukebox.core.model.context.SoundReference;
import delta.lotro.jukebox.core.model.context.SoundReferences;
import delta.lotro.jukebox.core.model.context.io.xml.SoundContextsXMLConstants;
import delta.lotro.jukebox.core.model.context.io.xml.SoundContextsXMLWriter;

/**
 * Loads music data for areas/dungeons.
 * @author DAM
 */
public class GeoMusicLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(GeoMusicLoader.class);

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
    saveContextFile(GeneratedJukeboxFiles.AREA_CONTEXTS,areaContexts,SoundContextsXMLConstants.AREA_CONTEXT_TAG);
    SoundContextsManager dungeonContexts=handleDungeons();
    saveContextFile(GeneratedJukeboxFiles.DUNGEON_CONTEXTS,dungeonContexts,SoundContextsXMLConstants.DUNGEON_CONTEXT_TAG);
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
    if (LOGGER.isDebugEnabled())
    {
      String musicLabel=_musicMapper.getLabel(musicType);
      LOGGER.debug("Area ID={}, music type={} ({})", Integer.valueOf(areaId), Integer.valueOf(musicType), musicLabel);
    }
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
    return ret;
  }

  private SoundContextsManager handleDungeons()
  {
    SoundContextsManager ret=new SoundContextsManager();
    DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
    List<Dungeon> dungeons=dungeonsMgr.getDungeons();
    for(Dungeon dungeon : dungeons)
    {
      SoundContext context=handleDungeon(dungeon);
      ret.registerSoundContext(context);
    }
    return ret;
  }

  private SoundContext handleDungeon(Dungeon dungeon)
  {
    int dungeonId=dungeon.getIdentifier();
    PropertiesSet dungeonProps=_facade.loadProperties(dungeonId+DATConstants.DBPROPERTIES_OFFSET);
    int musicType=((Integer)dungeonProps.getProperty("Dungeon_Music")).intValue();
    if (LOGGER.isDebugEnabled())
    {
      String musicLabel=_musicMapper.getLabel(musicType);
      LOGGER.debug("Dungeon ID={}, music type={} ({})", Integer.valueOf(dungeonId), Integer.valueOf(musicType), musicLabel);
    }
    // Name
    String dungeonName=DatUtils.getStringProperty(dungeonProps,"Dungeon_Name");
    SoundContext ret=new SoundContext(dungeonId,dungeonName,"");
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
    return ret;
  }
}
