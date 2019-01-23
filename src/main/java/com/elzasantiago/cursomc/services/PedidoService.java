   package com.elzasantiago.cursomc.services;


import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.elzasantiago.cursomc.domain.ItemPedido;
import com.elzasantiago.cursomc.domain.PagamentoComBoleto;
import com.elzasantiago.cursomc.domain.Pedido;
import com.elzasantiago.cursomc.domain.enums.EstadoPagamento;
import com.elzasantiago.cursomc.repositories.ItemPedidoRepository;
import com.elzasantiago.cursomc.repositories.PagamentoRepository;
import com.elzasantiago.cursomc.repositories.PedidoRepository;
import com.elzasantiago.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
@Autowired
private PedidoRepository repo;
	
@Autowired
private BoletoService boletoService;

@Autowired
private PagamentoRepository pagamentoRepository;

@Autowired
private ProdutoService produtoService;

@Autowired
private EmailService emailService;

@Autowired
private ClienteService clienteService;


@Autowired
private ItemPedidoRepository itemPedidoRepository;

	
	public Pedido find(Integer id) {
				Optional<Pedido> obj = repo.findById(id);
				return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));				
	}
	
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());	
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		//System.out.println(obj);
		emailService.sendOrderConfirmationHtmlEmail(obj);
		return obj;
	}
	
}
