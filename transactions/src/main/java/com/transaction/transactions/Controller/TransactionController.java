package com.transaction.transactions.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.transaction.transactions.Services.JwtService;

import com.transaction.transactions.Dto.TransactionRequest;
import com.transaction.transactions.Model.TransactionModel;
import com.transaction.transactions.Services.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtService jwtService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createTransaction(@RequestBody TransactionRequest request, @RequestHeader(name = "Authorization") String token) {
        Map<String, String> response = new HashMap<>();
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                response.put("Error", "Unauthorized access. No token provided.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extract the userId from the token
            String jwt = token.substring(7);
            Long userId = jwtService.extractUserId(jwt);

            if (userId == null) {
                response.put("Error", "Unauthorized access. Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Pass the userId to the service layer
            transactionService.createTransaction(userId, request);

            response.put("Message", "Transaction created successfully.");
            response.put("Status", String.valueOf(HttpStatus.CREATED.value()));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
