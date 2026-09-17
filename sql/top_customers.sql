SELECT
    c.id AS customer_id,
    c.name AS customer_name,
    c.country,
    SUM(p.amount) AS total_paid,
    COUNT(p.id) AS payment_count,
    AVG(p.amount) AS average_ticket
FROM customers c
         JOIN payments p
              ON p.customer_id = c.id
WHERE p.status = 'PROCESSED'
  AND p.created_at >= SYSDATE - 30
GROUP BY
    c.id,
    c.name,
    c.country
ORDER BY total_paid DESC
    FETCH FIRST 10 ROWS ONLY;