package delta.games.lotro.tools.updater;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import delta.common.utils.files.FileCopy;
import delta.common.utils.files.FilesDeleter;
import delta.updates.data.DirectoryDescription;
import delta.updates.data.DirectoryEntryDescription;
import delta.updates.data.EntryUtils;
import delta.updates.data.FileDescription;
import delta.updates.data.SoftwareDescription;
import delta.updates.data.SoftwarePackageDescription;
import delta.updates.data.Version;
import delta.updates.data.operations.OperationType;
import delta.updates.data.operations.UpdateOperation;
import delta.updates.data.operations.UpdateOperations;
import delta.updates.engine.LocalDataManager;
import delta.updates.engine.UpdateOperationsBuilder;
import delta.updates.tools.PackagesBuilder;
import delta.updates.tools.ToolsConfig;
import delta.updates.utils.DescriptionBuilder;

/**
 * Updates the given directory so that any difference between
 * the registered state and the current state will be used to build an update/patch package.
 * @author DAM
 */
public class MainBuildUpdatePackage
{
  /**
   * Main method for this tool.
   * @param args
   */
  public static void main(String[] args)
  {
    // Constants
    //String baseURL="http://localhost:8080/delta-web-genea-1.1-SNAPSHOT/app16/${file}";
    String baseURL="https://sourceforge.net/projects/lotrocompanion/files/16/${file}/download";
    File to=new File("d:/tmp/lc16-packages");
    ToolsConfig config=new ToolsConfig(baseURL,to); 
    File from=new File("D:/shared/damien/dev/lotrocompanion/releases/20.3/LotRO Companion/app");
    String packageName="patch20.3.34.1.2";
    Version newVersion=new Version(2032,"20.3.34.1.2");

    // Build software description
    LocalDataManager local=new LocalDataManager(from);
    SoftwareDescription software=local.getSoftware();
    DirectoryDescription directory=local.getDirectoryDescription();
    DescriptionBuilder builder=new DescriptionBuilder();
    DirectoryDescription description=(DirectoryDescription)builder.build(from);
    description.removeEntry(".updates");
    description.setName("");

    // Compute diffs
    UpdateOperationsBuilder diffBuilder=new UpdateOperationsBuilder();
    UpdateOperations operations=diffBuilder.computeDiff(directory,description);
    if (operations.getOperations().size()==0)
    {
      System.out.println("No updates!");
      return;
    }

    // Build patch package
    PackagesBuilder packagesBuilder=new PackagesBuilder(from,config);
    File packageDir=new File(to,"__tmp");
    SoftwarePackageDescription newPackage=null;
    try
    {
      DirectoryDescription packageDescription=buildPackageDir(operations,from,packageDir);
      int packageID=software.getPackages().size();
      newPackage=packagesBuilder.buildPackage(packageID,packageName,packageDescription);
    }
    finally
    {
      // Clean-up
      if (packageDir.exists())
      {
        FilesDeleter deleter=new FilesDeleter(packageDir,null,true);
        deleter.doIt();
      }
    }
    // Handle deleted entries if any
    List<String> deletedEntries=getEntriesToDelete(operations);
    for(String deletedEntry : deletedEntries)
    {
      newPackage.addEntryToDelete(deletedEntry);
    }

    // Update the software description
    software.setVersion(newVersion);
    software.setDate(new Date().getTime());
    List<SoftwarePackageDescription> newPackageDescriptions=new ArrayList<SoftwarePackageDescription>();
    newPackageDescriptions.add(newPackage);
    packagesBuilder.updateSoftware(software,newPackageDescriptions);
  }

  private static DirectoryDescription buildPackageDir(UpdateOperations operations, File fromDir, File packageDir)
  {
    for(UpdateOperation operation : operations.getOperations())
    {
      OperationType type=operation.getOperation();
      if ((type==OperationType.ADD) || (type==OperationType.UPDATE))
      {
        DirectoryEntryDescription entry=operation.getEntry();
        String path=EntryUtils.getPath(entry);
        File to=new File(packageDir,path);
        if (entry instanceof FileDescription)
        {
          File from=new File(fromDir,path);
          to.getParentFile().mkdirs();
          FileCopy.copy(from,to);
        }
        else
        {
          to.mkdirs();
        }
      }
    }
    DescriptionBuilder builder=new DescriptionBuilder();
    DirectoryDescription packageDescription=(DirectoryDescription)builder.build(packageDir);
    if (packageDescription!=null)
    {
      packageDescription.setName("");
    }
    return packageDescription;
  }

  private static List<String> getEntriesToDelete(UpdateOperations operations)
  {
    List<String> ret=new ArrayList<String>();
    for(UpdateOperation operation : operations.getOperations())
    {
      OperationType type=operation.getOperation();
      if (type==OperationType.DELETE)
      {
        DirectoryEntryDescription entry=operation.getEntry();
        String path=EntryUtils.getPath(entry);
        ret.add(path);
      }
    }
    return ret;
  }
}
