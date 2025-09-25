package com.validus.backend.web.mapper;

import com.validus.backend.domain.model.PaymentBatch;
import com.validus.backend.domain.model.PaymentBatchItem;
import com.validus.backend.web.dto.PaymentBatchItemResponse;
import com.validus.backend.web.dto.PaymentBatchResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentBatchMapper {

    @org.mapstruct.Mapping(target = "items", source = "items")
    PaymentBatchResponse toResponse(PaymentBatch batch);

    List<PaymentBatchItemResponse> toItemResponses(List<PaymentBatchItem> items);
}
