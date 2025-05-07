package delta.games.lotro.tools.extraction.sounds;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trilarion.sound.vorbis.jcraft.jorbis.VorbisFile;

import delta.common.utils.io.FileIO;
import delta.lotro.jukebox.core.model.base.SoundDescription;
import delta.lotro.jukebox.core.model.base.SoundFormat;

/**
 * Utility methods related to sounds management.
 * @author DAM
 */
public class SoundUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SoundUtils.class);

  /**
   * Show supported sound types.
   * @param out Output stream.
   */
  public static void showSupportedSoundTypes(PrintStream out)
  {
    for(Type type : AudioSystem.getAudioFileTypes())
    {
      out.println(type);
    }
  }

  /**
   * Inspect sound:
   * <ul>
   * <li>get format,
   * <li>get sample rate,
   * <li>get duration
   * </ul>.
   * Store results in <code>results</code>.
   * @param soundData Input sound.
   * @param results Storage for results.
   */
  public static void inspectSound(byte[] soundData, SoundDescription results)
  {
    inspectFormat(soundData,results);
    SoundFormat format=results.getFormat();
    if (format==SoundFormat.OGG_VORBIS)
    {
      float duration=computeDurationOGG(soundData);
      results.setDuration((int)(duration*1000));
    }
    else
    {
      float duration=computeDuration(soundData);
      results.setDuration((int)(duration*1000));
    }
  }

  private static void inspectFormat(byte[] soundData, SoundDescription results)
  {
    AudioFileFormat format=null;
    try
    {
      InputStream is=new ByteArrayInputStream(soundData);
      format=AudioSystem.getAudioFileFormat(is);
    }
    catch (Exception e)
    {
      LOGGER.warn("Could not get format of sound!",e);
      return;
    }
    Type type=format.getType();
    if (type==null)
    {
      LOGGER.warn("Could not get format of sound: no type!");
      return;
    }
    AudioFormat audioFormat=format.getFormat();
    float sampleRate=audioFormat.getSampleRate();
    results.setSampleRate(sampleRate);
    if ("WAVE".equals(type.toString()))
    {
      results.setFormat(SoundFormat.WAV);
    }
    else if ("OGG".equals(type.toString()))
    {
      results.setFormat(SoundFormat.OGG_VORBIS);
    }
    else
    {
      LOGGER.warn("Could not get format of sound: unsupported type!");
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Format: {}",format.getFormat());
      LOGGER.debug("Byte length: {}",Integer.valueOf(format.getByteLength()));
      LOGGER.debug("Frame length: {}",Integer.valueOf(format.getFrameLength()));
      LOGGER.debug("Properties: {}",format.properties());
    }
  }

  /**
   * Compute sound duration.
   * @param soundData Raw sound data.
   * @return A duration in seconds.
   */
  private static float computeDurationOGG(byte[] soundData)
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
    catch (Exception e)
    {
      LOGGER.warn("Error when computing sound duration",e);
    }
    return ret;
  }

  private static float computeDuration(byte[] soundData)
  {
    InputStream is=new ByteArrayInputStream(soundData);
    try
    {
      AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(is);
      AudioFormat format=audioInputStream.getFormat();
      long frames=audioInputStream.getFrameLength();
      float durationInSeconds=(frames/format.getFrameRate());
      return durationInSeconds;
    }
    catch (Exception e)
    {
      LOGGER.warn("Could not get format of sound!",e);
    }
    return 0;
  }
}
