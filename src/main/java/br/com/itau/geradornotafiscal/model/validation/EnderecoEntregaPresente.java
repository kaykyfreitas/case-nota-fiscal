package br.com.itau.geradornotafiscal.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnderecoEntregaPresenteValidator.class)
public @interface EnderecoEntregaPresente {

    String message() default "Pedido sem endereco de entrega";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
