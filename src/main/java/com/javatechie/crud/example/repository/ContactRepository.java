package com.javatechie.crud.example.repository;

import com.javatechie.crud.example.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);
}
