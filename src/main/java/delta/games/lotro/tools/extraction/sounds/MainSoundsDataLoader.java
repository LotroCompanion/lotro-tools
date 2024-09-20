package delta.games.lotro.tools.extraction.sounds;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.script.ScriptsTable;
import delta.games.lotro.dat.loaders.script.ScriptTableLoader;
import delta.games.lotro.tools.extraction.GeneratedJukeboxFiles;
import delta.lotro.jukebox.core.model.base.SoundDescription;
import delta.lotro.jukebox.core.model.base.io.xml.SoundsXMLWriter;

/**
 * Loader for sounds data.
 * @author DAM
 */
public class MainSoundsDataLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainSoundsDataLoader.class);

  private DataFacade _facade;

  private MainSoundsDataLoader()
  {
    _facade=new DataFacade();
  }

  private ScriptsTable loadScript(int id)
  {
    byte[] scriptTableData=_facade.loadData(id);
    ScriptsTable ret=null;
    ScriptTableLoader loader=new ScriptTableLoader(_facade);
    try
    {
      ret=loader.decode(scriptTableData);
    }
    catch(Exception e)
    {
      LOGGER.warn("Decoding error for script ID="+id,e);
    }
    return ret;
  }

  private void doIt()
  {
    int[] ids= { 0x7000009, 0x700042E, 0x070017f8,
        0x700042F, // Notes
        0x700000D, 0x700001C, 0x7000008, 0x7000000
    };
    ScriptsInspectorForSounds inspector=new ScriptsInspectorForSounds(_facade);
    for(int id : ids)
    {
      ScriptsTable table=loadScript(id);
      inspector.inspect(table);
    }
    SoundsDataAggregator aggregator=inspector.getAggregator();
    SoundContextManager contextMgr=aggregator.getSoundContextManager();
    //aggregator.dump();
    // Load context data
    handleContextData(contextMgr);
    // Write sounds
    SoundsRegistry registry=aggregator.getSoundsRegistry();
    List<SoundDescription> sounds=registry.getKnownSounds();
    File toFile=GeneratedJukeboxFiles.SOUNDS;
    SoundsXMLWriter.writeSoundsFile(toFile,sounds);
  }

  private void handleContextData(SoundContextManager contextMgr)
  {
    // Items
    ItemsMusicLoader itemsMusicLoader=new ItemsMusicLoader(_facade,contextMgr);
    itemsMusicLoader.doIt();
    // Areas
    GeoMusicLoader geoMusicLoader=new GeoMusicLoader(_facade,contextMgr);
    geoMusicLoader.doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainSoundsDataLoader().doIt();
  }
}
