package edu.stanford.fsi.reap.web.rest;

import edu.stanford.fsi.reap.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileResource {
  private final FileService fileService;

  public FileResource(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/{filename}")
  public ResponseEntity<?> redirectToOss(@PathVariable String filename) {
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
        .header("Location", fileService.generatePresignedUrlForDownload(filename))
        .build();
  }
}
