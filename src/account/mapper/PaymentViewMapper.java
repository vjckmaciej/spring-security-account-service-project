package account.mapper;

import account.domain.Payment;
import account.domain.User;
import account.dto.PaymentViewDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentViewMapper {

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "lastname", source = "user.lastname")
    @Mapping(target = "period", source = "payment.period", qualifiedByName = "prettyPeriod")
    @Mapping(target = "salary", source = "payment.salary", qualifiedByName = "formatCents")
    PaymentViewDTO toView(User user, Payment payment);

    @Named("prettyPeriod")
    default String prettyPeriod(String mmYYYY) {
        // Month-YYYY po EN
        var parts = mmYYYY.split("-");
        int m = Integer.parseInt(parts[0]);
        String y = parts[1];
        String[] names = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        return names[m-1] + "-" + y;
    }

    @Named("formatCents")
    default String formatCents(Long cents) {
        long dollars = cents / 100;
        long rest = cents % 100;
        return dollars + " dollar(s) " + rest + " cent(s)";
    }
}
