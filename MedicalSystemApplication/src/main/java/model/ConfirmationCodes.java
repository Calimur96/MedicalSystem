package model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class ConfirmationCodes {

    @Id
    private Long id;

    @Column(name = "email_verification_code", nullable = false)
    private int emailVerificationCode;

    @Column(name = "phone_verification_code", nullable = false)
    private int phoneVerificationCode;

}
