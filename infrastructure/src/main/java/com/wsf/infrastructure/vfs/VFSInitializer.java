package com.wsf.infrastructure.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;



public class VFSInitializer {
	public void test() throws FileSystemException {
		FileSystemManager fileSystemManager = VFS.getManager();
		fileSystemManager.addOperationProvider();
	}
}
