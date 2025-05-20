package delta.games.lotro.tools.voicesExtractor.npc;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for the voices of a single NPC.
 * @author DAM
 */
public class NpcVoices
{
  private List<NpcVoice> _voices;

  /**
   * Constructor.
   */
  public NpcVoices()
  {
    _voices=new ArrayList<NpcVoice>();
  }

  /**
   * Add a sound.
   * @param questID ID of involved quest.
   * @param scriptID ID of involved script.
   * @param soundID ID of the involved sound.
   */
  public void addSound(int questID, int scriptID, int soundID)
  {
    NpcVoice voice=new NpcVoice();
    voice.questID=questID;
    voice.scriptID=scriptID;
    voice.soundID=soundID;
    _voices.add(voice);
  }

  /**
   * Get the sound identifiers for a given quest and script ID.
   * @param questID ID of involved quest.
   * @param scriptID ID of involved script.
   * @return A list of sound identifiers.
   */
  public List<Integer> getSounds(int questID, int scriptID)
  {
    List<Integer> ret=new ArrayList<Integer>();
    for(NpcVoice voice : _voices)
    {
      if ((voice.questID==questID) && (voice.scriptID==scriptID))
      {
        ret.add(Integer.valueOf(voice.soundID));
      }
    }
    return ret;
  }
}
