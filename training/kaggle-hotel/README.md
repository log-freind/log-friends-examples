# 호텔 예약 취소 오프라인 실험

Kaggle CSV를 검사하고 정해진 후보 모델을 비교·평가·저장하는 CPU 실험입니다. **SDK 이벤트 수집이나 Console의 지원 학습 경로와는 별개**이며, AI 자율 선택·운영 예측 서빙을 구현한 것은 아닙니다.

> 현재 로컬 개발 소스 기준입니다. 공개 main에 없는 보조 파일은 링크하지 않습니다. 과거 실험의 점수·Job 상태를 현재 성능이나 배포 상태로 보장하지 않습니다.

## 데이터와 처리

- 출처는 코드에 기록된 Kaggle `ahsan81/hotel-reservations-classification-dataset` 버전 1의 `Hotel Reservations.csv`입니다. 코드에 기록된 업로더 라이선스는 CC BY 4.0이며 재사용 시 출처·라이선스·변경 내용을 표시해야 합니다. 원수집 과정·진위·비식별 수준은 보증하지 않습니다.
- `simulate.py`는 ZIP 안의 CSV SHA-256을 검토된 스냅샷과 대조합니다. 다른 버전은 거절하며 데이터는 저장소에 포함하지 않습니다.
- 필수 컬럼·ID 고유성·정답·입력 범위를 검사하고 잘못된 달력 날짜는 수량을 보고한 뒤 제외합니다. 도착일 그룹으로 약 60% 학습·20% 검증·20% 평가를 나눕니다.
- 학습 구간에서만 전처리를 학습하고 다수 클래스 기준 모델·로지스틱 회귀·히스토그램 그래디언트 부스팅을 비교합니다. 검증 average precision으로 모델을 고정한 뒤 평가하며, 임계값은 0.5입니다.

## 로컬 실행

Python 3.12가 필요합니다. 검토된 ZIP을 별도로 준비하고 **Examples 저장소 루트**에서 실행하세요. 다운로드·이용 조건은 데이터 제공처에서 확인해야 합니다.

```bash
python3.12 -m venv /tmp/logfriends-hotel-venv
/tmp/logfriends-hotel-venv/bin/pip install --only-binary=:all: -r training/kaggle-hotel/requirements.txt
PYTHONDONTWRITEBYTECODE=1 /tmp/logfriends-hotel-venv/bin/python -m unittest discover -s training/kaggle-hotel -p 'test_*.py'
OPENBLAS_NUM_THREADS=1 OMP_NUM_THREADS=1 MKL_NUM_THREADS=1 \
  /tmp/logfriends-hotel-venv/bin/python training/kaggle-hotel/simulate.py \
  --archive /path/to/lf-kaggle-hotel.zip --output /path/to/new-output
```

출력 폴더는 새 이름이어야 합니다. `model.joblib`·`metrics.json`·`split-manifest.csv`를 저장하고 자체 모델의 재로딩 예측을 확인합니다. 외부 joblib/pickle 파일은 임의로 로딩하지 마세요.

## 한계와 Kubernetes 파일

도착일은 예약 생성·결과 확정 시각이 아닙니다. 입력값의 실제 예측 시점 가용성이나 시간 누수 제거·정답 성숙을 증명하지 못합니다. 고객 ID가 없어 분할 간 동일 고객 중복도 검사하지 못합니다. 가격·특별 요청 수는 시점이 불명확해 모델 입력에서 제외합니다.

결과에는 `production_ready=false`·`sdk_ingest_verified=false`·`ai_model_selection=false`를 기록합니다. 점수를 서비스 성능이나 실제 수집 검증으로 제시하지 마세요.

`Dockerfile`·`prepare-configmap.mjs`·`job.yaml`·`job-3cpu.yaml`·`artifact-reader.yaml`·`recover-artifacts.mjs`는 과거 독립 NAS 실험용입니다. 현재 배포·실행 성공·파일 보존을 보장하지 않습니다. 재사용 전 이미지·실행명·PVC·출력 경로·권한을 검토하고 기존 결과를 보존해야 합니다. CPU 상한은 실제 사용률이나 속도 향상 보장이 아닙니다.

[Examples](../../README.md) · [고정 주문 학습](../order-cpu/README.md)
