CREATE TABLE Manufacturer (
                              id SERIAL PRIMARY KEY, -- id автоматически инкрементируется
                              name VARCHAR(255) NOT NULL,
                              foundation_date DATE NOT NULL -- Дата основания
);

CREATE TABLE Product (
                         id SERIAL PRIMARY KEY, -- id продукта
                         price NUMERIC(10, 2) NOT NULL, -- цена с двумя десятичными знаками
                         name VARCHAR(255) NOT NULL, -- название продукта
                         release_date DATE NOT NULL, -- дата выпуска
                         country VARCHAR(255) NOT NULL, -- название Country
                         manufacturer_id INT NOT NULL, -- внешний ключ на Manufacturer
                         FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(id)
);

CREATE TABLE Review (
                        id SERIAL PRIMARY KEY, -- id отзыва
                        date_time TIMESTAMP WITH TIME ZONE NOT NULL, -- дата и время создания
                        user_name VARCHAR(255) NOT NULL, -- имя пользователя
                        rating INT CHECK (rating >= 1 AND rating <= 5), -- рейтинг от 1 до 5
                        product_id INT NOT NULL, -- внешний ключ на Product
                        FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE INDEX idx_product_reviews ON Review (product_id);