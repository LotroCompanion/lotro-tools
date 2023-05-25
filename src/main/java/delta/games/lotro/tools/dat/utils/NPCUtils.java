package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.common.Interactable;
import delta.games.lotro.lore.agents.npcs.NPCsManager;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.utils.InteractableUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Utility methods related to NPCs.
 * @author DAM
 */
public class NPCUtils
{
  /**
   * Build a proxy to a NPC.
   * @param npcId NPC identifier.
   * @return A proxy.
   */
  public static Proxy<NpcDescription> buildNPCProxy(int npcId)
  {
    NPCsManager npcsManager=NPCsManager.getInstance();
    NpcDescription npc=npcsManager.getNPCById(npcId);
    String npcName=(npc!=null)?npc.getName():null;
    Proxy<NpcDescription> proxy=new Proxy<NpcDescription>();
    proxy.setId(npcId);
    proxy.setName(npcName);
    proxy.setObject(npc);
    return proxy;
  }

  /**
   * Build a proxy to an interactable.
   * @param id Object identifier.
   * @return A proxy.
   */
  public static Proxy<Interactable> buildInteractableProxy(int id)
  {
    Interactable interactable=InteractableUtils.findInteractable(id);
    String name=(interactable!=null)?interactable.getName():null;
    Proxy<Interactable> proxy=new Proxy<Interactable>();
    proxy.setId(id);
    proxy.setName(name);
    proxy.setObject(interactable);
    return proxy;
  }
}
