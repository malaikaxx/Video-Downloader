package com.streamvault;

import com.streamvault.ContactMessage;
import com.streamvault.User;
import com.streamvault.ContactMessageRepository;
import com.streamvault.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ContactMessageRepository contactMessageRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String email, LocalDate birthdate, String phoneNumber, String rawPassword) {
        if (userRepository.existsByUsernameIgnoreCase(username.trim())) {
            throw new IllegalArgumentException("Username is already taken. Please choose another.");
        }
        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new IllegalArgumentException("Email is already registered. Please sign in.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setBirthdate(birthdate);
        user.setPhoneNumber(phoneNumber.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }

    public User authenticate(String loginId, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(loginId.trim(), loginId.trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid Username/Email or Password.");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid Username/Email or Password.");
        }
        return user;
    }

    public void saveContactMessage(String fullName, String email, String subject, String message) {
        ContactMessage msg = new ContactMessage();
        msg.setFullName(fullName.trim());
        msg.setEmail(email.trim());
        msg.setSubject(subject.trim());
        msg.setMessage(message.trim());
        contactMessageRepository.save(msg);
    }
}