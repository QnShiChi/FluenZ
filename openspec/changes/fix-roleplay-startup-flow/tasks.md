## 1. Backend Roleplay Support
- [x] 1.1 Cập nhật `RoleplayService.java` để hỗ trợ linh hoạt số lượng Turn (Thay vì hardcode Turn 1 và Turn 2, cần xử lý Turn cuối cùng chuyên xuất Evaluation, và các Turn trước đó chỉ đối đáp).
- [x] 1.2 Cập nhật System Prompt để AI hiểu nó đang ở lượt tương tác giữa chừng (không kết thúc ngay lập tức ở Turn 2).

## 2. Frontend Flow Init & Modification
- [x] 2.1 Mở `RoleplayChatUI.tsx`, loại bỏ các đoạn code liên quan đến `playOpeningSequence` và TTS đọc `contextQuestion` ẩn.
- [x] 2.2 Cập nhật `useEffect` khởi tạo: Khi UI vừa load, ngay lập tức render `roleplayContext.contextQuestion` thành một tin nhắn bong bóng mặc định của AI, và chuyển `roleplayPhase` sang trạng thái chờ người dùng nói.
- [x] 2.3 Điều chỉnh logic đếm lượt (`turnNumber`) khi gọi `requestAiReply` để khớp với 5 bước (User -> AI -> User -> AI+Eval).

## 3. Cải Tiến Split Message Logic
- [x] 3.1 Cập nhật `splitFinalRoleplayResponse` trong `RoleplayChatUI.tsx` để không chỉ dựa vào `---`.
- [x] 3.2 Viết regex fallback (tìm `Danh gia:`, `Đánh giá:`, `Evaluation:`) để tách chắc chắn khối Answer và khối Evaluation thành 2 đoạn riêng biệt, phục vụ render 2 block tin nhắn.
