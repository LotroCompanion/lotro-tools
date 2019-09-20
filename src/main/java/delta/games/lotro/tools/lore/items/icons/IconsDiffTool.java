package delta.games.lotro.tools.lore.items.icons;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import delta.common.utils.files.FileCopy;

/**
 * Diff tool for item icons.
 * @author DAM
 */
public class IconsDiffTool
{
  private static final Logger LOGGER=Logger.getLogger(IconsDiffTool.class);

  File root=new File("..\\lotro-item-icons-db");
  File oldDir=new File(root,"icons.old\\icons");
  File newDir=new File(root,"icons.new\\icons");

  private void compareImages(String name) throws Exception
  {
    File oldFile=new File(oldDir,name);
    File newFile=new File(newDir,name);
    if (oldFile.exists())
    {
      BufferedImage img1=ImageIO.read(oldFile);
      BufferedImage img2=ImageIO.read(newFile);
      double p=getDifferencePercent(img1,img2);
      if (p>5)
      {
        System.out.println(name+": diff percent: "+p);
        FileCopy.copy(newFile,oldFile);
      }
    }
    else
    {
      FileCopy.copy(newFile,oldFile);
      System.out.println(name+": orphan");
    }
  }

  private double getDifferencePercent(BufferedImage img1, BufferedImage img2)
  {
    int width=img1.getWidth();
    int height=img1.getHeight();
    int width2=img2.getWidth();
    int height2=img2.getHeight();
    if (width!=width2||height!=height2)
    {
      return 100.0;
    }

    long diff=0;
    for(int y=0;y<height;y++)
    {
      for(int x=0;x<width;x++)
      {
        diff+=pixelDiff(img1.getRGB(x,y),img2.getRGB(x,y));
      }
    }
    long maxDiff=3L*255*width*height;

    return 100.0*diff/maxDiff;
  }

  private int pixelDiff(int rgb1, int rgb2)
  {
    int r1=(rgb1>>16)&0xff;
    int g1=(rgb1>>8)&0xff;
    int b1=rgb1&0xff;
    int r2=(rgb2>>16)&0xff;
    int g2=(rgb2>>8)&0xff;
    int b2=rgb2&0xff;
    return Math.abs(r1-r2)+Math.abs(g1-g2)+Math.abs(b1-b2);
  }

  private void removeObsoleteFiles()
  {
    File[] files=oldDir.listFiles();
    for(File file:files)
    {
      String name=file.getName();
      File newFile=new File(newDir,name);
      if (!newFile.exists())
      {
        file.delete();
      }
    }
  }

  private void compareFiles()
  {
    File[] files=newDir.listFiles();
    for(File file:files)
    {
      String name=file.getName();
      try
      {
        compareImages(name);
      }
      catch (Throwable t)
      {
        LOGGER.warn("Error in images comparison",t);
      }
    }
  }

  private void doIt()
  {
    removeObsoleteFiles();
    compareFiles();
  }
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new IconsDiffTool().doIt();
  }
}
