package com.validus.service;

import com.validus.domain.Invoice;
import com.validus.domain.InvoiceStatus;
import com.validus.repository.InvoiceRepository;
import com.validus.service.dto.InvoiceCreateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

  private final InvoiceRepository repo;

  public InvoiceService(InvoiceRepository repo) { this.repo = repo; }

  public Invoice create(InvoiceCreateDTO dto) {
    if (repo.existsByInvoiceNumber(dto.invoiceNumber)) {
      throw new IllegalArgumentException("Invoice number already exists");
    }
    Invoice i = new Invoice();
    i.setSupplierName(dto.supplierName);
    i.setInvoiceNumber(dto.invoiceNumber);
    i.setCompany(dto.company);
    i.setAmount(dto.amount);
    i.setStatus(InvoiceStatus.RECEIVED);
    return repo.save(i);
  }

  public Page<Invoice> list(InvoiceStatus status, Pageable pageable) {
    if (status == null) return repo.findAll(pageable);
    return repo.findByStatus(status, pageable);
  }

  public Invoice get(Long id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
  }

  public Invoice updateStatus(Long id, InvoiceStatus next) {
    Invoice inv = get(id);
    inv.setStatus(next);
    return repo.save(inv);
  }

  public void delete(Long id) { repo.deleteById(id); }
}
