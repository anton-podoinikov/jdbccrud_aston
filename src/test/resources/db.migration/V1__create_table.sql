CREATE SCHEMA IF NOT EXISTS public;
SET search_path TO public;

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL
);

CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          price NUMERIC(10, 2) NOT NULL
);
CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL REFERENCES public.users (id)
);
CREATE TABLE order_products (
                                order_id INT NOT NULL REFERENCES public.orders (id),
                                product_id INT NOT NULL REFERENCES public.products (id),
                                PRIMARY KEY (order_id, product_id)
);
INSERT INTO public.users (username, email)
VALUES ('Anton', 'antpkov@gmail.com'),
       ('Oleg', 'oleg@mail.com'),
       ('Ivan', 'ivan@gmail.ru'),
       ('Vasya', 'vas@gmail.ru');

INSERT INTO public.products (name, price)
VALUES ('Кола', 1.50),
       ('Молоко', 5.50),
       ('Хлеб', 7.50),
       ('Йогурт', 3.53),
       ('Колбаса', 8.54),
       ('Сыр', 9.50),
       ('Рыба', 11.50),
       ('Мороженое', 14.55),
       ('Конфеты', 2.50),
       ('Шоколад', 7.56),
       ('Сок', 8.50),
       ('Вода', 6.50);

INSERT INTO public.orders (user_id)
VALUES (1),
       (2),
       (3),
       (4);

INSERT INTO public.order_products (order_id, product_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 4),
       (2, 5),
       (3, 6),
       (3, 7),
       (3, 8),
       (4, 9),
       (4, 10),
       (4, 11),
       (4, 12);
