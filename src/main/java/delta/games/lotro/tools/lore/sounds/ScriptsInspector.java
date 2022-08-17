package delta.games.lotro.tools.lore.sounds;

import java.util.List;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.script.Script;
import delta.games.lotro.dat.data.script.ScriptNode;
import delta.games.lotro.dat.data.script.ScriptNodeData;
import delta.games.lotro.dat.data.script.ScriptNodePort;
import delta.games.lotro.dat.data.script.ScriptNodeType;
import delta.games.lotro.dat.data.script.ScriptPortType;
import delta.games.lotro.dat.data.script.ScriptsTable;
import delta.games.lotro.dat.data.script.port.SwitchPortData;

/**
 * Inspects scripts to find sounds with their context (area, house item...).
 * @author DAM
 */
public class ScriptsInspector
{
  private List<PropertyValue> _propertyValues;

  /**
   * Inspect a scripts table.
   * @param table Data to inspect.
   */
  public void inspect(ScriptsTable table)
  {
    for(Script script : table.getScripts())
    {
      inspectScript(script);
    }
  }

  private void inspectScript(Script script)
  {
    for(ScriptNode root : script.getRoots())
    {
      inspectScriptNode(root);
    }
  }

  private void inspectScriptNode(ScriptNode node)
  {
    ScriptNodeType nodeType=node.getNodeType();
    if (nodeType==ScriptNodeType.SOUND)
    {
      handleSoundNode(node);
    }
    for(ScriptNodePort port : node.getPorts())
    {
      inspectNodePort(node,port);
    }
  }

  private void inspectNodePort(ScriptNode parent, ScriptNodePort port)
  {
    ScriptNodeType nodeType=parent.getNodeType();
    ScriptPortType portType=port.getPortType();
    if ((nodeType==ScriptNodeType.SWITCH) && (portType==ScriptPortType.SWITCH_VALUE))
    {
      SwitchPortData data=(SwitchPortData)port.getData();
      List<PropertyValue> propertyValues=data.getProperties();
      setContext(propertyValues);
    }
    // Context is active during child nodes inspection
    for(ScriptNode childNode : port.getNodes())
    {
      inspectScriptNode(childNode);
    }
    // Clear context
    setContext(null);
  }

  private void setContext(List<PropertyValue> propertyValues)
  {
    _propertyValues=propertyValues;
  }

  private void handleSoundNode(ScriptNode node)
  {
    ScriptNodeData data=node.getData();
    if (data!=null)
    {
      PropertiesSet props=data.getProperties();
      Integer soundID=(Integer)props.getProperty("SoundScript_SoundID");
      System.out.println("Found sound ID: "+soundID);
      if ((_propertyValues!=null) && (_propertyValues.size()>0))
      {
        System.out.println("With context:");
        for(PropertyValue propertyValue : _propertyValues)
        {
          System.out.println("\t"+propertyValue);
        }
      }
    }
  }
}
