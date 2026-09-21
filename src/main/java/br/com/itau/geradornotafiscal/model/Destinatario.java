package br.com.itau.geradornotafiscal.model;

import java.util.List;

import br.com.itau.geradornotafiscal.model.validation.EnderecoEntregaPresente;
import br.com.itau.geradornotafiscal.model.validation.RegimeTributacaoParaPessoaJuridica;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EnderecoEntregaPresente
@RegimeTributacaoParaPessoaJuridica
public class Destinatario {
	@JsonProperty("nome")
	@NotBlank(message = "Nome do destinatario e obrigatorio")
	private String nome;

	@JsonProperty("tipo_pessoa")
	@NotNull(message = "tipo_pessoa e obrigatorio")
	private TipoPessoa tipoPessoa;

	@JsonProperty("regime_tributacao")
	private RegimeTributacaoPJ regimeTributacao;

	@JsonProperty("documentos")
	private List<@Valid Documento> documentos;

	@JsonProperty("enderecos")
	@NotEmpty(message = "Destinatario deve conter ao menos um endereco")
	private List<@Valid Endereco> enderecos;

}
