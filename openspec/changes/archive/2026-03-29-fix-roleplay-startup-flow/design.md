## Context

Hiện tại, màn hình Roleplay AI yêu cầu người dùng phải tự thu âm lược đầu tiên sau khi nghe hướng dẫn về ngữ cảnh. Tuy nhiên luồng này tạo ra UX không tự nhiên, và đôi khi gặp lỗi TTS (Voice engine Tiếng Anh cố đọc Text Tiếng Việt). Bên cạnh đó, các tin nhắn trả lời của AI ở lượt cuối (bao gồm cả Answer tiếng Anh và Evaluation tiếng Việt) đôi khi bị dính chùm vào 1 block duy nhất nếu backend LLM không trả về đúng định dạng phân cách `---`.

## Goals / Non-Goals

**Goals:**
- Loại bỏ hoàn toàn phase hướng dẫn/chờ lượt đầu tiên của người dùng.
- AI sẽ tự động lấy `contextQuestion` để mở đầu cuộc trò chuyện.
- Nâng cấp số lượt hội thoại: AI khởi đầu -> User -> AI -> User -> AI (chốt & nhận xét).
- Đảm bảo logic tách message nội bộ ở Frontend (`splitFinalRoleplayResponse`) hoạt động hoàn hảo để render Answer và Evaluation ở 2 block chat UI khác nhau, kể cả khi định dạng LLM trả về có sai lệch nhỏ.

**Non-Goals:**
- Không thay đổi core model LLM hiện tại (OpenRouter).
- Không thay đổi tiêu chí đánh giá (evaluation criteria) của RoleplayService.

## Decisions

1. **Khởi tạo luồng AI thay vì Speaker**:
   - `RoleplayChatUI.tsx`: Bỏ `playOpeningSequence()`. Ngay sau khi mount, gọi thẳng `streamAiMessage(roleplayContext.contextQuestion)` hoặc một hàm fetch/simulate để AI nhắn tin nhắn đầu tiên. Sau khi render xong, mở khóa Mic cho người dùng (`setRoleplayPhase(3)`).
2. **Cập nhật Backend Prompt (`RoleplayService.java`)**:
   - Thay vì chỉ có 2 turn (Turn 1: ACK, Turn 2: Evaluate), ta cần điều chỉnh Prompt để hỗ trợ kịch bản 3 turn của AI (hoặc ít nhất giữ Turn 1 và Turn 2 linh động tuỳ thuộc vào history chat).
   - Turn 1 của Backend sẽ là "Respond and keep conversation going" (thay vì giới hạn 1 câu acknowledge).
   - Turn cuối cùng vẫn giữ rule tách biệt bằng `---` và `Evaluation`.
3. **Cải tiến Regex phân tách nội dung (Message Split)**:
   - Trong `splitFinalRoleplayResponse`, bổ sung logic fallback: Nếu không tìm thấy `---`, chia cắt dựa trên các keyword phổ biến của hệ thống như `Danh gia:`, `Đánh giá:`, `Evaluation:`. Nhằm đảm bảo 99% Answer và Evaluation được đưa vào hai block giao diện khác nhau với `tone: 'evaluation'`.

## Risks / Trade-offs

- **[Risk] Frontend Lifecycle Race Conditions** → Mitigation: Xóa cẩn thận các `setTimeout` cũ của `playOpeningSequence` và dùng `useEffect` tinh gọn để khởi chạy tin nhắn đầu tiên một cách đồng bộ.
- **[Risk] Prompt Injection / Misunderstanding** ở Turn giữa → Mitigation: Cập nhật System Prompt một cách rõ ràng để AI hiểu role của mình ở các lượt nói khác nhau (lượt giữa chuyên hỏi tiếp/duy trì hội thoại, lượt cuối chuyên nhận xét).
