package delta.games.lotro.tools.extraction;

import java.io.File;

import delta.lotro.jukebox.core.config.DataFiles;
import delta.lotro.jukebox.core.config.LotroJukeboxCoreConfig;

/**
 * Constants for files used in the generation of data for the Jukebox application.
 * @author DAM
 */
public class GeneratedJukeboxFiles
{
  /**
   * Sounds.
   */
  public static final File SOUNDS=LotroJukeboxCoreConfig.getInstance().getFile(DataFiles.SOUNDS);
  /**
   * Music items.
   */
  public static final File MUSIC_ITEMS=LotroJukeboxCoreConfig.getInstance().getFile(DataFiles.MUSIC_ITEMS);
  /**
   * Instruments.
   */
  public static final File INSTRUMENTS=LotroJukeboxCoreConfig.getInstance().getFile(DataFiles.INSTRUMENTS);
  /**
   * Area contexts.
   */
  public static final File AREA_CONTEXTS=LotroJukeboxCoreConfig.getInstance().getFile(DataFiles.AREAS);
  /**
   * Dungeon contexts.
   */
  public static final File DUNGEON_CONTEXTS=LotroJukeboxCoreConfig.getInstance().getFile(DataFiles.DUNGEONS);
}
