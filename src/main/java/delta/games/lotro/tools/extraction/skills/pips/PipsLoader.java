package delta.games.lotro.tools.extraction.skills.pips;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Load for PIPs definitions.
 * @author DAM
 */
public class PipsLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade
   */
  public PipsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load the PIPs data.
   */
  public void loadPipDB()
  {
    PropertiesSet props=_facade.loadProperties(1879048735+DATConstants.DBPROPERTIES_OFFSET);
    Object[] PipControl_Directory=(Object[])(props.getProperty("PipControl_Directory"));
    for(Object PipControl : PipControl_Directory)
    {
      int pipControlID=((Integer)PipControl).intValue();
      props=_facade.loadProperties(pipControlID+DATConstants.DBPROPERTIES_OFFSET);
      Integer pipType=(Integer)props.getProperty("Pip_Type");
      String pipName=(String)props.getProperty("Pip_Name");
      Integer pipMin=(Integer)props.getProperty("Pip_Min");
      Integer pipMax=(Integer)props.getProperty("Pip_Max");
      Integer pipHome=(Integer)props.getProperty("Pip_Home");
      Integer minIcon=(Integer)props.getProperty("Pip_Examination_Min_Icon");
      Integer maxIcon=(Integer)props.getProperty("Pip_Examination_Max_Icon");
      Integer examinationHomeIcon=(Integer)props.getProperty("Pip_Examination_Home_Icon");
    }
  }
}
