package delta.games.lotro.tools.extraction.utils;

import delta.common.utils.variables.VariableValueProvider;
import delta.games.lotro.dat.data.strings.renderer.OptionItem;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;

/**
 * Utilities related to string rendering.
 * @author DAM
 */
public class StringRenderingUtils
{
  /**
   * Build a <code>StringRenderer</code> that will use all options.
   * @return A <code>StringRenderer</code>.
   */
  public static StringRenderer buildAllOptionsRenderer()
  {
    VariableValueProvider provider=new VariableValueProvider()
    {
      @Override
      public String getVariable(String variableName)
      {
        return "";
      }
    };
    StringRenderer renderer=new StringRenderer(provider)
    {
      /**
       * Render all options.
       */
      @Override
      public void renderOptions(StringBuilder sb, OptionItem[] options, String value)
      {
        int nbOptions=options.length;
        for(int i=0;i<nbOptions;i++)
        {
          if (i>0) sb.append(" / ");
          sb.append(options[i].getText());
        }
      }
    };
    return renderer;
  }
}
