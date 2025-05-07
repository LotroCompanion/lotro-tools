package delta.games.lotro.tools.extraction.sounds;

import java.util.List;

import delta.common.utils.io.streams.IndentableStream;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.enums.AbstractMapper;
import delta.games.lotro.dat.data.enums.MapperUtils;
import delta.lotro.jukebox.core.model.base.SoundDescription;

/**
 * @author dm
 */
public class SoundContextDump
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SoundContextDump(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Dump the contents of the given context manager.
   * @param contextManager Context manager.
   * @param out Output stream.
   */
  public void dump(SoundContextManager contextManager, IndentableStream out)
  {
    out.println("Sounds sorted by property");
    for(PropertyDefinition propertyDefinition : contextManager.getProperties())
    {
      PropertySoundsRegistry registry=contextManager.getProperty(propertyDefinition);
      dump(registry,out);
    }
  }

  /**
   * Dump the contents of the given registry to the given stream.
   * @param registry Registry.
   * @param out Output stream.
   */
  private void dump(PropertySoundsRegistry registry, IndentableStream out)
  {
    PropertyDefinition property=registry.getProperty();
    out.println("Property: "+property);
    out.incrementIndendationLevel();
    List<Integer> values=registry.getPropertyValues();
    for(Integer value : values)
    {
      String meaning=getPropertyValueMeaning(property,value.intValue());
      out.println("Value: "+value+((meaning!=null)?" ("+meaning+")":""));
      List<SoundDescription> sounds=registry.getSoundsForValue(value.intValue());
      out.incrementIndendationLevel();
      for(SoundDescription sound : sounds)
      {
        out.println("Sound: ID="+sound.getIdentifier()+", channel: "+sound.getTypes());
      }
      out.decrementIndentationLevel();
    }
    out.decrementIndentationLevel();
  }

  private String getPropertyValueMeaning(PropertyDefinition property, int value)
  {
    PropertyType type=property.getPropertyType();
    if (type==PropertyType.ENUM_MAPPER)
    {
      AbstractMapper mapper=MapperUtils.getEnum(_facade,property.getData());
      return mapper.getLabel(value);
    }
    return null;
  }
}
