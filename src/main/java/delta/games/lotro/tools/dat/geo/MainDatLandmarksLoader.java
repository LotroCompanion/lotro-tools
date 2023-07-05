package delta.games.lotro.tools.dat.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.geo.landmarks.LandmarkDescription;
import delta.games.lotro.lore.geo.landmarks.io.xml.LandmarksXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get landmarks from DAT files.
 * @author DAM
 */
public class MainDatLandmarksLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatLandmarksLoader.class);

  private DataFacade _facade;
  private Map<Integer,LandmarkDescription> _data;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLandmarksLoader(DataFacade facade)
  {
    _facade=facade;
    _data=new HashMap<Integer,LandmarkDescription>();
    _i18n=new I18nUtils("landmarks",facade.getGlobalStringsManager());
  }

  private LandmarkDescription handleLandmark(int landmarkId)
  {
    PropertiesSet props=_facade.loadProperties(landmarkId+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return null;
    }
    //System.out.println("*********** Place: "+landmarkId+" ****************");
    //System.out.println(landmarkProperties.dump());
    // Name
    String name=_i18n.getNameStringProperty(props,"Name",landmarkId,0);
    // Type:
    // MapNote_Type: 4398046511104=2^(43-1), see Enum: MapNoteType, (id=587202775), 43=Point of Interest
    // 1099511627776 = 2^(41-1) => Settlement
    LandmarkDescription ret=new LandmarkDescription(landmarkId,name);
    return ret;
  }

  /**
   * Save landmarks data.
   */
  public void save()
  {
    // Data
    List<LandmarkDescription> landmarks=new ArrayList<LandmarkDescription>(_data.values());
    Collections.sort(landmarks,new IdentifiableComparator<LandmarkDescription>());
    boolean ok=LandmarksXMLWriter.writeLandmarksFile(GeneratedFiles.LANDMARKS,landmarks);
    if (ok)
    {
      LOGGER.info("Wrote landmarks file: "+GeneratedFiles.LANDMARKS);
    }
    // Labels
    _i18n.save();
  }

  /**
   * Load all landmarks.
   */
  public void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==1210)
        {
          LandmarkDescription landmark=handleLandmark(id);
          if (landmark!=null)
          {
            _data.put(Integer.valueOf(id),landmark);
          }
        }
      }
    }
    save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatLandmarksLoader(facade).doIt();
    facade.dispose();
  }
}
