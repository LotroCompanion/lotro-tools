package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import delta.common.utils.collections.CompoundComparator;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxy;
import delta.games.lotro.lore.deeds.comparators.DeedDescriptionComparator;
import delta.games.lotro.lore.deeds.comparators.DeedNameComparator;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;

/**
 * Container for deeds.
 * @author DAM
 */
public class DeedsContainer
{
  private File _deedsFile;
  private List<DeedDescription> _deeds;

  /**
   * Constructor.
   * @param deedsFile
   */
  public DeedsContainer(File deedsFile)
  {
    _deedsFile=deedsFile;
    load();
  }

  private void load()
  {
    DeedXMLParser parser=new DeedXMLParser();
    _deeds=parser.parseXML(_deedsFile);
    resolveProxies();
  }

  private void resolveProxies()
  {
    for(DeedDescription deed : _deeds)
    {
      DeedProxy previousProxy=deed.getPreviousDeedProxy();
      if (previousProxy!=null)
      {
        resolveProxy(previousProxy);
      }
      DeedProxy nextProxy=deed.getNextDeedProxy();
      if (nextProxy!=null)
      {
        resolveProxy(nextProxy);
      }
    }
  }

  private void resolveProxy(DeedProxy proxy)
  {
    int idToSearch=proxy.getId();
    for(DeedDescription deed : _deeds)
    {
      if (deed.getIdentifier()==idToSearch)
      {
        proxy.setDeed(deed);
        proxy.setKey(deed.getKey());
        proxy.setName(deed.getName());
        break;
      }
    }
    if (proxy.getDeed()==null)
    {
      System.out.println("Unresolved deed: id="+idToSearch);
    }
  }

  /**
   * Get the managed deeds file.
   * @return the managed deeds file.
   */
  public File getFile()
  {
    return _deedsFile;
  }

  /**
   * Add a deed.
   * @param deed Deed to add.
   */
  public void addDeed(DeedDescription deed)
  {
    _deeds.add(deed);
  }

  /**
   * Get the number of deeds in this container.
   * @return A count.
   */
  public int getCount()
  {
    return _deeds.size();
  }

  /**
   * Get a list of all deeds in this container.
   * @return A possibly empty but not <code>null</code> list of deeds.
   */
  public List<DeedDescription> getAll()
  {
    return new ArrayList<DeedDescription>(_deeds);
  }

  /**
   * Get deeds using a name.
   * @param name Name to search.
   * @return A possibly empty but not <code>null</code> list of deeds.
   */
  public List<DeedDescription> getDeedByName(String name)
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    for(DeedDescription deed : _deeds)
    {
      if (deed.getName().equals(name))
      {
        deeds.add(deed);
      }
    }
    return deeds;
  }

  /**
   * Write a XML file with a sorted list of deeds.
   * @param deeds Deeds to sort and write.
   * @param out Output file.
   */
  public static void writeSortedDeeds(List<DeedDescription> deeds, File out)
  {
    List<Comparator<DeedDescription>> comparators=new ArrayList<Comparator<DeedDescription>>();
    comparators.add(new DeedNameComparator());
    comparators.add(new DeedDescriptionComparator());
    CompoundComparator<DeedDescription> comparator=new CompoundComparator<DeedDescription>(comparators);
    Collections.sort(deeds,comparator);
    DeedXMLWriter writer=new DeedXMLWriter();
    writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }
}
