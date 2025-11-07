package com.yourorg.attendance;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.global.opencv_imgproc;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class FaceTrainer {

    // datasetPath example: "dataset"
    // modelPath example: "trainer.yml"
    public static void train(String datasetPath, String modelPath) {
        File root = new File(datasetPath);
        File[] personDirs = root.listFiles(File::isDirectory);
        if (personDirs == null || personDirs.length == 0) {
            System.out.println("No dataset found in " + datasetPath);
            return;
        }

        List<Mat> images = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        int label = 0;

        // We assume folder order corresponds to labels; better: use mapping table to user_id
        for (File personDir : personDirs) {
            File[] imgs = personDir.listFiles((d, name) -> {
                String l = name.toLowerCase();
                return l.endsWith(".jpg") || l.endsWith(".png");
            });
            if (imgs == null) { label++; continue; }

            for (File imgFile : imgs) {
                Mat img = opencv_imgcodecs.imread(imgFile.getAbsolutePath());
                if (img.empty()) continue;
                Mat gray = new Mat();
                opencv_imgproc.cvtColor(img, gray, opencv_imgproc.COLOR_BGR2GRAY);
                images.add(gray);
                labels.add(label);
            }
            label++;
        }

        if (images.isEmpty()) {
            System.out.println("No images to train.");
            return;
        }

        MatVector matVector = new MatVector(images.size());
        Mat labelsMat = new Mat(images.size(), 1, opencv_core.CV_32SC1);
        IntBuffer ib = labelsMat.createBuffer();

        for (int i = 0; i < images.size(); i++) {
            matVector.put(i, images.get(i));
            ib.put(i, labels.get(i));
        }

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.train(matVector, labelsMat);
        recognizer.save(modelPath);
        System.out.println("Training complete. Model saved to: " + modelPath);
    }
}
