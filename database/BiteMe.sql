DROP DATABASE IF EXISTS biteme;
CREATE DATABASE biteme;
USE biteme;

-- Table structure for table `Users`
CREATE TABLE Users (
    userId VARCHAR(50) PRIMARY KEY,
    firstName VARCHAR(50),
    lastName VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    phoneNumber VARCHAR(20),
    password VARCHAR(255),
    role ENUM('CUSTOMER_BUSINESS', 'CUSTOMER_PRIVATE', 'MANAGER', 'CEO', 'RESTAURANT'),
    creditCard VARCHAR(20),
    credit DECIMAL(10, 2) DEFAULT 0,
    connected BOOLEAN DEFAULT FALSE
);

-- Table structure for table `Restaurants`
CREATE TABLE Restaurants (
    restaurantId VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    location VARCHAR(255),
    branch ENUM('NORTH', 'SOUTH', 'CENTER'),
    FOREIGN KEY (restaurantId) REFERENCES Users(userId)
);

-- Table structure for table `Managers`
CREATE TABLE Managers (
    managerId VARCHAR(50),
    restaurantId VARCHAR(50),
    FOREIGN KEY (managerId) REFERENCES Users(userId),
    FOREIGN KEY (restaurantId) REFERENCES Restaurants(restaurantId)
);

-- Table structure for table `MenuItems`
CREATE TABLE MenuItems (
    itemId INT AUTO_INCREMENT PRIMARY KEY,
    restaurantId VARCHAR(50),
    name VARCHAR(100),
    description TEXT,
    price DECIMAL(10, 2),
    isInStock BOOLEAN DEFAULT TRUE,
    type ENUM('drink', 'main', 'first', 'salad', 'dessert'),
    quantity INT,
    FOREIGN KEY (restaurantId) REFERENCES Restaurants(restaurantId)
);

-- Table structure for table `Orders`
CREATE TABLE Orders (
    orderId INT AUTO_INCREMENT PRIMARY KEY,
    customerId VARCHAR(50),
    restaurantId VARCHAR(50),
    totalPrice DECIMAL(10, 2),
    status ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED'),
    deliveryType ENUM('SELF_PICKUP', 'DELIVERY', 'EARLY_DELIVERY', 'SHARED_DELIVERY'),
    isPayed BOOLEAN DEFAULT FALSE,
    orderTime DATETIME,
    requiredTime DATETIME,
	actualArrivalTime DATETIME,
    deliveryAddress VARCHAR(255),
    recipientName VARCHAR(100),
    recipientPhone VARCHAR(20),
    discountApplied BOOLEAN DEFAULT FALSE,
    robot BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (customerId) REFERENCES Users(userId),
    FOREIGN KEY (restaurantId) REFERENCES Restaurants(restaurantId)
);

-- Table structure for table `OrderItems`
CREATE TABLE OrderItems (
    orderItemId INT AUTO_INCREMENT PRIMARY KEY,
    orderId INT,
    itemId INT,
    quantity INT,
    specialInstructions TEXT,
    FOREIGN KEY (orderId) REFERENCES Orders(orderId),
    FOREIGN KEY (itemId) REFERENCES MenuItems(itemId)
);

-- Dumping data for table `Users`
INSERT INTO Users (userId, firstName, lastName, email, phoneNumber, password, role) VALUES 
('user1', 'John', 'Doe', 'john.doe@example.com', '1234567890', '112233', 'CUSTOMER_PRIVATE'),
('user2', 'Jane', 'Smith', 'jane.smith@example.com', '0987654321', '445566', 'CUSTOMER_BUSINESS'),
('north', 'North', 'Manager', 'north.manager@biteme.com', '1112223333', '123', 'MANAGER'),
('south', 'South', 'Manager', 'south.manager@biteme.com', '4445556666', '456', 'MANAGER'),
('center', 'Center', 'Manager', 'center.manager@biteme.com', '7778889999', '789', 'MANAGER'),
('ceo', 'CEO', 'User', 'ceo@biteme.com', '9988776655', '12345', 'CEO'),
('rest1', 'Restaurant A', '', 'restauranta@example.com', '0521234567', 'restpass1', 'RESTAURANT'),
('rest2', 'Restaurant B', '', 'restaurantb@example.com', '0522345678', 'restpass2', 'RESTAURANT'),
('rest3', 'Restaurant C', '', 'restaurantc@example.com', '0523456789', 'restpass3', 'RESTAURANT'),
('rest4', 'Restaurant D', '', 'restaurantd@example.com', '0524567890', 'restpass4', 'RESTAURANT'),
('rest5', 'Restaurant E', '', 'restaurante@example.com', '0525678901', 'restpass5', 'RESTAURANT'),
('rest6', 'Restaurant F', '', 'restaurantf@example.com', '0526789012', 'restpass6', 'RESTAURANT'),
('rest7', 'Restaurant G', '', 'restaurantg@example.com', '0527890123', 'restpass7', 'RESTAURANT'),
('rest8', 'Restaurant H', '', 'restauranth@example.com', '0528901234', 'restpass8', 'RESTAURANT'),
('rest9', 'Restaurant I', '', 'restauranti@example.com', '0529012345', 'restpass9', 'RESTAURANT'),
('rest10', 'Restaurant J', '', 'restaurantj@example.com', '0520123456', 'restpass10', 'RESTAURANT');

-- Dumping data for table `Restaurants`
INSERT INTO Restaurants (restaurantId, name, location, branch) VALUES 
('rest1', 'Restaurant A', '123 Main St', 'NORTH'),
('rest2', 'Restaurant B', '456 Elm St', 'NORTH'),
('rest3', 'Restaurant C', '789 Oak St', 'NORTH'),
('rest4', 'Restaurant D', '101 Pine St', 'SOUTH'),
('rest5', 'Restaurant E', '202 Maple St', 'SOUTH'),
('rest6', 'Restaurant F', '303 Birch St', 'SOUTH'),
('rest7', 'Restaurant G', '404 Cedar St', 'SOUTH'),
('rest8', 'Restaurant H', '505 Walnut St', 'CENTER'),
('rest9', 'Restaurant I', '606 Cherry St', 'CENTER'),
('rest10', 'Restaurant J', '707 Ash St', 'CENTER');

-- Dumping data for table `Managers`
INSERT INTO Managers (managerId, restaurantId) VALUES 
('north', 'rest1'), ('north', 'rest2'), ('north', 'rest3'),
('south', 'rest4'), ('south', 'rest5'), ('south', 'rest6'), ('south', 'rest7'),
('center', 'rest8'), ('center', 'rest9'), ('center', 'rest10');

-- Dumping data for table `MenuItems`
INSERT INTO MenuItems (restaurantId, name, description, price, isInStock, type, quantity) VALUES 
('rest1', 'Burger', 'Delicious beef burger', 10.99, TRUE, 'main', 10),
('rest1', 'Salad', 'Fresh garden salad', 5.99, TRUE, 'salad', 10),
('rest1', 'Soda', 'Refreshing soda', 1.99, TRUE, 'drink', 10),
('rest2', 'Pizza', 'Cheesy pizza', 8.99, TRUE, 'main', 10),
('rest2', 'Ice Cream', 'Vanilla ice cream', 3.99, TRUE, 'dessert', 10),
('rest2', 'Pasta', 'Creamy pasta', 7.99, TRUE, 'main', 10),
('rest3', 'Steak', 'Juicy steak', 15.99, TRUE, 'main', 10),
('rest3', 'Wine', 'Red wine', 4.99, TRUE, 'drink', 10),
('rest4', 'Soup', 'Hot soup', 4.99, TRUE, 'first', 10),
('rest4', 'Cake', 'Chocolate cake', 5.99, TRUE, 'dessert', 10),
('rest5', 'Sandwich', 'Ham sandwich', 6.99, TRUE, 'main', 10),
('rest5', 'Juice', 'Orange juice', 2.99, TRUE, 'drink', 10),
('rest6', 'Fish', 'Grilled fish', 12.99, TRUE, 'main', 10),
('rest6', 'Coffee', 'Hot coffee', 2.49, TRUE, 'drink', 10),
('rest7', 'Tacos', 'Spicy beef tacos', 9.99, TRUE, 'main', 10),
('rest7', 'Smoothie', 'Fruit smoothie', 3.49, TRUE, 'drink', 10),
('rest8', 'Pancakes', 'Fluffy pancakes', 7.49, TRUE, 'dessert', 10),
('rest8', 'Tea', 'Green tea', 1.99, TRUE, 'drink', 10),
('rest9', 'Sushi', 'Fresh sushi rolls', 14.99, TRUE, 'main', 10),
('rest9', 'Miso Soup', 'Traditional miso soup', 3.99, TRUE, 'first', 10),
('rest10', 'BBQ Ribs', 'Smoky BBQ ribs', 18.99, TRUE, 'main', 10),
('rest10', 'Lemonade', 'Homemade lemonade', 2.99, TRUE, 'drink', 10);