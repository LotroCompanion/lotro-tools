package delta.games.lotro.tools.extraction.utils;

import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Utility methods related to stat modifiers.
 * @author DAM
 */
public class ModifiersUtils
{
  /**
   * Get some stat modifiers.
   * @param props Properties to use.
   * @param statModArrayPropName Property name to use.
   * @return A list of stat modifiers, or <code>null</code>.
   */
  public static ModPropertyList getStatModifiers(PropertiesSet props, String statModArrayPropName)
  {
    Object[] statIDObjs=(Object[])props.getProperty(statModArrayPropName);
    if (statIDObjs==null)
    {
      return null;
    }
    boolean doKeep=false;
    ModPropertyList ret=new ModPropertyList();
    for (Object statIDObj : statIDObjs)
    {
      Integer statID=(Integer)statIDObj;
      if ((statID != null) && (statID.intValue()>0))
      {
        ret.addID(statID.intValue());
        doKeep=true;
      }
    }
    return doKeep?ret:null;
  }
}
