export function headers() {
  return { "Content-Type": "application/json" };
}

export function baseUrl() {
  return __ENV.BASE_URL || "http://host.docker.internal:8080";
}

export function pfPayload() {
  return JSON.stringify({
    id_pedido: 1,
    data: "2022-05-01",
    valor_total_itens: 100.0,
    valor_frete: 10.0,
    itens: [{ id_item: 1, descricao: "Teclado USB", valor_unitario: 50, quantidade: 2 }],
    destinatario: {
      nome: "John Doe",
      tipo_pessoa: "FISICA",
      documentos: [{ tipo: "CPF", numero: "88740347095" }],
      enderecos: [{ finalidade: "ENTREGA", regiao: "SUDESTE" }],
    },
  });
}

export function pedidoGrandePayload() {
  return JSON.stringify({
    id_pedido: 2,
    data: "2022-05-01",
    valor_total_itens: 600.0,
    valor_frete: 10.0,
    itens: [
      { id_item: 1, descricao: "Item 1", valor_unitario: 100, quantidade: 1 },
      { id_item: 2, descricao: "Item 2", valor_unitario: 100, quantidade: 1 },
      { id_item: 3, descricao: "Item 3", valor_unitario: 100, quantidade: 1 },
      { id_item: 4, descricao: "Item 4", valor_unitario: 100, quantidade: 1 },
      { id_item: 5, descricao: "Item 5", valor_unitario: 100, quantidade: 1 },
      { id_item: 6, descricao: "Item 6", valor_unitario: 100, quantidade: 1 },
    ],
    destinatario: {
      nome: "John Doe",
      tipo_pessoa: "FISICA",
      documentos: [{ tipo: "CPF", numero: "88740347095" }],
      enderecos: [{ finalidade: "ENTREGA", regiao: "SUDESTE" }],
    },
  });
}
