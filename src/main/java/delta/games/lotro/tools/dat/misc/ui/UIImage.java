package delta.games.lotro.tools.dat.misc.ui;

/**
 * UI data of type 'image'.
 * @author DAM
 */
public class UIImage extends UIData
{
  /**
   * Image identifier.
   */
  public int _imageDID;
  /**
   * Filename.
   */
  public String _filename;

  public String toString()
  {
    StringBuilder sb=new StringBuilder("Image: DID=");
    sb.append(_imageDID);
    if (_filename.length()>0)
    {
      sb.append(" (").append(_filename).append(')');
    }
    return sb.toString();
  }
}
