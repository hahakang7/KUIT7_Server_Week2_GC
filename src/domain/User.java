package domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
  private String userId;
  private String username;
  private String email;
  private LocalDateTime createdAt;
  private byte[] profileImage; // Large Object 유도용

  public User(String username, String email, int imageSizeBytes) {
    this.userId = UUID.randomUUID().toString();
    this.username = username;
    this.email = email;
    this.createdAt = LocalDateTime.now();
    this.profileImage = new byte[imageSizeBytes];
  }

  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public byte[] getProfileImage() { return profileImage; }
}
