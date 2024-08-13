package delta.games.lotro.tools.lore.sounds;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.archive.DetailedFileEntry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.SoundInfo;
import delta.games.lotro.dat.loaders.SoundInfoLoader;
import delta.lotro.jukebox.core.model.base.SoundDescription;

/**
 * Sound analyzer.
 * @author DAM
 */
public class SoundAnalyzer
{
  private static final Logger LOGGER=Logger.getLogger(SoundAnalyzer.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SoundAnalyzer(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Handle a single sound.
   * @param soundInfoID Sound info identifier.
   * @return A sound description or <code>null</code> if a problem occurred.
   */
  public SoundDescription handleSound(int soundInfoID)
  {
    byte[] data=_facade.loadData(soundInfoID);
    if (data==null)
    {
      LOGGER.warn("Could not load sound ID="+soundInfoID);
      return null;
    }
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    SoundInfo soundInfo=SoundInfoLoader.decodeSoundInfo(bis);
    if (soundInfo==null)
    {
      LOGGER.warn("Could not decode sound ID="+soundInfoID);
      return null;
    }
    int soundID=soundInfo.getSoundID();
    String soundName=soundInfo.getName();
    DetailedFileEntry entry=_facade.getDetailedEntry(soundID);
    if (entry==null)
    {
      LOGGER.warn("Could not load raw sound for sound ID="+soundInfoID);
      return null;
    }
    byte[] soundData=entry.getData();
    byte[] rawSoundData=SoundInfoLoader.decodeSound(new ByteArrayInputStream(soundData));
    int rawLength=rawSoundData.length;
    SoundDescription ret=new SoundDescription(soundInfoID);
    ret.setName(soundName);
    ret.setRawSize(rawLength);
    long date=entry.getEntry().getTimestamp();
    ret.setTimestamp(date);
    SoundUtils.inspectSound(rawSoundData,ret);
    return ret;
  }
}
