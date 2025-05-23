package com.example.ploop_backend.initializer;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.mission.model.Category;
import com.example.ploop_backend.domain.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MissionRepository missionRepository;

    @Override
    public void run(String... args) {
        if (missionRepository.count() == 0) {
            missionRepository.saveAll(List.of(
                    Mission.builder()
                            .name("Pick up 1 empty can")
                            .description("길가나 공원에 떨어진 빈 캔을 수거하세요.")
                            .category(Category.EMPTY_CAN)
                            .requiredCount(1)
                            .build(),

                    Mission.builder()
                            .name("Collect 3 plastic bottles")
                            .description("플라스틱 병 3개를 주워 정해진 장소에 버려주세요.")
                            .category(Category.PLASTIC_BOTTLE)
                            .requiredCount(3)
                            .build(),

                    Mission.builder()
                            .name("Gather 5 plastic bottle caps")
                            .description("페트병 뚜껑을 5개 수거해보세요.")
                            .category(Category.BOTTLE_CAP)
                            .requiredCount(5)
                            .build(),

                    Mission.builder()
                            .name("Collect 3 paper cups")
                            .description("쓰레기로 버려진 종이컵을 3개 주워주세요.")
                            .category(Category.PAPER_CUP)
                            .requiredCount(3)
                            .build(),

                    Mission.builder()
                            .name("Remove 2 plastic bags")
                            .description("날아다니는 비닐봉지 또는 버려진 비닐 2개를 주워주세요.")
                            .category(Category.VINYL_BAG)
                            .requiredCount(2)
                            .build()
            ));
        }
    }
}

