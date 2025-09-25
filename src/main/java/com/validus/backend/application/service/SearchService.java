package com.validus.backend.application.service;

import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.repository.InvoiceRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final InvoiceRepository invoiceRepository;

    public Page<Invoice> searchInvoices(InvoiceStatus status,
                                        String companyCode,
                                        String supplierId,
                                        LocalDate from,
                                        LocalDate to,
                                        Pageable pageable) {
        return invoiceRepository.search(status, companyCode, supplierId, from, to, pageable);
    }
}
