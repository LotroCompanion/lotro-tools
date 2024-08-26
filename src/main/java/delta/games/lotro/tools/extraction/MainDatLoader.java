package delta.games.lotro.tools.extraction;

import java.io.File;
import java.util.Locale;

import org.apache.log4j.Logger;

import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.io.xml.TreasureXMLWriter;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.hash.KnownVariablesManager;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.extraction.achievables.MainDatAchievablesLoader;
import delta.games.lotro.tools.extraction.achievables.rewards.DatRewardsLoader;
import delta.games.lotro.tools.extraction.achievables.tasks.MainTaskDataBuilder;
import delta.games.lotro.tools.extraction.agents.mobs.MainDatGenericMobLootLoader;
import delta.games.lotro.tools.extraction.agents.mobs.MainDatMobsLoader;
import delta.games.lotro.tools.extraction.agents.npcs.MainDatNPCsLoader;
import delta.games.lotro.tools.extraction.allegiances.MainDatAllegiancesLoader;
import delta.games.lotro.tools.extraction.characters.CharacterClassDataLoader;
import delta.games.lotro.tools.extraction.characters.InitialGearLoader;
import delta.games.lotro.tools.extraction.characters.MainCharacterDataLoader;
import delta.games.lotro.tools.extraction.characters.MainStatTomesLoader;
import delta.games.lotro.tools.extraction.characters.MainTraitDataLoader;
import delta.games.lotro.tools.extraction.characters.MainXpTableLoader;
import delta.games.lotro.tools.extraction.characters.MonsterClassDataLoader;
import delta.games.lotro.tools.extraction.characters.NationalitiesLoader;
import delta.games.lotro.tools.extraction.characters.RaceDataLoader;
import delta.games.lotro.tools.extraction.collections.MainDatCollectionsLoader;
import delta.games.lotro.tools.extraction.combat.MainDatCombatLoader;
import delta.games.lotro.tools.extraction.common.MainDatColorLoader;
import delta.games.lotro.tools.extraction.common.enums.MainDatEnumsLoader;
import delta.games.lotro.tools.extraction.common.progressions.MainProgressionsMerger;
import delta.games.lotro.tools.extraction.common.stats.MainStatsLoader;
import delta.games.lotro.tools.extraction.crafting.MainDatCraftingLoader;
import delta.games.lotro.tools.extraction.crafting.MainDatRecipesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.effects.MainBuffsLoader;
import delta.games.lotro.tools.extraction.effects.mood.MainMoodDataLoader;
import delta.games.lotro.tools.extraction.emotes.MainDatEmotesLoader;
import delta.games.lotro.tools.extraction.factions.MainDatFactionsLoader;
import delta.games.lotro.tools.extraction.geo.MainDatLandmarksLoader;
import delta.games.lotro.tools.extraction.geo.areas.MainDatGeoAreasLoader;
import delta.games.lotro.tools.extraction.geo.dungeons.MainDatDungeonsLoader;
import delta.games.lotro.tools.extraction.global.MainGameDataBuilder;
import delta.games.lotro.tools.extraction.global.MainServersBuilder;
import delta.games.lotro.tools.extraction.instances.MainDatInstancesTreeLoader;
import delta.games.lotro.tools.extraction.instances.MainDatPrivateEncountersLoader;
import delta.games.lotro.tools.extraction.items.ConsumablesLoader;
import delta.games.lotro.tools.extraction.items.GenericItemEffectsLoader;
import delta.games.lotro.tools.extraction.items.MainDatContainerLoader;
import delta.games.lotro.tools.extraction.items.MainDatDisenchantmentsLoader;
import delta.games.lotro.tools.extraction.items.MainDatItemsLoader;
import delta.games.lotro.tools.extraction.items.MainDatItemsSetsLoader;
import delta.games.lotro.tools.extraction.items.MainDatPaperItemsLoader;
import delta.games.lotro.tools.extraction.items.MainWeaponDamageLoader;
import delta.games.lotro.tools.extraction.items.legendary.MainDatLegendarySystem2Loader;
import delta.games.lotro.tools.extraction.items.legendary.MainDatLegendarySystemLoader;
import delta.games.lotro.tools.extraction.items.legendary.MainDatLegendaryTitlesLoader;
import delta.games.lotro.tools.extraction.maps.PlacesLoader;
import delta.games.lotro.tools.extraction.misc.MainBillingGroupsLoader;
import delta.games.lotro.tools.extraction.misc.MainHobbiesLoader;
import delta.games.lotro.tools.extraction.misc.MainPerksLoader;
import delta.games.lotro.tools.extraction.misc.MainPropertyResponseMapsLoader;
import delta.games.lotro.tools.extraction.misc.WebStoreItemsLoader;
import delta.games.lotro.tools.extraction.pvp.MainDatPVPLoader;
import delta.games.lotro.tools.extraction.relics.MainDatRelicMeldingRecipesLoader;
import delta.games.lotro.tools.extraction.relics.MainDatRelicsLoader;
import delta.games.lotro.tools.extraction.rewardsTrack.MainDatRewardsTracksLoader;
import delta.games.lotro.tools.extraction.skills.MainSkillDataLoader;
import delta.games.lotro.tools.extraction.titles.MainDatTitlesLoader;
import delta.games.lotro.tools.extraction.trade.MainDatTradeLoader;
import delta.games.lotro.tools.extraction.ui.SocketIconsLoader;
import delta.games.lotro.tools.reports.ReferenceDataGenerator;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

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
    boolean live=LotroCoreConfig.isLive();
    // Game
    new MainGameDataBuilder().doIt();
    // Servers
    new MainServersBuilder().doIt();
    // Stats
    new MainStatsLoader(_facade).doIt();
    // Colors
    new MainDatColorLoader(_facade).doIt();
    // Enums
    new MainDatEnumsLoader(_facade).doIt();
    // XP
    new MainXpTableLoader(_facade).doIt();
    // PVP
    new MainDatPVPLoader(_facade).doIt();
    // Mood
    new MainMoodDataLoader(_facade).doIt();
    // Geo
    new MainDatLandmarksLoader(_facade).doIt();
    // Combat data
    new MainDatCombatLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Weapon damage
    new MainWeaponDamageLoader(_facade).doIt();
    // Geographic areas
    new MainDatGeoAreasLoader(_facade).doIt();
    // Dungeons
    MainDatDungeonsLoader dungeonsLoader=new MainDatDungeonsLoader(_facade);
    dungeonsLoader.doIt();
    dungeonsLoader.loadPositions();
    // Skills
    MainSkillDataLoader skillsLoader=new MainSkillDataLoader(_facade);
    skillsLoader.doIt();
    // Places
    PlacesLoader placesLoader=new PlacesLoader(_facade);
    // Effects
    EffectLoader effectsLoader=new EffectLoader(_facade,placesLoader);
    // Traits
    new MainTraitDataLoader(_facade,effectsLoader).doIt();
    // Emotes
    new MainDatEmotesLoader(_facade).doIt();
    // Stat tomes
    new MainStatTomesLoader(_facade).doIt();
    // Character class data
    new CharacterClassDataLoader(_facade).doIt();
    // Monster class data
    new MonsterClassDataLoader(_facade).doIt();
    // Nationalities
    new NationalitiesLoader(_facade).doIt();
    // Race data
    new RaceDataLoader(_facade).doIt();
    // Titles
    new MainDatTitlesLoader(_facade).doIt();
    // Factions
    FactionsRegistry factionsRegistry=new MainDatFactionsLoader(_facade).doIt();
    // Crafting
    MainDatCraftingLoader craftingLoader=new MainDatCraftingLoader(_facade);
    craftingLoader.doIt();
    // Items
    if (live)
    {
      new SocketIconsLoader(_facade).doIt();
    }
    skillsLoader.loadRequirements(effectsLoader);
    skillsLoader.loadEffects(effectsLoader);
    new GenericItemEffectsLoader(_facade,effectsLoader).doIt();
    // Items
    new MainDatItemsLoader(_facade,effectsLoader).doIt();
    new MainProgressionsMerger().doIt();
    // Web Store
    new WebStoreItemsLoader(_facade).doIt();
    // Character data
    new MainCharacterDataLoader(_facade).doIt();
    // Items sets
    new MainDatItemsSetsLoader(_facade,effectsLoader).doIt();
    // Property Response maps
    new MainPropertyResponseMapsLoader(_facade,effectsLoader).doIt();
    // Perks
    new MainPerksLoader(_facade,effectsLoader).doIt();
    // Save effects
    effectsLoader.save();
    new MainProgressionsMerger().doIt();
    // Initial gear
    new InitialGearLoader(_facade).doIt();
    LootsManager lootsManager=new LootsManager();
    if (live)
    {
      // Paper items
      new MainDatPaperItemsLoader(_facade).doIt();
      // Legendary data
      new MainDatLegendarySystemLoader(_facade).doIt();
      new MainDatLegendarySystem2Loader(_facade).doIt();
      // Legendary titles
      new MainDatLegendaryTitlesLoader(_facade).doIt();
      // Relics
      new MainDatRelicsLoader(_facade).doIt();
    }
    // Recipes
    new MainDatRecipesLoader(_facade).doIt();
    craftingLoader.updateRecipeIcons();
    // Private encounters
    MainDatPrivateEncountersLoader peLoader=new MainDatPrivateEncountersLoader(_facade);
    peLoader.doIt();
    // Containers
    new MainDatContainerLoader(_facade,lootsManager).doIt();
    // Mobs
    new MainDatMobsLoader(_facade,lootsManager).doIt();
    // NPCs
    new MainDatNPCsLoader(_facade).doIt();
    // Disenchantment
    if (live)
    {
      new MainDatDisenchantmentsLoader(_facade,lootsManager).doIt();
    }
    // Mobs loot
    new MainDatGenericMobLootLoader(_facade,lootsManager).doIt();
    // Save loots
    lootsManager.dump(System.out);
    // Write loot data
    TreasureXMLWriter.writeLootsFile(GeneratedFiles.LOOTS,lootsManager);
    // Quests and deeds
    DatRewardsLoader rewardsLoader=new DatRewardsLoader(_facade);
    new MainDatAchievablesLoader(_facade,rewardsLoader).doIt();
    new MainProgressionsMerger().doIt();
    // Associate deeds to faction levels
    MainDatFactionsLoader.associateDeeds(factionsRegistry);
    // Private encounter loader: resolve proxies
    peLoader.finish();
    // Buffs
    new MainBuffsLoader().doIt();
    if (live)
    {
      // Collections
      new MainDatCollectionsLoader(_facade,rewardsLoader).doIt();
    }
    // Vendors & barterers
    new MainDatTradeLoader(_facade).doIt();
    // Instances tree
    if (live)
    {
      new MainDatInstancesTreeLoader(_facade).doIt();
    }
    // Merge progressions
    new MainProgressionsMerger().doIt();
    // Reference data
    new ReferenceDataGenerator(_facade).doIt();
    // Tasks data
    new MainTaskDataBuilder().doIt();
    if (live)
    {
      // Relics melding recipes
      new MainDatRelicMeldingRecipesLoader(_facade).doIt();
      // Allegiances
      new MainDatAllegiancesLoader(_facade).doIt();
    }
    // Billing groups
    new MainBillingGroupsLoader(_facade).doIt();
    if (live)
    {
      // Rewards tracks
      new MainDatRewardsTracksLoader(_facade).doIt();
      // Hobbies
      new MainHobbiesLoader(_facade).doIt();
    }
    // Consumables
    new ConsumablesLoader().doIt();
  }

  private void cleanup()
  {
    // Commons
    deleteFile(GeneratedFiles.GAME_DATA);
    deleteFile(GeneratedFiles.STATS);
    deleteFile(GeneratedFiles.COLORS);
    deleteFile(GeneratedFiles.COMBAT_DATA);
    deleteDirectory(GeneratedFiles.ENUMS_DIR);
    deleteFile(GeneratedFiles.XP_TABLE);
    deleteFile(GeneratedFiles.PVP);
    deleteFile(GeneratedFiles.MOOD);
    // Labels
    // Do not delete the labels directory because it is shared with the geo data loader
    // deleteDirectory(GeneratedFiles.LABELS)
    // Geo
    deleteFile(GeneratedFiles.LANDMARKS);
    // Dungeons
    deleteFile(GeneratedFiles.DUNGEONS);
    // Areas
    deleteFile(GeneratedFiles.GEO_AREAS);
    deleteDirectory(GeneratedFiles.AREA_ICONS);
    // Character data
    deleteFile(GeneratedFiles.STAT_CONTRIBS);
    deleteFile(GeneratedFiles.START_STATS);
    deleteFile(GeneratedFiles.CLASSES);
    deleteFile(GeneratedFiles.MONSTER_CLASSES);
    deleteDirectory(GeneratedFiles.CLASS_ICONS_DIR);
    deleteFile(GeneratedFiles.TRAIT_TREES);
    deleteFile(GeneratedFiles.INITIAL_GEAR);
    deleteFile(GeneratedFiles.RACES);
    deleteDirectory(GeneratedFiles.RACE_ICONS_DIR);
    deleteFile(GeneratedFiles.NATIONALITIES);
    // - skills
    deleteFile(GeneratedFiles.SKILLS);
    deleteDirectory(GeneratedFiles.SKILL_ICONS_DIR);
    // - virtues
    deleteFile(GeneratedFiles.VIRTUES);
    // - traits
    deleteFile(GeneratedFiles.TRAITS);
    deleteDirectory(GeneratedFiles.TRAIT_ICONS_DIR);
    deleteFile(GeneratedFiles.SKIRMISH_TRAITS);
    // - stat tomes
    deleteFile(GeneratedFiles.STAT_TOMES);
    // Titles
    deleteFile(GeneratedFiles.TITLES);
    deleteDirectory(GeneratedFiles.TITLE_ICONS);
    // Items
    deleteFile(GeneratedFiles.GENERIC_ITEM_EFFECTS);
    deleteFile(GeneratedFiles.ITEMS);
    deleteDirectory(GeneratedFiles.ITEM_ICONS_DIR);
    deleteDirectory(GeneratedFiles.ITEM_LARGE_ICONS_DIR);
    deleteDirectory(GeneratedFiles.SOCKET_ICONS_DIR);
    deleteFile(GeneratedFiles.PASSIVES);
    deleteFile(GeneratedFiles.PASSIVES_USAGE);
    deleteFile(GeneratedFiles.CONSUMABLES);
    deleteFile(GeneratedFiles.PAPER_ITEMS);
    deleteFile(GeneratedFiles.ITEM_COSMETICS);
    deleteFile(GeneratedFiles.VALUE_TABLES);
    deleteFile(GeneratedFiles.DPS_TABLES);
    deleteFile(GeneratedFiles.SPEED_TABLES);
    deleteFile(GeneratedFiles.WEAPON_DAMAGE);
    // - legacies
    deleteFile(GeneratedFiles.LEGACIES);
    deleteFile(GeneratedFiles.NON_IMBUED_LEGACIES);
    deleteDirectory(GeneratedFiles.LEGACIES_ICONS);
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
    // Effects
    deleteFile(GeneratedFiles.EFFECTS);
    deleteDirectory(GeneratedFiles.EFFECT_ICONS_DIR);
    // Collections
    deleteFile(GeneratedFiles.COLLECTIONS);
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
    // NPCs
    deleteFile(GeneratedFiles.NPCS);
    // Misc icons
    deleteDirectory(GeneratedFiles.MISC_ICONS);
    // Allegiances
    deleteFile(GeneratedFiles.ALLEGIANCES);
    deleteDirectory(GeneratedFiles.ALLEGIANCES_ICONS);
    // Billing groups
    deleteFile(GeneratedFiles.BILLING_GROUPS);
    // Hobbies
    deleteFile(GeneratedFiles.HOBBIES);
    deleteDirectory(GeneratedFiles.HOBBY_ICONS);
    // Perks
    deleteFile(GeneratedFiles.PERKS);
    deleteDirectory(GeneratedFiles.PERK_ICONS);
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
    deleteFile(GeneratedFiles.PROGRESSIONS_LEGENDARY);
    deleteFile(GeneratedFiles.PROGRESSIONS_EFFECTS);
    deleteFile(GeneratedFiles.PROGRESSIONS);
  }

  private void deleteFile(File toDelete)
  {
    if (toDelete==null)
    {
      LOGGER.warn("Cannot delete null file!");
      return;
    }
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
    if (toDelete==null)
    {
      LOGGER.warn("Cannot delete null directory!");
      return;
    }
    FilesDeleter deleter=new FilesDeleter(toDelete,null,true);
    deleter.doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    new MainDatLoader(facade).doIt();
    facade.dispose();
  }
}
