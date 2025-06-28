package com.bookNDrive.payment_service.mappers;

import com.bookNDrive.payment_service.dtos.sended.PaymentDto;
import com.bookNDrive.payment_service.models.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper( PaymentMapper.class );

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "formulaId", target = "formulaId")
    @Mapping(source = "status", target = "status")
    PaymentDto paymentToPaymentDto(Payment payment);


}
