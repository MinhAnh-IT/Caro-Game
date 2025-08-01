## Quy tắt bắt buộc
1. Sử dụng tiếng việt để trả lời.
2. Viết code luôn tuân thủ kiến trúc layers và các quy tắc của Clean Architecture.
3. Viết code clean và dễ bảo trì.
4. Luôn luôn tuân thủ các quy tắc của SOLID.
5. Luôn luôn viết unit test cho code mới.
6. Đưa các file nằm đúng thư mục theo chuẩn của Clean Architecture:
   - Entity: nằm trong package entity.
   - Repository: nằm trong package repository.
   - Service: nằm trong package service.
   - Controller: nằm trong package controller.
   - Mapper: nằm trong package mapper.
   - DTO: nằm trong package dto.
   - Exception: nằm trong package exception.
   - Config: nằm trong package config.
   - Constants: nằm trong package constants.
   - Utils: nằm trong package utils.
   - Validation: nằm trong package validation.

7. Luôn luôn sử dụng @FieldDefault để khai báo các biến trong class.
8. Luôn luôn sử dụng @Schema để mô tả các trường trong DTO.
9. Luôn đặt file nằm đúng thư mục ví dụ về service dùng để triển khai phải nằm ở package service.impl, không được để lẫn lộn với các package khác. và inteface service phải nằm ở package service.intefaces.
8. Viết chức năng nào thì viết unit test cho chức năng đó, và chạy test cho đến khi pass hết.
9. Những API cá nhận luôn luôn sử dụng @AuthenticationPrinciple CustomUserdeatails để lấy thông tin người dùng hiện tại. (Không truyền userId từ client lên server, không sử dụng SecurityContext).
9. Luôn luôn viết docs swagger cho các API mới.
    Viết rõ ràng từng response (Ví dụ code 200 sẽ có body thế nào, 500 body như thế nào)
    Viết tất cả các code có thể xảy ra với endpoint đó, để Client dễ dàng tích hợp
10. Các text hoặc number trong code phải được khai báo ở một lớp constants riêng biệt, không được viết trực tiếp trong code.
11. Viết code phải comment code giải thích rõng ràng ở đầu mỗi hàm (bằng tiếng anh)
12. Luôn phân tách rõ ràng các tầng:
    Controller chỉ xử lý request/response.

    Service xử lý logic nghiệp vụ.

    Repository chỉ xử lý truy vấn dữ liệu.

    Mapper chuyển đổi giữa DTO và Entity.

13. Không được dùng new trực tiếp trong service/controller.

    Thay vào đó, sử dụng dependency injection.

    Chỉ dùng new trong các factory method có kiểm soát.

14. Các lỗi phải được xử lý rõ ràng, Hãy tạo enum lỗi ở StatusCode và trả về mã lỗi phù hợp.

15. Tạo các lớp DTO riêng biệt cho request và response.
16. Không dùng chung entity cho input/output.
17. Tách riêng các hàm xử lý logic phức tạp thành các private method nhỏ dễ test.
18. Một hàm không được dài quá 30 dòng (theo quy tắc clean code).
19. Luôn đặt tên method/biến rõ ràng, theo chuẩn camelCase không dùng tên viết tắt.
20. Tách riêng file cấu hình (config) và không hardcode URL, secret, port... trong code.
21. Dùng @Value hoặc application.yaml.
22. Nếu sửa code ở đâu thì phải sửa luôn unit test ở đó.
22. Tạo các ExceptionHandler chung ở tầng advice để bắt và xử lý lỗi toàn cục.
23. Luôn tạo file README.md cho module mới, giải thích chức năng và cách chạy test.
23. Dùng @Validated và @Valid ở controller để validate dữ liệu đầu vào.
24. Tạo annotation riêng nếu logic validate phức tạp.
25. Không dùng @Autowired, thay vào đó sử dụng @RequiredArgsConstructor hoặc @AllArgsConstructor để inject dependency.
26. Các phần xử lí logic phải được sử lí ở service không được để logic trong controller.
27. Không sử dụng @Transactional ở tầng controller, chỉ sử dụng ở tầng service.
28. Service phải có interface riêng biệt, không được viết trực tiếp trong class service.
29. Các trạng thái phải sử dụng enum riêng biệt, không dùng string hoặc int.
30. Không sử dụng static method trừ khi thực sự cần thiết.
31. tiếp tục thực hiện nếu maximum tool attempts reached
32. Mỗi API phải có:
    @Operation(summary, description)
    @ApiResponse cho tất cả các status code (200, 400, 404, 409, 500...)
    DTO dùng @Schema(description = "...", example = "...") cho từng field.
