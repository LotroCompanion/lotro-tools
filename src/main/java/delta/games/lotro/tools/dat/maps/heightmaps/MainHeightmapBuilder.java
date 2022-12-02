package delta.games.lotro.tools.dat.maps.heightmaps;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.geo.HeightMap;
import delta.games.lotro.dat.loaders.HeightMapDataLoader;

/**
 * Loader for height map from LOTRO DAT files.
 * @author DAM
 */
public class MainHeightmapBuilder
{
  private static final Logger LOGGER=Logger.getLogger(MainHeightmapBuilder.class);

  private byte[] _buffer;
  private DataFacade _facade;
  private HeightMapDataLoader _loader;
  private static final int MIN_X=153;
  private static final int MAX_X=171;
  private static final int MIN_Y=128;
  private static final int MAX_Y=142;
  private static final int WIDTH=(MAX_X-MIN_X+1)*32;
  private static final int HEIGHT=(MAX_Y-MIN_Y+1)*32;

  /**
   * Constructor.
   */
  public MainHeightmapBuilder()
  {
    _facade=new DataFacade();
    _loader=new HeightMapDataLoader(_facade);
  }

  private void doIt()
  {
    _buffer=new byte[WIDTH*HEIGHT*3];

    int region=1;
    //int[] regions={1,2,3,4,14};
    //for(int region : regions)
    {
      System.out.println("Region "+region);
      for(int blockX=MIN_X;blockX<=MAX_X;blockX++)
      {
        System.out.println("X="+blockX);
        for(int blockY=MIN_Y;blockY<=MAX_Y;blockY++)
        {
          HeightMap map=_loader.loadHeightMapData(region,blockX,blockY);
          if (map!=null)
          {
            paintHeightMap(map,blockX,blockY);
          }
        }
      }
    }
    String name="image"+region+".png";
    buildImage(_buffer,WIDTH,HEIGHT,name);
  }

  private void buildImage(byte[] data, int width, int height,String name)
  {
    DataBuffer buffer = new DataBufferByte(data, data.length);

    //3 bytes per pixel: red, green, blue
    WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 3 * width, 3, new int[] {0, 1, 2}, (Point)null);
    ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE); 
    BufferedImage image = new BufferedImage(cm, raster, true, null);

    File to=new File(name);
    try
    {
      ImageIO.write(image,"png",to);
    } catch(Exception e)
    {
      LOGGER.warn("Could not write height image file: "+to,e);
    }
  }

  private void paintHeightMap(HeightMap map, int blockX, int blockY)
  {
    // Decode data
    //byte[] buffer=new byte[32*32*3];
    int baseY=(MAX_Y-blockY)*32;
    for(int y=0;y<32;y++)
    {
      for(int x=0;x<32;x++)
      {
        int h=map.getRawHeightAt(x,y);
        int c = (int)(Math.log(((16 * h) % 65535) + 1)/Math.log(1.045));
        byte color=(byte)c;
        int fullX=((blockX-MIN_X)*32)+x;
        int fullY=baseY+(31-y);
        int offset=((WIDTH*fullY)+fullX)*3;
        _buffer[offset]=color;
        _buffer[offset+1]=color;
        _buffer[offset+2]=color;
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Path of file to use.
   */
  public static void main(String[] args)
  {
    new MainHeightmapBuilder().doIt();
  }
}
