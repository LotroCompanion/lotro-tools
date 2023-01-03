package delta.games.lotro.tools.reports;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.TextFileWriter;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.EndOfLine;
import delta.common.utils.text.StringTools;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.loaders.DataIdMapLoader;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;
import delta.games.lotro.dat.loaders.wstate.WSLUtils;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.wlib.ClassDefinition;
import delta.games.lotro.dat.wlib.WLibData;

/**
 * Generator for reference data.
 * @author DAM
 */
public class ReferenceDataGenerator
{
  private static final Logger LOGGER=Logger.getLogger(ReferenceDataGenerator.class);

  private static final File ROOT_DIR=new File("../lotro-companion-private-doc/dat");

  private DataFacade _facade;

  /**
   * Constructor.
   */
  public ReferenceDataGenerator()
  {
    _facade=new DataFacade();
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    dumpWeenieContent();
    dumpLevelTableDirectory();
    dumpProperties();
    dumpEnums();
    dumpMaps();
    dumpWLibClasses();
    dumpWLibClassesHierarchy();
  }

  private void dumpWeenieContent()
  {
    byte[] data=_facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    Integer dataId=map.getDataIdForLabel("WEENIECONTENT");
    if (dataId!=null)
    {
      data=_facade.loadData(dataId.intValue());
      DIDMapper subMap=DataIdMapLoader.decodeDataIdMap(data);
      List<String> subLabels=subMap.getLabels();
      for(String subLabel : subLabels)
      {
        if (subLabel.startsWith("\u0000\u0000")) continue;
        int subDataId=subMap.getDataIdForLabel(subLabel).intValue();
        File toDir=new File(ROOT_DIR,"WeenieContent");
        File to=new File(toDir,subLabel+".txt");
        dumpProperties(subDataId,to);
        //File toWState=new File(toDir,subLabel+".wsl.txt");
        //dumpWState(subDataId,toWState);
      }
    }
  }

  @SuppressWarnings("unused")
  private void dumpWState(int id, File to)
  {
    try
    {
      WStateDataSet decodedData=_facade.loadWState(id);
      if (decodedData!=null)
      {
        StringBuilder sb=new StringBuilder();
        List<Integer> mainReferences=decodedData.getOrphanReferences();
        for(Integer mainReference : mainReferences)
        {
          Object value=decodedData.getValueForReference(mainReference.intValue());
          String display=WSLUtils.getDecodedDataDisplay(value);
          sb.append("#"+mainReference+": "+display).append(EndOfLine.NATIVE_EOL);
        }
        writeFile(to,sb.toString().trim());
      }
    }
    catch(Throwable e)
    {
      LOGGER.error("Error with WSL ID="+id+", "+to, e);
    }
  }

  private void dumpProperties(int id, File to)
  {
    PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (props!=null)
    {
      String dump=props.dump();
      writeFile(to,dump);
    }
  }

  private void dumpLevelTableDirectory()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020E);
    Object[] classIds=(Object[])properties.getProperty("AdvTable_LevelTableList");
    for(Object classId : classIds)
    {
      int propsId=((Integer)classId).intValue();
      File toDir=new File(ROOT_DIR,"LevelTables");
      File to=new File(toDir,"LevelTable-"+propsId+".txt");
      dumpProperties(propsId,to);
    }
  }

  private void dumpProperties()
  {
    StringBuilder sb=new StringBuilder();
    PropertiesRegistry registry=_facade.getPropertiesRegistry();
    List<Integer> ids=registry.getPropertyIds();
    for(Integer id : ids)
    {
      sb.append(id).append(" - ");
      PropertyDefinition def=registry.getPropertyDef(id.intValue());
      String name=def.getName();
      sb.append(name).append(", type=");
      PropertyType type=def.getPropertyType();
      sb.append(type);
      int data=def.getData();
      if (data!=0)
      {
        sb.append(", data=").append(data);
      }
      Object defaultValue=def.getDefaultValue();
      if (defaultValue!=null)
      {
        String defaultValueStr=StringTools.smartToString(defaultValue);
        sb.append(", default value=").append(defaultValueStr);
      }
      Object minValue=def.getMinValue();
      if (minValue!=null)
      {
        String minValueStr=StringTools.smartToString(minValue);
        sb.append(", min value=").append(minValueStr);
      }
      Object maxValue=def.getMaxValue();
      if (maxValue!=null)
      {
        String maxValueStr=StringTools.smartToString(maxValue);
        sb.append(", max value=").append(maxValueStr);
      }
      sb.append(EndOfLine.NATIVE_EOL);
      List<PropertyDefinition> childProperties=def.getChildProperties();
      if (childProperties!=null)
      {
        for(PropertyDefinition childProperty : childProperties)
        {
          sb.append('\t').append(childProperty).append(EndOfLine.NATIVE_EOL);
        }
      }
    }

    File to=new File(ROOT_DIR,"properties.txt");
    writeFile(to,sb.toString());
  }

  private void dumpEnums()
  {
    StringBuilder sb=new StringBuilder();
    DATInspectionTool.loadEnumsRegistry(_facade,sb);
    File to=new File(ROOT_DIR,"enums.txt");
    writeFile(to,sb.toString());
  }

  private void dumpMaps()
  {
    PropertiesSet props=null;
    UILayout layout=new UILayoutLoader(_facade).loadUiLayout(0x22000041);
    for(UIElement uiElement : layout.getChildElements())
    {
      if (uiElement.getIdentifier()==268437543) // MapBackground
      {
        props=uiElement.getProperties();
        break;
      }
    }
    File to=new File(ROOT_DIR,"mapsSystemProps.txt");
    writeFile(to,props.dump());
  }

  private void dumpWLibClasses()
  {
    WLibData wlibData=_facade.getWLibData();
    List<Integer> classIndexes=wlibData.getClassIndexes();
    StringBuilder sb=new StringBuilder();
    for(Integer classIndex : classIndexes)
    {
      ClassDefinition classDefinition=wlibData.getClass(classIndex.intValue());
      sb.append(classDefinition).append(EndOfLine.NATIVE_EOL);
    }
    String contents=sb.toString().trim();
    File wDir=new File(ROOT_DIR,"W");
    File to=new File(wDir,"classes.txt");
    writeFile(to,contents);
  }

  private void dumpWLibClassesHierarchy()
  {
    WLibData wlibData=_facade.getWLibData();
    ClassesTreeGenerator builder=new ClassesTreeGenerator();
    String classTree=builder.dumpClassesTree(wlibData);
    File wDir=new File(ROOT_DIR,"W");
    File to=new File(wDir,"classesTree.txt");
    writeFile(to,classTree);
  }

  private void writeFile(File to, String contents)
  {
    TextFileWriter w=new TextFileWriter(to,EncodingNames.UTF_8);
    w.start();
    w.writeSomeText(contents);
    w.terminate();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    new ReferenceDataGenerator().doIt();
  }
}
