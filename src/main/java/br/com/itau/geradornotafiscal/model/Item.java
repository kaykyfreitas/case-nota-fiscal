package br.com.itau.geradornotafiscal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Item {
	@JsonProperty("id_item")
	private String idItem;

	@JsonProperty("descricao")
	@NotBlank(message = "Descricao do item e obrigatoria")
	private String descricao;

	@JsonProperty("valor_unitario")
	@PositiveOrZero(message = "valor_unitario deve ser maior ou igual a zero")
	private double valorUnitario;

	@JsonProperty("quantidade")
	@Min(value = 1, message = "quantidade deve ser maior ou igual a 1")
	private int quantidade;
}
