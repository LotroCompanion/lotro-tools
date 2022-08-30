package delta.games.lotro.tools.lore.sounds;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.log4j.Logger;

import com.github.trilarion.sound.vorbis.jcraft.jorbis.VorbisFile;

import delta.common.utils.io.FileIO;
import delta.games.lotro.dat.archive.DetailedFileEntry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.SoundInfo;
import delta.games.lotro.dat.loaders.SoundInfoLoader;
import delta.lotro.jukebox.core.model.SoundDescription;
import delta.lotro.jukebox.core.model.SoundFormat;

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
    int rawLength=soundData.length;
    float duration=computeDuration(soundData);
    SoundDescription ret=new SoundDescription(soundID);
    ret.setName(soundName);
    ret.setDuration((int)duration);
    ret.setFormat(SoundFormat.OGG_VORBIS);
    ret.setRawSize(rawLength);
    long date=entry.getEntry().getTimestamp();
    ret.setTimestamp(date);
    return ret;
  }

  /**
   * Compute sound duration.
   * @param soundData Raw sound data.
   * @return A duration in seconds.
   */
  private float computeDuration(byte[] soundData)
  {
    float ret=0;
    VorbisFile f=null;
    try
    {
      File tmp=new File("tmp.ogg");
      FileIO.writeFile(tmp,soundData);
      f=new VorbisFile(tmp.getAbsolutePath());
      ret=f.time_total(-1);
    }
    catch(Exception e)
    {
      LOGGER.warn("Error when computing sound duration", e);
      e.printStackTrace();
    }
    finally
    {
      /*
      if (f!=null)
      {
        try
        {
          f.close();
        }
        catch(IOException ioe)
        {
          LOGGER.warn("Error when closing sound file!", ioe);
        }
      }
      */
    }
    return ret;
  }
}
