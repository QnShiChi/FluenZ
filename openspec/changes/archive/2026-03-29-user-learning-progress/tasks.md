## 1. Database & Entity Models
- [x] 1.1 Tạo Entity `UserStats` (lưu tổng số phút, chuỗi hiện tại, chuỗi lớn nhất, tổng số lần nói, ngày học cuối).
- [x] 1.2 Tạo Entity `UserChunkProgress` (đánh dấu hoàn thành chunk).
- [x] 1.3 Tạo Entity `UserDailyActivity` (lưu số phút đã học theo từng ngày để render lịch).
- [x] 1.4 Thêm các repository tương ứng và migration nếu cần.

## 2. Backend Progression APIs
- [x] 2.1 Viết API Endpoint `POST /api/progress/chunk-complete/{chunkId}`, tự động update `UserChunkProgress` (và đánh dấu các SubPhrases bên trong).
- [x] 2.2 Trong API trên, cộng 3 phút vào `UserDailyActivity` hôm nay, nếu đạt 5 phút thì `is_goal_reached = true`. Update `UserStats` (Streak và phút tổng).
- [x] 2.3 Viết API Endpoint `POST /api/progress/voice-activity` chạy ngầm để tăng `total_spoken_count` +1 mỗi khi user nói và gửi audio thành công.
- [x] 2.4 Viết API Endpoint `GET /api/users/me/profile` kết xuất toàn bộ dữ liệu thống kê, lịch học tuần này của User trả về Frontend.

## 3. Frontend Profile UI
- [x] 3.1 Thêm icon "Hồ sơ" (Profile) vào thanh Navigation dọc bên trái của ứng dụng.
- [x] 3.2 Tạo trang `/profile` hoàn chỉnh với các thành phần: Avatar, Widget Mục tiêu hôm nay (tiến độ 5 phút), Lịch học tuần.
- [x] 3.3 Trong trang `/profile`, thêm các Widget Động lực: "Tôi đã vượt qua cơn lười X ngày", "Tiến tới nói lưu loát Y / 100 giờ", "Tôi đã mở miệng Z lần".
- [x] 3.4 Tạo hook `useProfile` để gọi API lấy dữ liệu.

## 4. Frontend Gamification & Practice Integration
- [x] 4.1 Cài đặt thư viện Toast (vd react-hot-toast) hoặc tự dùng một Floating UI Notification đẹp mắt.
- [x] 4.2 Ở màn hình chúc mừng hoàn thành của `PracticePage.tsx`, gọi API `chunk-complete`, sau đó hiển thị Toast thông báo: "🎉 Chúc mừng! Bạn học được thêm 3 phút...".
- [x] 4.3 Cập nhật thẻ Chunk UI trong Màn hình Situation Detail để hiển thị trạng thái `Completed` (có màu sắc khác biệt hoặc tick xanh).
