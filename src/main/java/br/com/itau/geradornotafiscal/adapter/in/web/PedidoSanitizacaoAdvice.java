package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Documento;
import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.Pedido;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.List;

@RestControllerAdvice
public class PedidoSanitizacaoAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return Pedido.class.isAssignableFrom(methodParameter.getParameterType());
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        sanitizar((Pedido) body);
        return body;
    }

    private void sanitizar(Pedido pedido) {
        if (pedido == null) {
            return;
        }
        sanitizarItens(pedido.getItens());
        sanitizarDestinatario(pedido.getDestinatario());
    }

    private void sanitizarItens(List<Item> itens) {
        if (itens == null) {
            return;
        }
        for (Item item : itens) {
            if (item == null) {
                continue;
            }
            item.setIdItem(limpar(item.getIdItem()));
            item.setDescricao(limpar(item.getDescricao()));
        }
    }

    private void sanitizarDestinatario(Destinatario destinatario) {
        if (destinatario == null) {
            return;
        }
        destinatario.setNome(limpar(destinatario.getNome()));
        sanitizarDocumentos(destinatario.getDocumentos());
        sanitizarEnderecos(destinatario.getEnderecos());
    }

    private void sanitizarDocumentos(List<Documento> documentos) {
        if (documentos == null) {
            return;
        }
        for (Documento documento : documentos) {
            if (documento == null) {
                continue;
            }
            documento.setNumero(limpar(documento.getNumero()));
        }
    }

    private void sanitizarEnderecos(List<Endereco> enderecos) {
        if (enderecos == null) {
            return;
        }
        for (Endereco endereco : enderecos) {
            if (endereco == null) {
                continue;
            }
            endereco.setCep(limpar(endereco.getCep()));
            endereco.setLogradouro(limpar(endereco.getLogradouro()));
            endereco.setNumero(limpar(endereco.getNumero()));
            endereco.setBairro(limpar(endereco.getBairro()));
            endereco.setCidade(limpar(endereco.getCidade()));
            endereco.setEstado(limpar(endereco.getEstado()));
            endereco.setComplemento(limpar(endereco.getComplemento()));
        }
    }

    private String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
