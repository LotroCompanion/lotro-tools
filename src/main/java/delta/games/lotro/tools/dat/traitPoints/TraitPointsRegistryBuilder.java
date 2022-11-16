package delta.games.lotro.tools.dat.traitPoints;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.character.status.traitPoints.TraitPointsRegistry;
import delta.games.lotro.character.status.traitPoints.io.xml.TraitPointsRegistryXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Builder for the trait points registry.
 * @author DAM
 */
public class TraitPointsRegistryBuilder
{
  private static final Logger LOGGER=Logger.getLogger(TraitPointsRegistryBuilder.class);

  private TraitPointsRegistry _registry;

  /**
   * Constructor.
   */
  public TraitPointsRegistryBuilder()
  {
    _registry=new TraitPointsRegistry();
  }

  /**
   * Get the built registry.
   * @return the trait points registry.
   */
  public TraitPointsRegistry getRegistry()
  {
    return _registry;
  }

  /**
   * Build trait points registry.
   */
  public void doIt()
  {
    // Target file
    File toFile=GeneratedFiles.TRAIT_POINTS;
    TraitPointsRegistryXMLWriter writer=new TraitPointsRegistryXMLWriter();
    boolean ok=writer.write(toFile,_registry,EncodingNames.UTF_8);
    if (ok)
    {
      LOGGER.info("Wrote file: "+toFile);
    }
    else
    {
      LOGGER.error("Failed to build trait points registry file: "+toFile);
    }
  }
}
