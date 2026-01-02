ALTER TABLE accounts
ALTER COLUMN balance_currency TYPE VARCHAR(3);

ALTER TABLE operations
ALTER COLUMN amount_currency TYPE VARCHAR(3),
  ALTER COLUMN resulting_balance_currency TYPE VARCHAR(3);
