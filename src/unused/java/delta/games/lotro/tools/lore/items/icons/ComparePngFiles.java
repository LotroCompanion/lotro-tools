package delta.games.lotro.tools.lore.items.icons;

import java.awt.image.Raster;
import java.io.File;

import javax.media.jai.RenderedOp;

import delta.common.utils.io.FileIO;
import delta.tools.images.ImageTools;

/**
 * Compare 2 PNG files.
 * @author DAM
 */
public class ComparePngFiles
{
  public static boolean compareFiles(File file1, File file2)
  {
    boolean same=compareRawFiles(file1,file2);
    if (!same)
    {
      String name1=file1.getName();
      String name2=file2.getName();
      
      if ((name1.toLowerCase().endsWith(".png")) && (name2.toLowerCase().endsWith(".png")))
      {
        same=comparePNGFiles(file1,file2);
      }
    }
    return same;
  }

  private static final int MAX_PIXEL_DIFF=50;

  private static final int MAX_DIFFS=40;

  private static boolean compareSample(int x, double[] buffer1, double[] buffer2, int nbBands1, int nbBands2)
  {
    boolean same=true;
    if (nbBands1==nbBands2)
    {
      for(int i=0;i<nbBands1;i++)
      {
        double diff=buffer1[x*nbBands1+i]-buffer2[x*nbBands2+i];
        if (Math.abs(diff)>MAX_PIXEL_DIFF)
        {
          same=false;
          break;
        }
      }
    }
    else if ((nbBands1==3) && (nbBands2==4))
    {
      for(int i=0;i<nbBands1;i++)
      {
        double diff=buffer1[x*nbBands1+i]-buffer2[x*nbBands2+i];
        if (Math.abs(diff)>MAX_PIXEL_DIFF)
        {
          same=false;
          break;
        }
      }
    }
    else if ((nbBands1==4) && (nbBands2==3))
    {
      for(int i=0;i<nbBands2;i++)
      {
        double diff=buffer1[x*nbBands1+i]-buffer2[x*nbBands2+i];
        if (Math.abs(diff)>MAX_PIXEL_DIFF)
        {
          same=false;
          break;
        }
      }
    }
    else
    {
      System.out.println("Unmanaged case!");
    }
    return same;
  }

  public static boolean comparePNGFiles(File file1, File file2)
  {
    RenderedOp image1=ImageTools.readImage(file1);
    int h1=image1.getHeight();
    int w1=image1.getWidth();
    RenderedOp image2=ImageTools.readImage(file2);
    int h2=image2.getHeight();
    int w2=image2.getWidth();
    if ((h1==h2) && (w1==w2))
    {
      int nbDiffs=0;
      //int nbPixels=h1*w1;
      boolean same=true;
      Raster r1=image1.getData();
      int nbBands1=r1.getNumBands();
      Raster r2=image2.getData();
      int nbBands2=r2.getNumBands();
      double[] buffer1=null;
      double[] buffer2=null;
      for(int i=0;i<h1;i++)
      {
        buffer1=r1.getPixels(0,i,w1,1,buffer1);
        buffer2=r2.getPixels(0,i,w1,1,buffer2);
        for(int j=0;j<w1;j++)
        {
          if (!compareSample(j,buffer1,buffer2,nbBands1,nbBands2))
          {
            nbDiffs++;
            if (nbDiffs>MAX_DIFFS) same=false;
          }
        }
        /*
        int length1=buffer1.length;
        int length2=buffer2.length;
        if (length1==length2)
        {
          for(int j=0;j<length1;j++)
          {
            if (buffer1[j]!=buffer2[j])
            {
              nbDiffs++;
              same=false;
            }
          }
        }
        else
        {
          System.out.println("Bad length!");
          same=false;
        }
        */
      }
      //System.out.println(nbDiffs+ " / "+nbPixels);
      return same;
    }
    System.out.println("Size differ: h1="+h1+", h2="+h2+", w1="+w1+",w2="+w2);
    return false;
  }

  public static boolean compareRawFiles(File file1, File file2)
  {
    byte[] good=FileIO.readFile(file1);
    byte[] bad=FileIO.readFile(file2);
    if (good.length==bad.length)
    {
      int length=good.length;
      boolean same=true;
      for(int i=0;i<length;i++)
      {
        if (good[i]!=bad[i])
        {
          same=false;
          break;
        }
      }
      return same;
    }
    return false;
  }
}
