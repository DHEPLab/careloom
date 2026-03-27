package edu.stanford.fsi.reap.service;

import java.net.URL;
import java.util.UUID;

public abstract class FileService {
  public abstract URL generatePresignedUrlForUpload(String format);

  public abstract String generatePresignedUrlForDownload(String key);

  protected String generateFilename(String format) {
    return UUID.randomUUID().toString().replace("-", "") + "." + format;
  }
}
