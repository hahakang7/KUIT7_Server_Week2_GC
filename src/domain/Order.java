package domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
  private String orderId;
  private User user;
  private List<Product> products;
  private double totalAmount;
  private String status;
  private LocalDateTime orderedAt;

  public Order(User user) {
    this.orderId = "ORD-" + System.nanoTime();
    this.user = user;
    this.products = new ArrayList<>();
    this.status = "PENDING";
    this.orderedAt = LocalDateTime.now();
    this.totalAmount = 0.0;
  }

  public void addProduct(Product product) {
    this.products.add(product);
    this.totalAmount += product.getPrice();
  }

  public String getOrderId() { return orderId; }
  public User getUser() { return user; }
  public List<Product> getProducts() { return products; }
  public double getTotalAmount() { return totalAmount; }
  public String getStatus() { return status; }
}
