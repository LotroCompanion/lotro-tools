package delta.games.lotro.tools.dat;

import java.io.File;

/**
 * Constants for files used in the generation of data from the DAT files.
 * @author DAM
 */
public class GeneratedFiles
{
  /**
   * Progressions for items.
   */
  public static final File PROGRESSIONS_ITEMS=new File("../lotro-companion/data/lore/progressions_items.xml").getAbsoluteFile();
  /**
   * Progressions for characters.
   */
  public static final File PROGRESSIONS_CHARACTERS=new File("../lotro-companion/data/lore/progressions_characters.xml").getAbsoluteFile();
  /**
   * All progressions.
   */
  public static final File PROGRESSIONS=new File("../lotro-companion/data/lore/progressions.xml").getAbsoluteFile();
  /**
   * Relics.
   */
  public static final File RELICS=new File("../lotro-companion/data/lore/relics.xml").getAbsoluteFile();
  /**
   * Relics (2).
   */
  public static final File RELICS2=new File("../lotro-relics/relics.xml").getAbsoluteFile();
  /**
   * Relic icons.
   */
  public static final File RELIC_ICONS=new File("../lotro-relics/relicIcons.zip").getAbsoluteFile();
}
