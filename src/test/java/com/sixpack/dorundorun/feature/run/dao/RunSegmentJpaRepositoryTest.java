package com.sixpack.dorundorun.feature.run.dao;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.repository.RepositoryTest;

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
			.nickname("테스터")
			.deviceToken("test-device-token-123")
			.personalConsentAt(LocalDateTime.now())
			.build();
		testUser = userRepository.save(testUser);

		testRunSession = RunSession.builder()
			.user(testUser)
			.distanceTotal(5000L)
			.durationTotal(1800L)
			.paceAvg(360L)
			.cadenceAvg(180)
			.cadenceMax(200)
			.build();
		testRunSession = runSessionRepository.save(testRunSession);

		testSegmentData = Arrays.asList(
			new RunSegmentData(LocalDateTime.of(2025, 9, 13, 10, 0, 0),
				37.123456, 127.123456, 50.0, 100L, 350L, 15.0, 175),
			new RunSegmentData(LocalDateTime.of(2025, 9, 13, 10, 0, 1),
				37.123457, 127.123457, 51.0, 200L, 360L, 16.0, 180),
			new RunSegmentData(LocalDateTime.of(2025, 9, 13, 10, 0, 2),
				37.123458, 127.123458, 52.0, 300L, 370L, 17.0, 185)
		);
	}

	@Test
	@DisplayName("RunSegment 저장 테스트")
	void saveRunSegment() {
		// given
		RunSegmentInfo info = new RunSegmentInfo(testSegmentData, false, false);
		RunSegment runSegment = RunSegment.builder()
			.runSession(testRunSession)
			.data(info)
			.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);

		// then
		assertThat(savedRunSegment.getId()).isNotNull();
		assertThat(savedRunSegment.getRunSession().getId()).isEqualTo(testRunSession.getId());
		assertThat(savedRunSegment.getData().segments()).hasSize(3);
		assertThat(savedRunSegment.getData().segments().get(0).latitude()).isEqualTo(37.123456);
		assertThat(savedRunSegment.getData().segments().get(2).cadence()).isEqualTo(185);
	}

	@Test
	@DisplayName("RunSegment ID로 조회 테스트")
	void findRunSegmentById() {
		// given
		RunSegmentInfo info = new RunSegmentInfo(testSegmentData, false, false);
		RunSegment savedRunSegment = runSegmentRepository.save(
			RunSegment.builder().runSession(testRunSession).data(info).build()
		);

		// when
		Optional<RunSegment> foundRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(foundRunSegment).isPresent();
		assertThat(foundRunSegment.get().getData().segments()).hasSize(3);
		assertThat(foundRunSegment.get().getData().segments().get(1).longitude()).isEqualTo(127.123457);
		assertThat(foundRunSegment.get().getRunSession().getUser().getNickname()).isEqualTo("테스터");
		assertThat(foundRunSegment.get().getRunSession().getUser().getDeviceToken()).isEqualTo("test-device-token-123");
	}

	@Test
	@DisplayName("RunSegment JSON 데이터 매핑 저장/조회 테스트")
	void saveAndRetrieveJsonData() {
		// given
		List<RunSegmentData> complexData = Arrays.asList(
			new RunSegmentData(LocalDateTime.of(2025, 9, 13, 10, 0, 0),
				null, 127.123456, null, 100L, 350L, 15.0, null),
			new RunSegmentData(LocalDateTime.of(2025, 9, 13, 10, 0, 1),
				37.123457, 127.123457, 51.0, 200L, 360L, 16.0, 180)
		);
		RunSegmentInfo info = new RunSegmentInfo(complexData, false, false);

		RunSegment runSegment = RunSegment.builder()
			.runSession(testRunSession)
			.data(info)
			.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		runSegmentRepository.flush();
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(retrievedRunSegment).isPresent();
		List<RunSegmentData> retrievedData = retrievedRunSegment.get().getData().segments();

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
		RunSegmentInfo info = new RunSegmentInfo(List.of(), false, false);
		RunSegment runSegment = RunSegment.builder()
			.runSession(testRunSession)
			.data(info)
			.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(retrievedRunSegment).isPresent();
		assertThat(retrievedRunSegment.get().getData().segments()).isEmpty();
	}

	@Test
	@DisplayName("RunSegment 업데이트 테스트")
	void updateRunSegmentData() {
		// given - 처음엔 1개만 저장
		RunSegmentInfo initialInfo = new RunSegmentInfo(testSegmentData.subList(0, 1), false, false);
		RunSegment savedRunSegment = runSegmentRepository.save(
			RunSegment.builder().runSession(testRunSession).data(initialInfo).build()
		);

		// when - 3개로 업데이트
		RunSegmentInfo updatedInfo = new RunSegmentInfo(testSegmentData, false, false);
		runSegmentRepository.deleteById(savedRunSegment.getId());
		RunSegment newSavedRunSegment = runSegmentRepository.save(
			RunSegment.builder().runSession(testRunSession).data(updatedInfo).build()
		);

		// then
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(newSavedRunSegment.getId());
		assertThat(retrievedRunSegment).isPresent();
		assertThat(retrievedRunSegment.get().getData().segments()).hasSize(3);
	}

	@Test
	@DisplayName("RunSegment 삭제 테스트")
	void deleteRunSegment() {
		// given
		RunSegmentInfo info = new RunSegmentInfo(testSegmentData, false, false);
		RunSegment savedRunSegment = runSegmentRepository.save(
			RunSegment.builder().runSession(testRunSession).data(info).build()
		);

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
		RunSegmentInfo info = new RunSegmentInfo(testSegmentData, false, false);
		RunSegment runSegment = RunSegment.builder()
			.runSession(testRunSession)
			.data(info)
			.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);

		// then
		assertThat(savedRunSegment.getRunSession()).isNotNull();
		assertThat(savedRunSegment.getRunSession().getId()).isEqualTo(testRunSession.getId());
		assertThat(savedRunSegment.getRunSession().getUser().getNickname()).isEqualTo("테스터");
		assertThat(savedRunSegment.getRunSession().getUser().getDeviceToken()).isEqualTo("test-device-token-123");
		assertThat(savedRunSegment.getRunSession().getDistanceTotal()).isEqualTo(5000L);
	}

	@Test
	@DisplayName("대용량 RunSegmentData 저장/조회 테스트")
	void saveLargeRunSegmentData() {
		// given
		List<RunSegmentData> largeDataSet = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			LocalDateTime time = LocalDateTime.of(2025, 9, 13, 10, 0, 0).plusSeconds(i);
			largeDataSet.add(new RunSegmentData(
				time,
				37.123456 + (i * 0.0001),
				127.123456 + (i * 0.0001),
				50.0 + i,
				(long)(100 + i * 10),
				(long)(350 + i),
				(double)(15 + i),
				175 + i
			));
		}
		RunSegmentInfo info = new RunSegmentInfo(largeDataSet, false, false);

		RunSegment runSegment = RunSegment.builder()
			.runSession(testRunSession)
			.data(info)
			.build();

		// when
		RunSegment savedRunSegment = runSegmentRepository.save(runSegment);
		Optional<RunSegment> retrievedRunSegment = runSegmentRepository.findById(savedRunSegment.getId());

		// then
		assertThat(retrievedRunSegment).isPresent();
		assertThat(retrievedRunSegment.get().getData().segments()).hasSize(1000);
	}
}
