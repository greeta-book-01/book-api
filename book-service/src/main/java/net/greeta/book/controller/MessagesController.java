package net.greeta.book.controller;

import net.greeta.book.entity.Message;
import net.greeta.book.requestmodels.AdminQuestionRequest;
import net.greeta.book.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessagesController {

    private MessagesService messagesService;

    @Autowired
    public MessagesController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @PostMapping("/secure/add/message")
    public void postMessage(@RequestBody Message messageRequest, JwtAuthenticationToken token) {
        String userEmail = token.getToken().getClaimAsString("email");
        messagesService.postMessage(messageRequest, userEmail);
    }

    @PutMapping("/secure/admin/message")
    public void putMessage(@RequestBody AdminQuestionRequest adminQuestionRequest, JwtAuthenticationToken token) throws Exception {
        String userEmail = token.getToken().getClaimAsString("email");
        messagesService.putMessage(adminQuestionRequest, userEmail);
    }

}














