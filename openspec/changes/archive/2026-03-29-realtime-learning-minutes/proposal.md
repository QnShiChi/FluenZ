## Why

Logic tiến độ hiện tại cộng một số phút cố định cho mỗi chunk hoàn thành lần đầu. Cách này không còn phù hợp với hành vi học thật của người dùng vì:

- Không phản ánh thời gian thực mà người dùng vừa dành ra để học.
- Không cộng thêm thời gian khi người dùng ôn lại chunk cũ, dù họ vẫn đang học thật.
- Gắn chặt việc cộng phút với "first-time completion", trong khi trạng thái completed và thời lượng học là hai khái niệm khác nhau.

Chúng ta cần chuyển sang mô hình theo thời gian học thực tế để profile, daily goal, streak, và toast feedback phản ánh đúng hơn nỗ lực của user, nhưng vẫn có guardrail để tránh trường hợp treo máy làm phồng số liệu.

## What Changes

- **Realtime learning minutes**: Khi user hoàn thành một chunk, hệ thống cộng thời lượng học dựa trên `totalTimeSeconds` của session vừa hoàn thành thay vì cộng cứng theo chunk.
- **Per-chunk time cap**: Mỗi lần hoàn thành một chunk chỉ được tính tối đa `10 phút` để tránh thời lượng ảo do treo máy hoặc để tab quá lâu.
- **Minimum rounded minute**: Nếu user học dưới `60 giây`, hệ thống vẫn cộng tối thiểu `1 phút`.
- **Replay still counts**: Chunk mới hay chunk cũ đều được cộng phút nếu user thực sự vừa hoàn thành session.
- **Completion stays separate**: Chunk đã học vẫn phải được đánh dấu `completed` để UI hiện tick active, và việc học lại không được làm mất trạng thái này.
- **Updated gamification feedback**: Toast sau khi hoàn thành chunk phải hiển thị số phút thực tế vừa được cộng theo rule mới.

## Capabilities

### Modified Capabilities
- `daily-learning-tracking`
- `chunk-completion-tracking`
- `progress-gamification-ui`
- `practice-session`

## Impact

- **Backend API**:
  - `POST /api/practice/complete` tiếp tục nhận `totalTimeSeconds` làm nguồn dữ liệu thời gian thực cho session.
  - `POST /api/progress/chunk-complete/{chunkId}` sẽ nhận thêm số giây học của session vừa xong hoặc dùng payload tương đương để tính phút realtime thay vì cộng cứng 3 phút.
- **Business logic**:
  - Bỏ điều kiện "chỉ cộng phút ở lần hoàn thành đầu tiên".
  - Giữ nguyên điều kiện đánh dấu `completed` theo chunk để phục vụ tick active.
- **Frontend**:
  - Luồng complete chunk cần gửi đủ dữ liệu thời lượng session để backend tính phút realtime.
  - Toast cần hiển thị số phút thực tế được cộng sau khi áp dụng rounding và cap.
