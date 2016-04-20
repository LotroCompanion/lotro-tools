package delta.games.lotro.tools.lore.items.icons;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import delta.common.utils.text.TextUtils;

/**
 * Extracts icons from a set a LOTRO screen shots.
 * @author DAM
 */
public class IconsExtractor
{
  private static final File INPUT_DIR=new File("d:\\tmp\\icons\\source");
  private static final File TO_DIR=new File("d:\\tmp\\icons\\to");

  private static final int START_X=203;
  private static final int START_Y=272;
  private static final int ICON_SIZE=32;
  private static final int OFFSET_X=40;
  private static final int OFFSET_Y=40;
  private static final int NB_ICONS_X=20;
  private static final int NB_ICONS_Y=12;

  private int _index;

  private void doIt() throws Exception
  {
    File iconFile=new File("iconIds.txt");
    List<String> iconIds=TextUtils.readAsLines(iconFile);
    String[] files=INPUT_DIR.list();
    Arrays.sort(files);
    for(String file : files)
    {
      File inputFile=new File(INPUT_DIR,file);
      doFile(iconIds,inputFile);
    }
  }

  private void doFile(List<String> iconIds, File file) throws Exception
  {
    BufferedImage image=ImageIO.read(file);
    for(int i=0;i<NB_ICONS_Y;i++)
    {
      for(int j=0;j<NB_ICONS_X;j++)
      {
        int x=START_X+j*OFFSET_X;
        int y=START_Y+i*OFFSET_Y;
        BufferedImage subImage=image.getSubimage(x,y,ICON_SIZE,ICON_SIZE);
        File to=new File(TO_DIR,iconIds.get(_index)+".png");
        to.getParentFile().mkdirs();
        ImageIO.write(subImage,"png",to);
        _index++;
        if (_index>=iconIds.size()) return;
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   * @throws Exception If a problem occurs.
   */
  public static void main(String[] args) throws Exception
  {
    new IconsExtractor().doIt();
  }
}
