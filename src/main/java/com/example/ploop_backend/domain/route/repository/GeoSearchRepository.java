package com.example.ploop_backend.domain.route.repository;

import com.example.ploop_backend.dto.route.LatLngDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 반경 내(미터 단위) 쓰레기통/쓰레기 좌표를 조회하는 네이티브 쿼리 리포지토리.
 * - MySQL 8.x, POINT(SRID 4326), SPATIAL INDEX 가정
 * - 테이블명은 예시: trash_bin, trash_spot  (프로젝트 테이블명과 다르면 아래 상수만 수정)
 *
 * 성능 팁:
 *  - ALTER TABLE trash_bin  ADD SPATIAL INDEX idx_bin_loc  (location);
 *  - ALTER TABLE trash_spot ADD SPATIAL INDEX idx_spot_loc (location);
 *  - location 컬럼 타입: POINT NOT NULL SRID 4326
 */
@Repository
@RequiredArgsConstructor
public class GeoSearchRepository {

    private static final String TABLE_BINS  = "trash_bin";
    private static final String TABLE_SPOTS = "trash_spot";

    private final NamedParameterJdbcTemplate jdbc;

    // 거리 계산 및 정렬: ST_Distance_Sphere(POINT, POINT) (미터 단위)
    private static final String QUERY_TEMPLATE = """
            SELECT t.lat AS lat, t.lng AS lng
            FROM trash_bin t
            WHERE ST_Distance_Sphere(
                    POINT(t.longitude, t.latitude),
                    ST_SRID(POINT(?, ?), 4326)
                ) <= ?
            ORDER BY ST_Distance_Sphere(
                    POINT(t.longitude, t.latitude),
                    ST_SRID(POINT(?, ?), 4326)
                ) ASC
            LIMIT ?;
        """;

    public List<LatLngDto> findBinsWithin(LatLngDto center, int radiusMeters, int limit) {
        return query(TABLE_BINS, center, radiusMeters, limit);
    }

    public List<LatLngDto> findSpotsWithin(LatLngDto center, int radiusMeters, int limit) {
        return query(TABLE_SPOTS, center, radiusMeters, limit);
    }

    private List<LatLngDto> query(String table, LatLngDto c, int radius, int limit) {
        String sql = QUERY_TEMPLATE.formatted(table);
        Map<String, Object> params = Map.of(
                "lng", c.getLng(),
                "lat", c.getLat(),
                "radius", radius,
                "limit", Math.max(1, limit)
        );
        return jdbc.query(sql, params, (rs, i) ->
                new LatLngDto(rs.getDouble("lat"), rs.getDouble("lng"))
        );
    }
}
