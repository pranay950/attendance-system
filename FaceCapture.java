package com.yourorg.attendance;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FaceCapture {

    // datasetPath example: "dataset"
    public static void capture(String datasetPath, String personName, int userId) {
        CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not available.");
            return;
        }

        File dir = new File(datasetPath + File.separator + personName);
        if (!dir.exists()) dir.mkdirs();

        Mat frame = new Mat();
        int count = 0;
        System.out.println("Capturing faces for " + personName + " (userId=" + userId + ") ...");

        while (count < 20) { // capture 20 images
            if (!camera.read(frame)) continue;
            if (frame.empty()) continue;

            // convert to grayscale
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(frame, gray, opencv_imgproc.COLOR_BGR2GRAY);

            // detect faces
            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(gray, faces);

            for (int i = 0; i < faces.size() && count < 20; i++) {
                Rect r = faces.get(i);
                Mat faceROI = new Mat(gray, r);
                opencv_imgproc.resize(faceROI, faceROI, new Size(200, 200));

                String filename = datasetPath + File.separator + personName + File.separator + "img" + count + ".jpg";
                opencv_imgcodecs.imwrite(filename, faceROI);
                System.out.println("Saved: " + filename);

                // Insert into face_images table
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO face_images (user_id, image_path) VALUES (?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, userId);
                    ps.setString(2, filename);
                    ps.executeUpdate();
                } catch (Exception ex) {
                    System.out.println("Warning: could not insert into face_images: " + ex.getMessage());
                }

                count++;
            }
        }

        camera.release();
        System.out.println("Face capture completed: " + count + " images saved for " + personName);
    }
}
