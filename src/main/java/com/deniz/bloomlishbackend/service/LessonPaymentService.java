package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.LessonMapper;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonPaymentService {

    private final Options iyzicoOptions;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonMapper lessonMapper;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    // =========================
    // ÖDEME BAŞLAT
    // =========================
    public String startLessonPayment(Long lessonId, User student) {

        if (student == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LESSON_NOT_FOUND"));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (lesson.getDate().isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LESSON_DATE_PASSED");
        }

        if (lesson.getDate().isEqual(today) && lesson.getStartTime().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LESSON_TIME_PASSED");
        }

        // ✅ Ders dolu mu? (tek kişilik)
        if (enrollmentRepository.existsByLesson(lesson)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LESSON_FULL");
        }

        // ✅ Öğrenci zaten kayıtlı mı?
        if (enrollmentRepository.existsByLessonAndStudent(lesson, student)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ALREADY_ENROLLED");
        }

        BigDecimal price = BigDecimal.valueOf(lesson.getPrice());

        CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(UUID.randomUUID().toString());
        request.setPrice(price);
        request.setPaidPrice(price);
        request.setCurrency(Currency.TRY.name());

        // BasketId içine lessonId + studentId gömüyoruz
        String basketId = lessonId + ":" + student.getUserID();
        request.setBasketId(basketId);

        String callbackUrl = backendUrl + "/api/payments/lesson-callback";
        request.setCallbackUrl(callbackUrl);

        // Buyer
        Buyer buyer = new Buyer();
        buyer.setId(student.getUserID().toString());
        buyer.setName(student.getUsername());
        buyer.setSurname("Student");
        buyer.setEmail(student.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setRegistrationAddress("Adres");
        buyer.setIp("127.0.0.1");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34000");
        request.setBuyer(buyer);

        // Basket item
        BasketItem item = new BasketItem();
        item.setId(lesson.getId().toString());
        item.setName(lesson.getName());
        item.setCategory1(lesson.getCategory() != null ? lesson.getCategory() : "Lesson");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(price);
        request.setBasketItems(List.of(item));

        // Billing
        Address billingAddress = new Address();
        billingAddress.setContactName(student.getUsername());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Ödeme Adresi");
        billingAddress.setZipCode("34000");
        request.setBillingAddress(billingAddress);

        // Shipping
        Address shippingAddress = new Address();
        shippingAddress.setContactName(student.getUsername());
        shippingAddress.setCity("Istanbul");
        shippingAddress.setCountry("Turkey");
        shippingAddress.setAddress("Teslimat Adresi");
        shippingAddress.setZipCode("34000");
        request.setShippingAddress(shippingAddress);

        CheckoutFormInitialize checkoutForm = CheckoutFormInitialize.create(request, iyzicoOptions);

        if (checkoutForm == null || checkoutForm.getPaymentPageUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "IYZICO_ERROR:" + (checkoutForm != null ? checkoutForm.getErrorMessage() : "null"));
        }

        return checkoutForm.getPaymentPageUrl();
    }

    // =========================
    // CALLBACK (ÖDEME SONRASI)
    // =========================
    @Transactional
    public void handleLessonPaymentCallback(String token) {

        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EMPTY");
        }

        RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
        request.setLocale(Locale.TR.getValue());
        request.setToken(token);

        CheckoutForm checkoutForm = CheckoutForm.retrieve(request, iyzicoOptions);

        if (checkoutForm == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CHECKOUTFORM_NULL");
        }

        if (!"SUCCESS".equalsIgnoreCase(checkoutForm.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PAYMENT_FAILED");
        }

        String[] parts = checkoutForm.getBasketId().split(":");
        if (parts.length != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BASKETID_INVALID");
        }

        Long lessonId = Long.parseLong(parts[0]);
        Long studentId = Long.parseLong(parts[1]);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LESSON_NOT_FOUND"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND"));

        // (opsiyonel) ders geçmiş/başlamışsa kayıt oluşturma
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (lesson.getDate().isBefore(today) || (lesson.getDate().isEqual(today) && lesson.getStartTime().isBefore(now))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LESSON_NOT_AVAILABLE");
        }

        // öğrenci zaten kayıtlı ise tekrar ekleme
        if (enrollmentRepository.existsByLessonAndStudent(lesson, student)) {
            return;
        }

        //  ders dolu mu? (başkası aldıysa)
        if (enrollmentRepository.existsByLesson(lesson)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LESSON_FULL");
        }

        Enrollment enrollment = Enrollment.builder()
                .lesson(lesson)
                .student(student)
                .paid(true)
                .enrolledAt(LocalDateTime.now())
                .build();

        try {
            enrollmentRepository.save(enrollment);
        } catch (DataIntegrityViolationException ex) {
            // unique constraint uk_enrollment_lesson patladıysa
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LESSON_FULL");
        }
    }

    // =========================
    // ÖĞRENCİNİN DERSLERİ
    // =========================
    public List<LessonDto> getMyLessons(User student) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        return enrollments.stream()
                .map(e -> lessonMapper.toDto(e.getLesson()))
                .collect(Collectors.toList());
    }
}
