package delta.games.lotro.tools.dat.utils.i18n;

import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Default string processor.
 * @author DAM
 */
public class DefaultStringProcessor implements StringProcessor
{
  private int _options;

  /**
   * Constructor.
   */
  public DefaultStringProcessor()
  {
    _options=0;
  }

  /**
   * Set options.
   * @param options Options.
   */
  public void setOptions(int options)
  {
    _options=options;
  }

  @Override
  public String processString(String input)
  {
    String value=DatStringUtils.cleanupString(input);
    if ((_options&I18nUtils.OPTION_REMOVE_MARKS)!=0)
    {
      value=StringUtils.removeMarks(value);
    }
    if ((_options&I18nUtils.OPTION_REMOVE_TRAILING_MARK)!=0)
    {
      value=StringUtils.fixName(value);
    }
    return value;
  }
}
