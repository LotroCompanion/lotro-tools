package delta.games.lotro.tools.dat;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.hash.KnownVariablesManager;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.dat.agents.mobs.MainDatGenericMobLootLoader;
import delta.games.lotro.tools.dat.agents.mobs.MainDatMobsLoader;
import delta.games.lotro.tools.dat.agents.npcs.MainDatNpcLoader;
import delta.games.lotro.tools.dat.allegiances.MainDatAllegiancesLoader;
import delta.games.lotro.tools.dat.characters.MainCharacterDataLoader;
import delta.games.lotro.tools.dat.characters.MainSkillDataLoader;
import delta.games.lotro.tools.dat.characters.MainStatTomesLoader;
import delta.games.lotro.tools.dat.characters.MainTraitDataLoader;
import delta.games.lotro.tools.dat.characters.RaceDataLoader;
import delta.games.lotro.tools.dat.collections.MainDatCollectionsLoader;
import delta.games.lotro.tools.dat.combat.MainDatCombatLoader;
import delta.games.lotro.tools.dat.crafting.MainDatCraftingLoader;
import delta.games.lotro.tools.dat.crafting.MainDatRecipesLoader;
import delta.games.lotro.tools.dat.emotes.MainDatEmotesLoader;
import delta.games.lotro.tools.dat.factions.MainDatFactionsLoader;
import delta.games.lotro.tools.dat.instances.MainDatInstancesTreeLoader;
import delta.games.lotro.tools.dat.instances.MainDatPrivateEncountersLoader;
import delta.games.lotro.tools.dat.items.MainDatDisenchantmentsLoader;
import delta.games.lotro.tools.dat.items.MainDatItemsLoader;
import delta.games.lotro.tools.dat.items.MainDatItemsSetsLoader;
import delta.games.lotro.tools.dat.items.MainDatPaperItemsLoader;
import delta.games.lotro.tools.dat.items.legendary.LegaciesLoader;
import delta.games.lotro.tools.dat.items.legendary.MainDatLegendarySystem2Loader;
import delta.games.lotro.tools.dat.items.legendary.MainDatLegendarySystemLoader;
import delta.games.lotro.tools.dat.items.legendary.MainDatLegendaryTitlesLoader;
import delta.games.lotro.tools.dat.misc.MainBillingGroupsLoader;
import delta.games.lotro.tools.dat.misc.MainBuffsLoader;
import delta.games.lotro.tools.dat.misc.MainDatColorLoader;
import delta.games.lotro.tools.dat.misc.MainDatEnumsLoader;
import delta.games.lotro.tools.dat.misc.MainHobbiesLoader;
import delta.games.lotro.tools.dat.misc.MainStatsLoader;
import delta.games.lotro.tools.dat.misc.MiscIconsManager;
import delta.games.lotro.tools.dat.others.CosmeticPetLoader;
import delta.games.lotro.tools.dat.others.MountsLoader;
import delta.games.lotro.tools.dat.others.boxes.MainDatContainerLoader;
import delta.games.lotro.tools.dat.quests.DatRewardsLoader;
import delta.games.lotro.tools.dat.quests.MainDatAchievablesLoader;
import delta.games.lotro.tools.dat.relics.MainDatRelicMeldingRecipesLoader;
import delta.games.lotro.tools.dat.relics.MainDatRelicsLoader;
import delta.games.lotro.tools.dat.rewardsTrack.MainDatRewardsTracksLoader;
import delta.games.lotro.tools.dat.titles.MainDatTitlesLoader;
import delta.games.lotro.tools.dat.traitPoints.TraitPointsRegistryBuilder;
import delta.games.lotro.tools.lore.MainServersBuilder;
import delta.games.lotro.tools.lore.tasks.MainTaskDataBuilder;
import delta.games.lotro.tools.reports.ReferenceDataGenerator;

/**
 * Global procedure to load data from DAT files.
 * @author DAM
 */
public class MainDatLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void doIt()
  {
    cleanup();
    load();
    KnownVariablesManager.getInstance().showFailures();
  }

  private void load()
  {
    // Servers
    new MainServersBuilder().doIt();
    // Stats
    new MainStatsLoader(_facade).doIt();
    // Colors
    new MainDatColorLoader(_facade).doIt();
    // Enums
    new MainDatEnumsLoader(_facade).doIt();
    // Combat data
    new MainDatCombatLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Skills
    new MainSkillDataLoader(_facade).doIt();
    // Traits
    new MainTraitDataLoader(_facade).doIt();
    // Emotes
    new MainDatEmotesLoader(_facade).doIt();
    // Stat tomes
    new MainStatTomesLoader(_facade).doIt();
    // Race data
    new RaceDataLoader(_facade).doIt();
    // Titles
    new MainDatTitlesLoader(_facade).doIt();
    // Factions
    new MainDatFactionsLoader(_facade).doIt();
    // Items
    new MainDatItemsLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Character data
    new MainCharacterDataLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Items sets
    new MainDatItemsSetsLoader(_facade).doIt();
    // Paper items
    new MainDatPaperItemsLoader(_facade).doIt();
    // Legendary data
    new MainDatLegendarySystemLoader(_facade).doIt();
    new MainDatLegendarySystem2Loader(_facade).doIt();
    // Legendary titles
    new MainDatLegendaryTitlesLoader(_facade).doIt();
    // Relics
    new MainDatRelicsLoader(_facade).doIt();
    // Crafting
    new MainDatCraftingLoader(_facade).doIt();
    // Recipes
    new MainDatRecipesLoader(_facade).doIt();
    // Quests and deeds
    DatRewardsLoader rewardsLoader=new DatRewardsLoader(_facade);
    new MainDatAchievablesLoader(_facade,rewardsLoader).doIt();
    new MainProgressionsMerger().doIt();
    // Associate deeds to faction levels
    MainDatFactionsLoader.associateDeeds(FactionsRegistry.getInstance());
    // Buffs
    new MainBuffsLoader(_facade).doIt();
    // Trait points
    new TraitPointsRegistryBuilder().doIt();
    // Mounts
    new MountsLoader(_facade).doIt();
    // Cosmetic pets
    new CosmeticPetLoader(_facade).doIt();
    // Collections
    new MainDatCollectionsLoader(_facade,rewardsLoader).doIt();
    // Vendors & barterers
    new MainDatNpcLoader(_facade).doIt();
    // Private encounters
    new MainDatPrivateEncountersLoader(_facade).doIt();
    // Instances tree
    new MainDatInstancesTreeLoader(_facade).doIt();
    // Containers
    new MainDatContainerLoader(_facade).doIt();
    // Disenchantment
    new MainDatDisenchantmentsLoader(_facade).doIt();
    // Mobs
    new MainDatMobsLoader(_facade,LootsManager.getInstance()).doIt();
    new MainDatGenericMobLootLoader(_facade,LootsManager.getInstance()).doIt();
    // Merge progressions
    new MainProgressionsMerger().doIt();
    // Reference data
    new ReferenceDataGenerator().doIt();
    // Tasks data
    new MainTaskDataBuilder().doIt();
    // Relics melding recipes
    new MainDatRelicMeldingRecipesLoader(_facade).doIt();
    // Allegiances
    new MainDatAllegiancesLoader(_facade).doIt();
    // Billing groups
    new MainBillingGroupsLoader(_facade).doIt();
    // Rewards tracks
    new MainDatRewardsTracksLoader(_facade).doIt();
    // Hobbies
    new MainHobbiesLoader(_facade).doIt();
  }

  private void cleanup()
  {
    // Commons
    deleteFile(GeneratedFiles.STATS);
    deleteFile(GeneratedFiles.COLORS);
    deleteFile(GeneratedFiles.COMBAT_DATA);
    deleteDirectory(GeneratedFiles.ENUMS_DIR);
    // Character data
    deleteFile(GeneratedFiles.STAT_CONTRIBS);
    deleteFile(GeneratedFiles.START_STATS);
    deleteFile(GeneratedFiles.CLASSES);
    deleteFile(GeneratedFiles.RACES);
    deleteFile(GeneratedFiles.NATIONALITIES);
    // - skills
    deleteFile(GeneratedFiles.SKILLS);
    deleteDirectory(GeneratedFiles.SKILL_ICONS_DIR);
    // - virtues
    deleteFile(GeneratedFiles.VIRTUES);
    // - traits
    deleteFile(GeneratedFiles.TRAITS);
    deleteDirectory(GeneratedFiles.TRAIT_ICONS_DIR);
    // - stat tomes
    deleteFile(GeneratedFiles.STAT_TOMES);
    // Titles
    deleteFile(GeneratedFiles.TITLES);
    deleteFile(GeneratedFiles.TITLE_ICONS);
    deleteDirectory(MainDatTitlesLoader.TITLE_ICONS_DIR);
    // Items
    deleteFile(GeneratedFiles.ITEMS);
    deleteDirectory(GeneratedFiles.ITEM_ICONS_DIR);
    deleteDirectory(GeneratedFiles.ITEM_LARGE_ICONS_DIR);
    deleteFile(GeneratedFiles.PASSIVES);
    deleteFile(GeneratedFiles.PASSIVES_USAGE);
    deleteFile(GeneratedFiles.CONSUMABLES);
    deleteFile(GeneratedFiles.PAPER_ITEMS);
    deleteFile(GeneratedFiles.ITEM_COSMETICS);
    deleteFile(GeneratedFiles.VALUE_TABLES);
    // - legacies
    deleteFile(GeneratedFiles.LEGACIES);
    deleteFile(GeneratedFiles.NON_IMBUED_LEGACIES);
    deleteFile(GeneratedFiles.LEGACIES_ICONS);
    deleteDirectory(LegaciesLoader.LEGACIES_ICONS_DIR);
    // Items sets
    deleteFile(GeneratedFiles.SETS);
    // Legendary system
    deleteFile(GeneratedFiles.LEGENDARY_DATA);
    // Legendary system (reloaded)
    deleteFile(GeneratedFiles.LEGENDARY_DATA2);
    deleteFile(GeneratedFiles.LEGENDARY_ATTRS);
    deleteFile(GeneratedFiles.TRACERIES);
    deleteFile(GeneratedFiles.ENHANCEMENT_RUNES);
    // Legendary titles
    deleteFile(GeneratedFiles.LEGENDARY_TITLES);
    // Relics
    deleteFile(GeneratedFiles.RELICS);
    deleteFile(GeneratedFiles.RELIC_MELDING_RECIPES);
    deleteDirectory(GeneratedFiles.RELIC_ICONS_DIR);
    // Recipes
    deleteFile(GeneratedFiles.RECIPES);
    // Emotes
    deleteFile(GeneratedFiles.EMOTES);
    deleteDirectory(GeneratedFiles.EMOTE_ICONS_DIR);
    // Factions
    deleteFile(GeneratedFiles.FACTIONS);
    // Quests and deeds
    deleteFile(GeneratedFiles.QUESTS);
    deleteFile(GeneratedFiles.DEEDS);
    deleteFile(GeneratedFiles.TASKS);
    // Crafting
    deleteFile(GeneratedFiles.CRAFTING_DATA);
    // Buffs
    deleteFile(GeneratedFiles.BUFFS);
    deleteFile(GeneratedFiles.EFFECT_ICONS);
    // Trait points
    deleteFile(GeneratedFiles.TRAIT_POINTS);
    // Collections
    deleteFile(GeneratedFiles.COLLECTIONS);
    // Mounts
    deleteFile(GeneratedFiles.MOUNTS);
    // Cosmetic pets
    deleteFile(GeneratedFiles.PETS);
    // Vendors
    deleteFile(GeneratedFiles.VENDORS);
    // Barterers
    deleteFile(GeneratedFiles.BARTERS);
    // Instances
    deleteFile(GeneratedFiles.PRIVATE_ENCOUNTERS);
    deleteFile(GeneratedFiles.INSTANCES_TREE);
    // Containers
    deleteFile(GeneratedFiles.CONTAINERS);
    // Loot tables
    deleteFile(GeneratedFiles.LOOTS);
    deleteFile(GeneratedFiles.GENERIC_MOB_LOOTS);
    deleteFile(GeneratedFiles.INSTANCES_LOOTS);
    // Disenchantment
    deleteFile(GeneratedFiles.DISENCHANTMENTS);
    // Mobs
    deleteFile(GeneratedFiles.MOBS);
    // Misc icons
    deleteFile(GeneratedFiles.MISC_ICONS);
    deleteDirectory(MiscIconsManager.MISC_ICONS_DIR);
    // Allegiances
    deleteFile(GeneratedFiles.ALLEGIANCES);
    deleteFile(GeneratedFiles.ALLEGIANCES_ICONS);
    // Billing groups
    deleteFile(GeneratedFiles.BILLING_GROUPS);
    // Hobbies
    deleteFile(GeneratedFiles.HOBBIES);
    deleteFile(GeneratedFiles.HOBBY_ICONS);
    deleteDirectory(MainHobbiesLoader.HOBBY_ICONS_DIR);
    // Misc
    deleteFile(GeneratedFiles.WEB_STORE_ITEMS);
    deleteFile(GeneratedFiles.WORLD_EVENTS);
    deleteFile(GeneratedFiles.REWARDS_TRACKS);

    // Progressions
    deleteFile(GeneratedFiles.PROGRESSIONS_COMBAT);
    deleteFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    deleteFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    deleteFile(GeneratedFiles.PROGRESSIONS_ITEMS_SETS);
    deleteFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    deleteFile(GeneratedFiles.PROGRESSIONS_BUFFS);
    deleteFile(GeneratedFiles.PROGRESSIONS_LEGENDARY);
    deleteFile(GeneratedFiles.PROGRESSIONS);
  }

  private void deleteFile(File toDelete)
  {
    if (toDelete.exists())
    {
      boolean ok=toDelete.delete();
      if (!ok)
      {
        LOGGER.warn("Could not delete file: "+toDelete);
      }
    }
  }

  private void deleteDirectory(File toDelete)
  {
    FilesDeleter deleter=new FilesDeleter(toDelete,null,true);
    deleter.doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatLoader(facade).doIt();
    facade.dispose();
  }
}
