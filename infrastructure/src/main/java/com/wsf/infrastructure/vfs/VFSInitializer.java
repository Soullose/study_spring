package com.wsf.infrastructure.vfs;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;


public class VFSInitializer {


    private FileObject getOrCreateDataDir() throws FileSystemException {
        FileSystemManager fileSystemManager = VFS.getManager();
        FileObject dataDir = fileSystemManager.resolveFile(SystemUtils.getUserDir().getAbsolutePath())
                .resolveFile(VFSConstant.DATA_DIR_NAME);

        if (!dataDir.exists()) {
            dataDir.createFolder();
        }

        DefaultFileMonitor defaultFileMonitor = new DefaultFileMonitor(new SimpleFileListener());
        defaultFileMonitor.setDelay(500);
        defaultFileMonitor.addFile(dataDir);
        defaultFileMonitor.start();


        return dataDir;
    }


    public void init() {
        try {
            FileObject rootDir = VFS.getManager().createVirtualFileSystem(VFSConstant.VFS_PROTOCOL);
            getOrCreateDataDir();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }


    private static class SimpleFileListener implements FileListener {

        public void fileChanged(FileChangeEvent arg0) throws Exception {
            System.out.println("File changed");

        }

        public void fileCreated(FileChangeEvent arg0) throws Exception {
            System.out.println("File created");
            FileObject fileObject = arg0.getFileObject();
            System.out.println(fileObject.getName());

        }

        public void fileDeleted(FileChangeEvent arg0) throws Exception {
            System.out.println("File deleted");
        }
    }
}
