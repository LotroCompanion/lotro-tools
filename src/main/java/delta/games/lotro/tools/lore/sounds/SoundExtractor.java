package delta.games.lotro.tools.lore.sounds;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.dat.archive.DetailedFileEntry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.SoundInfo;
import delta.games.lotro.dat.loaders.SoundInfoLoader;
import delta.lotro.jukebox.core.model.SoundDescription;
import delta.lotro.jukebox.core.model.SoundFormat;

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
   * @param soundInfoID Sound info identifier.
   * @return <code>true</code> if it succeeded, <code>false</code> otherwise..
   */
  public boolean saveSound(File toDir, int soundInfoID)
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
    int rawLength=rawSoundData.length;
    SoundDescription ret=new SoundDescription(soundInfoID);
    ret.setName(soundName);
    ret.setRawSize(rawLength);
    long date=entry.getEntry().getTimestamp();
    ret.setTimestamp(date);
    SoundUtils.inspectSound(rawSoundData,ret);
    SoundFormat format=ret.getFormat();
    String extension;
    if (format==SoundFormat.WAV) extension=".wav";
    else if (format==SoundFormat.OGG_VORBIS) extension=".ogg";
    else extension=".unknown";
    if ((soundName==null) || (soundName.isEmpty())) soundName=String.valueOf(soundID);
    File f=new File(toDir,soundName+extension);
    boolean ok=FileIO.writeFile(f,rawSoundData);
    return ok;
  }
}
