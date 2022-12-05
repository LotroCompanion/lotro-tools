package delta.games.lotro.tools.dat.frames;

import java.io.File;
import java.util.List;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.ui.UIData;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UIFinder;
import delta.games.lotro.dat.data.ui.UIImage;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.data.ui.UIStateData;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;

/**
 * Loader for character portrait icons.
 * @author DAM
 */
public class FrameIconsLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public FrameIconsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load icons.
   */
  public void doIt()
  {
    UILayout layout=new UILayoutLoader(_facade).loadUiLayout(0x22000033); // toolbar
    UIFinder finder=new UIFinder(_facade);
    String[] path={
        "AvatarPanel",
        "VitalsParent",
        "AvatarPanel_BG"
    };
    UIElement element=(UIElement)finder.find(layout,path);
    List<UIStateData> stateDatas=element.getStateDatas();
    for(UIStateData stateData : stateDatas)
    {
      for(UIData data : stateData.getDatas())
      {
        if (data instanceof UIImage)
        {
          UIImage imageData=(UIImage)data;
          int imageDID=imageData.getImageDID();
          File to=new File(stateData.getStateLabel()+".png");
          DatIconsUtils.buildImageFile(_facade,imageDID,to);
        }
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new FrameIconsLoader(facade).doIt();
    facade.dispose();
  }
}
