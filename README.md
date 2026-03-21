# KUIT7_Server_Week2_GC
쿠잇 서버 2주차 GC 미션 
## 1. 미션 목표

JVM의 두 가지 대표적인 Garbage Collector인 

- **Parallel GC**
- **G1 GC**

를 직접 코드로 구현하고 실험하여 GC 동작 방식의 차이를 Heap 로그와 수치를 통해 분석하는 것이 목표입니다.

옵션만 바꾸는 것이 아니라 GC가 실제로 동작하도록 유도하는 시나리오를 Java 코드로 직접 작성하고 아래 항목을 스스로 확인해야 합니다.

- GC 발생 시점과 빈도
- Stop-The-World (STW) 일시 중단 시간
- Heap 사용량 변화 (Young / Old 영역)
- GC 튜닝 옵션 적용 전후 성능 차이

---

## 2. Java 코드 구현 요구사항

### 2-1. 요구사항

1. **파일 분리** — `ParallelGCTest.java` / `G1GCTest.java` 로 구분하여 작성
2. **최소 클래스 수** — 객체 생성에 사용되는 데이터 클래스 1개 이상 정의
3. **객체 생성 조건**
    - 크기가 큰 객체 (Large Object): 1MB 이상의 `byte[]` 또는 String 포함 객체
    - 단기 생존 객체: 루프 내에서 생성 후 즉시 참조 해제
    - 장기 생존 객체: `List` 등에 보관하여 Old Generation 유도
4. **총 실행 시간** — 최소 10초 이상 (GC 로그가 충분히 쌓일 수 있도록)
5. **힙 메모리 설정** — `Xms256m -Xmx512m` 으로 제한하여 GC 유발

### 2-2. 구현 예시

참고용 코드 예시이지만 직접 시나리오를 추가하고 변형하시면 될 것 같습니다

```java
public class ParallelGCTest {
    static List<byte[]> longLived = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 500; i++) {
            // 단기 생존: 즉시 해제
            byte[] shortLived = new byte[512 * 1024]; // 512KB

            // 장기 생존: Old 영역 유도
            if (i % 10 == 0) {
                longLived.add(new byte[1024 * 1024]); // 1MB
            }
            Thread.sleep(50);
        }
    }
}
```

> `User`, `Order`, `Product` 같은 도메인 객체를 직접 정의하고 현실적인 시나리오로 확장해보는 게 좋은 방법일 것 같습니다!
> 

---

## 3. (예시) 실행 및 GC 로그 수집

실행 및 GC 로그 수집 방식은 자유입니다!

### 3-1. Parallel GC 실행

```bash
javac ParallelGCTest.java

java -Xms256m -Xmx512m \
     -XX:+UseParallelGC \
     -XX:ParallelGCThreads=4 \
     -Xlog:gc*:file=gc_parallel.log:time,uptime,level,tags \
     ParallelGCTest
```

### 3-2. G1 GC 실행

```bash
javac G1GCTest.java

java -Xms256m -Xmx512m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Xlog:gc*:file=gc_g1.log:time,uptime,level,tags \
     G1GCTest
```

Java 11 미만 환경에서는 `-Xlog` 대신 `-verbose:gc -Xloggc:gc.log` 옵션을 사용

### 3-3. 튜닝 전 & 후 비교 실험

각 GC에 대해 기본 옵션(튜닝 전)과 **최적화 옵션(튜닝 후)** 두 번 실행하여 결과를 비교합니다.

**Parallel GC 튜닝 옵션 예시**

- `XX:ParallelGCThreads=N` — 스레드 수 조정
- `XX:GCTimeRatio=N` — GC 시간 비율 목표
- `XX:MaxGCPauseMillis=N` — 최대 STW 목표

**G1 GC 튜닝 옵션 예시**

- `XX:MaxGCPauseMillis=100` — 목표 일시 중단 시간 단축
- `XX:G1HeapRegionSize=N` — Region 크기 조정
- `XX:InitiatingHeapOccupancyPercent=N` — Old GC 시작 임계값

---

## 4. GC 로그 분석 포인트

생성된 GC 로그에서 아래 항목들을 확인하고 표로 정리해주시거나 화면 캡쳐 올려주시면 됩니다!

- Minor GC (Young GC) 발생 횟수 및 평균 소요 시간
- Major GC / Full GC 발생 횟수 및 최대 STW 시간
- Heap 사용량 변화 추이 (GC 전/후 Young, Old 영역 크기)
- G1 GC의 경우: Concurrent Marking 단계별 소요 시간
- 튜닝 전/후 GC 총 소요 시간 및 처리량(Throughput) 비교
