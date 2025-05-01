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
                    Mission.builder().name("빈 캔 줍기").description("길가나 공원에 떨어진 빈 캔을 수거하세요.").category(Category.EMPTY_CAN).build(),
                    Mission.builder().name("플라스틱 병 수거").description("플라스틱 병 3개를 주워 정해진 장소에 버려주세요.").category(Category.PLASTIC_BOTTLE).build(),
                    Mission.builder().name("페트병 뚜껑 모으기").description("페트병 뚜껑을 5개 수거해보세요.").category(Category.BOTTLE_CAP).build(),
                    Mission.builder().name("종이컵 수거").description("쓰레기로 버려진 종이컵을 3개 주워주세요.").category(Category.PAPER_CUP).build(),
                    Mission.builder().name("비닐봉지 제거").description("날아다니는 비닐봉지 또는 버려진 비닐을 주워주세요.").category(Category.VINYL_BAG).build()
            ));

        }
    }
}

