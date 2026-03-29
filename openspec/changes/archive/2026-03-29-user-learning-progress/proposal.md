## Why

Người dùng hiện tại khi học xong một Chunk không nhận được cảm giác "hoàn thành rõ ràng" hay theo dõi được sự tiến bộ của bản thân mỗi ngày. Để sản phẩm mang tính động lực (Gamification) cao hơn, chúng tôi bổ sung hệ thống Hồ sơ cá nhân (Profile) kèm theo các chỉ số như: mục tiêu 5 phút mỗi ngày, chuỗi học tập (Streak), tổng giờ học tích luỹ và số lần luyện nói. Việc cập nhật realtime và có feedback nổi (Toast) ngay sau khi học sẽ giúp UX tốt hơn rất nhiều.

## What Changes

- **Đánh dấu Hoàn Thành Chunk**: Chunk học xong sẽ chuyển trạng thái `Completed`, các câu bên trong tự động được đánh dấu. Có thể học lại thoải mái nhưng hệ thống không thay đổi lại trạng thái chưa học.
- **Tính Thời Gian Học An Toàn**: Mỗi khi hoàn thành một Chunk, cộng một khoảng thời gian cố định (vd: 3 phút) vào chỉ số học trong ngày của User để tránh việc "treo máy" làm sai lệch thời gian ảo.
- **Chỉ Số Mở Miệng**: Tính tổng số lần thu âm hợp lệ (Voice submit) dồn vào Profile của User.
- **Hồ Sơ Cá Nhân (Profile Tab)**: Một trang mới trên thanh Navigation bên trái chứa thống kê học tập (Mục tiêu ngày, Chuỗi streak, Tiến trình 100 giờ học, Thống kê tuần).
- **Gamification Feedback UI**: Hiển thị Toast / Popup ngay sau khi hoàn thành Chunk để báo cho User biết họ vừa tích luỹ thêm được bao nhiêu phút và chuỗi (Streak) có tăng hay không.

## Capabilities

### New Capabilities
- `user-profile-ui`: Giao diện trang Profile (Dashboard thống kê cá nhân).
- `progress-gamification-ui`: UI hiển thị Toast popup và đánh dấu hoàn thành Chunk.
- `daily-learning-tracking`: API Backend tính toán và lưu trữ thời gian học, Streak, mục tiêu 5 phút.
- `chunk-completion-tracking`: API Backend đánh dấu hoàn thành Chunk và SubPhrases.

### Modified Capabilities


## Impact

- **Database**: Sẽ cần tạo các model/bảng mới như `UserStats` (lưu tổng số phút, tổng số lần nói, streak hiện tại, streak cao nhất) và log ngày `DailyLearningLog` (lưu phút học theo từng ngày để vẽ lịch tuần). Bảng lưu trạng thái Chunk (ví dụ `UserChunkProgress`).
- **Frontend**: Navigation Bar thay đổi để thêm Tab "Hồ sơ". Các màn hình danh sách Chunk cần lấy từ API trạng thái để đổi màu/check-mark.
- **Backend API**:
  - `POST /api/progress/chunk-complete`: Đánh dấu hoàn tất Chunk, tự động cộng 3 phút, tăng Streak nếu điểm rơi >= 5 phút.
  - `POST /api/progress/voice-activity`: Tăng chỉ số "số lần mở miệng".
  - `GET /api/users/me/profile`: Trả về toàn bộ data thống kê.
