package com.wsf.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.wsf.api.service.FileService;

public class FileServiceImpl implements FileService {
    @Override
    public String extractChecksum(String path, String algorithm) throws NoSuchAlgorithmException, IOException {
        return "";
    }

    @Override
    public String extractChecksum(InputStream stream, String algorithm) throws NoSuchAlgorithmException, IOException {
        return "";
    }
}
