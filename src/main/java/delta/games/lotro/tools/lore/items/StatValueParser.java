package delta.games.lotro.tools.lore.items;

import delta.common.utils.NumericTools;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Parses stat values.
 * @author DAM
 */
public class StatValueParser
{
  /**
   * Parse a stat value.
   * @param valueStr Input string.
   * @return A value or <code>null</code>.
   */
  public static FixedDecimalsInteger parseStatValue(String valueStr)
  {
    FixedDecimalsInteger ret=null;
    int factor=1;
    if (valueStr.startsWith("="))
    {
      valueStr=valueStr.substring(1);
      factor=100;
    }
    if (valueStr.endsWith("%"))
    {
      valueStr=valueStr.substring(0,valueStr.length()-1);
    }
    valueStr=valueStr.replace(',','.').trim();
    if (valueStr.contains("."))
    {
      Float statValue=NumericTools.parseFloat(valueStr);
      if (statValue!=null)
      {
        float value=statValue.floatValue();
        value*=factor;
        ret=new FixedDecimalsInteger(value);
      }
    }
    else
    {
      Integer statValue=NumericTools.parseInteger(valueStr);
      if (statValue!=null)
      {
        int value=statValue.intValue();
        ret=new FixedDecimalsInteger(value);
      }
    }
    return ret;
  }
}
