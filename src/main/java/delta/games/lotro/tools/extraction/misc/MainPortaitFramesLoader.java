package delta.games.lotro.tools.extraction.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIData;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UIFinder;
import delta.games.lotro.dat.data.ui.UIImage;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.data.ui.UIStateData;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.portraitFrames.PortraitFrameDescription;
import delta.games.lotro.lore.portraitFrames.io.xml.PortraitFramesXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.Utils;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for portrait frames.
 * @author DAM
 */
public class MainPortaitFramesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainPortaitFramesLoader.class);

  private DataFacade _facade;
  private EnumMapper _enum;
  private I18nUtils _i18n;
  private Map<Integer,PortraitFrameDescription> _loadedData;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainPortaitFramesLoader(DataFacade facade)
  {
    _facade=facade;
    _loadedData=new HashMap<Integer,PortraitFrameDescription>();
    _enum=_facade.getEnumsManager().getEnumMapper(587202770);
    _i18n=new I18nUtils("portraitFrames",facade.getGlobalStringsManager());
  }

  /**
   * Load icons.
   */
  public void doIt()
  {
    loadData();
    loadIcons();
    checkIcons();
    save();
  }

  private void loadData()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"gmVitalDisplayOverrideControl");
    if (props==null)
    {
      return;
    }
    Object[] list=(Object[])props.getProperty("VitalDisplayOverride_Array");
    for(Object idObj : list)
    {
      int id=((Integer)idObj).intValue();
      PortraitFrameDescription frame=loadPortraitFrame(id);
      if (frame!=null)
      {
        _loadedData.put(Integer.valueOf(frame.getIdentifier()),frame);
      }
    }
  }

  private PortraitFrameDescription loadPortraitFrame(int id)
  {
    long dbPropertiesId=id+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties==null)
    {
      LOGGER.warn("Could not handle portrait frame ID={}",Integer.valueOf(id));
      return null;
    }
    PortraitFrameDescription ret=new PortraitFrameDescription();
    ret.setIdentifier(id);
    // Name
    String name=_i18n.getNameStringProperty(properties,"VitalDisplayOverride_CSM_Name",id,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    //String name=_i18n.getNameStringProperty(properties,"Title_String",titleID,_titleProcessor);
    ret.setName(name);
    // Code
    int code=((Integer)properties.getProperty("VitalDisplayOverride_RequiredEnum")).intValue();
    ret.setCode(code);
    // Icon name
    int uiState=((Integer)properties.getProperty("VitalDisplayOverride_UIState")).intValue();
    String iconName=_enum.getLabel(uiState);
    ret.setIconName(iconName);
    // Is for freeps
    boolean isForFreeps=Utils.getBooleanValue((Integer)properties.getProperty("VitalDisplayOverride_IsForFreepCharacters"),false);
    ret.setIsForFreeps(isForFreeps);
    // Is for creeps
    boolean isForCreeps=Utils.getBooleanValue((Integer)properties.getProperty("VitalDisplayOverride_IsForMonsterCharacters"),false);
    ret.setIsForCreeps(isForCreeps);
    // Is for PVP characters
    boolean isForPVPCharacters=Utils.getBooleanValue((Integer)properties.getProperty("VitalDisplayOverride_IsForPvPCharacters"),false);
    ret.setIsForPvpCharacters(isForPVPCharacters);
    return ret;
  }

  /**
   * Load icons.
   */
  private void loadIcons()
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
          File frameIconFile=new File(GeneratedFiles.FRAME_ICONS,stateData.getStateLabel()+".png").getAbsoluteFile();
          DatIconsUtils.buildImageFile(_facade,imageDID,frameIconFile);
        }
      }
    }
  }

  private void checkIcons()
  {
    for(PortraitFrameDescription frame : _loadedData.values())
    {
      String iconName=frame.getIconName();
      File frameIconFile=new File(GeneratedFiles.FRAME_ICONS,iconName+".png").getAbsoluteFile();
      if (!frameIconFile.exists())
      {
        LOGGER.warn("Frame icon file not found: {}",frameIconFile);
      }
    }
  }

  private void save()
  {
    // Save descriptions
    int nbFrames=_loadedData.size();
    LOGGER.info("Writing {} frames",Integer.valueOf(nbFrames));
    List<PortraitFrameDescription> frames=new ArrayList<PortraitFrameDescription>(_loadedData.values());
    boolean ok=new PortraitFramesXMLWriter().write(GeneratedFiles.PORTRAIT_FRAMES,frames,EncodingNames.UTF_8);
    if (ok)
    {
      LOGGER.info("Wrote portrait frames file: {}",GeneratedFiles.PORTRAIT_FRAMES);
    }
    // Labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainPortaitFramesLoader(facade).doIt();
    facade.dispose();
  }
}
