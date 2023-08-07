package delta.games.lotro.tools.voicesExtractor;

/**
 * Sound format.
 * @author DAM
 */
public enum SoundFormat
{
  /**
   * Ogg+Vorbis.
   */
  OGG_VORBIS("Ogg Vorbis"),
  /**
   * WAV.
   */
  WAV("WAV");

  private String _label;

  private SoundFormat(String label)
  {
    _label=label;
  }

  @Override
  public String toString()
  {
    return _label;
  }
}
