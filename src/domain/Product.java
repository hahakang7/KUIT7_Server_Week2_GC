package domain;

public class Product {
  private String productId;
  private String name;
  private String description;
  private double price;
  private int stock;

  public Product(String name, double price, int descriptionLength) {
    this.productId = "PROD-" + System.nanoTime();
    this.name = name;
    this.price = price;
    this.stock = 100;
    // String Pool 우회: 동적으로 생성된 String은 새 인스턴스
    this.description = "A".repeat(descriptionLength);
  }

  public String getProductId() { return productId; }
  public String getName() { return name; }
  public String getDescription() { return description; }
  public double getPrice() { return price; }
  public int getStock() { return stock; }
}
