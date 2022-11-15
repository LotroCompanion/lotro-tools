package delta.games.lotro.tools.dat.emotes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.io.xml.EmoteXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get emotes definitions from DAT files.
 * @author DAM
 */
public class MainDatEmotesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatEmotesLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEmotesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /*
Sample emote properties:
************* 1879071841 *****************
Emote_AllowedWhileMounted: 1
Emote_CommandString: 
  -1: hug
Emote_Description: 
  -1: Using this emote causes you to hug someone.
Emote_IconImage: 1092280913
Emote_Message_FirstPerson: 
  -1: You hug yourself.  Don't you feel better?
Emote_Message_FirstPersonWithTarget: 
  -1: You give 
  -2:  a big hug.
Emote_Message_ThirdPerson: 
  -1: #1:
  -2:  hugs #1:{herself[f]|himself} and seems happier.
Emote_Message_ThirdPersonWithTarget: 
  -1: 
  -2:  gives 
  -3:  a big hug.
Emote_Mounted_ScriptID: 268435904
Emote_MustBeGranted: 0
Emote_ScriptID: 268446269
Emote_SourceText: 
  -1: Everyone can use this emote.
  */

  private EmoteDescription load(int indexDataId)
  {
    EmoteDescription emote=null;
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      emote=new EmoteDescription();
      emote.setIdentifier(indexDataId);
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
      // Command
      String command=DatUtils.getStringProperty(properties,"Emote_CommandString");
      emote.setCommand(command);
      // Description
      String description=DatUtils.getStringProperty(properties,"Emote_Description");
      emote.setDescription(description);
      // Icon
      int iconId=((Integer)properties.getProperty("Emote_IconImage")).intValue();
      File emoteIcon=new File(GeneratedFiles.EMOTE_ICONS_DIR,iconId+".png").getAbsoluteFile();
      if (!emoteIcon.exists())
      {
        DatIconsUtils.buildImageFile(_facade,iconId,emoteIcon);
      }
      emote.setIconId(iconId);
      // Auto
      int granted=((Integer)properties.getProperty("Emote_MustBeGranted")).intValue();
      emote.setAuto(granted==0);
    }
    else
    {
      LOGGER.warn("Could not handle emote ID="+indexDataId);
    }
    return emote;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==WStateClass.EMOTE);
    }
    return false;
  }

  /**
   * Load emotes.
   */
  public void doIt()
  {
    List<EmoteDescription> emotes=new ArrayList<EmoteDescription>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        EmoteDescription emote=load(id);
        if (emote!=null)
        {
          //System.out.println("Emote: "+emote);
          emotes.add(emote);
        }
      }
    }
    // Save emotes
    int nbEmotes=emotes.size();
    LOGGER.info("Writing "+nbEmotes+" emotes");
    boolean ok=EmoteXMLWriter.writeEmotesFile(GeneratedFiles.EMOTES,emotes);
    if (ok)
    {
      System.out.println("Wrote emotes file: "+GeneratedFiles.EMOTES);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatEmotesLoader(facade).doIt();
    facade.dispose();
  }
}
