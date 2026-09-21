package br.com.itau.geradornotafiscal.model.validation;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Finalidade;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class EnderecoEntregaPresenteValidator implements ConstraintValidator<EnderecoEntregaPresente, Destinatario> {

    @Override
    public boolean isValid(Destinatario destinatario, ConstraintValidatorContext context) {
        if (destinatario == null) {
            return true;
        }
        List<Endereco> enderecos = destinatario.getEnderecos();
        if (enderecos == null) {
            return false;
        }
        return enderecos.stream().anyMatch(this::isEnderecoEntrega);
    }

    private boolean isEnderecoEntrega(Endereco endereco) {
        if (endereco == null) {
            return false;
        }
        Finalidade finalidade = endereco.getFinalidade();
        return (finalidade == Finalidade.ENTREGA || finalidade == Finalidade.COBRANCA_ENTREGA)
                && endereco.getRegiao() != null;
    }
}
