package br.com.itau.geradornotafiscal.core.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum Regiao {
    NORTE(new BigDecimal("1.08")),
    NORDESTE(new BigDecimal("1.085")),
    CENTRO_OESTE(new BigDecimal("1.07")),
    SUDESTE(new BigDecimal("1.048")),
    SUL(new BigDecimal("1.06"));

    private final BigDecimal fatorFrete;
}
