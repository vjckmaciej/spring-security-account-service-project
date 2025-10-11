package account.mapper;

import account.domain.Payment;
import account.dto.PaymentDTO;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", expression = "java(dto.getEmployee().toLowerCase())")
    @Mapping(target = "period", source = "period")
    @Mapping(target = "salary", source = "salary")
    Payment toEntity(PaymentDTO dto);

    @IterableMapping(elementTargetType = Payment.class)
    List<Payment> toEntities(List<PaymentDTO> dtos);
}
