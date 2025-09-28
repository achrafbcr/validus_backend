package com.validus.backend.web.mapper;

import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.model.InvoiceLine;
import com.validus.backend.web.dto.InvoiceLineDto;
import com.validus.backend.web.dto.InvoiceRequest;
import com.validus.backend.web.dto.InvoiceResponse;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "auditLogs", ignore = true)
    @Mapping(target = "lines", ignore = true)
    Invoice toEntity(InvoiceRequest request);

    @Mapping(target = "lines", source = "lines")
    InvoiceResponse toResponse(Invoice invoice);

    List<InvoiceLineDto> toLineDtos(List<InvoiceLine> lines);

    InvoiceLine toLineEntity(InvoiceLineDto dto);

    List<InvoiceLine> toLineEntities(List<InvoiceLineDto> dtos);

    @AfterMapping
    default void linkLines(@MappingTarget Invoice invoice, InvoiceRequest request) {
        invoice.getLines().clear();
        if (request.getLines() != null) {
            request.getLines().forEach(lineDto -> {
                InvoiceLine line = toLineEntity(lineDto);
                line.setInvoice(invoice);
                invoice.getLines().add(line);
            });
        }
    }
}
