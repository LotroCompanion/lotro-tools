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
   * Factions.
   */
  public static final File FACTIONS=LotroCoreConfig.getInstance().getFile(DataFiles.FACTIONS);
  /**
   * Traits.
   */
  public static final File TRAITS=LotroCoreConfig.getInstance().getFile(DataFiles.TRAITS);
  /**
   * Trait icons.
   */
  public static final File TRAIT_ICONS=new File("../lotro-data/traits/traitIcons.zip").getAbsoluteFile();
  /**
   * Virtues.
   */
  public static final File VIRTUES=LotroCoreConfig.getInstance().getFile(DataFiles.VIRTUES);
  /**
   * Virtue icons.
   */
  public static final File VIRTUE_ICONS=new File("../lotro-data/virtues/virtueIcons.zip").getAbsoluteFile();
  /**
   * Skills.
   */
  public static final File SKILLS=LotroCoreConfig.getInstance().getFile(DataFiles.SKILLS);
  /**
   * Skill icons.
   */
  public static final File SKILL_ICONS=new File("../lotro-data/skills/skillIcons.zip").getAbsoluteFile();
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
   * Progressions for achievables.
   */
  public static final File PROGRESSIONS_ACHIEVABLES=new File("data/progressions/tmp/progressions_achievables.xml").getAbsoluteFile();
  /**
   * Progressions for combat data.
   */
  public static final File PROGRESSIONS_COMBAT=new File("data/progressions/tmp/progressions_combat.xml").getAbsoluteFile();
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
   * Default non-imbued legacies.
   */
  public static final File DEFAULT_NON_IMBUED_LEGACIES=LotroCoreConfig.getInstance().getFile(DataFiles.DEFAULT_NON_IMBUED_LEGACIES);
  /**
   * Non-imbued legacies.
   */
  public static final File NON_IMBUED_LEGACIES=LotroCoreConfig.getInstance().getFile(DataFiles.NON_IMBUED_LEGACIES);
  /**
   * Legacies icons.
   */
  public static final File LEGACIES_ICONS=new File("../lotro-data/legendary/legaciesIcons.zip").getAbsoluteFile();
  /**
   * Passives.
   */
  public static final File PASSIVES=LotroCoreConfig.getInstance().getFile(DataFiles.PASSIVES);
  /**
   * Passives usage.
   */
  public static final File PASSIVES_USAGE=LotroCoreConfig.getInstance().getFile(DataFiles.PASSIVES_USAGE);
  /**
   * Data for the legendary system.
   */
  public static final File LEGENDARY_DATA=LotroCoreConfig.getInstance().getFile(DataFiles.LEGENDARY_DATA);
  /**
   * Consumables.
   */
  public static final File CONSUMABLES=LotroCoreConfig.getInstance().getFile(DataFiles.CONSUMABLES);
  /**
   * Colors.
   */
  public static final File COLORS=LotroCoreConfig.getInstance().getFile(DataFiles.COLORS);
  /**
   * Quests.
   */
  public static final File QUESTS=LotroCoreConfig.getInstance().getFile(DataFiles.QUESTS);
  /**
   * Deeds.
   */
  public static final File DEEDS=LotroCoreConfig.getInstance().getFile(DataFiles.DEEDS);
  /**
   * Sets.
   */
  public static final File SETS=LotroCoreConfig.getInstance().getFile(DataFiles.SETS);
  /**
   * Data for the combat system.
   */
  public static final File COMBAT_DATA=LotroCoreConfig.getInstance().getFile(DataFiles.COMBAT_DATA);
  /**
   * Data for the crafting system.
   */
  public static final File CRAFTING_DATA=LotroCoreConfig.getInstance().getFile(DataFiles.CRAFTING_DATA);
}
