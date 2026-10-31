package com.katza.guyfirstfbapplication;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Base64ImageActivity extends AppCompatActivity {

    private ImageView ivImage;
    private FirestoreImageDAO imageDAO;
    private EditText etFileName;

    private Bitmap selectedBitmap;

    private ActivityResultLauncher<String> galleryImageLauncher;
    private ActivityResultLauncher<Void> cameraPreviewLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base64_image);

        ivImage = findViewById(R.id.ivImage);
        etFileName = findViewById(R.id.etFileName);

        Button btnGallery = findViewById(R.id.btnGallery);
        Button btnCamera = findViewById(R.id.btnCamera);

        // בחירת תמונה מהגלריה
        galleryImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        Toast.makeText(this,
                                "לא נבחרה תמונה",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        selectedBitmap =
                                ImageUtils.convertUriToBitmap(this, uri);

                        ivImage.setImageBitmap(selectedBitmap);

                    } catch (IOException | RuntimeException error) {
                        Toast.makeText(this,
                                "שגיאה בטעינת התמונה",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // צילום תמונה באמצעות המצלמה
        cameraPreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap == null) {
                        Toast.makeText(this,
                                "הצילום בוטל",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedBitmap = bitmap;
                    ivImage.setImageBitmap(selectedBitmap);
                });

        btnGallery.setOnClickListener(view ->
                galleryImageLauncher.launch("image/*"));

        btnCamera.setOnClickListener(view ->
                cameraPreviewLauncher.launch(null));
        imageDAO = new FirestoreImageDAO();

        Button btnSaveImage = findViewById(R.id.btnSaveImage);
        Button btnReadImage = findViewById(R.id.btnReadImage);
        btnSaveImage.setOnClickListener(view -> {

            String fileName = etFileName.getText().toString().trim();

            if (selectedBitmap == null) {
                Toast.makeText(this,
                        "בחר תמונה לפני השמירה",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (fileName.isEmpty()) {
                Toast.makeText(this,
                        "הכנס שם לתמונה",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String base64 = ImageUtils.convertBitmapToBase64(selectedBitmap);

            if (base64 == null) {
                Toast.makeText(this,
                        "שגיאה בהמרת התמונה",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            imageDAO.saveToFirestore(
                    fileName,
                    base64,
                    new IFFirestoreImageCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(Base64ImageActivity.this,
                                    "התמונה נשמרה בהצלחה",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Toast.makeText(Base64ImageActivity.this,
                                    "שגיאה בשמירת התמונה: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
        btnReadImage.setOnClickListener(view -> {

            String fileName = etFileName.getText().toString().trim();

            if (fileName.isEmpty()) {
                Toast.makeText(this,
                        "הכנס שם לתמונה",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            imageDAO.readFromFirestore(
                    fileName,
                    new IFFirestoreImageCallback<String>() {

                        @Override
                        public void onSuccess(String base64) {

                            selectedBitmap =
                                    ImageUtils.convertBase64ToBitmap(base64);

                            if (selectedBitmap == null) {
                                Toast.makeText(Base64ImageActivity.this,
                                        "לא ניתן להציג את התמונה",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ivImage.setImageBitmap(selectedBitmap);

                            Toast.makeText(Base64ImageActivity.this,
                                    "התמונה נטענה בהצלחה",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Toast.makeText(Base64ImageActivity.this,
                                    "שגיאה בקריאת התמונה: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}