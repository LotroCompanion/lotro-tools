package delta.games.lotro.tools.extraction.geo.markers.classification;

/**
 * Logger classification.
 * @author DAM
 */
public class CategoryClassification extends Classification
{
  private int _code;
  private String _label;

  /**
   * Constructor.
   * @param code Logger code.
   * @param label Logger label.
   */
  public CategoryClassification(int code, String label)
  {
    _code=code;
    _label=label;
  }

  @Override
  public String toString()
  {
    return "Category: "+_code+" ("+_label+")";
  }
}
