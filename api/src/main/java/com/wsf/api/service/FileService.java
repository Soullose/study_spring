package com.wsf.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

public interface FileService {
    /**
     * 提取文件 checksum
     *
     * @param path      文件全路径
     * @param algorithm 算法名 例如 MD5、SHA-1、SHA-256等
     * @return          checksum
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws IOException              the io exception
     */
    String extractChecksum(String path, String algorithm) throws NoSuchAlgorithmException, IOException;

    /**
     * 提取文件 checksum
     *
     * @param stream        文件流
     * @param algorithm     算法名 例如 MD5、SHA-1、SHA-256等
     * @return              checksum
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws IOException              the io exception
     */
    String extractChecksum(InputStream stream, String algorithm) throws NoSuchAlgorithmException, IOException;
}
