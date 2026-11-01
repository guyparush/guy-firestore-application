package com.katza.guyfirstfbapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    // משתנים של המחלקה
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();


        setupButtons();
    }
    private void setupButtons() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AuthActivity.this,
                            "יש להזין מייל וסיסמה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                login(email, password);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AuthActivity.this,
                            "יש להזין מייל וסיסמה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                register(email, password);
            }
        });
    }
    private void register(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            openNextActivity();

                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "ההרשמה נכשלה";

                            Toast.makeText(AuthActivity.this,
                                    msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            checkUserProfile();

                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "ההתחברות נכשלה";

                            Toast.makeText(AuthActivity.this,
                                    msg, Toast.LENGTH_LONG).show();
                        }
                    }

                });

    }
    private void openNextActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void openCompleteRegistration() {
        startActivity(new Intent(AuthActivity.this,
                CompleteRegistrationActivity.class));
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            checkUserProfile();
        }
    }
    private void checkUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();

                            if (doc != null && doc.exists()) {
                                openNextActivity();
                            } else {
                                openCompleteRegistration();
                            }
                        } else {
                            Toast.makeText(AuthActivity.this,
                                    "שגיאה בבדיקת פרטי המשתמש",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}