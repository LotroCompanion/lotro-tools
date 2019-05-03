package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import delta.common.utils.collections.CompoundComparator;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.comparators.DeedDescriptionComparator;
import delta.games.lotro.lore.deeds.comparators.DeedNameComparator;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;

/**
 * Writes deeds to XML file.
 * @author DAM
 */
public class DeedsWriter
{
  /**
   * Write a XML file with a sorted list of deeds.
   * @param deeds Deeds to sort and write.
   * @param out Output file.
   * @return <code>true</code> if it succeeds, <code>false</code> otherwise.
   */
  public static boolean writeSortedDeeds(List<DeedDescription> deeds, File out)
  {
    List<Comparator<DeedDescription>> comparators=new ArrayList<Comparator<DeedDescription>>();
    comparators.add(new IdentifiableComparator<DeedDescription>());
    comparators.add(new DeedNameComparator());
    comparators.add(new DeedDescriptionComparator());
    CompoundComparator<DeedDescription> comparator=new CompoundComparator<DeedDescription>(comparators);
    Collections.sort(deeds,comparator);
    DeedXMLWriter writer=new DeedXMLWriter();
    return writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }
}
