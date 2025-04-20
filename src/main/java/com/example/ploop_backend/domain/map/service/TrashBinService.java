package com.example.ploop_backend.domain.map.service;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.repository.TrashBinRepository;
import com.example.ploop_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrashBinService {

    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    private final TrashBinRepository trashBinRepository;

    public TrashBin registerTrashBin(User user, MultipartFile image, double lat, double lng) throws IOException {
        String imagePath = saveImageToCloud(image); // 이미지 R2에 업로드

        TrashBin trashBin = TrashBin.builder() // URL, 위치, 사용자 정보를 쓰레기통 객체에 저장
                .createdBy(user)
                .latitude(lat)
                .longitude(lng)
                .imageUrl(imagePath)
                .createdAt(LocalDateTime.now())
                .build();

        return trashBinRepository.save(trashBin); // DB에 저장
    }

    public List<TrashBin> getAllTrashBins() {
        return trashBinRepository.findAllByOrderByCreatedAtDesc();
    } // 쓰레기통 전체 조회 최신순

    public void deleteTrashBin(Long id, User user) {
        TrashBin bin = trashBinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 쓰레기통입니다."));

        trashBinRepository.delete(bin);
    }

    // 업로드된 이미지 R2에 저장 -> URL 반환
    public String saveImageToCloud(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        int randomNum = new Random().nextInt(100000);
        String randomFileName = timestamp + "_" + randomNum + extension;

        String fileKey = "trash-bin/" + randomFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        String fileUrl = "https://image.ploop.store/" + fileKey;

        return fileUrl;
    }
}
