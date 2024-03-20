package delta.games.lotro.tools.dat.maps.classification;

/**
 * Category classification.
 * @author DAM
 */
public class CategoryClassification extends Classification
{
  private int _code;
  private String _label;

  /**
   * Constructor.
   * @param code Category code.
   * @param label Category label.
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
