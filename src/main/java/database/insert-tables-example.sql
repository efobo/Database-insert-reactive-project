INSERT INTO Manufacturer (name, foundation_date)
VALUES
    ('Tesla', '2003-07-01'),
    ('Apple', '1976-04-01'),
    ('Samsung', '1938-03-01')
    RETURNING id;

INSERT INTO Country (name)
VALUES
    ('USA'),
    ('UK'),
    ('GERMANY'),
    ('RUSSIA'),
    ('CHINA')
    RETURNING id;


INSERT INTO Product (price, name, release_date, country_id, manufacturer_id)
VALUES
    (79999.99, 'Model S', '2020-01-01',
     (SELECT id FROM Country WHERE name = 'USA'),
     (SELECT id FROM Manufacturer WHERE name = 'Tesla')),
    (999.99, 'iPhone 14', '2022-09-16',
     (SELECT id FROM Country WHERE name = 'USA'),
     (SELECT id FROM Manufacturer WHERE name = 'Apple')),
    (500.00, 'Galaxy S22', '2022-02-25',
     (SELECT id FROM Country WHERE name = 'CHINA'),
     (SELECT id FROM Manufacturer WHERE name = 'Samsung'));


INSERT INTO Review (date_time, user_name, rating, product_id)
VALUES
    ('2025-01-01 10:00:00+00', 'JohnDoe', 5,
     (SELECT id FROM Product WHERE name = 'Model S')),
    ('2025-01-02 12:00:00+00', 'JaneSmith', 4,
     (SELECT id FROM Product WHERE name = 'iPhone 14')),
    ('2025-01-03 14:00:00+00', 'Alice', 3,
     (SELECT id FROM Product WHERE name = 'Galaxy S22'));

