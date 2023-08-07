package delta.games.lotro.tools.voicesExtractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.dat.archive.DetailedFileEntry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.SoundInfo;
import delta.games.lotro.dat.loaders.SoundInfoLoader;

/**
 * Sound extractor.
 * @author DAM
 */
public class SoundExtractor
{
  private static final Logger LOGGER=Logger.getLogger(SoundExtractor.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SoundExtractor(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Extract and save a sound file.
   * @param toDir Target directory.
   * @param questID Quest identifier.
   * @param soundInfoID Sound info identifier.
   * @return <code>true</code> if it succeeded, <code>false</code> otherwise..
   */
  public boolean saveSound(File toDir, int questID, int soundInfoID)
  {
    byte[] data=_facade.loadData(soundInfoID);
    if (data==null)
    {
      LOGGER.warn("Could not load sound ID="+soundInfoID);
      return false;
    }
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    SoundInfo soundInfo=SoundInfoLoader.decodeSoundInfo(bis);
    if (soundInfo==null)
    {
      LOGGER.warn("Could not decode sound ID="+soundInfoID);
      return false;
    }
    int soundID=soundInfo.getSoundID();
    String soundName=soundInfo.getName();
    DetailedFileEntry entry=_facade.getDetailedEntry(soundID);
    if (entry==null)
    {
      LOGGER.warn("Could not load raw sound for sound ID="+soundInfoID);
      return false;
    }
    byte[] soundData=entry.getData();
    byte[] rawSoundData=SoundInfoLoader.decodeSound(new ByteArrayInputStream(soundData));
    SoundDescription ret=new SoundDescription(soundInfoID);
    ret.setName(soundName);
    SoundFormat format=inspectFormat(rawSoundData);
    String extension;
    if (format==SoundFormat.WAV) extension=".wav";
    else if (format==SoundFormat.OGG_VORBIS) extension=".ogg";
    else extension=".unknown";
    if (soundName==null) soundName="";
    String filename=""+questID+"-"+soundID+((soundName.length()>0)?"-"+soundName:"");
    File f=new File(toDir,filename+extension);
    boolean ok=FileIO.writeFile(f,rawSoundData);
    return ok;
  }

  private static SoundFormat inspectFormat(byte[] soundData)
  {
    AudioFileFormat format=null;
    try
    {
      InputStream is=new ByteArrayInputStream(soundData);
      format=AudioSystem.getAudioFileFormat(is);
    }
    catch (Exception e)
    {
      return SoundFormat.OGG_VORBIS;
    }
    Type type=format.getType();
    if (type==null)
    {
      return null;
    }
    if ("WAVE".equals(type.toString()))
    {
      return SoundFormat.WAV;
    }
    LOGGER.warn("Could not get format of sound: unsupported type!");
    return null;
    // System.out.println("Format: "+format.getFormat());
    // System.out.println("Byte length: "+format.getByteLength());
    // System.out.println("Frame length: "+format.getFrameLength());
    // System.out.println("Properties: "+format.properties());
  }
}
