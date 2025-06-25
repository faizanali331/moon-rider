package com.javatechie.crud.example.service;




import com.javatechie.crud.example.entity.Contact;
import com.javatechie.crud.example.repository.ContactRepository;
import com.javatechie.crud.example.response.ContactResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContactService {

    @Autowired
    private ContactRepository repo; // Injects the ContactRepository for DB operations

    // Main method to process incoming email & phoneNumber and return consolidated info

    public ContactResponse identify(String email, String phoneNumber) {
        List<Contact> contacts = repo.findByEmailOrPhoneNumber(email, phoneNumber); // Fetch contacts that match either the email or phone number

        // If no matching contact is found, create a new 'primary' contact
        if (contacts.isEmpty()) {
            Contact newPrimary = new Contact(null, email, phoneNumber, null, "primary",
                    LocalDateTime.now(), LocalDateTime.now(), null);
            Contact saved = repo.save(newPrimary);

            // Build and return the response for this new contact
            return new ContactResponse(saved.getId(),
                    email != null ? List.of(email) : new ArrayList<>(),
                    phoneNumber != null ? List.of(phoneNumber) : new ArrayList<>(),
                    List.of());
        }

        // Find the oldest contact with 'primary' precedence
        Contact primary = null;
        for (Contact c : contacts) {
            if ("primary".equals(c.getLinkPrecedence())) {
                if (primary == null || c.getCreatedAt().isBefore(primary.getCreatedAt())) {
                    primary = c;
                }
            }
        }
        if (primary == null) {
            primary = contacts.get(0);  // If no primary was found, use the first contact as fallback
        }

        // Sets to collect unique emails, phone numbers, and secondary contact IDs
        Set<String> emails = new HashSet<>();
        Set<String> phoneNumbers = new HashSet<>();
        Set<Integer> secondaryIds = new HashSet<>();

        for (Contact c : contacts) {
            // Collect all available emails and phone numbers
            if (c.getEmail() != null) emails.add(c.getEmail());
            if (c.getPhoneNumber() != null) phoneNumbers.add(c.getPhoneNumber());

            // If this contact is also marked as 'primary' but is not the actual oldest one,
            // downgrade it to 'secondary' and link it to the true primary
            if (!c.getId().equals(primary.getId()) && "primary".equals(c.getLinkPrecedence())) {
                c.setLinkPrecedence("secondary");
                c.setLinkedId(primary.getId());
                c.setUpdatedAt(LocalDateTime.now());
                repo.save(c);
            }

            if (Objects.equals(c.getLinkedId(), primary.getId())) {
                secondaryIds.add(c.getId()); // If this contact is already linked to the primary, add to secondaryIds
            }
        }

        // Check whether the given (email, phoneNumber) pair already exists
        boolean alreadyExists = false;
        for (Contact c : contacts) {
            if (Objects.equals(c.getEmail(), email) &&
                    Objects.equals(c.getPhoneNumber(), phoneNumber)) {
                alreadyExists = true;
                break;
            }
        }

        // If this exact combination is new, create a new 'secondary' contact linked to primary
        if (!alreadyExists) {
            Contact newSecondary = new Contact(null, email, phoneNumber, primary.getId(), "secondary",
                    LocalDateTime.now(), LocalDateTime.now(), null);
            Contact saved = repo.save(newSecondary);
            if (saved.getEmail() != null) emails.add(saved.getEmail());
            if (saved.getPhoneNumber() != null) phoneNumbers.add(saved.getPhoneNumber());
            secondaryIds.add(saved.getId());
        }
        // Return consolidated contact response with primary ID, all emails, phone numbers, and secondary contact IDs
        return new ContactResponse(
                primary.getId(),
                new ArrayList<>(emails),
                new ArrayList<>(phoneNumbers),
                new ArrayList<>(secondaryIds)
        );
    }
}

