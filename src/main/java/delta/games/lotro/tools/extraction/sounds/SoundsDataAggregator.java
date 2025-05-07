package delta.games.lotro.tools.extraction.sounds;

import java.io.PrintStream;
import java.util.Deque;
import java.util.List;

import delta.common.utils.io.streams.IndentableStream;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.lotro.jukebox.core.model.base.SoundDescription;
import delta.lotro.jukebox.core.model.base.SoundType;
import delta.lotro.jukebox.core.model.base.SoundsManager;

/**
 * Sounds data aggregator.
 * @author DAM
 */
public class SoundsDataAggregator
{
  private DataFacade _facade;
  private SoundsRegistry _soundsRegistry;
  private SoundAnalyzer _analyzer;
  private SoundContextManager _contextManager;

  /**
   * Constructor.
   * @param facade Date facade.
   */
  public SoundsDataAggregator(DataFacade facade)
  {
    _facade=facade;
    _analyzer=new SoundAnalyzer(facade);
    _soundsRegistry=new SoundsRegistry();
    _contextManager=new SoundContextManager();
    init();
  }

  private void init()
  {
    // Use sounds manager first
    List<SoundDescription> sounds=SoundsManager.getInstance().getAllSounds();
    for(SoundDescription sound : sounds)
    {
      _soundsRegistry.registerSound(sound.getIdentifier(),sound);
    }
  }

  /**
   * Get the sounds registry.
   * @return the sounds registry.
   */
  public SoundsRegistry getSoundsRegistry()
  {
    return _soundsRegistry;
  }

  /**
   * Get the sound context manager.
   * @return the sound context manager.
   */
  public SoundContextManager getSoundContextManager()
  {
    return _contextManager;
  }

  /**
   * Handle a single sound.
   * @param soundID Sound identifier.
   * @param soundChannel Sound channel.
   * @param context Context.
   */
  public void handleSound(int soundID, int soundChannel, Deque<List<PropertyValue>> context)
  {
    int nb=getContextPropertiesCount(context);
    if (nb==0)
    {
      return;
    }
    for(List<PropertyValue> values : context)
    {
      PropertyValue first=values.get(0);
      PropertyDefinition propertyDef=first.getDefinition();
      SoundDescription sound=getSound(soundID,soundChannel);
      for(PropertyValue propertyValue : values)
      {
        Object value=propertyValue.getValue();
        if (value instanceof Number)
        {
          Number numberValue=(Number)value;
          int intValue=numberValue.intValue();
          _contextManager.registerSound(propertyDef,intValue,sound);
        }
      }
    }
  }

  private SoundDescription getSound(int soundID, int soundChannel)
  {
    // Check local registry
    SoundDescription sound=_soundsRegistry.getSound(soundID);
    if (sound==null)
    {
      // Fetch sound in the DAT files
      sound=_analyzer.handleSound(soundID);
      if (sound==null)
      {
        return null;
      }
      // Register sound in the local registry
      _soundsRegistry.registerSound(soundID,sound);
    }
    SoundType type=SoundsRegistry.getSoundType(soundChannel);
    sound.addType(type);
    return sound;
  }

  private int getContextPropertiesCount(Deque<List<PropertyValue>> context)
  {
    int nb=0;
    for(List<PropertyValue> values : context)
    {
      nb+=values.size();
    }
    return nb;
  }

  /**
   * Dump the contents of this registry to the given stream.
   * @param ps Output stream. 
   */
  public void dump(PrintStream ps)
  {
    IndentableStream out=new IndentableStream(ps);
    int nbSounds=_soundsRegistry.getKnownSounds().size();
    out.println("Nb sounds: "+nbSounds);
    new SoundContextDump(_facade).dump(_contextManager,out);
  }
}
