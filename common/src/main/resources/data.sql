INSERT INTO usuarios (nombre, direccion, telefono, email, password, rol) VALUES 
('Admin Local', 'Central', '00000000', 'admin@fastorder.com', '$2a$10$wK1F5z3U5O6b5jK7V8x9Z.k1J4M5z8r6R9G8pY5h5N2V4A1J4K7y', 'ADMIN'),
('Repartidor Juan', 'Zona 1', '11111111', 'juan@fastorder.com', '$2a$10$wK1F5z3U5O6b5jK7V8x9Z.k1J4M5z8r6R9G8pY5h5N2V4A1J4K7y', 'REPARTIDOR'),
('Cliente Maria', 'Zona 2', '22222222', 'maria@fastorder.com', '$2a$10$wK1F5z3U5O6b5jK7V8x9Z.k1J4M5z8r6R9G8pY5h5N2V4A1J4K7y', 'CLIENTE');

INSERT INTO comercios (nombre, categoria, direccion, abierto) VALUES
('Super Burger', 'RESTAURANTE', 'Calle 10, Local A', true);

INSERT INTO productos (comercio_id, nombre, precio, stock, disponible) VALUES
(1, 'Hamburguesa Doble', 45.00, 100, true),
(1, 'Papas Fritas', 15.00, 200, true),
(1, 'Gaseosa', 10.00, 150, true);
