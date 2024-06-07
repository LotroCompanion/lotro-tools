package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Tool methods for data identification.
 * @author DAM
 */
public class DataIdMgr
{
  private static final Logger LOGGER=Logger.getLogger(DataIdMgr.class);

  private Map<Integer,DataIdentification> _cache=new HashMap<Integer,DataIdentification>();
  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DataIdMgr(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("markers",facade.getGlobalStringsManager());
  }

  /**
   * Identify a piece of data.
   * @param did Data identifier.
   * @return the data identification.
   */
  public DataIdentification identify(int did)
  {
    DataIdentification dataId=_cache.get(Integer.valueOf(did));
    if (dataId==null)
    {
      int wClass=getClassIndex(_facade,did);
      PropertiesSet props=_facade.loadProperties(did+DATConstants.DBPROPERTIES_OFFSET);
      String name=getNameFromProps(did,props);
      dataId=new DataIdentification(did,name,wClass);
      _cache.put(Integer.valueOf(did),dataId);
    }
    return dataId;
  }

  /**
   * Get the class of a piece of data.
   * @param facade Data facade.
   * @param did Data identifier.
   * @return A class or <code>null</code> if not found.
   */
  private static int getClassIndex(DataFacade facade, int did)
  {
    byte[] data=facade.loadData(did);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return classDefIndex;
    }
    LOGGER.warn("Cannot load data: "+did);
    return 0;
  }

  /**
   * Get a name from some properties
   * @param did Data ID.
   * @param props Properties to use.
   * @return A name.
   */
  public String getNameFromProps(int did, PropertiesSet props)
  {
    String ret=null;
    if (props!=null)
    {
      ret=_i18n.getNameStringProperty(props,"Name",did,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      if (ret==null)
      {
        ret=_i18n.getNameStringProperty(props,"Area_Name",did,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      }
    }
    if (ret==null)
    {
      ret="?";
    }
    return ret;
  }

  /**
   * Save data (labels).
   * @param toDir Root directory to use.
   */
  public void save(File toDir)
  {
    _i18n.save(toDir);
  }
}
