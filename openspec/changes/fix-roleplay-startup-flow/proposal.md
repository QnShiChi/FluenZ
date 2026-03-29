## Why

Hiện tại, luồng tương tác ở màn hình Roleplay yêu cầu người dùng phải chủ động nói trước dựa trên câu hướng dẫn (gây bối rối và thỉnh thoảng gây lỗi treo TTS). Việc để AI chủ động mở lời trước bằng câu hỏi ngữ cảnh sẽ giúp cuộc trò chuyện tự nhiên hơn, đúng nghĩa "vào là nói". Đồng thời, tin nhắn tiếng Anh và lời nhận xét tiếng Việt của AI ở lượt cuối cần được tách thành 2 block riêng biệt trên UI để người dùng dễ đọc và theo dõi.

## What Changes

- **Đổi luồng khởi đầu**: AI sẽ sử dụng trực tiếp `contextQuestion` để làm câu mở màn (Tin nhắn đầu tiên) ngay khi người dùng bước vào màn hình Roleplay.
- **Nâng cấp luồng hội thoại lên 5 bước**:
  1. AI hỏi (Lượt 1 - tự động sinh từ `contextQuestion`)
  2. Người dùng trả lời
  3. AI đáp lại (Lượt 2 - tương tác tiếp nối)
  4. Người dùng nói tiếp / phản hồi
  5. AI chốt lại và Đánh giá (Lượt 3 - Answer + Evaluation)
- **Tách riêng 2 khối tin nhắn**: Frontend sẽ xử lý tách chuỗi trả lời của AI ở lượt cuối thành 2 message object độc lập, render thành 2 bong bóng chat khác biệt (một cho Answer tiếng Anh, một cho Evaluation tiếng Việt).

## Capabilities

### New Capabilities
- `roleplay-flow`: Định nghĩa luồng hội thoại 5 bước, trong đó AI là người chủ động bắt đầu cuộc trò chuyện.
- `roleplay-message-ui`: Định nghĩa giao diện tách biệt block tiếng Anh và block nhận xét tiếng Việt cho AI.

### Modified Capabilities


## Impact

- **Frontend**: Khởi tạo lại logic trong `RoleplayChatUI.tsx` để AI append message đầu tiên, state quản lý `roleplayPhase` phải vẽ lại. Viết thêm logic tách hai block chat từ 1 chuỗi response của GPT.
- **Backend**: Cập nhật `PracticeController.java` và thư viện/prompt trong `RoleplayService.java` để hỗ trợ turn thứ 3 của quá trình (do AI có thêm 1 lượt phụ giữa chừng).
