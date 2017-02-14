package delta.games.lotro.tools.lore.items.tulkas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import delta.common.utils.NumericTools;
import delta.common.utils.text.EndOfLine;
import delta.common.utils.text.TextUtils;

/**
 * Parser for the items DB file or the Tulkas LOTRO plugin.
 * @author DAM
 */
public class TulkasItemsDBParser
{
  /**
   * Oldest format, with explicit stats.
   */
  public static final int OLD_VERSION=1;
  /**
   * Newer format, with coded stats.
   */
  public static final int NEW_VERSION=2;
  /**
   * Items index.
   */
  public static final int INDEX=3;

  private File _inputFile;
  private int _version;
  private String _encoding;

  /**
   * Constructor.
   * @param from Source file.
   * @param encoding File encoding.
   * @param version Tulkas DB version.
   */
  public TulkasItemsDBParser(File from, String encoding, int version)
  {
    _inputFile=from;
    _encoding=encoding;
    _version=version;
  }

  private void parseItemsDef(Map<Object,Object> values, String strValue)
  {
    List<String> items=new ArrayList<String>();
    StringBuilder sb=new StringBuilder();
    int insertIndex=0;
    char[] context=new char[100];
    int length=strValue.length();
    for(int i=0;i<length;i++)
    {
      char c=strValue.charAt(i);
      if (c=='"')
      {
        if ((insertIndex>0)&&(context[insertIndex-1]=='"'))
        {
          insertIndex--;
        }
        else
        {
          context[insertIndex++]=c;
        }
        sb.append(c);
      }
      else if (c=='[')
      {
        context[insertIndex++]=c;
        sb.append(c);
      }
      else if (c==']')
      {
        if ((insertIndex>0)&&(context[insertIndex-1]=='['))
        {
          insertIndex--;
        }
        else
        {
          context[insertIndex++]=c;
        }
        sb.append(c);
      }
      else if (c=='{')
      {
        context[insertIndex++]=c;
        sb.append(c);
      }
      else if (c=='}')
      {
        if ((insertIndex>0)&&(context[insertIndex-1]=='{'))
        {
          insertIndex--;
        }
        else
        {
          context[insertIndex++]=c;
        }
        sb.append(c);
      }
      else if (c==';')
      {
        if (insertIndex==0)
        {
          items.add(sb.toString());
          sb.setLength(0);
        }
        else
        {
          sb.append(c);
        }
      }
      else
      {
        sb.append(c);
      }
    }
    if (sb.length()>0)
    {
      items.add(sb.toString());
    }
    for(String item:items)
    {
      parseItemDef(values,item);
    }
  }

  private void parseItemDef(Map<Object,Object> values, String strValue)
  {
    // ["toto"] = value;
    // [55] = value;
    // where value is "toto", NN, {values}
    int leftSquareBracketIndex=strValue.indexOf("[");
    if (leftSquareBracketIndex!=-1)
    {
      int rightSquareBracketIndex=strValue.indexOf("]",leftSquareBracketIndex+1);
      if (rightSquareBracketIndex!=-1)
      {
        int equalIndex=strValue.indexOf('=',rightSquareBracketIndex+1);
        if (equalIndex!=-1)
        {
          String keyStr=strValue.substring(leftSquareBracketIndex+1,rightSquareBracketIndex);
          Object key=parseKey(keyStr);
          String valueStr=strValue.substring(equalIndex+1);
          Object value=parseValue(valueStr);
          values.put(key,value);
        }
      }
    }
  }

  private Object parseKey(String keyStr)
  {
    Object ret=null;
    keyStr=keyStr.trim();
    int length=keyStr.length();
    if (length>0)
    {
      if ((length>=2)&&(keyStr.charAt(0)=='\"')&&(keyStr.charAt(length-1)=='\"'))
      {
        ret=keyStr.substring(1,length-1);
      }
      else
      {
        ret=NumericTools.parseInteger(keyStr);
      }
    }
    return ret;
  }

  private Object parseValue(String valueStr)
  {
    Object ret=null;
    valueStr=valueStr.trim();
    int length=valueStr.length();
    if (length>0)
    {
      if ((length>=2)&&(valueStr.charAt(0)=='\"')&&(valueStr.charAt(length-1)=='\"'))
      {
        // String
        String value=valueStr.substring(1,length-1);
        if (value.startsWith("0x"))
        {
          ret=Integer.valueOf(value.substring(2),16);
        }
        else
        {
          ret=value;
        }
      }
      else if ((length>=2)&&(valueStr.charAt(0)=='{')&&(valueStr.charAt(length-1)=='}'))
      {
        String itemValue=valueStr.substring(1,length-1);
        if (itemValue.length()>0)
        {
          if (itemValue.charAt(0)=='[')
          {
            // Map
            HashMap<Object,Object> map=new HashMap<Object,Object>();
            parseItemsDef(map,itemValue);
            ret=map;
          }
          else
          {
            // List
            List<Object> list=new ArrayList<Object>();
            String[] values=itemValue.split(",");
            for(String value : values)
            {
              Object o=parseValue(value);
              list.add(o);
            }
            ret=list;
          }
        }
      }
      else if (("false".equals(valueStr)) || ("true".equals(valueStr)))
      {
        ret=Boolean.valueOf(valueStr);
      }
      else if (valueStr.startsWith("0x"))
      {
        ret=Integer.valueOf(valueStr.substring(2),16);
      }
      else
      {
        int index=valueStr.indexOf('.');
        if (index>=0)
        {
          ret=NumericTools.parseFloat(valueStr);
        }
        else
        {
          ret=NumericTools.parseInteger(valueStr);
        }
      }
    }
    return ret;
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    HashMap<Integer,HashMap<Object,Object>> items=loadItems();
    if (_version==OLD_VERSION)
    {
      TulkasItemsLoader1 loader=new TulkasItemsLoader1();
      // Inspect items
      //loader.inspectItems(items);
      // Handle items
      loader.buildItems(items);
      // Handle sets
      loader.buildSets(items);
    }
    else if (_version==NEW_VERSION)
    {
      TulkasItemsLoader2 loader=new TulkasItemsLoader2();
      // Inspect items
      //loader.inspectItems(items);
      // Handle items
      loader.buildItems(items);
      // Handle sets
      loader.buildSets(items);
    }
    else if (_version==INDEX)
    {
      TulkasItemsIndexLoader loader=new TulkasItemsIndexLoader();
      // Inspect items
      //loader.inspectItems(items);
      // Handle items
      loader.buildItems(items);
    }
  }

  private HashMap<Integer,HashMap<Object,Object>> loadItems()
  {
    Locale.setDefault(Locale.US);
    List<String> lines=TextUtils.readAsLines(_inputFile,_encoding);

    List<String> itemLines=new ArrayList<String>();
    boolean foundFirstLine=false;
    StringBuilder currentLine=new StringBuilder();
    for(String line:lines)
    {
      if (!foundFirstLine)
      {
        if (line.equals("{")) foundFirstLine=true;
      }
      else
      {
        if (line.equals("};"))
        {
          if (currentLine.length()>0)
          {
            itemLines.add(currentLine.toString());
            currentLine.setLength(0);
          }
          break;
        }
        if (line.startsWith("{"))
        {
          line=line.substring(1);
        }
        if (line.endsWith("\";};};"))
        {
          line=line.substring(0,line.length()-2);
        }
        if ((line.length()>0)&&(line.charAt(0)=='['))
        {
          if (currentLine.length()>0)
          {
            itemLines.add(currentLine.toString());
            currentLine.setLength(0);
          }
          currentLine.append(line);
        }
        else
        {
          currentLine.append(EndOfLine.UNIX);
          currentLine.append(line);
        }
      }
    }
    lines=null;
    HashMap<Integer,HashMap<Object,Object>> items=new HashMap<Integer,HashMap<Object,Object>>();
    for(String line:itemLines)
    {
      HashMap<Object,Object> map=new HashMap<Object,Object>();
      parseItemsDef(map,line);
      Map.Entry<Object,Object> entry=map.entrySet().iterator().next();
      Integer key=(Integer)entry.getKey();
      @SuppressWarnings("unchecked")
      HashMap<Object,Object> values=(HashMap<Object,Object>)entry.getValue();
      items.put(key,values);
    }
    itemLines=null;
    return items;
  }

  /**
   * Main method.
   * @param args Not used.
   */
  /*
  public static void main(String[] args)
  {
    File old=new File("Items.lua");
    new TulkasItemsDBParser(old,EncodingNames.UTF_8,OLD_VERSION).doIt();
    File index=new File("17_1_Items.lua");
    new TulkasItemsDBParser(index,EncodingNames.UTF_8,INDEX).doIt();
    File new13p1=new File("Items13.1.lua");
    new TulkasItemsDBParser(new13p1,EncodingNames.UTF_8,NEW_VERSION).doIt();
  }
  */
}
