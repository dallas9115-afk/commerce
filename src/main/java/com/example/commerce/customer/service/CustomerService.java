package com.example.commerce.customer.service;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.admin.repository.AdminRepository;
import com.example.commerce.customer.dto.*;
import com.example.commerce.customer.entity.Customer;
import com.example.commerce.customer.entity.CustomerStatus;
import com.example.commerce.customer.repository.CustomerRepository;
import com.example.commerce.global.config.PasswordEncoder;
import com.example.commerce.global.exception.ErrorCode;
import com.example.commerce.global.exception.ServiceException;
import com.example.commerce.global.security.AdminUserDetails;
import com.example.commerce.global.security.JwtUtil;
import com.example.commerce.global.security.UserPrincipal;
import com.example.commerce.global.security.entity.RefreshToken;
import com.example.commerce.global.security.repository.RefreshTokenRepository;
import com.example.commerce.order.repository.OrderRepository; // [추가]
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    // JWT 발급을 위한 JwtUtil 의존성 주입
    private final JwtUtil jwtUtil;

    // [추가] 고객의 주문 통계를 가져오기 위한 의존성 주입
    private final OrderRepository orderRepository;

    @Transactional
    public SignupCustomerResponse createCustomer(SignupCustomerRequest request) {
        if (customerRepository.existsByEmail(request.getCustomerEmail())) {
            throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getCustomerPassword());

        Customer customer = new Customer(
                request.getCustomerName(),
                request.getCustomerEmail(),
                encodedPassword,
                request.getCustomerPhone(),
                CustomerStatus.ACTIVE //  고객은 기본 활성 상태
        );

        Customer savedCustomer = customerRepository.save(customer);

        return new SignupCustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhone(),
                savedCustomer.getStatus().getStatusName(),
                savedCustomer.getCreatedAt()
        );
    }

    // 로그인(JWT 토큰 활용)
    @Transactional
    public LoginCustomerResponse customerLogIn(LoginCustomerRequest request) {
        Customer customer = customerRepository.findByEmail(request.getCustomerEmail())
                .orElseThrow(() -> new ServiceException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCustomerPassword(), customer.getPassword())) {
            throw new ServiceException(ErrorCode.WRONG_PW);
        }

        // 활성 상태 검증 (정지, 비활성 고객은 로그인 불가)
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new ServiceException(ErrorCode.INVALID_STATUS);
        }

        // JWT 토큰 생성 (역할은 "CUSTOMER"로 명시)
        String accessToken = jwtUtil.createToken(customer.getId(), customer.getEmail(), "CUSTOMER");
        String refreshToken = jwtUtil.createRefreshToken(customer.getEmail());

        // Refresh Token DB 저장 (이미 존재하면 Update, 없으면 Insert)
        RefreshToken tokenEntity = refreshTokenRepository.findByEmail(customer.getEmail())
                .orElse(new RefreshToken(customer.getEmail(), refreshToken));

        tokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(tokenEntity);

        return new LoginCustomerResponse(
                accessToken,
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getStatus().getStatusName()
        );
    }

    // 고객 상세 조회
    public GetOneCustomerResponse findCustomer(Long id, UserPrincipal userPrincipal) {

        // userPrincipal 이 활성화 상태인지 확인
        if (userPrincipal.getRole().equals("CUSTOMER")){
            isActiveCustomer(getCustomerById(userPrincipal.getId()));
        } else if (userPrincipal instanceof AdminUserDetails){
            isActiveAdmin(getAdminById(userPrincipal.getId()));
        } else {
            throw new ServiceException(ErrorCode.BEFORE_LOGIN);
        }

        Customer customer = getCustomerById(id);

        // 💡 [추가] 고객의 총 주문 수와 구매 금액 계산
        long totalOrderCount = orderRepository.countByCustomerId(customer.getId());
        long totalPurchaseAmount = orderRepository.sumTotalPriceByCustomerId(customer.getId());

        return new GetOneCustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus().getStatusName(),
                customer.getCreatedAt(),
                customer.getModifiedAt(),
                totalOrderCount,     // [추가]
                totalPurchaseAmount  // [추가]
        );
    }

    // 고객 리스트 조회
    public Page<GetOneCustomerResponse> findAllCustomer(UserPrincipal userPrincipal, String keyword, CustomerStatus status, Pageable pageable) {

        // 어차피 관리자만 로그인 되었기 때문에 관리자가 활성상태인지만 확인
        isActiveAdmin(getAdminById(userPrincipal.getId()));

        Page<Customer> customers = customerRepository.searchCustomers(keyword, status, pageable);

        return customers.map(customer -> {
            // [추가] 각 고객별 주문 통계 조회
            long totalOrderCount = orderRepository.countByCustomerId(customer.getId());
            long totalPurchaseAmount = orderRepository.sumTotalPriceByCustomerId(customer.getId());

            return new GetOneCustomerResponse(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getStatus().getStatusName(),
                    customer.getCreatedAt(),
                    customer.getModifiedAt(),
                    totalOrderCount,     // [추가]
                    totalPurchaseAmount  // [추가]
            );
        });
    }

    // 유저 수정
    @Transactional
    public GetOneCustomerResponse updateCustomer(Long id, UpdateCustomerRequest request, UserPrincipal userPrincipal){

        // userPrincipal 이 활성화 상태인지 확인
        if (userPrincipal.getRole().equals("CUSTOMER")){
            isActiveCustomer(getCustomerById(userPrincipal.getId()));
        } else if (userPrincipal instanceof AdminUserDetails){
            isActiveAdmin(getAdminById(userPrincipal.getId()));
        } else {
            throw new ServiceException(ErrorCode.BEFORE_LOGIN);
        }

        Customer customer = getCustomerById(id);

        // 이메일 중복 검사
        if (!customer.getEmail().equals(request.getCustomerEmail()) && adminRepository.existsByEmail(request.getCustomerEmail())) {
            throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
        }

        customer.update(
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerPhone()
        );

        // [추가] 수정 후 응답에도 통계 포함
        long totalOrderCount = orderRepository.countByCustomerId(customer.getId());
        long totalPurchaseAmount = orderRepository.sumTotalPriceByCustomerId(customer.getId());

        return new GetOneCustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus().getStatusName(),
                customer.getCreatedAt(),
                customer.getModifiedAt(),
                totalOrderCount,     // [추가]
                totalPurchaseAmount  // [추가]
        );
    }

    @Transactional
    public UpdateCustomerStatusResponse updateCustomerStatus(Long id, UpdateCustomerStatusRequest request, UserPrincipal userPrincipal){
        isActiveAdmin(getAdminById(userPrincipal.getId()));

        Customer customer = getCustomerById(id);

        CustomerStatus status = CustomerStatus.from(request.getCustomerStatus());

        return new UpdateCustomerStatusResponse(
                customer.getId(),
                status.getStatusName(),
                customer.getModifiedAt()
        );
    }

    // 유저 삭제
    @Transactional
    public void deleteCustomer(Long id, UserPrincipal userPrincipal){
        isActiveAdmin(getAdminById(userPrincipal.getId()));

        Customer customer = getCustomerById(id);

        customerRepository.delete(customer);
    }

    //Id 를 기준으로 admin 반환
    public Admin getAdminById(long adminId){
        return adminRepository.findById(adminId).orElseThrow(
                ()->new ServiceException(ErrorCode.ADMIN_NOT_FOUND)
        );
    }

    // 관리자가 활성상태가 맞는지 확인하는 로직
    public void isActiveAdmin(Admin admin){
        if(!admin.getStatus().isLoginable()){
            switch (admin.getStatus()) {
                case PENDING -> throw new ServiceException(ErrorCode.ADMIN_PENDING);
                case REJECTED -> throw new ServiceException(ErrorCode.ADMIN_REJECTED);
                case STOPPED -> throw new ServiceException(ErrorCode.ACCOUNT_STOPPED);
                case INACTIVE -> throw new ServiceException(ErrorCode.ACCOUNT_INACTIVE);
                default -> throw new ServiceException(ErrorCode.FORBIDDEN_ADMIN);
            }
        }
    }

    //Id 를 기준으로 Customer 반환
    public Customer getCustomerById(long customerId){
        return customerRepository.findById(customerId).orElseThrow(
                ()-> new ServiceException(ErrorCode.CUSTOMER_NOT_FOUND)
        );
    }

    //Customer 상태가 ACTIVE 가 아니라면 throw
    public void isActiveCustomer(Customer customer){
        if (customer.getStatus()==CustomerStatus.INACTIVE){
            throw new ServiceException(ErrorCode.ACCOUNT_INACTIVE);
        }
        else if (customer.getStatus()==CustomerStatus.SUSPENDED){
            throw new ServiceException(ErrorCode.ACCOUNT_STOPPED);
        }
    }
}