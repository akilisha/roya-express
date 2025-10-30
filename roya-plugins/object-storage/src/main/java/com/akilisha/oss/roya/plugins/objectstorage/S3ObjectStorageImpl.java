package com.akilisha.oss.roya.plugins.objectstorage;

import io.helidon.config.Config;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Instant;
import java.time.Duration;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class S3ObjectStorageImpl implements ObjectStorage {
    private final S3Client s3;
    private final String defaultBucket;
    private final S3Presigner presigner;

    S3ObjectStorageImpl(Config cfg) {
        String endpoint = cfg.get("objectStorage.endpoint").asString().orElse("http://localhost:9000");
        String regionStr = cfg.get("objectStorage.region").asString().orElse("us-east-1");
        boolean pathStyle = cfg.get("objectStorage.pathStyleAccess").asBoolean().orElse(true);
        String accessKey = cfg.get("objectStorage.accessKey").asString().orElse("minioadmin");
        String secretKey = cfg.get("objectStorage.secretKey").asString().orElse("minioadmin");
        this.defaultBucket = cfg.get("objectStorage.defaultBucket").asString().orElse(null);

        var creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        var s3cfg = S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build();
        this.s3 = S3Client.builder()
            .credentialsProvider(creds)
            .serviceConfiguration(s3cfg)
            .region(Region.of(regionStr))
            .endpointOverride(java.net.URI.create(endpoint))
            .build();

        this.presigner = S3Presigner.builder()
            .credentialsProvider(creds)
            .region(Region.of(regionStr))
            .endpointOverride(java.net.URI.create(endpoint))
            .build();
    }

    @Override
    public String put(String bucket, String key, byte[] data, String contentType, Map<String, String> metadata) {
        return put(bucket, key, RequestBody.fromBytes(data), contentType, metadata);
    }

    @Override
    public String put(String bucket, String key, InputStream data, long contentLength, String contentType, Map<String, String> metadata) {
        return put(bucket, key, RequestBody.fromInputStream(data, contentLength), contentType, metadata);
    }

    private String put(String bucket, String key, RequestBody body, String contentType, Map<String,String> metadata) {
        String b = bucketOrDefault(bucket);
        ensureBucket(b);
        PutObjectRequest.Builder req = PutObjectRequest.builder()
            .bucket(b)
            .key(key)
            .contentType(contentType);
        if (metadata != null && !metadata.isEmpty()) req = req.metadata(metadata);
        PutObjectResponse resp = s3.putObject(req.build(), body);
        return resp.eTag();
    }

    @Override
    public Optional<ObjectData> get(String bucket, String key) {
        String b = bucketOrDefault(bucket);
        try {
            HeadObjectResponse head = s3.headObject(HeadObjectRequest.builder().bucket(b).key(key).build());
            ResponseBytes<GetObjectResponse> bytes = s3.getObjectAsBytes(GetObjectRequest.builder().bucket(b).key(key).build());
            long size = head.contentLength();
            String ct = head.contentType();
            Map<String,String> meta = head.metadata();
            var stream = new java.io.ByteArrayInputStream(bytes.asByteArray());
            ObjectData data = new ObjectData() {
                @Override public InputStream stream() { return stream; }
                @Override public long size() { return size; }
                @Override public String contentType() { return ct; }
                @Override public Map<String, String> metadata() { return meta; }
            };
            return Optional.of(data);
        } catch (S3Exception e) {
            return Optional.empty();
        }
        
    }

    @Override
    public boolean delete(String bucket, String key) {
        String b = bucketOrDefault(bucket);
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(b).key(key).build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    @Override
    public List<ObjectInfo> list(String bucket, String prefix) {
        String b = bucketOrDefault(bucket);
        ListObjectsV2Request req = ListObjectsV2Request.builder()
            .bucket(b)
            .prefix(prefix == null ? "" : prefix)
            .build();
        ListObjectsV2Response resp = s3.listObjectsV2(req);
        List<ObjectInfo> out = new ArrayList<>();
        for (S3Object o : resp.contents()) {
            out.add(new ObjectInfo() {
                @Override public String key() { return o.key(); }
                @Override public long size() { return o.size(); }
                @Override public Instant lastModified() { return o.lastModified(); }
                @Override public String etag() { return o.eTag(); }
            });
        }
        return out;
    }

    @Override
    public URL presignedGet(String bucket, String key, Duration ttl) {
        String b = bucketOrDefault(bucket);
        GetObjectRequest get = GetObjectRequest.builder().bucket(b).key(key).build();
        GetObjectPresignRequest preq = GetObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .getObjectRequest(get)
            .build();
        PresignedGetObjectRequest presigned = presigner.presignGetObject(preq);
        return presigned.url();
    }

    @Override
    public URL presignedPut(String bucket, String key, Duration ttl, String contentType) {
        String b = bucketOrDefault(bucket);
        PutObjectRequest put = PutObjectRequest.builder().bucket(b).key(key).contentType(contentType).build();
        PutObjectPresignRequest preq = PutObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .putObjectRequest(put)
            .build();
        PresignedPutObjectRequest presigned = presigner.presignPutObject(preq);
        return presigned.url();
    }

    @Override
    public String multipartPut(String bucket, String key, InputStream data, long contentLength, String contentType, Map<String,String> metadata, int partSizeMb) {
        String b = bucketOrDefault(bucket);
        ensureBucket(b);
        int partSize = Math.max(partSizeMb, 5) * 1024 * 1024; // S3 minimum 5MB parts (except last)

        CreateMultipartUploadRequest.Builder createReq = CreateMultipartUploadRequest.builder()
            .bucket(b)
            .key(key)
            .contentType(contentType);
        if (metadata != null && !metadata.isEmpty()) createReq = createReq.metadata(metadata);
        CreateMultipartUploadResponse createResp = s3.createMultipartUpload(createReq.build());
        String uploadId = createResp.uploadId();
        List<CompletedPart> completed = new ArrayList<>();

        try {
            byte[] buffer = new byte[partSize];
            int partNumber = 1;
            long remaining = contentLength;
            while (remaining > 0) {
                int toRead = (int)Math.min(buffer.length, remaining);
                int read = 0;
                while (read < toRead) {
                    int r = data.read(buffer, read, toRead - read);
                    if (r == -1) break;
                    read += r;
                }
                if (read <= 0) break;
                UploadPartRequest upReq = UploadPartRequest.builder()
                    .bucket(b)
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .contentLength((long) read)
                    .build();
                UploadPartResponse upResp = s3.uploadPart(upReq, RequestBody.fromBytes(read == buffer.length ? buffer : java.util.Arrays.copyOf(buffer, read)));
                completed.add(CompletedPart.builder().partNumber(partNumber).eTag(upResp.eTag()).build());
                partNumber++;
                remaining -= read;
            }

            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder().parts(completed).build();
            CompleteMultipartUploadRequest compReq = CompleteMultipartUploadRequest.builder()
                .bucket(b)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completedUpload)
                .build();
            CompleteMultipartUploadResponse compResp = s3.completeMultipartUpload(compReq);
            return compResp.eTag();
        } catch (Exception e) {
            try {
                s3.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(b).key(key).uploadId(uploadId).build());
            } catch (Exception ignore) {}
            if (e instanceof RuntimeException re) throw re;
            throw new RuntimeException(e);
        }
    }

    private void ensureBucket(String bucket) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            }
        }
    }

    private String bucketOrDefault(String bucket) {
        String b = bucket != null ? bucket : defaultBucket;
        if (b == null || b.isBlank()) throw new IllegalArgumentException("Bucket is required (no default configured)");
        return b;
    }
}



