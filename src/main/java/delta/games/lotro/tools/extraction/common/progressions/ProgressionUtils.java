package delta.games.lotro.tools.extraction.common.progressions;

import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.maths.Progression;

/**
 * Progressions loading utilities.
 * @author DAM
 */
public class ProgressionUtils
{
  /**
   * Progressions manager.
   */
  public static final ProgressionsManager PROGRESSIONS_MGR=new ProgressionsManager();

  /**
   * Get a progression curve.
   * @param facade Data facade.
   * @param progressId Progression ID.
   * @return A progression curve or <code>null</code> if not found.
   */
  public static Progression getProgression(DataFacade facade, int progressId)
  {
    Progression ret=PROGRESSIONS_MGR.getProgression(progressId);
    if (ret==null)
    {
      long progressPropertiesId=progressId+DATConstants.DBPROPERTIES_OFFSET;
      PropertiesSet progressProperties=facade.loadProperties(progressPropertiesId);
      if (progressProperties!=null)
      {
        ret=ProgressionFactory.buildProgression(progressId, progressProperties);
        if (ret!=null)
        {
          PROGRESSIONS_MGR.registerProgression(progressId,ret);
        }
      }
    }
    return ret;
  }
}
