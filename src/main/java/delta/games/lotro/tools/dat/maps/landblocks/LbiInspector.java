package delta.games.lotro.tools.dat.maps.landblocks;

import java.io.PrintStream;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.EntityDescriptor;
import delta.games.lotro.lore.maps.landblocks.Cell;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;
import delta.games.lotro.tools.dat.maps.data.LbiLink;
import delta.games.lotro.tools.dat.maps.data.Weenie;

/**
 * Inspects the contents of LandBlockInfos.
 * @author DAM
 */
public class LbiInspector
{
  private static final Logger LOGGER=Logger.getLogger(LbiInspector.class);

  /**
   * Inspect a single LandBlockInfo.
   * @param lbi Input data.
   */
  public void handleLbi(LandBlockInfo lbi)
  {
    checkEntities(lbi);
    checkLinks(lbi);
    checkOrphanWeenies(lbi);
  }

  /**
   * Dump the contents of a landblock.
   * @param lbi Landblock info.
   * @param out Output stream.
   */
  public void dump(LandBlockInfo lbi, PrintStream out)
  {
    out.println("Props: "+lbi.getProps());
    // Cells
    List<Cell> cells=lbi.getCells();
    out.println("Cells: ("+cells.size()+")");
    for(Cell cell : cells)
    {
      out.println("\t"+cell);
    }
    // Entities
    List<EntityDescriptor> entities=lbi.getEntities();
    out.println("Entities: ("+entities.size()+")");
    for(EntityDescriptor entity : entities)
    {
      out.println("\t"+entity);
    }
    // Links
    List<LbiLink> links=lbi.getLinks();
    out.println("Links: ("+links.size()+")");
    for(LbiLink link : links)
    {
      out.println("\t"+link);
    }
    // Weenies
    List<Weenie> weenies=lbi.getWeenies();
    out.println("Weenies: ("+weenies.size()+")");
    for(Weenie weenie : weenies)
    {
      out.println("\t"+weenie);
    }
  }

  private void checkEntities(LandBlockInfo lbi)
  {
    List<EntityDescriptor> entities=lbi.getEntities();
    for(EntityDescriptor entity : entities)
    {
      long entityIid=entity.getIid();
      Weenie weenie=lbi.getWeenieByIid(entityIid);
      if (weenie!=null)
      {
        //System.out.println("\tFound weenie: "+String.format("%08X",Long.valueOf(weenie.getIid())));
      }
      else
      {
        LOGGER.warn("No weenie for entity: "+String.format("%08X",Long.valueOf(entityIid)));
      }
    }
    // Conclusion: each entity has an associated weenie
    // Verified on all landscape LandBlocks for all 4 regions
  }

  private void checkLinks(LandBlockInfo lbi)
  {
    // Iterate on links
    List<LbiLink> links=lbi.getLinks();
    for(LbiLink link : links)
    {
      // From
      long fromIid=link.getFromIid();
      String from=findSource(lbi,fromIid);
      if (from==null)
      {
        LOGGER.warn("No from source for IID="+String.format("%08X",Long.valueOf(fromIid)));
      }
      // From
      long toIid=link.getToIid();
      String to=findSource(lbi,toIid);
      if (to==null)
      {
        LOGGER.warn("No to source for IID="+String.format("%08X",Long.valueOf(toIid)));
      }
      if ((from!=null) && (to!=null))
      {
        System.out.println("Link: from "+from+" to "+to+" - type="+link.getType());
      }
    }
  }

  private String findSource(LandBlockInfo lbi, long iid)
  {
    String source=null;
    EntityDescriptor entity=lbi.getEntityByIid(iid);
    if (entity!=null)
    {
      source="Entity "+String.format("%08X",Long.valueOf(iid))+" "+entity.getType();
    }
    else
    {
      Weenie weenie=lbi.getWeenieByIid(iid);
      if (weenie!=null)
      {
        source="Weenie "+String.format("%08X",Long.valueOf(iid));
      }
    }
    return source;
  }

  private void checkOrphanWeenies(LandBlockInfo lbi)
  {
    // Iterate on weenies
    List<Weenie> weenies=lbi.getWeenies();
    for(Weenie weenie : weenies)
    {
      long iid=weenie.getIid();
      EntityDescriptor entity=lbi.getEntityByIid(iid);
      if (entity==null)
      {
        System.out.println("Orphan weenie: "+weenie);
      }
    }
    // Use cases for orphan weenies (weenies with no associated entity):
    // - use as from in Generator_PositionSet links, attached to 'to' entities of type GeneratorPoint
    // - use as from and to in Generator links (weenie to weenie)
    //    Found properties: Quest_InstanceItemKey(from), Entity_ContentLayers(to)
    //    Found properties on from: NPC_PlayingStartingIdle: 1, NPC_StartingIdleAnimation: 83 (Victim_Hurt_LyingDown)
    // - weenies with no link:
    //    4 with Name and PortalDestination properties
    //    Found properties: MapNote_Enabled
  }
}
