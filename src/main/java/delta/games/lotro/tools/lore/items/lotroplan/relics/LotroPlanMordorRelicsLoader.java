package delta.games.lotro.tools.lore.items.lotroplan.relics;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.tools.lore.items.lotroplan.LotroPlanItemsDbLoader;

/**
 * Loads Mordor relics definitions from the "mordor" items table.
 * @author DAM
 */
public class LotroPlanMordorRelicsLoader
{
  /**
   * Load Gorgoroth relics.
   * @return A list of relics.
   */
  public List<Relic> loadGorgorothRelics()
  {
    List<Relic> relics=new ArrayList<Relic>();
    LotroPlanItemsDbLoader tableLoader=new LotroPlanItemsDbLoader();
    List<Item> items=tableLoader.loadTable("mordor_relics.txt");
    for(Item item : items)
    {
      Relic relic=itemToRelic(item);
      relics.add(relic);
    }
    return relics;
  }

  private Relic itemToRelic(Item item)
  {
    String name=item.getName();
    if (name.startsWith("-")) name=name.substring(1);
    name=name.trim();
    Integer requiredLevel=item.getItemLevel();
    RelicType type=getRelicTypeFromName(name);
    Relic relic=new Relic(name,type,requiredLevel);
    BasicStatsSet stats=relic.getStats();
    stats.setStats(item.getStats());
    return relic;
  }

  private RelicType getRelicTypeFromName(String name)
  {
    if (name.contains("Gem")) return RelicType.GEM;
    if (name.contains("Setting")) return RelicType.SETTING;
    if (name.contains("Rune")) return RelicType.RUNE;
    return null;
  }
}
