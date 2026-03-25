import domain.User;
import domain.Order;
import domain.Product;

import java.util.ArrayList;
import java.util.List;

public class ParallelGCTest {

  // Old Generation 유도용 장기 생존 컨테이너
  static List<Order> longLivedOrders = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    System.out.println("=== Parallel GC Test 시작 ===");
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 600; i++) {

      // [단기 생존 객체] 매 루프 생성 후 즉시 참조 해제 (Young GC 대상)
      for (int j = 0; j < 5; j++) {
        User shortLivedUser = new User(
            "user_" + j,
            "user" + j + "@test.com",
            256 * 1024  // 256KB - Young Generation 압박
        );
        Product shortLivedProduct = new Product(
            "Product_" + j,
            9900.0,
            500  // 500자 description
        );
        Order shortLivedOrder = new Order(shortLivedUser);
        shortLivedOrder.addProduct(shortLivedProduct);
        // 루프 종료 시 참조 소멸 -> GC 대상
      }

      // [장기 생존 객체] 10번마다 Old Generation으로 승격 유도
      if (i % 10 == 0) {
        User vipUser = new User(
            "vip_" + i,
            "vip" + i + "@shop.com",
            1024 * 1024  // 1MB Large Object
        );
        Order vipOrder = new Order(vipUser);
        for (int k = 0; k < 3; k++) {
          vipOrder.addProduct(new Product("VIP_Product_" + k, 99000.0, 1000));
        }
        longLivedOrders.add(vipOrder); // Old Generation 승격
      }

      // [처리량 중시] 짧은 sleep으로 GC 압박 유지
      Thread.sleep(20);

      // 진행 상황 출력 (100번마다)
      if (i % 100 == 0) {
        long heapUsed = Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory();
        System.out.printf("[%d회] Heap 사용: %.1f MB, longLived 크기: %d%n",
            i, heapUsed / (1024.0 * 1024.0), longLivedOrders.size());
      }
    }

    long elapsed = System.currentTimeMillis() - startTime;
    System.out.printf("=== 종료: %.1f초, longLived 보유: %d개 ===%n",
        elapsed / 1000.0, longLivedOrders.size());
  }
}
