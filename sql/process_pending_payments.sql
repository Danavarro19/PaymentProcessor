CREATE TABLE payment_processing_errors (
                                           id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           error_message VARCHAR2(4000),
                                           created_at TIMESTAMP DEFAULT SYSTIMESTAMP
);

CREATE OR REPLACE PROCEDURE process_pending_payments
AS
BEGIN
UPDATE payments
SET status = 'PROCESSED'
WHERE status = 'PENDING';

COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;

INSERT INTO payment_processing_errors (
    error_message
)
VALUES (
           SQLERRM
       );

COMMIT;
END process_pending_payments;
/