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
  public static final File CLASSES=LotroCoreConfig.getInstance().getFile(DataFiles.CLASSES);
  /**
   * Start stats.
   */
  public static final File START_STATS=LotroCoreConfig.getInstance().getFile(DataFiles.START_STATS);
  /**
   * Stat contribs.
   */
  public static final File STAT_CONTRIBS=LotroCoreConfig.getInstance().getFile(DataFiles.STAT_CONTRIBS);
  /**
   * Races.
   */
  public static final File RACES=LotroCoreConfig.getInstance().getFile(DataFiles.RACES);
  /**
   * Traits.
   */
  public static final File TRAITS=LotroCoreConfig.getInstance().getFile(DataFiles.TRAITS);
  /**
   * Trait icons.
   */
  public static final File TRAIT_ICONS=new File("../lotro-data/traits/traitIcons.zip").getAbsoluteFile();
  /**
   * Titles.
   */
  public static final File TITLES=LotroCoreConfig.getInstance().getFile(DataFiles.TITLES);
  /**
   * Title icons.
   */
  public static final File TITLE_ICONS=new File("../lotro-data/titles/titleIcons.zip").getAbsoluteFile();
  /**
   * Emotes.
   */
  public static final File EMOTES=LotroCoreConfig.getInstance().getFile(DataFiles.EMOTES);
  /**
   * Emote icons.
   */
  public static final File EMOTE_ICONS=new File("../lotro-data/emotes/emoteIcons.zip").getAbsoluteFile();
  /**
   * Items.
   */
  public static final File ITEMS=LotroCoreConfig.getInstance().getFile(DataFiles.ITEMS);
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
  public static final File PROGRESSIONS_ITEMS=new File("data/progressions/tmp/progressions_items.xml").getAbsoluteFile();
  /**
   * Progressions for characters.
   */
  public static final File PROGRESSIONS_CHARACTERS=new File("data/progressions/tmp/progressions_characters.xml").getAbsoluteFile();
  /**
   * All progressions.
   */
  public static final File PROGRESSIONS=LotroCoreConfig.getInstance().getFile(DataFiles.PROGRESSIONS);
  /**
   * Recipes.
   */
  public static final File RECIPES=LotroCoreConfig.getInstance().getFile(DataFiles.RECIPES);
  /**
   * Relics.
   */
  public static final File RELICS=LotroCoreConfig.getInstance().getFile(DataFiles.RELICS);
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
   * Non-imbued legacies.
   */
  public static final File NON_IMBUED_LEGACIES=LotroCoreConfig.getInstance().getFile(DataFiles.NON_IMBUED_LEGACIES);
  /**
   * Colors.
   */
  public static final File COLORS=LotroCoreConfig.getInstance().getFile(DataFiles.COLORS);
}
