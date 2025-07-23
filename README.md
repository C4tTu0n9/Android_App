# PRM392 - Beta Clone

Dự án đặt vé xem phim

## 📱 Tính năng chính

- Liệt kê các tính năng chính của ứng dụng

## 🛠 Yêu cầu hệ thống

- Android Studio Flamingo | 2022.2.1 trở lên
- Android SDK 24 trở lên
- Gradle 8.9.1 trở lên
- JDK 11 trở lên

## 🚀 Cách cài đặt

1. Clone dự án:
   ```bash
   git clone https://gitlab.com/fptu_ong/prm392.git
   ```

2. Mở dự án bằng Android Studio
   - Chọn "Open an Existing Project"
   - Dẫn đến thư mục chứa dự án đã clone

3. Đồng bộ dự án với Gradle
   - Chọn "Sync Project with Gradle Files" (biểu tượng con voi)
   - Đợi quá trình đồng bộ hoàn tất

4. Chạy ứng dụng
   - Kết nối thiết bị Android hoặc sử dụng máy ảo (emulator)
   - Nhấn nút "Run" (mũi tên màu xanh) hoặc phím tắt Shift + F10

## 🏗 Cấu trúc dự án

```
AppCore/
├── src/
│   ├── main/
│   │   ├── java/         # Mã nguồn Java
│   │   ├── res/          # Tài nguyên (layout, drawable, strings, v.v.)
│   │   └── AndroidManifest.xml
│   └── test/            # Unit tests
└── build.gradle         # Cấu hình build
```
Cấu trúc dự án trong thư mục AppCore/app/src/main/java/com/example/appcore/
```
AppCore/app/src/main/java/com/example/appcore/
├── ui/                        # Lớp giao diện người dùng và logic hiển thị
│   ├── activity/
│   │   └── MainActivity.java
│   │   └── AuthActivity.java  # Đăng nhập, đăng ký
│   ├── home/                  # Các màn hình liên quan đến trang chủ/danh sách phim
│   │   ├── HomeFragment.java
│   │   ├── HomeViewModel.java
│   │   └── adapters/
│   │       └── MovieAdapter.java
│   ├── movie_details/         # Chi tiết phim
│   │   ├── MovieDetailsFragment.java
│   │   └── MovieDetailsViewModel.java
│   ├── booking/               # Đặt vé
│   │   ├── BookingFragment.java
│   │   └── BookingViewModel.java
│   └── common/                # Các thành phần UI dùng chung (dialogs, custom views)
│       └── CustomDialog.java
├── data/                      # Lớp quản lý dữ liệu (không chứa logic nghiệp vụ)
│   ├── model/                 # Các POJO/Data Class đại diện cho cấu trúc dữ liệu Firebase
│   │   ├── Movie.java
│   │   ├── Showtime.java
│   │   ├── Booking.java
│   │   └── User.java
│   ├── remote/                # Nguồn dữ liệu từ xa (Firebase)
│   │   └── FirebaseDataSource.java # Lớp wrapper cho các thao tác Firebase
│   └── repository/            # Trừu tượng hóa nguồn dữ liệu (ẩn Firebase chi tiết)
│       ├── MovieRepository.java
│       ├── BookingRepository.java
│       └── AuthRepository.java
├── domain/                    # Lớp logic nghiệp vụ (business logic - độc lập với UI/Data)
│   ├── usecase/               # Các trường hợp sử dụng (tức là các hành động người dùng có thể thực hiện)
│   │   ├── GetMoviesUseCase.java
│   │   ├── BookTicketUseCase.java
│   │   ├── LoginUserUseCase.java
│   │   └── ...
│   └── entity/                # Các lớp đại diện cho đối tượng nghiệp vụ (có thể giống với model nếu đơn giản)
│       ├── MovieEntity.java
│       ├── BookingEntity.java
│       └── UserEntity.java
│
├── di/                        # Dependency Injection (ví dụ: Dagger Hilt) - Giúp quản lý các đối tượng
│   └── AppModules.java
├── utils/                     # Các lớp tiện ích chung (helpers, extensions)
│   ├── Constants.java
│   ├── ValidationUtils.java
│   └── DateFormatter.java
├── App.java                   # Lớp Application (nếu có các khởi tạo toàn cục)
└── BaseActivity.java          # Các Activity cơ sở để tái sử dụng mã
```
## 🤝 Đóng góp

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Quy tắc đặt tên branch:
- theo định dạng:
```
<author>_<branch-type>_<branch-name>
```
Ví dụ: TuongPC_feature_new-experimental-changes
4. Commit thay đổi (`git commit -m 'feat:Add some AmazingFeature'`)
5. Push lên branch (`git push origin feature/AmazingFeature`)
6. Tạo Pull Request

## 📄 Giấy phép

Dự án này được cấp phép theo [MIT](https://choosealicense.com/licenses/mit/) - xem file [LICENSE](LICENSE) để biết thêm chi tiết.

## 👥 Đội ngũ phát triển

- Phạm Cát Tường - Dev & Maintainer
- Lê Hữu Đạt - Dev
- Hoàng Thị Hương Giang - Dev
- Nguyễn Đình Quyền - Dev
- Hà Trọng Hùng - Dev

## 📞 Liên hệ

Email: ehehe@fpt.edu.vn

---

<div align="center">
  <sub>Được tạo với ❤️ bởi đội ngũ phát triển của nhóm PRM392</sub>
</div>