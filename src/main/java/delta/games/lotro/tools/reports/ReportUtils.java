package delta.games.lotro.tools.reports;

import java.util.Map;

import delta.common.utils.variables.VariablesResolver;
import delta.games.lotro.character.BaseCharacterSummary;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;
import delta.games.lotro.utils.strings.ContextVariableValueProvider;
import delta.games.lotro.utils.strings.RenderingUtils;

/**
 * Utility methods related to reports.
 * @author DAM
 */
public class ReportUtils
{
  private static final BaseCharacterSummary DEFAULT_SUMMARY=RenderingUtils.buildDefaultSummary();

  /**
   * Build a renderer for reports.
   * @return A renderer.
   */
  public static VariablesResolver buildRenderer()
  {
    Map<String,String> context=RenderingUtils.setupContext(DEFAULT_SUMMARY);
    ContextVariableValueProvider provider=new ContextVariableValueProvider(context);
    StringRenderer renderer=new StringRenderer(provider);
    return renderer.getResolver();
  }
}
