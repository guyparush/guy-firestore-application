package com.katza.guyfirstfbapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CompleteRegistrationActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etBirthDate;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String birthDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_registration);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectBirthDate();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }
    private void selectBirthDate() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view,
                                          int year, int month, int day) {
                        birthDate = String.format(Locale.US,
                                "%02d/%02d/%04d", day, month + 1, year);
                        etBirthDate.setText(birthDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }
    private void saveUserProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל הפרטים", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "המשתמש אינו מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("birthDate", birthDate);
        data.put("email", user.getEmail());

        db.collection("Users").document(user.getUid()).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(
                                    CompleteRegistrationActivity.this,
                                    MainActivity.class
                            );
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(
                                    CompleteRegistrationActivity.this,
                                    "שמירת הפרטים נכשלה. נסו שוב",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(
                    CompleteRegistrationActivity.this,
                    AuthActivity.class
            ));
            finish();
        }
    }
}