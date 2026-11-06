package problem2;

import java.time.LocalDate;

public record Member(
        Long id,
        String email,
        String fullName,
        int age,
        String phone,
        Integer loyaltyPoints,
        LocalDate joinedOn
) {}