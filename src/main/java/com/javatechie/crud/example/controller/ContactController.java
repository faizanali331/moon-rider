package com.javatechie.crud.example.controller;



import com.javatechie.crud.example.response.ContactResponse;
import com.javatechie.crud.example.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api") // Base URL for all endpoints in this controller will start with "/api"
public class ContactController {

    @Autowired
    private ContactService contactService; // Inject the ContactService using Spring's dependency injection

    @PostMapping("/identify")
    public ContactResponse identifyContact(@RequestBody Map<String, String> body) {
        String email = body.get("email"); // Extract the "email" and "phoneNumber" from the incoming JSON request body
        String phoneNumber = body.get("phoneNumber");
        return contactService.identify(email, phoneNumber);// Extract the "email" and "phoneNumber" from the incoming JSON request body
    }
}
