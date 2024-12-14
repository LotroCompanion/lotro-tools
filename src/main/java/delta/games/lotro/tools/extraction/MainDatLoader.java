package delta.games.lotro.tools.extraction;

import java.util.Locale;

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
import delta.games.lotro.tools.extraction.characters.VirtueDataLoader;
import delta.games.lotro.tools.extraction.collections.MainDatCollectionsLoader;
import delta.games.lotro.tools.extraction.combat.MainDatCombatLoader;
import delta.games.lotro.tools.extraction.common.MainDatColorLoader;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.common.enums.MainDatEnumsLoader;
import delta.games.lotro.tools.extraction.common.progressions.MainProgressionsMerger;
import delta.games.lotro.tools.extraction.common.stats.MainStatsLoader;
import delta.games.lotro.tools.extraction.crafting.MainDatCraftingLoader;
import delta.games.lotro.tools.extraction.crafting.MainDatRecipesLoader;
import delta.games.lotro.tools.extraction.effects.AdditionalEffectsLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.effects.HotspotEffectsLoader;
import delta.games.lotro.tools.extraction.effects.MainBuffsLoader;
import delta.games.lotro.tools.extraction.effects.SomeMoreEffectsLoader;
import delta.games.lotro.tools.extraction.effects.mood.MainMoodDataLoader;
import delta.games.lotro.tools.extraction.emotes.MainDatEmotesLoader;
import delta.games.lotro.tools.extraction.factions.MainDatFactionsLoader;
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
import delta.games.lotro.tools.extraction.misc.MainBillingGroupsLoader;
import delta.games.lotro.tools.extraction.misc.MainHobbiesLoader;
import delta.games.lotro.tools.extraction.misc.MainPerksLoader;
import delta.games.lotro.tools.extraction.misc.MainPropertyResponseMapsLoader;
import delta.games.lotro.tools.extraction.misc.PropertyResponseMapsLoader;
import delta.games.lotro.tools.extraction.misc.WebStoreItemsLoader;
import delta.games.lotro.tools.extraction.pvp.MainDatPVPLoader;
import delta.games.lotro.tools.extraction.relics.MainDatRelicMeldingRecipesLoader;
import delta.games.lotro.tools.extraction.relics.MainDatRelicsLoader;
import delta.games.lotro.tools.extraction.rewardsTrack.MainDatRewardsTracksLoader;
import delta.games.lotro.tools.extraction.skills.MainSkillDataLoader;
import delta.games.lotro.tools.extraction.titles.MainDatTitlesLoader;
import delta.games.lotro.tools.extraction.trade.MainDatTradeLoader;
import delta.games.lotro.tools.extraction.ui.SocketIconsLoader;
import delta.games.lotro.tools.extraction.utils.CleanupUtils;
import delta.games.lotro.tools.reports.ReferenceDataGenerator;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Global procedure to load data from DAT files.
 * @author DAM
 */
public class MainDatLoader
{
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
    // Combat data
    new MainDatCombatLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Weapon damage
    new MainWeaponDamageLoader(_facade).doIt();
    // Places
    PlacesLoader placesLoader=new PlacesLoader(_facade);
    // Effects
    EffectLoader effectsLoader=new EffectLoader(_facade,placesLoader);
    // Skills
    MainSkillDataLoader skillsLoader=new MainSkillDataLoader(_facade,effectsLoader);
    skillsLoader.doIt();
    // Traits
    new MainTraitDataLoader(_facade,effectsLoader).doIt();
    // Skills (complements)
    skillsLoader.loadRequirements();
    new MainProgressionsMerger().doIt();
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
    new GenericItemEffectsLoader(_facade,effectsLoader).doIt();
    // Virtues
    new VirtueDataLoader(_facade,effectsLoader).doIt();
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
    PropertyResponseMapsLoader propertyResponseMapsLoader=new PropertyResponseMapsLoader(_facade,effectsLoader);
    new MainPropertyResponseMapsLoader(_facade,propertyResponseMapsLoader).doIt();
    // Perks
    new MainPerksLoader(_facade,effectsLoader).doIt();
    // Yet some other effects
    new AdditionalEffectsLoader(effectsLoader).doIt();
    new SomeMoreEffectsLoader(_facade,effectsLoader).doIt();
    new HotspotEffectsLoader(_facade,effectsLoader).doIt();
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
    MainDatPrivateEncountersLoader peLoader=new MainDatPrivateEncountersLoader(_facade,propertyResponseMapsLoader);
    peLoader.doIt();
    // NPCs
    new MainDatNPCsLoader(_facade,effectsLoader).doIt();
    // Save effects
    effectsLoader.save();
    // Containers
    new MainDatContainerLoader(_facade,lootsManager).doIt();
    // Mobs
    new MainDatMobsLoader(_facade,lootsManager).doIt();
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
    CleanupUtils.deleteFile(GeneratedFiles.GAME_DATA);
    CleanupUtils.deleteFile(GeneratedFiles.STATS);
    CleanupUtils.deleteFile(GeneratedFiles.COLORS);
    CleanupUtils.deleteFile(GeneratedFiles.COMBAT_DATA);
    CleanupUtils.deleteDirectory(GeneratedFiles.ENUMS_DIR);
    CleanupUtils.deleteFile(GeneratedFiles.XP_TABLE);
    CleanupUtils.deleteFile(GeneratedFiles.PVP);
    CleanupUtils.deleteFile(GeneratedFiles.MOOD);
    CleanupUtils.deleteFile(GeneratedFiles.INDUCTIONS);
    // Labels
    // Do not delete the labels directory because it is shared with the geo data loader
    // CleanupUtils.deleteDirectory(GeneratedFiles.LABELS)
    // Character data
    CleanupUtils.deleteFile(GeneratedFiles.STAT_CONTRIBS);
    CleanupUtils.deleteFile(GeneratedFiles.START_STATS);
    CleanupUtils.deleteFile(GeneratedFiles.CLASSES);
    CleanupUtils.deleteFile(GeneratedFiles.MONSTER_CLASSES);
    CleanupUtils.deleteDirectory(GeneratedFiles.CLASS_ICONS_DIR);
    CleanupUtils.deleteFile(GeneratedFiles.TRAIT_TREES);
    CleanupUtils.deleteFile(GeneratedFiles.INITIAL_GEAR);
    CleanupUtils.deleteFile(GeneratedFiles.RACES);
    CleanupUtils.deleteDirectory(GeneratedFiles.RACE_ICONS_DIR);
    CleanupUtils.deleteFile(GeneratedFiles.NATIONALITIES);
    // - skills
    CleanupUtils.deleteFile(GeneratedFiles.SKILLS);
    CleanupUtils.deleteDirectory(GeneratedFiles.SKILL_ICONS_DIR);
    // - virtues
    CleanupUtils.deleteFile(GeneratedFiles.VIRTUES);
    // - traits
    CleanupUtils.deleteFile(GeneratedFiles.TRAITS);
    CleanupUtils.deleteDirectory(GeneratedFiles.TRAIT_ICONS_DIR);
    CleanupUtils.deleteFile(GeneratedFiles.SKIRMISH_TRAITS);
    // - stat tomes
    CleanupUtils.deleteFile(GeneratedFiles.STAT_TOMES);
    // Titles
    CleanupUtils.deleteFile(GeneratedFiles.TITLES);
    CleanupUtils.deleteDirectory(GeneratedFiles.TITLE_ICONS);
    // Items
    CleanupUtils.deleteFile(GeneratedFiles.GENERIC_ITEM_EFFECTS);
    CleanupUtils.deleteFile(GeneratedFiles.ITEMS);
    CleanupUtils.deleteDirectory(GeneratedFiles.ITEM_ICONS_DIR);
    CleanupUtils.deleteDirectory(GeneratedFiles.ITEM_LARGE_ICONS_DIR);
    CleanupUtils.deleteDirectory(GeneratedFiles.SOCKET_ICONS_DIR);
    CleanupUtils.deleteFile(GeneratedFiles.PASSIVES);
    CleanupUtils.deleteFile(GeneratedFiles.PASSIVES_USAGE);
    CleanupUtils.deleteFile(GeneratedFiles.CONSUMABLES);
    CleanupUtils.deleteFile(GeneratedFiles.PAPER_ITEMS);
    CleanupUtils.deleteFile(GeneratedFiles.ITEM_COSMETICS);
    CleanupUtils.deleteFile(GeneratedFiles.VALUE_TABLES);
    CleanupUtils.deleteFile(GeneratedFiles.DPS_TABLES);
    CleanupUtils.deleteFile(GeneratedFiles.SPEED_TABLES);
    CleanupUtils.deleteFile(GeneratedFiles.WEAPON_DAMAGE);
    // - legacies
    CleanupUtils.deleteFile(GeneratedFiles.LEGACIES);
    CleanupUtils.deleteFile(GeneratedFiles.NON_IMBUED_LEGACIES);
    CleanupUtils.deleteDirectory(GeneratedFiles.LEGACIES_ICONS);
    // Items sets
    CleanupUtils.deleteFile(GeneratedFiles.SETS);
    // Legendary system
    CleanupUtils.deleteFile(GeneratedFiles.LEGENDARY_DATA);
    // Legendary system (reloaded)
    CleanupUtils.deleteFile(GeneratedFiles.LEGENDARY_DATA2);
    CleanupUtils.deleteFile(GeneratedFiles.LEGENDARY_ATTRS);
    CleanupUtils.deleteFile(GeneratedFiles.TRACERIES);
    CleanupUtils.deleteFile(GeneratedFiles.ENHANCEMENT_RUNES);
    // Legendary titles
    CleanupUtils.deleteFile(GeneratedFiles.LEGENDARY_TITLES);
    // Relics
    CleanupUtils.deleteFile(GeneratedFiles.RELICS);
    CleanupUtils.deleteFile(GeneratedFiles.RELIC_MELDING_RECIPES);
    CleanupUtils.deleteDirectory(GeneratedFiles.RELIC_ICONS_DIR);
    // Recipes
    CleanupUtils.deleteFile(GeneratedFiles.RECIPES);
    // Emotes
    CleanupUtils.deleteFile(GeneratedFiles.EMOTES);
    CleanupUtils.deleteDirectory(GeneratedFiles.EMOTE_ICONS_DIR);
    // Factions
    CleanupUtils.deleteFile(GeneratedFiles.FACTIONS);
    // Quests and deeds
    CleanupUtils.deleteFile(GeneratedFiles.QUESTS);
    CleanupUtils.deleteFile(GeneratedFiles.DEEDS);
    CleanupUtils.deleteFile(GeneratedFiles.TASKS);
    // Crafting
    CleanupUtils.deleteFile(GeneratedFiles.CRAFTING_DATA);
    // Buffs
    CleanupUtils.deleteFile(GeneratedFiles.BUFFS);
    // Effects
    CleanupUtils.deleteFile(GeneratedFiles.EFFECTS);
    CleanupUtils.deleteDirectory(GeneratedFiles.EFFECT_ICONS_DIR);
    // Collections
    CleanupUtils.deleteFile(GeneratedFiles.COLLECTIONS);
    // Vendors
    CleanupUtils.deleteFile(GeneratedFiles.VENDORS);
    // Barterers
    CleanupUtils.deleteFile(GeneratedFiles.BARTERS);
    // Instances
    CleanupUtils.deleteFile(GeneratedFiles.PRIVATE_ENCOUNTERS);
    CleanupUtils.deleteFile(GeneratedFiles.INSTANCES_TREE);
    // Containers
    CleanupUtils.deleteFile(GeneratedFiles.CONTAINERS);
    // Loot tables
    CleanupUtils.deleteFile(GeneratedFiles.LOOTS);
    CleanupUtils.deleteFile(GeneratedFiles.GENERIC_MOB_LOOTS);
    CleanupUtils.deleteFile(GeneratedFiles.INSTANCES_LOOTS);
    // Disenchantment
    CleanupUtils.deleteFile(GeneratedFiles.DISENCHANTMENTS);
    // Mobs
    CleanupUtils.deleteFile(GeneratedFiles.MOBS);
    // NPCs
    CleanupUtils.deleteFile(GeneratedFiles.NPCS);
    // Misc icons
    CleanupUtils.deleteDirectory(GeneratedFiles.MISC_ICONS);
    // Allegiances
    CleanupUtils.deleteFile(GeneratedFiles.ALLEGIANCES);
    CleanupUtils.deleteDirectory(GeneratedFiles.ALLEGIANCES_ICONS);
    // Billing groups
    CleanupUtils.deleteFile(GeneratedFiles.BILLING_GROUPS);
    // Hobbies
    CleanupUtils.deleteFile(GeneratedFiles.HOBBIES);
    CleanupUtils.deleteDirectory(GeneratedFiles.HOBBY_ICONS);
    // Perks
    CleanupUtils.deleteFile(GeneratedFiles.PERKS);
    CleanupUtils.deleteDirectory(GeneratedFiles.PERK_ICONS);
    // Misc
    CleanupUtils.deleteFile(GeneratedFiles.WEB_STORE_ITEMS);
    CleanupUtils.deleteFile(GeneratedFiles.WORLD_EVENTS);
    CleanupUtils.deleteFile(GeneratedFiles.REWARDS_TRACKS);

    // Progressions
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_COMBAT);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_ITEMS_SETS);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_LEGENDARY);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_EFFECTS);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS_SKILLS);
    CleanupUtils.deleteFile(GeneratedFiles.PROGRESSIONS);
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
