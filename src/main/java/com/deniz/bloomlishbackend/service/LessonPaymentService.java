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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    //ÖDEME BAŞLAT

    public String startLessonPayment(Long lessonId, User student) {

        if (student == null) {
            throw new RuntimeException("Öğrenci bulunamadı. JWT gönderilmiyor olabilir!");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı."));

        // Daha önce bu derse kayıt olmuş mu?
        if (enrollmentRepository.existsByLessonAndStudent(lesson, student)) {
            throw new IllegalStateException("Bu derse zaten kayıt olmuşsunuz.");
        }

        BigDecimal price = BigDecimal.valueOf(lesson.getPrice());

        // Iyzico checkout request
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

        // Buyer bilgileri
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

        // Tek basket item (ders)
        BasketItem item = new BasketItem();
        item.setId(lesson.getId().toString());
        item.setName(lesson.getName());
        item.setCategory1(lesson.getCategory() != null ? lesson.getCategory() : "Lesson");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(price);
        request.setBasketItems(List.of(item));

        // Fatura adresi
        Address billingAddress = new Address();
        billingAddress.setContactName(student.getUsername());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Ödeme Adresi");
        billingAddress.setZipCode("34000");
        request.setBillingAddress(billingAddress);

        // Teslimat adresi
        Address shippingAddress = new Address();
        shippingAddress.setContactName(student.getUsername());
        shippingAddress.setCity("Istanbul");
        shippingAddress.setCountry("Turkey");
        shippingAddress.setAddress("Teslimat Adresi");
        shippingAddress.setZipCode("34000");
        request.setShippingAddress(shippingAddress);


        CheckoutFormInitialize checkoutForm = CheckoutFormInitialize.create(request, iyzicoOptions);

        // Hata kontrolü
        if (checkoutForm == null || checkoutForm.getPaymentPageUrl() == null) {
            throw new RuntimeException("Ödeme sayfası oluşturulamadı: " +
                    (checkoutForm != null ? checkoutForm.getErrorMessage() : "null"));
        }

        return checkoutForm.getPaymentPageUrl();
    }


    @Transactional
    public void handleLessonPaymentCallback(String token) {

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token boş olamaz.");
        }

        // Iyzico doğrulama isteği
        RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
        request.setLocale(Locale.TR.getValue());
        request.setToken(token);

        CheckoutForm checkoutForm = CheckoutForm.retrieve(request, iyzicoOptions);

        if (checkoutForm == null) {
            throw new IllegalStateException("Ödeme sonucu alınamadı (checkoutForm null döndü).");
        }


        if (!"SUCCESS".equalsIgnoreCase(checkoutForm.getPaymentStatus())) {
            throw new IllegalStateException("Ödeme başarısız. Status: " + checkoutForm.getPaymentStatus());
        }

        String[] parts = checkoutForm.getBasketId().split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("Geçersiz basketId formatı: " + checkoutForm.getBasketId());
        }

        Long lessonId = Long.parseLong(parts[0]);
        Long studentId = Long.parseLong(parts[1]);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı."));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı."));

        // Önceden kayıt varsa tekrar ekleme
        if (enrollmentRepository.existsByLessonAndStudent(lesson, student)) {
            System.out.println("Enrollment zaten var, tekrar oluşturulmadı.");
            return;
        }

        // Kayıt oluştur
        Enrollment enrollment = Enrollment.builder()
                .lesson(lesson)
                .student(student)
                .paid(true)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollmentRepository.save(enrollment);

    }

//öğrencinin kayıt olduğu dersler
    public List<LessonDto> getMyLessons(User student) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        return enrollments.stream()
                .map(e -> lessonMapper.toDto(e.getLesson()))
                .collect(Collectors.toList());
    }
}
