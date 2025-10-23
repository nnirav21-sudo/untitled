package org.lld.compositedesignpattern.filesystem;

import java.util.ArrayList;
import java.util.List;

public class FileSystemDemo {

   static interface FileSystem{
        void ls();
    }

    static class File implements FileSystem{
       String fileName;

       public File(String fileName){
           this.fileName = fileName;
       }
       @Override
        public void ls() {
           System.out.println("file name: "+fileName);
       }
    }
    static class Directory implements FileSystem{
       String directoryName;
       List<FileSystem> fileSystems;
       public Directory(String directoryName){
           this.directoryName = directoryName;
           this.fileSystems = new ArrayList<>();
       }

        @Override
        public void ls() {
           System.out.println("directory name: "+directoryName);
            for(FileSystem fileSystem:fileSystems){
                fileSystem.ls();
            }
        }
    }

    public static void main(String[] args){
       File file1 = new File("avengers");
       Directory directory = new Directory("movies");
       directory.fileSystems.add(file1);
       Directory directory2 = new Directory("comdey movies");
       File file2 = new File("hungama");
       directory2.fileSystems.add(file2);
       directory.fileSystems.add(directory2);
       directory.ls();

    }

}
