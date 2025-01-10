INSERT INTO Manufacturer (name, foundation_date)
VALUES
    ('Tesla', '2003-07-01'),
    ('Apple', '1976-04-01'),
    ('Samsung', '1938-03-01')
    RETURNING id;

INSERT INTO Product (price, name, release_date, country, manufacturer_id)
VALUES
    (79999.99, 'Model S', '2020-01-01', 'USA', 1),
    (999.99, 'iPhone 14', '2022-09-16', 'USA', 2),
    (500.00, 'Galaxy S22', '2022-02-25', 'CHINA', 3);

INSERT INTO Review (date_time, user_name, rating, product_id)
VALUES
    ('2025-01-01 10:00:00+00', 'JohnDoe', 5, 1),
    ('2025-01-02 12:00:00+00', 'JaneSmith', 4, 2),
    ('2025-01-03 14:00:00+00', 'Alice', 3, 3);
