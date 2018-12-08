package delta.games.lotro.tools.characters.dat;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.utils.dat.DatStatUtils;

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

    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    // Save traits
    new TraitKeyGenerator(traitsManager).setup();
    List<TraitDescription> traits=traitsManager.getAll();
    Collections.sort(traits,new IdentifiableComparator<TraitDescription>());
    int nbTraits=traits.size();
    LOGGER.info("Writing "+nbTraits+" traits");
    boolean ok=TraitDescriptionXMLWriter.write(GeneratedFiles.TRAITS,traits);
    if (ok)
    {
      System.out.println("Wrote traits file: "+GeneratedFiles.TRAITS);
    }
    ok=TraitDescriptionXMLWriter.write(GeneratedFiles.TRAITS2,traits);
    if (ok)
    {
      System.out.println("Wrote traits file: "+GeneratedFiles.TRAITS2);
    }
    // Write trait icons
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.TRAIT_ICONS,TraitLoader.TRAIT_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote trait icons archive: "+GeneratedFiles.TRAIT_ICONS);
    }

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
