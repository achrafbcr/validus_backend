package com.validus.web;

import com.validus.domain.Invoice;
import com.validus.domain.InvoiceStatus;
import com.validus.service.InvoiceService;
import com.validus.service.dto.InvoiceCreateDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  private final InvoiceService service;
  public InvoiceController(InvoiceService service) { this.service = service; }

  @PostMapping
  public Invoice create(@RequestBody @Valid InvoiceCreateDTO dto) { return service.create(dto); }

  @GetMapping
  public Page<Invoice> list(@RequestParam(name = "status",required = false) InvoiceStatus status,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size",defaultValue = "10") int size) {
    return service.list(status, PageRequest.of(page, size));
  }

  @GetMapping("/{id}")
  public Invoice get(@PathVariable Long id) { return service.get(id); }

  @PatchMapping("/{id}/status")
  public Invoice changeStatus(@PathVariable("id") Long id,
                              @RequestParam(name = "next") InvoiceStatus next) {
    return service.updateStatus(id, next);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) { service.delete(id); }
}
