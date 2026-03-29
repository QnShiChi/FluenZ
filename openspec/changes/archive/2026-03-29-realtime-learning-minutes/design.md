## Overview

Change này tách rõ hai trách nhiệm:

- `chunk completion`: lưu trạng thái chunk đã học để UI bật tick active.
- `learning minutes accumulation`: cộng thời gian học thực tế của mỗi lần hoàn thành chunk, bất kể chunk mới hay cũ.

## Minute Calculation

Nguồn dữ liệu tính thời gian là `totalTimeSeconds` của practice session vừa hoàn tất.

Quy tắc tính `gainedMinutes`:

1. Lấy `sessionSeconds` từ payload hoàn thành chunk.
2. Clamp `sessionSeconds` về tối đa `600` giây.
3. Quy đổi sang phút với làm tròn lên theo từng phút học.
4. Nếu `sessionSeconds > 0` nhưng nhỏ hơn `60`, kết quả tối thiểu là `1 phút`.
5. Nếu session không có thời lượng hợp lệ hoặc bằng `0`, hệ thống không cộng phút.

Ví dụ:

- `35 giây` -> `1 phút`
- `61 giây` -> `2 phút`
- `540 giây` -> `9 phút`
- `1300 giây` -> cap còn `600 giây` -> `10 phút`

## Completion Semantics

- Mỗi lần user hoàn thành chunk, hệ thống luôn cố gắng upsert trạng thái `completed`.
- Nếu chunk đã completed từ trước thì không reset gì cả.
- Việc completed lần đầu hay học lại không ảnh hưởng đến chuyện có được cộng phút hay không.

## Daily Goal And Streak

- `UserDailyActivity.learningMinutes` tiếp tục là tổng phút trong ngày.
- `UserStats.totalLearningMinutes` tiếp tục là tổng phút tích luỹ toàn thời gian.
- Daily goal vẫn là `5 phút`.
- Streak chỉ tăng tại thời điểm user vừa chạm ngưỡng goal của ngày, giống logic hiện tại.

## API Direction

Để tránh backend phải suy luận session nào vừa kết thúc, API hoàn thành chunk nên nhận trực tiếp thời lượng session của chunk vừa học xong, ví dụ qua request body hoặc query param có tên rõ nghĩa như `totalTimeSeconds`.

Điểm quan trọng là:

- `practice/complete` tiếp tục lưu log session.
- `progress/chunk-complete` chịu trách nhiệm đánh dấu completed và cộng realtime minutes dựa trên thời lượng được gửi cùng request.

## Risks

- Nếu frontend gửi sai `totalTimeSeconds`, số phút được cộng cũng sai tương ứng.
- Nếu một practice flow trong tương lai cho phép nhiều chunk trong một session, spec implementation cần xác định lại thời lượng gán cho từng chunk thay vì tái dùng nguyên session tổng.
