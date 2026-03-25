import domain.User;
import domain.Order;
import domain.Product;

import java.util.ArrayList;
import java.util.List;

public class G1GCTest {

  // 장기 생존 - 일부 주기적으로 해제하여 Mixed GC 유도
  static List<Order> longLivedOrders = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    System.out.println("=== G1 GC Test 시작 ===");
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 400; i++) {

      // [단기 생존] 크기 변동 객체 생성 (G1 Region 분산 유도)
      int imageSize = (i % 3 == 0) ? 128 * 1024   // 128KB
                      : (i % 3 == 1) ? 512 * 1024   // 512KB
                      :                768 * 1024;   // 768KB
      User shortLivedUser = new User("user_" + i, "user@test.com", imageSize);
      Order shortLivedOrder = new Order(shortLivedUser);
      shortLivedOrder.addProduct(new Product("Item", 1000.0, 200));

      // [Humongous Object] G1의 Humongous Region 할당 유도
      // G1 기본 Region 크기(1~32MB)의 50% 초과 객체
      if (i % 20 == 0) {
        User hugeUser = new User(
            "huge_" + i,
            "huge@test.com",
            2 * 1024 * 1024  // 2MB - Humongous Object
        );
        longLivedOrders.add(new Order(hugeUser));
      }

      // [장기 생존 일부 추가] Old Generation에 지속적으로 객체 증가
      // G1의 Concurrent Marking 및 Mixed GC 발동 조건 충족
      if (i % 5 == 0 && i > 0) {
        User regularUser = new User("long_" + i, "long@test.com", 512 * 1024);
        Order regularOrder = new Order(regularUser);
        longLivedOrders.add(regularOrder);
      }

      // 주기적으로 오래된 장기 생존 객체 50% 해제 (Mixed GC 유도 핵심)
      if (i % 50 == 0 && longLivedOrders.size() > 20) {
        int removeCount = longLivedOrders.size() / 2;
        longLivedOrders.subList(0, removeCount).clear();
        System.out.printf("[%d회] Old 영역 절반 해제: %d개 제거%n", i, removeCount);
      }

      // [응답 지연 허용] G1은 STW 목표치를 지키며 GC 수행
      Thread.sleep(30);

      if (i % 80 == 0) {
        long heapUsed = Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory();
        System.out.printf("[%d회] Heap 사용: %.1f MB, longLived 크기: %d%n",
            i, heapUsed / (1024.0 * 1024.0), longLivedOrders.size());
      }
    }

    long elapsed = System.currentTimeMillis() - startTime;
    System.out.printf("=== 종료: %.1f초 ===%n", elapsed / 1000.0);
  }
}
