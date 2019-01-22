package com.elzasantiago.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.elzasantiago.cursomc.domain.Pedido;

public interface EmailService {

	void senderOrderConfirmationEmail(Pedido obj);
	void sendEmail(SimpleMailMessage msg);
}
