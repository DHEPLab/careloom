package edu.stanford.fsi.reap.service;

import java.net.URL;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Slf4j
public class AWSFileService extends FileService {
  private final S3Presigner s3Presigner;
  private static final int EXPIRATION = 5;

  @Value("${application.aws.bucket-name}")
  private String bucketName;

  public AWSFileService(S3Client s3Client) {
    this.s3Presigner = S3Presigner.builder().s3Client(s3Client).build();
  }

  public URL generatePresignedUrlForUpload(String format) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(generateFilename(format)).build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(EXPIRATION))
            .putObjectRequest(putObjectRequest)
            .build();

    return s3Presigner.presignPutObject(presignRequest).url();
  }

  public String generatePresignedUrlForDownload(String objectKey) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(EXPIRATION))
            .getObjectRequest(getObjectRequest)
            .build();

    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }
}
