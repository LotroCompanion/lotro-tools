package delta.games.lotro.tools.lore.traitPoints;

import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.stats.traitPoints.TraitPoint;
import delta.games.lotro.stats.traitPoints.TraitPointsRegistry;
import delta.games.lotro.stats.traitPoints.io.xml.TraitPointsRegistryXMLWriter;

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

  private void doIt()
  {
    buildRegistry();
    // Target file
    File toFile=new File("traitPoints.xml").getAbsoluteFile();
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

  private void buildRegistry()
  {
    initPoint("Minstrel:ClassDeed1", "Class deed 1 for Minstrels", CharacterClass.MINSTREL);
    initPoint("Minstrel:ClassDeed2", "Class deed 2 for Minstrels", CharacterClass.MINSTREL);
    initPoint("Minstrel:ClassDeed3", "Class deed 3 for Minstrels", CharacterClass.MINSTREL);
  }

  private void initPoint(String id, String label, CharacterClass requiredCharacterClass)
  {
    TraitPoint point=new TraitPoint(id,requiredCharacterClass);
    point.setLabel(label);
    _registry.registerTraitPoint(point);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    TraitPointsRegistryBuilder builder=new TraitPointsRegistryBuilder();
    builder.doIt();
  }
}
