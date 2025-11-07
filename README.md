# 🎯 Face Recognition Attendance System

A JavaFX + OpenCV-based attendance system that uses facial recognition to mark student attendance automatically.  
The system stores user details and attendance records in a MySQL database and uses the LBPH (Local Binary Patterns Histogram) algorithm for face recognition.

---

## 🧠 Features

✅ Real-time face detection and recognition using OpenCV  
✅ Attendance stored automatically in MySQL database  
✅ LBPH model training for new users  
✅ Prevents duplicate attendance entries  
✅ Stops recognition after successful detection or 10 failed attempts  
✅ Modular structure — Capture, Train, Recognize classes  

---

## 🏗️ Project Structure

attendance-system/
├── src/main/java/com/yourorg/attendance/
│ ├── UserInterface.java
│ ├── FaceCapture.java
│ ├── FaceTrainer.java
│ ├── FaceRecognizer.java
│ ├── DatabaseConnection.java
│ └── utils/
│ └── (support classes if needed)
├── dataset/
│ ├── User.5.1.jpg
│ ├── User.5.2.jpg
│ └── ...
├── trainer.yml
├── haarcascade_frontalface_default.xml
├── pom.xml
└── README.md
