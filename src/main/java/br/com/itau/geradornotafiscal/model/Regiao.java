package br.com.itau.geradornotafiscal.model;

public enum Regiao {
    NORTE(1.08),
    NORDESTE(1.085),
    CENTRO_OESTE(1.07),
    SUDESTE(1.048),
    SUL(1.06);

    private final double fatorFrete;

    Regiao(double fatorFrete) {
        this.fatorFrete = fatorFrete;
    }

    public double getFatorFrete() {
        return fatorFrete;
    }
}
