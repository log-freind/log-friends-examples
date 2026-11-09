# 합성 주문 CPU 학습 예제

합성 주문 한 실행의 CSV로 로지스틱 회귀를 학습·평가하고 모델을 저장하는 **독립 데모**입니다. LLM 호출·Pod의 이벤트 DB 접근·예측 서비스 배포는 하지 않습니다.

> 현재 로컬 개발 소스 기준입니다. 공개 main에 없는 보조 파일은 링크하지 않습니다. 아래 설명은 과거 NAS 실행 성공이나 현재 배포 상태를 보장하지 않습니다.

## 처리 흐름

1. CSV·manifest의 실행 ID와 SHA-256을 확인하고 주문 ID·정답·입력 타입·1시간 관찰 구간을 검사합니다. 기본 기대 건수는 200건이며 현재 Console 전체의 학습 승인 기준을 뜻하지 않습니다.
2. 관찰 시각 순으로 앞 80%를 학습 후보, 뒤 20%를 평가에 사용합니다. 평가 시작 이후에야 정답이 확정되는 학습 후보는 제외합니다.
3. 전처리와 로지스틱 회귀를 학습하고 다수 클래스 기준 모델과 비교합니다. 입력은 금액·수량·채널·쿠폰 여부·이전 구매 횟수이며 ID·시각·정답은 제외합니다.
4. `model.joblib`·`metrics.json`·`dataset-manifest.json`을 새 출력 폴더에 저장하고 모델 재로딩 후 예측이 같은지 검사합니다.

## 로컬 실행

Python 3.12가 필요합니다. **Examples 저장소 루트**에서 실행하세요.

```bash
python3.12 -m venv /tmp/logfriends-training-venv
/tmp/logfriends-training-venv/bin/pip install --only-binary=:all: -r training/order-cpu/requirements.txt
PYTHONDONTWRITEBYTECODE=1 /tmp/logfriends-training-venv/bin/python -m unittest discover -s training/order-cpu -p 'test_*.py'
```

별도로 준비한 CSV와 manifest로 학습할 때는 다음 인자를 지정합니다. 실행 ID는 입력과 일치해야 하며 출력 폴더는 아직 없어야 합니다.

```bash
/tmp/logfriends-training-venv/bin/python training/order-cpu/train.py \
  --input /path/to/dataset.csv \
  --manifest /path/to/dataset-manifest.json \
  --run-id '<synthetic run ID>' \
  --expected-rows 200 \
  --output /path/to/new-output
```

## 보조 파일과 한계

| 파일 (`training/order-cpu/` 기준) | 역할 |
|---|---|
| `export.mjs` / `export.sql` | DB에서 한 실행의 읽기 전용 스냅샷과 manifest 추출 |
| `prepare-configmap.mjs` | 체크섬 확인과 데이터 ConfigMap 파일 생성 |
| `Dockerfile` / `requirements.txt` | Python 3.12 이미지와 버전 고정 의존성 |
| `job.yaml` / `artifact-reader.yaml` | 학습·산출물 확인용 과거 NAS 템플릿 |
| `recover-artifacts.mjs` | 별도 읽기 경로로 산출물 회수 |

DB 추출과 NAS 회수 스크립트는 로컬 Console 설정·도구 및 별도 접근 권한이 필요하므로 위 오프라인 테스트와 구분하세요. 템플릿에는 과거 실행명·이미지·PVC 설정이 들어 있습니다. 재사용하려면 새 실행명·출력 경로와 대상 환경을 검토해야 하며, 완료 Job을 다시 적용해도 재학습되지 않습니다. 기존 산출물이나 PVC를 덮어쓰거나 삭제하지 마세요.

합성 데이터의 점수는 실제 고객 행동·운영 성능의 근거가 아닙니다. 같은 합성 사용자가 학습·평가에 걸칠 수 있어 신규 사용자 일반화도 검증하지 않습니다. 외부 `joblib`/pickle은 실행 가능한 직렬화 형식이므로 신뢰할 수 있는 자체 산출물만 로딩하세요.
현재 Console의 생성·비교·개선 경로는 [별도 runner](../generated-cpu/README.md)이며 이 고정 모델 데모와 다릅니다.

[Examples](../../README.md)
