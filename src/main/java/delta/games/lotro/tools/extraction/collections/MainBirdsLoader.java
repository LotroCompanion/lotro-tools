package delta.games.lotro.tools.extraction.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.collections.birds.BirdDescription;
import delta.games.lotro.lore.collections.birds.io.xml.BirdsXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get the birds from DAT files.
 * @author DAM
 */
public class MainBirdsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainBirdsLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainBirdsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("birds",facade.getGlobalStringsManager());
  }

  /**
   * Load data.
   */
  public void doIt()
  {
    List<BirdDescription> baubles=loadBirds();
    save(baubles);
  }

  private List<BirdDescription> loadBirds()
  {
    List<BirdDescription> ret=new ArrayList<BirdDescription>();
    PropertiesSet directoryProps=WeenieContentDirectory.loadWeenieContentProps(_facade,"HobbyBirdDirectory");
    if (directoryProps!=null)
    {
      ItemsManager itemsMgr=ItemsManager.getInstance();
      Object[] idsArray=(Object[])directoryProps.getProperty("Hobby_Birding_BirdItemList");
      for(Object entryObj : idsArray)
      {
        int id=((Integer)entryObj).intValue();
        Item item=itemsMgr.getItem(id);
        PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
        BirdDescription bird=new BirdDescription(item);
        // Call sound ID
        int callSoundID=((Integer)props.getProperty("Hobby_Birding_BirdCall")).intValue();
        bird.setCallSoundID(callSoundID);
        // Bird type
        int birdType=((Integer)props.getProperty("Hobby_Birding_BirdType")).intValue();
        bird.setTypeCode(birdType);
        // Elvish name
        String elvishName=_i18n.getStringProperty(props,"Hobby_Birding_ElvishName");
        bird.setElvishName(elvishName);
        // Icon ID
        int iconID=((Integer)props.getProperty("Hobby_Reward_Icon")).intValue();
        bird.setIconID(iconID);
        File iconFile=new File(GeneratedFiles.BIRD_ICONS_DIR,iconID+".png");
        if (!iconFile.exists())
        {
          DatIconsUtils.buildImageFile(_facade,iconID,iconFile);
        }
        // Large icon ID
        int largeIconID=((Integer)props.getProperty("Hobby_Reward_Icon_Large")).intValue();
        bird.setLargeIconID(largeIconID);
        File largeIconFile=new File(GeneratedFiles.BIRD_ICONS_DIR,largeIconID+".png");
        if (!largeIconFile.exists())
        {
          DatIconsUtils.buildImageFile(_facade,largeIconID,largeIconFile);
        }
        ret.add(bird);
      }
    }
    return ret;
  }

  private void save(List<BirdDescription> birds)
  {
    Collections.sort(birds,new IdentifiableComparator<BirdDescription>());
    boolean ok=BirdsXMLWriter.write(GeneratedFiles.BIRDS,birds);
    if (ok)
    {
      LOGGER.info("Wrote birds file: "+GeneratedFiles.BIRDS);
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
    MainBirdsLoader loader=new MainBirdsLoader(facade);
    loader.doIt();
    facade.dispose();
  }
}
