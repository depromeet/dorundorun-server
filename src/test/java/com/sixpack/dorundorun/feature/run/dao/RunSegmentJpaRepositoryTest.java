package com.sixpack.dorundorun.feature.run.dao;

import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.repository.RepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RunSegmentJpaRepositoryTest extends RepositoryTest {

	@Autowired
	private RunSegmentJpaRepository runSegmentRepository;

	@Autowired
	private RunSessionJpaRepository runSessionRepository;

	@Autowired
	private UserJpaRepository userRepository;

	private User testUser;
	private RunSession testRunSession;
	private List<RunSegmentData> testSegmentData;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.name("테스트사용자")
				.email("test@example.com")
				.password("password123")
				.nickname("테스터")
				.runningLevel("BEGINNER")
				.build();
		testUser = userRepository.save(testUser);
		testRunSession = RunSession.builder()
				.user(testUser)
				.totalDistance(5000L)
				.totalDuration(1800L)
				.avgPace(360L)
				.avgCadence(180)
				.maxCadence(200)
				.build();
		testRunSession = runSessionRepository.save(testRunSession);
		testSegmentData = Arrays.asList(
				new RunSegmentData(
						LocalDateTime.of(2025, 9, 13, 10, 0, 0),
						37.123456,
						127.123456,
						50.0,
						100L,
						350L,
						15L,
						175
				),
				new RunSegmentData(
						LocalDateTime.of(2025, 9, 13, 10, 0, 1),
						37.123457,
						127.123457,
						51.0,
						200L,
						360L,
						16L,
						180
				),
				new RunSegmentData(
						LocalDateTime.of(2025, 9, 13, 10, 0, 2),
						37.123458,
						127.123458,
						52.0,
						300L,
						370L,
						17L,
						185
				)
		);
	}

	@Test
	@DisplayName("RunSegment 저장 테스트")
	void saveRunSegment() {
		// given
		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(testSegmentData)
				.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		System.out.println(savedRunSegment.getData());

		// then
		assertThat(savedRunSegment.getId()).isNotNull();
		assertThat(savedRunSegment.getRunSession().getId()).isEqualTo(testRunSession.getId());
		assertThat(savedRunSegment.getData()).hasSize(3);
		assertThat(savedRunSegment.getData().get(0).latitude()).isEqualTo(37.123456);
		assertThat(savedRunSegment.getData().get(2).cadence()).isEqualTo(185);
	}

	@Test
	@DisplayName("RunSegment ID로 조회 테스트")
	void findRunSegmentById() {
		// given
		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(testSegmentData)
				.build();
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);

		// when
		Optional<RunSegment> foundRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(foundRunSegment).isPresent();
		assertThat(foundRunSegment.get().getData()).hasSize(3);
		assertThat(foundRunSegment.get().getData().get(1).longitude()).isEqualTo(127.123457);
		assertThat(foundRunSegment.get().getRunSession().getUser().getEmail()).isEqualTo("test@example.com");
	}

	@Test
	@DisplayName("RunSegment JSON 데이터 매핑 저장/조회 테스트")
	void saveAndRetrieveJsonData() {
		// given
		List<RunSegmentData> complexData = Arrays.asList(
				new RunSegmentData(
						LocalDateTime.of(2025, 9, 13, 10, 0, 0),
						null,  // null 위도
						127.123456,
						null,  // null 고도
						100L,
						350L,
						15L,
						null   // null 보폭
				),
				new RunSegmentData(
						LocalDateTime.of(2025, 9, 13, 10, 0, 1),
						37.123457,
						127.123457,
						51.0,
						200L,
						360L,
						16L,
						180
				)
		);

		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(complexData)
				.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		runSegmentRepository.flush();
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(retrievedRunSegment).isPresent();
		List<RunSegmentData> retrievedData = retrievedRunSegment.get().getData();

		assertThat(retrievedData).hasSize(2);

		// null 값 확인
		assertThat(retrievedData.get(0).latitude()).isNull();
		assertThat(retrievedData.get(0).altitude()).isNull();
		assertThat(retrievedData.get(0).cadence()).isNull();

		// 정상 값 확인
		assertThat(retrievedData.get(0).longitude()).isEqualTo(127.123456);
		assertThat(retrievedData.get(1).latitude()).isEqualTo(37.123457);
		assertThat(retrievedData.get(1).cadence()).isEqualTo(180);
	}

	@Test
	@DisplayName("빈 RunSegmentData 리스트 저장/조회 테스트")
	void saveAndRetrieveEmptyDataList() {
		// given
		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(List.of())
				.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(retrievedRunSegment).isPresent();
		assertThat(retrievedRunSegment.get().getData()).isEmpty();
	}

	@Test
	@DisplayName("RunSegment 업데이트 테스트")
	void updateRunSegmentData() {
		// given
		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(testSegmentData.subList(0, 1)) // 첫 번째 데이터만 저장
				.build();
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);

		// when - 데이터 추가 업데이트 (실제로는 새로운 객체를 생성해야 함 - 불변 객체)
		List<RunSegmentData> updatedData = Arrays.asList(
				testSegmentData.get(0),
				testSegmentData.get(1),
				testSegmentData.get(2)
		);

		RunSegment updatedRunSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(updatedData)
				.build();

		runSegmentRepository.deleteById(savedRunSegment.getId());
		RunSegment newSavedRunSegment = runSegmentRepository.save(updatedRunSegment);

		// then
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(newSavedRunSegment.getId());
		assertThat(retrievedRunSegment).isPresent();
		assertThat(retrievedRunSegment.get().getData()).hasSize(3);
	}

	@Test
	@DisplayName("RunSegment 삭제 테스트")
	void deleteRunSegment() {
		// given
		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(testSegmentData)
				.build();
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);

		// when
		runSegmentRepository.deleteById(savedRunSegment.getId());

		// then
		Optional<RunSegment> deletedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());
		assertThat(deletedRunSegment).isNotPresent();
	}

	@Test
	@DisplayName("RunSession과의 연관관계 테스트")
	void testRunSessionRelationship() {
		// given
		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(testSegmentData)
				.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);

		// then
		assertThat(savedRunSegment.getRunSession()).isNotNull();
		assertThat(savedRunSegment.getRunSession().getId()).isEqualTo(testRunSession.getId());
		assertThat(savedRunSegment.getRunSession().getUser().getEmail()).isEqualTo("test@example.com");
		assertThat(savedRunSegment.getRunSession().getTotalDistance()).isEqualTo(5000L);
	}

	@Test
	@DisplayName("대용량 RunSegmentData 저장/조회 테스트")
	void saveLargeRunSegmentData() {
		// given - 1000개의 데이터 포인트 생성
		List<RunSegmentData> largeDataSet = new java.util.ArrayList<>();

		for (int i = 0; i < 1000; i++) {
			LocalDateTime time = LocalDateTime.of(2025, 9, 13, 10, 0, 0).plusSeconds(i);

			largeDataSet.add(new RunSegmentData(
					time,
					37.123456 + (i * 0.0001),
					127.123456 + (i * 0.0001),
					50.0 + i,
					(long) (100 + i * 10),
					(long) (350 + i),
					(long) (15 + i),
					175 + i
			));
		}

		RunSegment runSegment = RunSegment.builder()
				.runSession(testRunSession)
				.data(largeDataSet)
				.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(retrievedRunSegment).isPresent();
		assertThat(retrievedRunSegment.get().getData().size()).isEqualTo(1000);
	}
}
