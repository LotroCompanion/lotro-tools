package delta.games.lotro.tools.lore.items.tulkas;

import org.apache.log4j.Logger;

import delta.games.lotro.utils.FixedDecimalsInteger;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Utilities to parse stat values.
 * @author DAM
 */
public class TulkasValuesUtils
{
  private static final Logger _logger=LotroLoggers.getLotroLogger();

  /**
   * Build a fixed decimal integer from an object value.
   * @param objectValue External representation string.
   * @return A value.
   */
  public static FixedDecimalsInteger fromObjectValue(Object objectValue)
  {
    FixedDecimalsInteger ret=null;
    if (objectValue instanceof Integer)
    {
      ret=new FixedDecimalsInteger(((Integer)objectValue).intValue());
    }
    else if (objectValue instanceof Float)
    {
      ret=new FixedDecimalsInteger(((Float)objectValue).floatValue());
    }
    else
    {
      _logger.warn("Unmanaged value: " + objectValue);
    }
    return ret;
  }
}
