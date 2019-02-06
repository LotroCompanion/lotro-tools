package delta.games.lotro.tools.dat;

import java.io.File;

import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;

/**
 * Constants for files used in the generation of data from the DAT files.
 * @author DAM
 */
public class GeneratedFiles
{
  /**
   * Classes.
   */
  public static final File CLASSES=new File("../lotro-companion/data/lore/characters/classes.xml").getAbsoluteFile();
  /**
   * Start stats.
   */
  public static final File START_STATS=new File("../lotro-companion/data/lore/characters/startStats.xml").getAbsoluteFile();
  /**
   * Stat contribs.
   */
  public static final File STAT_CONTRIBS=new File("../lotro-companion/data/lore/characters/statContribs.xml").getAbsoluteFile();
  /**
   * Races.
   */
  public static final File RACES=new File("../lotro-companion/data/lore/characters/races.xml").getAbsoluteFile();
  /**
   * Traits.
   */
  public static final File TRAITS=new File("../lotro-companion/data/lore/characters/traits.xml").getAbsoluteFile();
  /**
   * Traits (2).
   */
  public static final File TRAITS2=new File("../lotro-data/traits/traits.xml").getAbsoluteFile();
  /**
   * Trait icons.
   */
  public static final File TRAIT_ICONS=new File("../lotro-data/traits/traitIcons.zip").getAbsoluteFile();
  /**
   * Titles.
   */
  public static final File TITLES=new File("../lotro-companion/data/lore/titles.xml").getAbsoluteFile();
  /**
   * Titles (2).
   */
  public static final File TITLES2=new File("../lotro-data/titles/titles.xml").getAbsoluteFile();
  /**
   * Title icons.
   */
  public static final File TITLE_ICONS=new File("../lotro-data/titles/titleIcons.zip").getAbsoluteFile();
  /**
   * Emotes.
   */
  public static final File EMOTES=new File("../lotro-companion/data/lore/emotes.xml").getAbsoluteFile();
  /**
   * Emotes (2).
   */
  public static final File EMOTES2=new File("../lotro-data/emotes/emotes.xml").getAbsoluteFile();
  /**
   * Emote icons.
   */
  public static final File EMOTE_ICONS=new File("../lotro-data/emotes/emoteIcons.zip").getAbsoluteFile();
  /**
   * Items.
   */
  public static final File ITEMS=new File("../lotro-companion/data/lore/items.xml").getAbsoluteFile();
  /**
   * Item icons.
   */
  public static final File ITEM_ICONS_DIR=new File("../lotro-item-icons-db/icons").getAbsoluteFile();
  /**
   * Stats.
   */
  public static final File STATS=LotroCoreConfig.getInstance().getFile(DataFiles.STATS);
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
   * Recipes.
   */
  public static final File RECIPES=new File("../lotro-companion/data/lore/recipes.xml").getAbsoluteFile();
  /**
   * Recipes (2).
   */
  public static final File RECIPES2=new File("../lotro-data/recipes/recipes.xml").getAbsoluteFile();
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
  /**
   * Legendary titles.
   */
  public static final File LEGENDARY_TITLES=LotroCoreConfig.getInstance().getFile(DataFiles.LEGENDARY_TITLES);
  /**
   * Legacies.
   */
  public static final File LEGACIES=LotroCoreConfig.getInstance().getFile(DataFiles.LEGACIES);
  /**
   * Colors.
   */
  public static final File COLORS=LotroCoreConfig.getInstance().getFile(DataFiles.COLORS);
}
