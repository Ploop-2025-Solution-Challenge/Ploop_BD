package com.example.ploop_backend.domain.map.service;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.entity.TrashSpot;
import com.example.ploop_backend.domain.map.repository.TrashSpotRepository;
import com.example.ploop_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TrashSpotService {

    private final TrashSpotRepository trashSpotRepository;
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    public TrashSpot registerTrashSpot(User user, MultipartFile image, double lat, double lng) throws IOException {
        String imagePath = saveImageToCloud(image);

        TrashSpot spot = TrashSpot.builder()
                .reportedBy(user)
                .latitude(lat)
                .longitude(lng)
                .imageUrl(imagePath)
                .createdAt(LocalDateTime.now())
                .build();

        return trashSpotRepository.save(spot);
    }

    // 쓰레기통 전체 조회 최신순
    public List<TrashSpot> getAllTrashSpots() {
        return trashSpotRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<TrashSpot> getTrashSpotsWithinBounds(double minLat, double maxLat, double minLng, double maxLng) {
        return trashSpotRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);
    }

    public String saveImageToCloud(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        int randomNum = new Random().nextInt(100000);
        String randomFileName = timestamp + "_" + randomNum + extension;

        String fileKey = "trash-spot/" + randomFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "https://image.ploop.store/" + fileKey;
    }
}
