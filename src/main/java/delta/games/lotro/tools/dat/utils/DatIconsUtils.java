package delta.games.lotro.tools.dat.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.loaders.ImageLoader;

/**
 * Utility methods related to icons from DAT files.
 * @author DAM
 */
public class DatIconsUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatIconsUtils.class);

  /**
   * Load an icon image.
   * @param facade Data facade.
   * @param id Icon ID.
   * @return An image or <code>null</code> if not found.
   */
  public static BufferedImage loadImage(DataFacade facade, int id)
  {
    byte[] data=facade.loadData(id); 
    BufferedImage image=ImageLoader.decodeImage(data);
    if (image==null)
    {
      LOGGER.error("Could not load image ID="+id);
    }
    return image;
  }

  /**
   * Write an image to a file.
   * @param image Image to write.
   * @param to File to write to.
   * @return <code>true</code> if it succeeded, <code>false</code> otherwise.
   */
  private static boolean writeImage(BufferedImage image, File to)
  {
    boolean ret=false;
    if (image!=null)
    {
      try
      {
        to.getParentFile().mkdirs();
        ret=ImageIO.write(image,"png",to);
      }
      catch(Exception e)
      {
        LOGGER.error("Error when writing image: "+to,e);
      }
    }
    return ret;
  }

  /**
   * Build an image file using a foreground and a background image.
   * @param facade Data facade.
   * @param imageId Foreground icon ID.
   * @param backgroundImageId Background icon ID.
   * @param to File to write to.
   * @return <code>true</code> if it succeeded, <code>false</code> otherwise.
   */
  public static boolean buildImageFile(DataFacade facade, int imageId, int backgroundImageId, File to)
  {
    BufferedImage image=buildImage(facade,imageId,backgroundImageId);
    boolean ok=writeImage(image,to);
    return ok;
  }

  /**
   * Build an image file from an icon ID.
   * @param facade Data facade.
   * @param imageId Icon ID.
   * @param to File to write to.
   * @return <code>true</code> if it succeeded, <code>false</code> otherwise.
   */
  public static boolean buildImageFile(DataFacade facade, int imageId, File to)
  {
    BufferedImage image=loadImage(facade,imageId);
    boolean ok=writeImage(image,to);
    return ok;
  }

  /**
   * Build an icon image using a foreground and a background image.
   * @param facade Data facade.
   * @param imageId Foreground icon ID.
   * @param backgroundImageId Background icon ID.
   * @return An image or <code>null</code> if not found.
   */
  public static BufferedImage buildImage(DataFacade facade, int imageId, int backgroundImageId)
  {
    BufferedImage image=loadImage(facade,imageId);
    if (image==null)
    {
      return null;
    }
    BufferedImage background=loadImage(facade,backgroundImageId);
    if (background==null)
    {
      return image;
    }

    // Create the new image
    int w = Math.max(image.getWidth(), background.getWidth());
    int h = Math.max(image.getHeight(), background.getHeight());
    BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

    // Paint both images, preserving the alpha channels
    Graphics g = combined.getGraphics();
    g.drawImage(background, 0, 0, null);
    g.drawImage(image, 0, 0, null);
    return combined;
  }
}
