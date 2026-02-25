package com.example.commerce.order.service;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.admin.repository.AdminRepository;
import com.example.commerce.customer.entity.Customer;
import com.example.commerce.customer.entity.CustomerStatus;
import com.example.commerce.customer.repository.CustomerRepository;
import com.example.commerce.global.exception.ErrorCode;
import com.example.commerce.global.security.AdminUserDetails;
import com.example.commerce.global.security.UserPrincipal;
import com.example.commerce.order.dto.*;
import com.example.commerce.order.entity.Order;
import com.example.commerce.order.entity.OrderStatus;
import com.example.commerce.order.repository.OrderRepository;
import com.example.commerce.product.entity.Product;
import com.example.commerce.global.exception.ServiceException;
import com.example.commerce.product.entity.ProductStatus;
import com.example.commerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * Javadoc 연습용: 고객의 주문 생성, 관리자의 대리 주문, 주문 취소 및 상태 관리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;

    // 주문 생성
    @Transactional
    public CreateOrderResponse create(UserPrincipal userPrincipal, CreateOrderRequest request) {
        // 세션에 저장되어있는 id 를 기반으로
        // customer repostiory 에서 customer 를 찾음 (없으면 오류 반환)
        Customer customer = getCustomerById(userPrincipal.getId());
        isActiveCustomer(customer);

        // 상품이 정말로 존재하는지 (없으면 오류 반환)
        Product product = getProductById(request.getProductId());

        // 상품 상태가 판매중이지 확인
        productStatusIsValid(product.getId());

        // 재고가 남아있는지 확인
        product.chkStock(request.getQuantity());

        Order order = new Order(
                request.getQuantity(),
                calculateTotalPrice(request.getQuantity(), product.getPrice()),
                product,
                customer,
                null,
                false
        );

        Order newOrder = orderRepository.save(order);
        product.updateStock(product.getStock() - request.getQuantity());

        return new CreateOrderResponse(
                newOrder.getId(),
                newOrder.getOrderNo(),
                newOrder.getProduct().getName(),
                newOrder.getProduct().getPrice(),
                newOrder.getQuantity(),
                newOrder.getTotalPrice(),
                OrderStatus.PREPARING.getStatusName(),
                newOrder.getCreatedAt()
        );
    }

    @Transactional
    public CreateOrderByAdminResponse createByAdmin(UserPrincipal userPrincipal, CreateOrderByAdminRequest request) {
        // 관리자가 존재하는지
        Admin admin = getAdminById(userPrincipal.getId());
        isActiveAdmin(admin);

        // 관리자가 주문 관련 자격이 있는지 ...
        // 추가 필요 -> 완료

        // 요청한 고객이 존재하는지
        Customer customer = getCustomerById(request.getCustomerId());

        // 상품이 정말로 존재하는지
        Product product = getProductById(request.getProductId());

        // 상품 상태가 판매중이지 확인
        productStatusIsValid(product.getId());

        // 재고가 남아있는지 확인
        product.chkStock(request.getQuantity());

        Order order = new Order(
                request.getQuantity(),
                calculateTotalPrice(request.getQuantity(), product.getPrice()),
                product,
                customer,
                admin,
                false
        );

        Order newOrder = orderRepository.save(order);
        product.updateStock(product.getStock() - request.getQuantity());

        return new CreateOrderByAdminResponse(
                newOrder.getId(),
                newOrder.getOrderNo(),

                newOrder.getCustomer().getId(),
                newOrder.getCustomer().getName(),

                newOrder.getProduct().getName(),
                newOrder.getProduct().getPrice(),

                newOrder.getQuantity(),
                newOrder.getTotalPrice(),

                OrderStatus.PREPARING.getStatusName(),
                newOrder.getCreatedAt(),

                newOrder.getAdmin().getName(),
                newOrder.getAdmin().getEmail(),
                newOrder.getAdmin().getRole().getRoleName()
        );
    }

    public Page<GetOrdersByAdminResponse> getAllByAdmin(String keyword, OrderStatus orderStatus, Pageable pageable, UserPrincipal userPrincipal ){
        isActiveAdmin(getAdminById(userPrincipal.getId()));

        Page<Order> orders = orderRepository.searchOrders(keyword, orderStatus, null, pageable);

        return orders.map(order -> new GetOrdersByAdminResponse(
                order.getId(),
                order.getOrderNo(),
                order.getCustomer().getName(),
                order.getProduct().getName(),
                order.getOrderStatus().getStatusName(),
                order.getQuantity(),
                order.getCreatedAt(),
                order.getAdmin() != null ? order.getAdmin().getName() : "N/A"
        ));
    }

    public Page<GetOrdersResponse> getAllByCustomer(UserPrincipal userPrincipal, String keyword, OrderStatus orderStatus, Pageable pageable){
        Customer customer = getCustomerById(userPrincipal.getId());
        // 활성 상태 고객이 아니더라도 본인이 주문한 리스트는 확인 가능해야할듯

        //customer 필수
        Page<Order> orders = orderRepository.searchOrders(keyword,orderStatus,customer,pageable);

        return orders.map(order -> new GetOrdersResponse(
                order.getId(),
                order.getOrderNo(),
                order.getCustomer().getName(),
                order.getProduct().getName(),
                order.getOrderStatus().getStatusName(),
                order.getQuantity(),
                order.getCreatedAt()
        ));
    }

    // 주문 단 건 조회 (관리자용)
    public GetOneOrderByAdminResponse getOneAdminOrder(Long orderId, UserPrincipal userPrincipal) {

        // 관리자가 활성 상태인지 확인 (관리자 맞는지 따로 확인 안해도 되는지 체크하기)
        isActiveAdmin(getAdminById(userPrincipal.getId()));

        // 조회하고자 하는 주문이 정말로 존재하는지 (없으면 오류 반환)
        Order order = getOrderById(orderId);

        return new GetOneOrderByAdminResponse(
                order.getOrderNo(),
                order.getQuantity(),
                order.getOrderStatus().getStatusName(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getProduct().getName(),
                order.getProduct().getPrice(),
                order.getCreatedAt(),
                order.getAdmin() != null ? order.getAdmin().getName() : "N/A",
                order.getAdmin() != null ? order.getAdmin().getEmail() : "N/A",
                order.getAdmin() != null ? order.getAdmin().getRole() : null
        );
    }

    // 주문 단 건 조회 (고객용)
    public GetOneOrderResponse getOneOrder(Long orderId, UserPrincipal userPrincipal) {
        // 조회하고자 하는 주문이 정말로 존재하는지 (없으면 오류 반환)
        Order order = getOrderById(orderId);

        // 본인의 주문을 조회하려는게 맞는지 확인
        if (!order.getCustomer().getId().equals(userPrincipal.getId())){
            throw new ServiceException(ErrorCode.FORBIDDEN_CUSTOMER);
        }

        return new GetOneOrderResponse(
                order.getOrderNo(),
                order.getQuantity(),
                order.getOrderStatus().getStatusName(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getProduct().getName(),
                order.getProduct().getPrice(),
                order.getCreatedAt()
        );
    }

    /**
     * 주문 취소 로직 (관리자 및 고객 공용)
     * 팀원 리뷰 반영: 고객은 본인의 주문만 취소 가능하도록 보안 로직 강화
     */
    @Transactional
    public CancelOrderResponse cancelByAdmin(Long orderId, UserPrincipal userPrincipal, CancelOrderRequest request) {

        // 1. 요청자(관리자 또는 고객)의 활성 상태 확인
        if (userPrincipal.getRole().equals("CUSTOMER")){
            isActiveCustomer(getCustomerById(userPrincipal.getId()));
        } else if (userPrincipal instanceof AdminUserDetails){
            isActiveAdmin(getAdminById(userPrincipal.getId()));
        } else {
            throw new ServiceException(ErrorCode.BEFORE_LOGIN);
        }

        // 2. 취소 대상 주문 확인
        Order order = getOrderById(orderId);

        // 3. [보안 핵심] 요청자가 '고객'일 경우에만 본인 주문인지 검증 (IDOR 방어)
        if (userPrincipal.getRole().equals("CUSTOMER")) {
            if (!order.getCustomer().getId().equals(userPrincipal.getId())) {
                throw new ServiceException(ErrorCode.FORBIDDEN_CUSTOMER); // "본인 주문만 접근 가능" 에러
            }
        }

        // 4. 재고 복구 및 상태 변경
        Product product = order.getProduct();
        product.updateStock(product.getStock() + order.getQuantity());

        // 5. 엔티티 내부 cancel 메서드 호출 (Dirty Checking)
        order.cancel(request.getCancelReason());

        return new CancelOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getOrderStatus().getStatusName(),
                order.getCancelReason()
        );
    }

    /**
     * 주문 완료 처리 (배달 완료)
     */
    @Transactional
    public void deliverCompleted(Long orderId, UserPrincipal userPrincipal){
        isActiveAdmin(getAdminById(userPrincipal.getId()));
        Order order = getOrderById(orderId);
        order.updateStatus(OrderStatus.DELIVERED);
    }

    public long calculateTotalPrice(int quantity, int price) {
        return (long) quantity *price;
    }

    public Admin getAdminById(long adminId) {
        return adminRepository.findById(adminId).orElseThrow(
                () -> new ServiceException(ErrorCode.ADMIN_NOT_FOUND)
        );
    }

    // 관리자가 활성상태가 맞는지 확인하는 로직
    public void isActiveAdmin(Admin admin) {
        //isLoginable 은 활성(Active) 상태에서만 true 니까 활성상태가 아니라면 throw
        if (!admin.getStatus().isLoginable()) {
            switch (admin.getStatus()) {
                case PENDING -> throw new ServiceException(ErrorCode.ADMIN_PENDING);   // "계정 승인대기 중"
                case REJECTED -> throw new ServiceException(ErrorCode.ADMIN_REJECTED); // "계정 신청 거부됨"
                case STOPPED -> throw new ServiceException(ErrorCode.ACCOUNT_STOPPED);   // "계정 정지됨"
                case INACTIVE -> throw new ServiceException(ErrorCode.ACCOUNT_INACTIVE); // "계정 비활성화됨"
                default -> throw new ServiceException(ErrorCode.FORBIDDEN_ADMIN);
            }
        }
    }

    public Product getProductById(Long productId){
        return  productRepository.findById(productId).orElseThrow(
                () -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public Order getOrderById(Long orderId){
        return orderRepository.findById(orderId).orElseThrow(
                () -> new ServiceException(ErrorCode.ORDERING_NOT_FOUND));
    }

    public void productStatusIsValid(Long productId){
        Product product = getProductById(productId);
        if (product.getStatus()!=ProductStatus.AVAILABLE){
            throw new ServiceException(ErrorCode.INVALID_STATUS);
        }
    }

    public Customer getCustomerById(Long customerId){
        return customerRepository.findById(customerId).orElseThrow(
                ()-> new ServiceException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    //Customer 상태가 ACTIVE 가 아니라면 throw
    public void isActiveCustomer(Customer customer){
        if (customer.getStatus() == CustomerStatus.INACTIVE){
            throw new ServiceException(ErrorCode.ACCOUNT_INACTIVE);
        }
        else if (customer.getStatus() == CustomerStatus.SUSPENDED){
            throw new ServiceException(ErrorCode.ACCOUNT_STOPPED);
        }
    }
}