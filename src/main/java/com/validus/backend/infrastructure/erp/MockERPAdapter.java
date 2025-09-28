package com.validus.backend.infrastructure.erp;

import com.validus.backend.application.port.outgoing.ERPAdapter;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.model.PaymentBatch;
import com.validus.backend.infrastructure.erp.dto.PurchaseOrder;
import com.validus.backend.infrastructure.erp.dto.SupplierProfile;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockERPAdapter implements ERPAdapter {

    private final Map<String, SupplierProfile> suppliers = new HashMap<>();
    private final Map<String, PurchaseOrder> purchaseOrders = new HashMap<>();

    @PostConstruct
    void init() {
        suppliers.put("SUP-001", SupplierProfile.builder()
            .id("SUP-001")
            .name("Mock Supplier")
            .taxId("FR123456789")
            .rib("FR761234598765000")
            .active(true)
            .build());
        purchaseOrders.put("PO-1000", PurchaseOrder.builder()
            .number("PO-1000")
            .supplierId("SUP-001")
            .currency("EUR")
            .remainingAmount(new BigDecimal("10000"))
            .build());
    }

    @Override
    public SupplierProfile getSupplier(String supplierId) {
        return suppliers.get(supplierId);
    }

    @Override
    public PurchaseOrder getPurchaseOrder(String poNumber) {
        return purchaseOrders.get(poNumber);
    }

    @Override
    public String postAccountingEntry(Invoice invoice) {
        log.info("Mock posting accounting entry for invoice {}", invoice.getId());
        return "TXN-" + invoice.getId();
    }

    @Override
    public void executePaymentBatch(PaymentBatch batch) {
        log.info("Mock executing payment batch {} with {} items", batch.getId(), batch.getItems().size());
    }
}
