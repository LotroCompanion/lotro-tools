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
   * Stat tomes.
   */
  public static final File STAT_TOMES=LotroCoreConfig.getInstance().getFile(DataFiles.STAT_TOMES);
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
  public static final File ITEM_ICONS_DIR=new File("../lotro-icons/items").getAbsoluteFile();
  /**
   * Stats.
   */
  public static final File STATS=LotroCoreConfig.getInstance().getFile(DataFiles.STATS);
  /**
   * Progressions for items.
   */
  public static final File PROGRESSIONS_ITEMS=new File("data/progressions/tmp/progressions_items.xml").getAbsoluteFile();
  /**
   * Progressions for item sets.
   */
  public static final File PROGRESSIONS_ITEMS_SETS=new File("data/progressions/tmp/progressions_itemsSets.xml").getAbsoluteFile();
  /**
   * Progressions for characters.
   */
  public static final File PROGRESSIONS_CHARACTERS=new File("data/progressions/tmp/progressions_characters.xml").getAbsoluteFile();
  /**
   * Progressions for achievables.
   */
  public static final File PROGRESSIONS_ACHIEVABLES=new File("data/progressions/tmp/progressions_achievables.xml").getAbsoluteFile();
  /**
   * Progressions for buffs.
   */
  public static final File PROGRESSIONS_BUFFS=new File("data/progressions/tmp/progressions_buffs.xml").getAbsoluteFile();
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
   * Relic melding recipes.
   */
  public static final File RELIC_MELDING_RECIPES=LotroCoreConfig.getInstance().getFile(DataFiles.RELIC_MELDING_RECIPES);
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
   * Legendary attributes.
   */
  public static final File LEGENDARY_ATTRS=LotroCoreConfig.getInstance().getFile(DataFiles.LEGENDARY_ATTRS);
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
  /**
   * Trait points.
   */
  public static final File TRAIT_POINTS=LotroCoreConfig.getInstance().getFile(DataFiles.TRAIT_POINTS);
  /**
   * Effect-based buffs.
   */
  public static final File BUFFS=LotroCoreConfig.getInstance().getFile(DataFiles.BUFFS);
  /**
   * Effect icons.
   */
  public static final File EFFECT_ICONS=new File("../lotro-data/effects/effectIcons.zip").getAbsoluteFile();
  /**
   * Mounts.
   */
  public static final File MOUNTS=LotroCoreConfig.getInstance().getFile(DataFiles.MOUNTS);
  /**
   * Mount icons.
   */
  public static final File MOUNT_ICONS=new File("../lotro-data/mounts/mountIcons.zip").getAbsoluteFile();
  /**
   * Cosmetic pets.
   */
  public static final File PETS=LotroCoreConfig.getInstance().getFile(DataFiles.PETS);
  /**
   * Pet icons.
   */
  public static final File PET_ICONS=new File("../lotro-data/pets/petIcons.zip").getAbsoluteFile();
  /**
   * Barterers.
   */
  public static final File BARTERS=LotroCoreConfig.getInstance().getFile(DataFiles.BARTERS);
  /**
   * Vendors.
   */
  public static final File VENDORS=LotroCoreConfig.getInstance().getFile(DataFiles.VENDORS);
  /**
   * Value tables.
   */
  public static final File VALUE_TABLES=LotroCoreConfig.getInstance().getFile(DataFiles.VALUE_TABLES);
  /**
   * Loots.
   */
  public static final File LOOTS=LotroCoreConfig.getInstance().getFile(DataFiles.LOOTS);
  /**
   * Containers.
   */
  public static final File CONTAINERS=LotroCoreConfig.getInstance().getFile(DataFiles.CONTAINERS);
  /**
   * Disenchantments.
   */
  public static final File DISENCHANTMENTS=LotroCoreConfig.getInstance().getFile(DataFiles.DISENCHANTMENTS);
  /**
   * Misc icons.
   */
  public static final File MISC_ICONS=new File("../lotro-data/misc/miscIcons.zip").getAbsoluteFile();
  /**
   * Servers.
   */
  public static final File SERVERS=LotroCoreConfig.getInstance().getFile(DataFiles.SERVERS_DESCRIPTION);
  /**
   * Geographic areas.
   */
  public static final File GEO_AREAS=LotroCoreConfig.getInstance().getFile(DataFiles.GEO_AREAS);
  /**
   * Area icons.
   */
  public static final File AREA_ICONS=new File("../lotro-data/maps/areaIcons.zip").getAbsoluteFile();
  /**
   * Parchment maps.
   */
  public static final File PARCHMENT_MAPS=LotroCoreConfig.getInstance().getFile(DataFiles.PARCHMENT_MAPS);
  /**
   * Dungeons.
   */
  public static final File DUNGEONS=LotroCoreConfig.getInstance().getFile(DataFiles.DUNGEONS);
  /**
   * Private encounters.
   */
  public static final File PRIVATE_ENCOUNTERS=LotroCoreConfig.getInstance().getFile(DataFiles.PRIVATE_ENCOUNTERS);
  /**
   * Instances tree.
   */
  public static final File INSTANCES_TREE=LotroCoreConfig.getInstance().getFile(DataFiles.INSTANCES_TREE);
  /**
   * Instances loots.
   */
  public static final File INSTANCES_LOOTS=LotroCoreConfig.getInstance().getFile(DataFiles.INSTANCE_LOOTS);
  /**
   * Mobs.
   */
  public static final File MOBS=LotroCoreConfig.getInstance().getFile(DataFiles.MOBS);
  /**
   * Generic mob loot.
   */
  public static final File GENERIC_MOB_LOOTS=LotroCoreConfig.getInstance().getFile(DataFiles.GENERIC_MOB_LOOTS);
  /**
   * Resources maps.
   */
  public static final File RESOURCES_MAPS=LotroCoreConfig.getInstance().getFile(DataFiles.RESOURCES_MAPS);
  /**
   * Landblocks.
   */
  public static final File LANDBLOCKS=new File("../lotro-data/maps/landblocks.xml");
  /**
   * Paper items.
   */
  public static final File PAPER_ITEMS=LotroCoreConfig.getInstance().getFile(DataFiles.PAPER_ITEMS);
  /**
   * Tasks.
   */
  public static final File TASKS=LotroCoreConfig.getInstance().getFile(DataFiles.TASKS);
  /**
   * Enums directory.
   */
  public static final File ENUMS_DIR=LotroCoreConfig.getInstance().getFile(DataFiles.ENUMS_DIR);
}
