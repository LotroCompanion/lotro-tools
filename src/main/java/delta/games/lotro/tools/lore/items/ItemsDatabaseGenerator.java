package delta.games.lotro.tools.lore.items;

import java.io.File;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.tools.lore.items.lotroplan.LotroPlanItemsDbLoader;
import delta.games.lotro.tools.lore.items.merges.ItemNormalization;
import delta.games.lotro.tools.lore.items.merges.MergeItemsLegacyAndTulkasIndex;
import delta.games.lotro.tools.lore.items.merges.MergeWithLotroPlanDb;
import delta.games.lotro.tools.lore.items.merges.MergeWithTulkasNew;
import delta.games.lotro.tools.lore.items.tulkas.TulkasItemsDBParser;

/**
 * Whole items database generation process.
 * @author DAM
 */
public class ItemsDatabaseGenerator
{
  /**
   * Main method of this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    // 1) Generate independant databases
    // Build from Tulkas items index
    // items19.2.2.txt -> itemsTulkasIndex.xml
    {
      File from=new File("data/items/in/items21.txt").getAbsoluteFile();
      new TulkasItemsDBParser(from,EncodingNames.UTF_8,TulkasItemsDBParser.INDEX).doIt();
    }
    // Build from LATEST Tulkas items with stats
    // Items13.1.lua -> itemsTulkas13.1.xml
    {
      File from=new File("data/items/in/items13.1.txt").getAbsoluteFile();
      new TulkasItemsDBParser(from,EncodingNames.UTF_8,TulkasItemsDBParser.NEW_VERSION).doIt();
    }
    // Build from lotroplan database
    // itemsdb.txt -> itemsdb.xml
    new LotroPlanItemsDbLoader().doIt();
    // 2) Merges
    // itemsTulkasIndex.xml + itemsLegacy.xml -> itemsLegacy+TulkasIndex.xml
    new MergeItemsLegacyAndTulkasIndex().doIt();
    // itemsLegacy+TulkasIndex.xml + itemsTulkas13.1.xml -> itemsLegacy+Tulkas.xml
    new MergeWithTulkasNew().doIt();
    // itemsLegacy+Tulkas.xml + itemsdb.xml -> items-rc.xml
    new MergeWithLotroPlanDb().doIt();
    // 3) Normalize
    // items-rc.xml -> items.xml
    new ItemNormalization().doIt();
  }
}
