package delta.games.lotro.tools.extraction.skills.pips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.PipType;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.pip.PipDescription;
import delta.games.lotro.lore.pip.io.xml.PipXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Loader for PIPs definitions.
 * @author DAM
 */
public class PipsLoader
{
  private DataFacade _facade;
  private I18nUtils _i18n;
  private LotroEnum<PipType> _pipTypeEnum;

  /**
   * Constructor.
   * @param facade Facade.
   */
  public PipsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("pip",facade.getGlobalStringsManager());
    LotroEnumsRegistry lotroEnumRegistry=LotroEnumsRegistry.getInstance();
    _pipTypeEnum=lotroEnumRegistry.get(PipType.class);
  }

  /**
   * Load the PIPs data.
   * @return a list of loaded pips.
   */
  private List<PipDescription> load()
  {
    List<PipDescription> ret=new ArrayList<PipDescription>();
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"PipControl");
    Object[] pipArray=(Object[])props.getProperty("PipControl_Directory");
    for(Object pipIdObj : pipArray)
    {
      int pipId=((Integer)pipIdObj).intValue();
      PropertiesSet pipProps=_facade.loadProperties(pipId+DATConstants.DBPROPERTIES_OFFSET);
      // Type
      int pipTypeCode=((Integer)pipProps.getProperty("Pip_Type")).intValue();
      PipType pipType=_pipTypeEnum.getEntry(pipTypeCode);
      PipDescription pip=new PipDescription(pipType);
      // Name
      String pipName=_i18n.getNameStringProperty(pipProps,"Pip_Name",pipTypeCode,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      pip.setName(pipName);
      // Min
      int min=((Integer)pipProps.getProperty("Pip_Min")).intValue();
      pip.setMin(min);
      Integer minIcon=(Integer)pipProps.getProperty("Pip_Examination_Min_Icon");
      pip.setIconMin(minIcon);
      // Max
      int max=((Integer)pipProps.getProperty("Pip_Max")).intValue();
      pip.setMax(max);
      Integer maxIcon=(Integer)pipProps.getProperty("Pip_Examination_Max_Icon");
      pip.setIconMax(maxIcon);
      // Home
      int home=((Integer)pipProps.getProperty("Pip_Home")).intValue();
      pip.setHome(home);
      Integer homeIcon=(Integer)pipProps.getProperty("Pip_Examination_Home_Icon");
      pip.setIconHome(homeIcon);
      ret.add(pip);
    }
    return ret;
  }

  private void save(List<PipDescription> pips)
  {
    List<PipDescription> sortedList=new ArrayList<PipDescription>(pips);
    Collections.sort(sortedList,new IdentifiableComparator<PipDescription>());
    PipXMLWriter.writePipsFile(GeneratedFiles.PIPS,sortedList);
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<PipDescription> pips=load();
    save(pips);
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PipsLoader pipsLoader=new PipsLoader(facade);
    pipsLoader.doIt();
  }
}
