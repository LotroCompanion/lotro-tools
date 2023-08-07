package delta.games.lotro.tools.voicesExtractor;

/**
 * Description of a sound entry.
 * @author DAM
 */
public class SoundDescription
{
  private int _identifier;
  private String _name;
  private SoundFormat _format;

  /**
   * Constructor.
   * @param identifier Sound identifier.
   */
  public SoundDescription(int identifier)
  {
    _identifier=identifier;
    _name="";
    _format=SoundFormat.OGG_VORBIS;
  }

  /**
   * Get the sound identifier.
   * @return the identifier.
   */
  public int getIdentifier()
  {
    return _identifier;
  }

  /**
   * Get the sound name.
   * @return the sound name.
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Set the sound name.
   * @param name the name to set.
   */
  public void setName(String name)
  {
    if (name==null)
    {
      name="";
    }
    _name=name;
  }

  /**
   * Get the format of this sound.
   * @return the format of this sound.
   */
  public SoundFormat getFormat()
  {
    return _format;
  }

  /**
   * Set the format of this sound.
   * @param format the format to set.
   */
  public void setFormat(SoundFormat format)
  {
    _format=format;
  }

  @Override
  public String toString()
  {
    StringBuilder sb=new StringBuilder();
    sb.append("Sound: ID=").append(_identifier);
    if ((_name!=null) && (_name.length()>0))
    {
      sb.append(", name=").append(_name);
    }
    return sb.toString();
  }
}
