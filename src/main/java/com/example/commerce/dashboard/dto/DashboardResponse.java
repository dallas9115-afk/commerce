package com.example.commerce.dashboard.dto;

import com.example.commerce.order.dto.GetOrdersByAdminResponse;
import java.util.List;

public record DashboardResponse(
        SummaryDto summary,
        WidgetDto widgets,
        ChartDto charts,
        List<GetOrdersByAdminResponse> recentOrders // 기존 Order 도메인의 DTO 재사용
) {}