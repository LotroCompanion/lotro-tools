package delta.games.lotro.tools.characters.dat;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.utils.maths.Progression;
import delta.games.lotro.utils.maths.io.xml.ProgressionsXMLWriter;

/**
 * Loader for character data: classes, races, trait trees.
 * @author DAM
 */
public class MainCharacterDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainCharacterDataLoader.class);

  private void doIt()
  {
    DataFacade facade=new DataFacade();
    TraitsManager traitsManager=new TraitsManager();

    // Load race data
    new RaceDataLoader(facade,traitsManager).doIt();
    // Load character class data
    new CharacterClassDataLoader(facade,traitsManager).doIt();
    // Load trait trees
    new TraitTreesDataLoader(facade,traitsManager).doIt();

    // Save progressions
    List<Progression> progressions=ProgressionsManager.getInstance().getAll();
    int nbProgressions=progressions.size();
    LOGGER.info("Writing "+nbProgressions+" progressions");
    File progressionsFile=new File("../lotro-companion/data/lore/progressions.xml").getAbsoluteFile();
    ProgressionsXMLWriter.write(progressionsFile,progressions);
    // Save traits
    File traitsFile=new File("../lotro-companion/data/lore/characters/traits.xml").getAbsoluteFile();
    List<TraitDescription> traits=traitsManager.getAll();
    Collections.sort(traits,new IdentifiableComparator<TraitDescription>());
    int nbTraits=traits.size();
    LOGGER.info("Writing "+nbTraits+" traits");
    TraitDescriptionXMLWriter.write(traitsFile,traits);

    facade.dispose();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCharacterDataLoader().doIt();
  }
}
