package delta.games.lotro.tools.lore.sounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.id.IdentifiableComparator;
import delta.lotro.jukebox.core.model.SoundDescription;
import delta.lotro.jukebox.core.model.SoundType;

/**
 * Registry for known sounds.
 * @author DAM
 */
public class SoundsRegistry
{
  private Map<Integer,SoundDescription> _sounds;

  /**
   * Constructor.
   */
  public SoundsRegistry()
  {
    _sounds=new HashMap<Integer,SoundDescription>();
  }

  /**
   * Register a sound.
   * @param soundID Sound identifier.
   * @param sound Sound to register.
   */
  public void registerSound(int soundID, SoundDescription sound)
  {
    Integer key=Integer.valueOf(soundID);
    _sounds.put(key,sound);
  }

  /**
   * Get a sound using its identifier.
   * @param soundID Sound identifier.
   * @return A sound or <code>null<:code> if not found.
   */
  public SoundDescription getSound(int soundID)
  {
    Integer key=Integer.valueOf(soundID);
    SoundDescription ret=_sounds.get(key);
    return ret;
  }

  /**
   * Get all known sounds, sorted by their ID.
   * @return A list of sound descriptions.
   */
  public List<SoundDescription> getKnownSounds()
  {
    List<SoundDescription> ret=new ArrayList<SoundDescription>(_sounds.values());
    Collections.sort(ret,new IdentifiableComparator<SoundDescription>());
    return ret;
  }

  /**
   * Get the sound type from the channel ID.
   * @param soundChannel Channel ID.
   * @return A sound type or <code>null</code> if not found.
   */
  public static SoundType getSoundType(int soundChannel)
  {
    if (soundChannel==1) return SoundType.COMBAT;
    if (soundChannel==2) return SoundType.MUSIC;
    if (soundChannel==3) return SoundType.UI;
    if (soundChannel==4) return SoundType.VO;
    if (soundChannel==5) return SoundType.QUANTIZED_PLAYER_MUSIC;
    if (soundChannel==6) return SoundType.PLAYER_MUSIC;
    if (soundChannel==7) return SoundType.AMBIENT;
    if (soundChannel==8) return SoundType.SFX;
    // 0: default
    //LOGGER.warn("Unmanaged sound channel: "+soundChannel);
    return null;
  }
}
