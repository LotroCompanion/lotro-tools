package delta.games.lotro.tools.dat.sound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.SoundInfo;
import delta.games.lotro.dat.loaders.SoundInfoLoader;

/**
 * Playing with sound files.
 * @author DAM
 */
public class MainSoundTests
{
  private static final Logger LOGGER=Logger.getLogger(MainSoundTests.class);

  private DataFacade _facade=new DataFacade();

  private void doIt()
  {
    // Supported types
    for(Type type : AudioSystem.getAudioFileTypes())
    {
      System.out.println(type);
    }

    // PLay some sound files
    long[] soundInfoIDs= {
        0x2A00C1E0,
        0x2A001120,
        0x2A001128,
        0x2A0032A5,
        0x2A000D65,
        0x2A00C110
    };

    for(long soundInfoID : soundInfoIDs)
    {
      System.out.println("Sound info ID: "+soundInfoID);
      byte[] data=_facade.loadData(soundInfoID);
      ByteArrayInputStream bis=new ByteArrayInputStream(data);
      SoundInfo soundInfo=SoundInfoLoader.decodeSoundInfo(bis);
      int soundID=soundInfo.getSoundID();
      System.out.println("Sound ID: "+soundID);
      String soundName=soundInfo.getName();
      System.out.println("Sound name: "+soundName);
      byte[] soundData=_facade.loadData(soundID);
      ByteArrayInputStream soundBis=new ByteArrayInputStream(soundData);
      byte[] rawSoundData=SoundInfoLoader.decodeSound(soundBis);
      useBuffer(new ByteArrayInputStream(rawSoundData));
      playBuffer(new ByteArrayInputStream(rawSoundData));
    }
  }

  private static void useBuffer(InputStream is)
  {
    try
    {
      AudioFileFormat format=AudioSystem.getAudioFileFormat(is);
      System.out.println("Format: "+format.getFormat());
      System.out.println("Type: "+format.getType());
      System.out.println("Byte length: "+format.getByteLength());
      System.out.println("Frame length: "+format.getFrameLength());
      System.out.println("Properties: "+format.properties());
    }
    catch(Exception e)
    {
      LOGGER.error("Exception in useBuffer!", e);
    }
  }

  private static void playBuffer(InputStream is)
  {
    try
    {
      AudioInputStream in=AudioSystem.getAudioInputStream(is);
      if (in!=null)
      {
        AudioFormat baseFormat=in.getFormat();

        AudioFormat targetFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,baseFormat.getSampleRate(),16,baseFormat.getChannels(),
            baseFormat.getChannels()*2,baseFormat.getSampleRate(),false);

        AudioInputStream dataIn=AudioSystem.getAudioInputStream(targetFormat,in);

        byte[] buffer=new byte[4096];

        // get a line from a mixer in the system with the wanted format
        DataLine.Info info=new DataLine.Info(SourceDataLine.class,targetFormat);
        SourceDataLine line=(SourceDataLine)AudioSystem.getLine(info);

        if (line!=null)
        {
          line.open();

          line.start();
          int nBytesRead=0;
          while (nBytesRead!=-1)
          {
            nBytesRead=dataIn.read(buffer,0,buffer.length);
            if (nBytesRead!=-1)
            {
              line.write(buffer,0,nBytesRead);
            }
          }

          line.drain();
          line.stop();
          line.close();

          dataIn.close();
        }

        in.close();
        // playback finished
      }
    }
    catch (UnsupportedAudioFileException|IOException|LineUnavailableException e)
    {
      LOGGER.error("Exception in playBuffer!", e);
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainSoundTests().doIt();
  }
}
