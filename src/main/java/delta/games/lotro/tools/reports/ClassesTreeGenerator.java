package delta.games.lotro.tools.reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.collections.TreeNode;
import delta.common.utils.text.EndOfLine;
import delta.games.lotro.dat.wlib.ClassDefinition;
import delta.games.lotro.dat.wlib.WLibData;

/**
 * Tool to build a tree view of the WSL classes hierarchy.
 * @author DAM
 */
public class ClassesTreeGenerator
{
  /**
   * Dump a tree view of the WSL classes hierarchy.
   * @param data WLib data.
   * @return A displayable string.
   */
  public String dumpClassesTree(WLibData data)
  {
    TreeNode<ClassDefinition> tree=buildClassesTree(data);
    return dumpTree(tree);
  }

  private TreeNode<ClassDefinition> buildClassesTree(WLibData data)
  {
    TreeNode<ClassDefinition> rootNode=new TreeNode<ClassDefinition>(null);
    Map<Integer,TreeNode<ClassDefinition>> nodes=new HashMap<Integer,TreeNode<ClassDefinition>>();
    // Pass #1: create all nodes
    List<Integer> classIndexes=data.getClassIndexes();
    for(Integer classIndex : classIndexes)
    {
      ClassDefinition classDefinition=data.getClass(classIndex.intValue());
      TreeNode<ClassDefinition> node=new TreeNode<ClassDefinition>(classDefinition);
      nodes.put(classIndex,node);
    }
    // Pass #2: parent nodes
    for(Integer classIndex : classIndexes)
    {
      ClassDefinition classDefinition=data.getClass(classIndex.intValue());
      TreeNode<ClassDefinition> node=nodes.get(classIndex);
      ClassDefinition parentClass=classDefinition.getParent();
      if (parentClass!=null)
      {
        int parentIndex=parentClass.getClassIndex();
        TreeNode<ClassDefinition> parentNode=nodes.get(Integer.valueOf(parentIndex));
        node.changeSuperNode(parentNode);
      }
      else
      {
        node.changeSuperNode(rootNode);
      }
    }
    dumpTree(rootNode);
    return rootNode;
  }

  private String dumpTree(TreeNode<ClassDefinition> rootNode)
  {
    StringBuilder sb=new StringBuilder();
    dumpTree(sb,rootNode,-3);
    return sb.toString();
  }

  private void dumpTree(StringBuilder sb, TreeNode<ClassDefinition> node, int step)
  {
    ClassDefinition classDefinition=node.getData();
    dumpClass(sb,classDefinition,step);
    int nb=node.getNumberOfChildren();
    for(int i=0;i<nb;i++)
    {
      TreeNode<ClassDefinition> childNode=node.getChild(i);
      dumpTree(sb,childNode,step+3);
    }
  }

  private void dumpClass(StringBuilder sb, ClassDefinition classDefinition, int step)
  {
    if (step>=0)
    {
      for(int i=0;i<step;i++) sb.append(' ');
      String label="";
      if (classDefinition!=null)
      {
        String name=classDefinition.getName();
        if (name!=null)
        {
          label=name+" ";
        }
        int index=classDefinition.getClassIndex();
        label=label+"("+index+")";
      }
      sb.append(label).append(EndOfLine.NATIVE_EOL);
    }
  }
}
