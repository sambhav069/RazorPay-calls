CREATE TABLE IF NOT EXISTS payment_transactions (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    payment_id VARCHAR(100) ,
    order_id VARCHAR(100) ,
    payment_status VARCHAR(50) ,
    amount INTEGER ,
    refund_id VARCHAR(100),
    refund_status VARCHAR(50)
    );
