## 1. Backend Progress Logic
- [ ] 1.1 Cập nhật API hoàn thành chunk để nhận thời lượng session thực tế của chunk vừa học xong.
- [ ] 1.2 Thay logic cộng phút cố định bằng logic cộng phút realtime từ `totalTimeSeconds`, cap tối đa `10 phút`, và làm tròn tối thiểu `1 phút` khi học dưới `60 giây`.
- [ ] 1.3 Giữ nguyên logic upsert trạng thái `UserChunkProgress` / `UserDefaultChunkProgress` để chunk đã học luôn hiện completed.
- [ ] 1.4 Đảm bảo việc cộng phút diễn ra cho cả chunk học mới và chunk học lại.

## 2. Backend Profile And Gamification
- [ ] 2.1 Cập nhật response delta tiến độ để trả về số phút thực tế vừa được cộng.
- [ ] 2.2 Giữ nguyên logic daily goal `5 phút` và streak, nhưng áp dụng trên số phút realtime mới.
- [ ] 2.3 Bổ sung test cho các case dưới 1 phút, đúng 1 phút, trên 10 phút, và replay chunk cũ.

## 3. Frontend Practice Integration
- [ ] 3.1 Cập nhật luồng `PracticePage` hoặc `usePracticeStore` để gửi `totalTimeSeconds` khi gọi API `chunk-complete`.
- [ ] 3.2 Cập nhật toast hoàn thành chunk để hiển thị số phút thực tế được cộng theo response mới.
- [ ] 3.3 Xác nhận chunk đã học vẫn hiển thị tick active khi quay lại màn hình danh sách chunk.

## 4. Validation
- [ ] 4.1 Kiểm tra profile page hiển thị đúng `todayMinutes`, `totalLearningMinutes`, và weekly activity sau nhiều lần học lại cùng một chunk.
- [ ] 4.2 Kiểm tra user không thể nhận quá `10 phút` cho một lần hoàn thành chunk dù session bị treo lâu.
