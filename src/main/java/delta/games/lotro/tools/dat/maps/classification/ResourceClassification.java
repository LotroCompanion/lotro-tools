package delta.games.lotro.tools.dat.maps.classification;

/**
 * Resource node classification.
 * @author DAM
 */
public class ResourceClassification extends Classification
{
  private String _nodeType;
  private Integer _tier;
  private String _tierName;

  /**
   * Constructor.
   * @param nodeType Node type (Ruin,Mine,Wood).
   * @param tier Tier.
   * @param tierName Tier name.
   */
  public ResourceClassification(String nodeType, Integer tier, String tierName)
  {
    _nodeType=nodeType;
    _tier=tier;
    _tierName=tierName;
  }

  /**
   * Get the node type.
   * @return the node type.
   */
  public String getNodeType()
  {
    return _nodeType;
  }

  /**
   * Get the node tier.
   * @return the node tier.
   */
  public Integer getTier()
  {
    return _tier;
  }

  /**
   * Get the node tier name.
   * @return the node tier name.
   */
  public String getTierName()
  {
    return _tierName;
  }

  @Override
  public String toString()
  {
    return "Node type="+_nodeType+", tier="+_tier+", tier name="+_tierName;
  }
}
