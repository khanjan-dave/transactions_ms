-- Create database
CREATE DATABASE IF NOT EXISTS transaction_db;

-- Connect to database
\c transaction_db;

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    
    -- Transaction Details
    transaction_reference VARCHAR(50) UNIQUE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    
    -- Sender Information
    sender_user_id BIGINT NOT NULL,
    sender_username VARCHAR(50) NOT NULL,
    sender_account_number VARCHAR(20) NOT NULL,
    sender_balance_before DECIMAL(15, 2),
    sender_balance_after DECIMAL(15, 2),
    
    -- Receiver Information
    receiver_user_id BIGINT,
    receiver_username VARCHAR(50),
    receiver_account_number VARCHAR(20),
    receiver_balance_before DECIMAL(15, 2),
    receiver_balance_after DECIMAL(15, 2),
    
    -- Transaction Status & Metadata
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    description TEXT,
    notes TEXT,
    
    -- Security & Audit
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_fingerprint VARCHAR(255),
    
    -- Fraud Detection
    fraud_score DECIMAL(3, 2),
    fraud_flags TEXT,
    
    -- Timestamps
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'VALIDATING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REVERSED', 'CANCELLED'))
);

-- Create indexes
CREATE INDEX idx_transactions_sender ON transactions(sender_user_id, sender_account_number);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_user_id, receiver_account_number);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_date ON transactions(initiated_at DESC);
CREATE INDEX idx_transactions_reference ON transactions(transaction_reference);

-- Create transaction audit logs table
CREATE TABLE IF NOT EXISTS transaction_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    transaction_reference VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    details TEXT,
    error_message TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes for audit logs
CREATE INDEX idx_audit_transaction ON transaction_audit_logs(transaction_id);
CREATE INDEX idx_audit_reference ON transaction_audit_logs(transaction_reference);
CREATE INDEX idx_audit_date ON transaction_audit_logs(created_at DESC);

-- Display success message
SELECT 'Transaction database setup completed successfully!' AS message;