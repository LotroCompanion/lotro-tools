package delta.games.lotro.tools.extraction.geo.maps.finder;

import java.util.Comparator;

import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;

/**
 * Comparator for maps, using their geographic size. Big maps come first.
 * @author DAM
 */
public class MapSizeComparator implements Comparator<GeoreferencedBasemap>
{
  @Override
  public int compare(GeoreferencedBasemap o1, GeoreferencedBasemap o2)
  {
    float f1=o1.getGeoReference().getGeo2PixelFactor();
    float f2=o2.getGeoReference().getGeo2PixelFactor();
    return Float.compare(f1,f2);
  }
}
