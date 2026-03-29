## Context

Hiện tại, người dùng FluenZ sau khi học các Chunk không có nơi nào để nhìn nhận lại sự tiến bộ của mình theo thời gian dài hạn. Mục tiêu là giúp người dùng có động lực thông qua các chỉ số đo lường như: Daily Goal (5 phút), Weekly Calendar, Streak, 100 Giờ học, và Lần mở miệng. Mọi thứ phải rõ ràng, có feedback thời gian thực (Toast) và một trang Profile tách rời.

## Goals / Non-Goals

**Goals:**
- Theo dõi thời gian học mỗi ngày của User và cập nhật vào chuỗi (Streak) nếu đạt 5 phút.
- Ghi nhận lượng thời gian tăng lên (mặc định +3 phút / mỗi Chunk hoàn thành) một cách an toàn.
- Đếm số lần Voice được gửi lên LLM (Số lần mở miệng).
- Có Floating UI/Toast ngay sau khi học xong để báo kết quả.
- Giao diện Profile chứa mọi thống kê chi tiết.
- Các Chunk đã học xong có UI khác biệt và không bị reset progress khi bấm học lại.

**Non-Goals:**
- Không track chi tiết từng giây từng phút khi User treo tab. Lấy mô hình fixed time per completion (3 phút/chunk) kết hợp track session voice cho an toàn.
- Không phát triển hệ thống Leaderboard mạng xã hội ở giai đoạn này.

## Decisions

1. **Database Schema Enhancements:**
   - **`user_stats`**: Lưu tổng quan tiến độ (total_learning_minutes, total_spoken_count, current_streak, longest_streak, last_learning_date).
   - **`user_chunk_progress`** (hoặc mở rộng logic Progress có sẵn bằng ChunkID): Đánh dấu `is_completed = true` đè lên vĩnh viễn không reset.
   - **`user_daily_activities`**: Lưu nhật ký từng ngày để render Lịch Tuần (date, learning_minutes, is_goal_reached).
2. **Backend API Logic:**
   - REST API `POST /api/progress/chunk-complete/{chunkId}`: 
     - Seta `is_completed = true` vĩnh viễn cho Chunk và cập nhật all SubPhrases bên trong.
     - Cộng 3 phút mặc định vào `user_daily_activities` cho ngày hôm nay.
     - Nếu số phút trong ngày vượt mốc 5 phút: set `is_goal_reached = true` và tăng `current_streak` +1 (nếu hôm qua mới học, hoặc reset nếu cách ngày).
     - Trả về payload chứa sự thay đổi (`gainedMinutes`, `didReachGoal`, `newStreak`) để Frontend vẽ Toast.
   - REST API `POST /api/progress/voice-activity`:
     - Tăng biến `total_spoken_count` lên 1 (Gắn vào Flow khi Send micro).
   - REST API `GET /api/users/me/profile`: Gom toàn bộ Data về cho giao diện Profile.
3. **Frontend Application Logic:**
   - Tạo Store Zustand hoặc Context API cho `useProfileStore` để quản lý trạng thái realtime ở Dashboard. 
   - Render tab "Hồ sơ" bên thanh Sidebar.
   - Thêm `<Toaster />` hoặc Floating notification sau màn hình "Hoàn thành" để show popup ăn mừng báo hiệu đã cộng phút.

## Risks / Trade-offs

- **[Risk] User spam completion để cày số phút** → Mitigation: Lưu vết `completed_at` và chỉ tính cộng thời gian ở lần học hoàn thành đầu tiên (First-time Completion) trong ngày cho 1 Chunk. Nếu học lại Chunk đó trong cùng ngày, sẽ cộng ít phút hơn hoặc không cộng, nhằm ngăn chặn Hack/Spam thông số mục tiêu.
- **[Risk] Mất đồng bộ múi giờ ngày (Timezone) giữa File DB và Client** → Mitigation: DB lưu Date theo UTC, Backend xử lý tính ngày dựa trên Date của Client Zone gửi lên thông qua Header hoặc Payload.
