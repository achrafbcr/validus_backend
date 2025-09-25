package com.validus.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.validus.backend.application.port.outgoing.AiAdapter;
import com.validus.backend.application.port.outgoing.AttachmentStoragePort;
import com.validus.backend.common.exception.DuplicateInvoiceException;
import com.validus.backend.domain.enums.AttachmentType;
import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.model.InvoiceLine;
import com.validus.backend.domain.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

class ReceptionServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private AttachmentStoragePort storagePort;
    @Mock
    private AuditService auditService;
    @Mock
    private AiAdapter aiAdapter;

    @InjectMocks
    private ReceptionService receptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInvoice_shouldSetDefaultsAndPersist() {
        Invoice invoice = buildInvoice();
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Invoice saved = receptionService.createInvoice(invoice, "tester");
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.RECEIVED);
        verify(invoiceRepository).save(saved);
    }

    @Test
    void createInvoice_shouldPreventDuplicate() {
        Invoice invoice = buildInvoice();
        when(invoiceRepository.findBySupplierIdAndNumberAndInvoiceYear(any(), any(), any())).thenReturn(Optional.of(new Invoice()));
        assertThatThrownBy(() -> receptionService.createInvoice(invoice, "tester"))
            .isInstanceOf(DuplicateInvoiceException.class);
    }

    @Test
    void addAttachment_shouldInvokeStorageAndAi() throws Exception {
        Invoice invoice = buildInvoice();
        invoice.setId(UUID.randomUUID());
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(storagePort.store(any(), any(), any())).thenReturn("ref");
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        receptionService.addAttachment(invoice.getId(), AttachmentType.INVOICE_PDF, file, "tester");
        verify(storagePort).store(any(), any(), any());
        verify(aiAdapter).extract(invoice.getId(), "ref");
        assertThat(invoice.getAttachments()).hasSize(1);
    }

    private Invoice buildInvoice() {
        Invoice invoice = new Invoice();
        invoice.setNumber("INV-001");
        invoice.setSupplierId("SUP-001");
        invoice.setCompanyCode("COMP1");
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setCurrency("EUR");
        invoice.setAmountHT(new BigDecimal("100"));
        invoice.setAmountTVA(new BigDecimal("20"));
        invoice.setAmountTTC(new BigDecimal("120"));
        InvoiceLine line = new InvoiceLine();
        line.setDesignation("Service");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(new BigDecimal("100"));
        line.setTaxRate(new BigDecimal("20"));
        line.setAmountLine(new BigDecimal("120"));
        invoice.addLine(line);
        return invoice;
    }
}
