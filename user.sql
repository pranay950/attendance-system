CREATE DATABASE attendance_system;
USE attendance_system;

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  emp_roll VARCHAR(50) UNIQUE,
  dept VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
  attendance_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  attendance_date DATE,
  attendance_time TIME,
  status ENUM('present','absent','late') DEFAULT 'present',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE face_images (
  image_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  image_path VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
