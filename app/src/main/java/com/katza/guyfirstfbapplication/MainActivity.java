
package com.katza.guyfirstfbapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private View btnInsert;
    private EditText fullname;
    private EditText tz;
    private EditText grade;
    private TextView tvcontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        FirestoreDAO fsDAO = new FirestoreDAO();

        btnInsert = findViewById(R.id.btnInsert);
        fullname = findViewById(R.id.fullname);
        tz = findViewById(R.id.tz);
        grade = findViewById(R.id.grade);
        tvcontent = findViewById(R.id.tvcontent);

        // כפתור הוספת תלמיד
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.createDocument(
                        fullname.getText().toString(),
                        tz.getText().toString(),
                        Integer.parseInt(grade.getText().toString()));
            }
        });

        // כפתור הצגת תלמיד לפי תעודת זהות
        View btnSelect = findViewById(R.id.btnSelect);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.readDocumentById(
                        tz.getText().toString(),
                        new IFFirestoreCallback() {
                            @Override
                            public void onDataReceived(
                                    ArrayList<Student> students) {

                                if (students.isEmpty()) {
                                    tvcontent.setText("התלמיד לא נמצא");
                                } else {
                                    tvcontent.setText(
                                            students.get(0).toString());
                                }
                            }
                        });
            }
        });
    }
}
