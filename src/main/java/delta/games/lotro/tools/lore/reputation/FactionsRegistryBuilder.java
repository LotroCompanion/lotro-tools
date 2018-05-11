package delta.games.lotro.tools.lore.reputation;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.traces.LoggersRegistry;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.reputation.ReputationDeed;
import delta.games.lotro.lore.reputation.io.xml.FactionsXMLWriter;
import delta.games.lotro.tools.lore.deeds.LinkFactionLevelsToReputationDeeds;

/**
 * Builder for the factions registry.
 * @author DAM
 */
public class FactionsRegistryBuilder
{
  private static final Logger LOGGER=Logger.getLogger(FactionsRegistryBuilder.class);

  private FactionsRegistry _registry;

  /**
   * Constructor.
   */
  public FactionsRegistryBuilder()
  {
    _registry=new FactionsRegistry();
  }

  private void doIt()
  {
    // Build
    initFactions();
    // Setup deed keys
    new LinkFactionLevelsToReputationDeeds().doIt(_registry);
    // Write
    File toFile=new File("factions.xml").getAbsoluteFile();
    boolean ok=FactionsXMLWriter.writeFactionsFile(toFile,_registry);
    if (ok)
    {
      LOGGER.info("Wrote file: "+toFile);
    }
    else
    {
      LOGGER.error("Failed to build factions registry file: "+toFile);
    }
  }

  /**
   * Initialize and register all the factions.
   */
  private void initFactions()
  {
    FactionsFactory factory=new FactionsFactory();
    // Categories
    List<String> categories=factory.getCategories();
    for(String category : categories)
    {
      List<Faction> factions=factory.getByCategory(category);
      for(Faction faction : factions)
      {
        _registry.registerFaction(faction);
      }
    }
    // Guild faction
    Faction guildFaction=factory.getGuildFaction();
    _registry.registerFaction(guildFaction);
    // Deeds
    for(ReputationDeed deed : factory.getDeeds())
    {
      _registry.addDeed(deed);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    /*LoggersRegistry loggers=*/LoggersRegistry.getInstance();
    FactionsRegistryBuilder builder=new FactionsRegistryBuilder();
    builder.doIt();
  }
}
