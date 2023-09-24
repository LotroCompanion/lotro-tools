package delta.games.lotro.tools.dat;

import java.io.File;

import delta.lotro.jukebox.core.config.LotroJukeboxCoreConfig;

/**
 * Constants for files used in the generation of data for the Jukebox application.
 * @author DAM
 */
public class GeneratedJukeboxFiles
{

  // For the jukebox
  /**
   * Sounds.
   */
  public static final File SOUNDS=LotroJukeboxCoreConfig.getInstance().getFile(delta.lotro.jukebox.core.config.DataFiles.SOUNDS);
  /**
   * Music items.
   */
  public static final File MUSIC_ITEMS=LotroJukeboxCoreConfig.getInstance().getFile(delta.lotro.jukebox.core.config.DataFiles.MUSIC_ITEMS);
  /**
   * Instruments.
   */
  public static final File INSTRUMENTS=LotroJukeboxCoreConfig.getInstance().getFile(delta.lotro.jukebox.core.config.DataFiles.INSTRUMENTS);
  /**
   * Area contexts.
   */
  public static final File AREA_CONTEXTS=LotroJukeboxCoreConfig.getInstance().getFile(delta.lotro.jukebox.core.config.DataFiles.AREAS);
  /**
   * Dungeon contexts.
   */
  public static final File DUNGEON_CONTEXTS=LotroJukeboxCoreConfig.getInstance().getFile(delta.lotro.jukebox.core.config.DataFiles.DUNGEONS);
}
