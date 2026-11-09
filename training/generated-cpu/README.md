# 생성·비교·개선 CPU Runner

Console의 지원 합성 주문 데이터 계약을 사용하는 학습 실행 코드입니다. 임의의 데이터·모델을 다루는 범용 AutoML이나 예측 서빙 구현은 아닙니다.

> 현재 로컬 개발 소스 기준입니다. 이 README만으로 공개 main에 runner 코드가 추가되거나 NAS에 배포되는 것은 아닙니다. 아래 파일 경로는 로컬 소스 기준입니다.

| 파일 | 역할 |
|---|---|
| `runner.py` | 생성 프로그램 검사, 주문 데이터 검증, 학습·평가·저장 |
| `catalog_runner.py` | 허용된 모델 후보 비교 |
| `improvement_runner.py` | 정해진 개선 공간 안에서 후보 평가 |
| `Dockerfile` | Console의 검사 코드·카탈로그와 runner를 함께 패키징 |

## 로컬 테스트

Python 3.12와 옆의 `log-friends-console` 저장소가 필요합니다. **Examples 저장소 루트**에서 실행하세요.

```bash
python3.12 -m venv /tmp/logfriends-training-venv
/tmp/logfriends-training-venv/bin/pip install --only-binary=:all: -r training/order-cpu/requirements.txt
/tmp/logfriends-training-venv/bin/python scripts/test-training.py
```

테스트 스크립트는 Console의 `src/main/resources/training/`에서 `program_guard.py`·`model_catalog.json`·`improvement_space.json`을 임시 디렉터리에 복사합니다. Docker 이미지와 같은 리소스 구성을 사용하며 원본 파일은 수정하지 않습니다. Dockerfile의 빌드 컨텍스트는 두 저장소를 포함하는 상위 작업공간입니다.

테스트는 합성 fixture를 사용하며 OpenAI 호출·Kaggle 다운로드·DB 접근·Kubernetes Job 생성을 하지 않습니다. 통과는 로컬 runner 동작의 근거이며 운영 모델 품질이나 현재 NAS 배포 상태를 증명하지 않습니다. Job·승인·실행 이력 관리는 Console 책임입니다.

[Examples](../../README.md) · [고정 주문 학습](../order-cpu/README.md)
