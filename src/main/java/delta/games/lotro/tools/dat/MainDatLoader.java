package delta.games.lotro.tools.dat;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.characters.MainCharacterDataLoader;
import delta.games.lotro.tools.dat.characters.MainSkillDataLoader;
import delta.games.lotro.tools.dat.characters.MainTraitDataLoader;
import delta.games.lotro.tools.dat.characters.SkillLoader;
import delta.games.lotro.tools.dat.characters.TraitLoader;
import delta.games.lotro.tools.dat.characters.VirtueDataLoader;
import delta.games.lotro.tools.dat.combat.MainDatCombatLoader;
import delta.games.lotro.tools.dat.crafting.MainDatCraftingLoader;
import delta.games.lotro.tools.dat.crafting.MainDatRecipesLoader;
import delta.games.lotro.tools.dat.emotes.MainDatEmotesLoader;
import delta.games.lotro.tools.dat.factions.MainDatFactionsLoader;
import delta.games.lotro.tools.dat.items.MainDatDisenchantmentsLoader;
import delta.games.lotro.tools.dat.items.MainDatItemsLoader;
import delta.games.lotro.tools.dat.items.MainDatItemsSetsLoader;
import delta.games.lotro.tools.dat.items.legendary.LegaciesLoader;
import delta.games.lotro.tools.dat.items.legendary.MainDatLegendarySystemLoader;
import delta.games.lotro.tools.dat.items.legendary.MainDatLegendaryTitlesLoader;
import delta.games.lotro.tools.dat.misc.MainBuffsLoader;
import delta.games.lotro.tools.dat.misc.MainDatColorLoader;
import delta.games.lotro.tools.dat.misc.MainStatsLoader;
import delta.games.lotro.tools.dat.misc.MiscIconsManager;
import delta.games.lotro.tools.dat.npc.MainDatNpcLoader;
import delta.games.lotro.tools.dat.others.CosmeticPetLoader;
import delta.games.lotro.tools.dat.others.MountsLoader;
import delta.games.lotro.tools.dat.others.boxes.MainDatContainerLoader;
import delta.games.lotro.tools.dat.quests.MainDatAchievablesLoader;
import delta.games.lotro.tools.dat.relics.MainDatRelicsLoader;
import delta.games.lotro.tools.dat.titles.MainDatTitlesLoader;
import delta.games.lotro.tools.lore.traitPoints.TraitPointsRegistryBuilder;

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
  }

  private void load()
  {
    // Stats
    new MainStatsLoader(_facade).doIt();
    // Colors
    new MainDatColorLoader(_facade).doIt();
    // Combat data
    new MainDatCombatLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Skills
    new MainSkillDataLoader(_facade).doIt();
    // Traits
    new MainTraitDataLoader(_facade).doIt();
    // Character data
    new MainCharacterDataLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Titles
    new MainDatTitlesLoader(_facade).doIt();
    // Factions
    new MainDatFactionsLoader(_facade).doIt();
    // Items
    new MainDatItemsLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Items sets
    new MainDatItemsSetsLoader(_facade).doIt();
    // Legendary data
    new MainDatLegendarySystemLoader(_facade).doIt();
    // Legendary titles
    new MainDatLegendaryTitlesLoader(_facade).doIt();
    // Relics
    new MainDatRelicsLoader(_facade).doIt();
    // Crafting
    new MainDatCraftingLoader(_facade).doIt();
    // Recipes
    new MainDatRecipesLoader(_facade).doIt();
    // Emotes
    new MainDatEmotesLoader(_facade).doIt();
    // Quests and deeds
    new MainDatAchievablesLoader(_facade).doIt();
    new MainProgressionsMerger().doIt();
    // Factions (reloaded)
    new MainDatFactionsLoader(_facade).doIt();
    // Buffs
    new MainBuffsLoader(_facade).doIt();
    // Trait points
    new TraitPointsRegistryBuilder().doIt();
    // Mounts
    new MountsLoader(_facade).doIt();
    // Cosmetic pets
    new CosmeticPetLoader(_facade).doIt();
    // Vendors & barterers
    new MainDatNpcLoader(_facade).doIt();
    // Containers
    new MainDatContainerLoader(_facade).doIt();
    // Disenchantment
    new MainDatDisenchantmentsLoader(_facade).doIt();
    // Merge progressions
    new MainProgressionsMerger().doIt();
  }

  private void cleanup()
  {
    // Commons
    deleteFile(GeneratedFiles.STATS);
    deleteFile(GeneratedFiles.COLORS);
    deleteFile(GeneratedFiles.COMBAT_DATA);
    // Character data
    deleteFile(GeneratedFiles.STAT_CONTRIBS);
    deleteFile(GeneratedFiles.START_STATS);
    deleteFile(GeneratedFiles.CLASSES);
    deleteFile(GeneratedFiles.RACES);
    // - skills
    deleteFile(GeneratedFiles.SKILLS);
    deleteFile(GeneratedFiles.SKILL_ICONS);
    deleteDirectory(SkillLoader.SKILL_ICONS_DIR);
    // - virtues
    deleteFile(GeneratedFiles.VIRTUES);
    deleteFile(GeneratedFiles.VIRTUE_ICONS);
    deleteDirectory(VirtueDataLoader.VIRTUE_ICONS_DIR);
    // - traits
    deleteFile(GeneratedFiles.TRAITS);
    deleteFile(GeneratedFiles.TRAIT_ICONS);
    deleteDirectory(TraitLoader.TRAIT_ICONS_DIR);
    // Titles
    deleteFile(GeneratedFiles.TITLES);
    deleteFile(GeneratedFiles.TITLE_ICONS);
    deleteDirectory(MainDatTitlesLoader.TITLE_ICONS_DIR);
    // Items
    deleteFile(GeneratedFiles.ITEMS);
    deleteDirectory(GeneratedFiles.ITEM_ICONS_DIR);
    deleteFile(GeneratedFiles.PASSIVES);
    deleteFile(GeneratedFiles.PASSIVES_USAGE);
    deleteFile(GeneratedFiles.CONSUMABLES);
    // - legacies
    deleteFile(GeneratedFiles.LEGACIES);
    deleteFile(GeneratedFiles.NON_IMBUED_LEGACIES);
    deleteFile(GeneratedFiles.LEGACIES_ICONS);
    deleteDirectory(LegaciesLoader.LEGACIES_ICONS_DIR);
    // Items sets
    deleteFile(GeneratedFiles.SETS);
    // Legendary system
    deleteFile(GeneratedFiles.LEGENDARY_DATA);
    // Legendary titles
    deleteFile(GeneratedFiles.LEGENDARY_TITLES);
    // Relics
    deleteFile(GeneratedFiles.RELICS);
    deleteFile(GeneratedFiles.RELIC_ICONS);
    deleteDirectory(MainDatRelicsLoader.RELIC_ICONS_DIR);
    // Recipes
    deleteFile(GeneratedFiles.RECIPES);
    // Emotes
    deleteFile(GeneratedFiles.EMOTES);
    deleteFile(GeneratedFiles.EMOTE_ICONS);
    deleteDirectory(MainDatEmotesLoader.EMOTE_ICONS_DIR);
    // Factions
    deleteFile(GeneratedFiles.FACTIONS);
    // Quests and deeds
    deleteFile(GeneratedFiles.QUESTS);
    deleteFile(GeneratedFiles.DEEDS);
    // Crafting
    deleteFile(GeneratedFiles.CRAFTING_DATA);
    // Buffs
    deleteFile(GeneratedFiles.BUFFS);
    // Trait points
    deleteFile(GeneratedFiles.TRAIT_POINTS);
    // Mounts
    deleteFile(GeneratedFiles.MOUNTS);
    deleteFile(GeneratedFiles.MOUNT_ICONS);
    deleteDirectory(MountsLoader.MOUNT_ICONS_DIR);
    // Cosmetic pets
    deleteFile(GeneratedFiles.PETS);
    deleteFile(GeneratedFiles.PET_ICONS);
    deleteDirectory(CosmeticPetLoader.PET_ICONS_DIR);
    // Vendors
    deleteFile(GeneratedFiles.VENDORS);
    // Barterers
    deleteFile(GeneratedFiles.BARTERS);
    // Containers
    deleteFile(GeneratedFiles.CONTAINERS);
    // Loot tables
    deleteFile(GeneratedFiles.LOOTS);
    // Disenchantment
    deleteFile(GeneratedFiles.DISENCHANTMENTS);
    // Misc icons
    deleteFile(GeneratedFiles.MISC_ICONS);
    deleteDirectory(MiscIconsManager.MISC_ICONS_DIR);

    // Progressions
    deleteFile(GeneratedFiles.PROGRESSIONS_COMBAT);
    deleteFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    deleteFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    deleteFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    deleteFile(GeneratedFiles.PROGRESSIONS_BUFFS);
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
