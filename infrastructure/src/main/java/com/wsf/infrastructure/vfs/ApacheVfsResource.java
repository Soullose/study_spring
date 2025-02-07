package com.wsf.infrastructure.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.VfsResource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

public class ApacheVfsResource  extends AbstractResource implements WritableResource {

    private final static Logger log = LoggerFactory.getLogger(ApacheVfsResource.class);

    private static final Method METHOD_GET_LOCAL_FILE = ReflectionUtils.findMethod(LocalFile.class, "getLocalFile");
    private static final Method METHOD_DO_ATTACH = ReflectionUtils.findMethod(LocalFile.class, "doAttach");

    static {
        METHOD_GET_LOCAL_FILE.setAccessible(true);
        METHOD_DO_ATTACH.setAccessible(true);
    }

    private FileObject fileObject;

    public ApacheVfsResource(String location) {
        log.debug("ApacheVfsResource:{}", location);
        try {
            this.fileObject = VFS.getManager().getBaseFile().resolveFile(location);
        } catch (FileSystemException e) {
            e.printStackTrace();
            this.fileObject = null;
        }
    }

    @Override
    public boolean isWritable() {
        try {
            return this.fileObject.isWriteable();
        } catch (FileSystemException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.fileObject.getContent().getOutputStream();
    }

    @Override
    public boolean exists() {
        try {
            return this.fileObject != null && this.fileObject.exists();
        } catch (FileSystemException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        try {
            return this.fileObject.isReadable();
        } catch (FileSystemException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isOpen() {
        return this.fileObject.isContentOpen();
    }

    @Override
    public boolean isFile() {
        try {
            return this.fileObject.isFile();
        } catch (FileSystemException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public URL getURL() throws IOException {
        try {

            return this.fileObject.getURL();
        } catch (FileSystemException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public URI getURI() throws IOException {
        return this.fileObject.getURI();
    }

    @Override
    public File getFile() throws IOException {
        FileObject file = this.fileObject;

        if (file instanceof DelegateFileObject) {
            file = ((DelegateFileObject<?>) file).getDelegateFile();
        }

        if (file instanceof LocalFile) {

            file = (LocalFile) file;

            try {
                METHOD_DO_ATTACH.invoke(file);
                return (File) METHOD_GET_LOCAL_FILE.invoke(file);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public long contentLength() throws IOException {
        return this.fileObject.getContent().getSize();
    }

    @Override
    public long lastModified() throws IOException {
        return this.fileObject.getContent().getLastModifiedTime();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return new VfsResource(fileObject.resolveFile(relativePath));
    }

    @Override
    public String getFilename() {
        return this.fileObject.getName().getBaseName();
    }

    @Override
    public String getDescription() {
        return this.fileObject.getName().getURI();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.fileObject.getContent().getInputStream();
    }
}
