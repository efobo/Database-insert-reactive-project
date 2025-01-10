CREATE TABLE Manufacturer (
                              id SERIAL PRIMARY KEY,
                              name VARCHAR(255) NOT NULL,
                              foundation_date DATE NOT NULL
);


CREATE TYPE Country AS ENUM (
    'USA',
    'UK',
    'GERMANY',
    'RUSSIA',
    'CHINA'
);


CREATE TABLE Product (
                         id SERIAL PRIMARY KEY,
                         price NUMERIC(10, 2) NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         release_date DATE NOT NULL,
                         country Country NOT NULL,
                         manufacturer_id INT NOT NULL,
                         FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(id)
);


CREATE TABLE Review (
                        id SERIAL PRIMARY KEY,
                        date_time TIMESTAMP WITH TIME ZONE NOT NULL,
                        user_name VARCHAR(255) NOT NULL,
                        rating INT CHECK (rating >= 1 AND rating <= 5),
                        product_id INT NOT NULL,
                        FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE INDEX idx_product_reviews ON Review (product_id);
