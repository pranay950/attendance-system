package com.yourorg.attendance;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Size;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

public class FaceRecognizer {

    private static final double CONFIDENCE_THRESHOLD = 60.0;

    public static void recognize(String modelPath) {
        CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.read(modelPath);

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Camera not available.");
            return;
        }

        Mat frame = new Mat();
        int failedAttempts = 0;
        boolean recognized = false;

        System.out.println("Starting recognition...");

        while (failedAttempts < 10 && !recognized) {
            if (!camera.read(frame)) continue;
            if (frame.empty()) continue;

            Mat gray = new Mat();
            opencv_imgproc.cvtColor(frame, gray, opencv_imgproc.COLOR_BGR2GRAY);

            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(gray, faces);

            for (int i = 0; i < faces.size(); i++) {
                Rect r = faces.get(i);
                Mat faceROI = new Mat(gray, r);
                opencv_imgproc.resize(faceROI, faceROI, new Size(200, 200));

                int[] label = new int[1];
                double[] confidence = new double[1];
                recognizer.predict(faceROI, label, confidence);

                System.out.println("Predicted label=" + label[0] + ", confidence=" + confidence[0]);

                // ✅ Fix: shift label to match actual SQL user_id numbering
                int userId = label[0] + 1;

                if (confidence[0] < CONFIDENCE_THRESHOLD) {
                    System.out.println("Recognized user_id " + userId + " with confidence " + confidence[0]);
                    markAttendance(userId);
                    recognized = true;
                    break;
                } else {
                    failedAttempts++;
                    System.out.println("Not recognized (attempts: " + failedAttempts + ")");
                    if (failedAttempts >= 10) break;
                }
            }
        }

        if (!recognized) {
            System.out.println("Recognition stopped after " + failedAttempts + " failed attempts.");
        }

        camera.release();
    }

    private static void markAttendance(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // ✅ Safety: verify user exists before marking attendance
            String verifyUser = "SELECT user_id FROM users WHERE user_id = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyUser);
            verifyStmt.setInt(1, userId);
            ResultSet verifyRs = verifyStmt.executeQuery();
            if (!verifyRs.next()) {
                System.out.println("User ID " + userId + " does not exist in users table. Attendance not marked.");
                return;
            }

            String check = "SELECT * FROM attendance WHERE user_id=? AND attendance_date=?";
            PreparedStatement checkStmt = conn.prepareStatement(check);
            checkStmt.setInt(1, userId);
            checkStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Attendance already marked today for user_id: " + userId);
                return;
            }

            String insert = "INSERT INTO attendance (user_id, attendance_date, attendance_time, status) VALUES (?, ?, ?, 'present')";
            PreparedStatement ps = conn.prepareStatement(insert);
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ps.setTime(3, java.sql.Time.valueOf(LocalTime.now()));
            ps.executeUpdate();

            System.out.println("✅ Attendance marked for user_id: " + userId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
