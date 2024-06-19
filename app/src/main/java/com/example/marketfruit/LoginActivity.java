package com.example.marketfruit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText numero, codigo;
    private Button enviarnumero,enviarcodigo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String VerificacionID;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth auth;
    private AlertDialog alertDialog;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        numero=(EditText)findViewById(R.id.numero);
        codigo=(EditText) findViewById(R.id.codigo);
        enviarnumero=(Button) findViewById(R.id.enviarnumero);
        enviarcodigo=(Button) findViewById(R.id.enviarcodigo);

        auth=FirebaseAuth.getInstance();
        alertDialog=new AlertDialog.Builder(LoginActivity.this).create();



        enviarnumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = numero.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginActivity.this, "Ingrese tu numero primero ....", Toast.LENGTH_SHORT).show();
                }else{
                    alertDialog.setTitle("Validando Numero");
                    alertDialog.setMessage("Porfavor espere mientras validamos su numero");
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(true);
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginActivity.this)
                            .setCallbacks(callbacks)
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options); //enviara el numero creo xd

                }
            }
        });

        enviarcodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numero.setVisibility(View.GONE);
                enviarnumero.setVisibility(View.GONE);
                String VerificaionCode = codigo.getText().toString();
                if (TextUtils.isEmpty(VerificaionCode)){
                    Toast.makeText(LoginActivity.this, "Ingrese el codigo recibido", Toast.LENGTH_SHORT).show();
                }else{
                    alertDialog.setTitle("Verificando");
                    alertDialog.setMessage("Espere porfavor ...");
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(true);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificacionID, VerificaionCode);
                    IngresoExitoso(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                IngresoExitoso(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                alertDialog.dismiss();
                Toast.makeText(LoginActivity.this, "ERROR: \n1. Numero Invalido \n2. Sin conexion a internet \n3. Sin codigo de region", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.VISIBLE);
                enviarnumero.setVisibility(View.VISIBLE);
                codigo.setVisibility(View.GONE);
                enviarcodigo.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                VerificacionID = s;
                resendingToken= token;
                alertDialog.dismiss();
                Toast.makeText(LoginActivity.this,"Codigo Enviado Exitosamente,Revisa tu bandeja de entrada", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.GONE);
                enviarnumero.setVisibility(View.GONE);
                codigo.setVisibility(View.VISIBLE);
                enviarcodigo.setVisibility(View.VISIBLE);


            }
        };


    }
    private void IngresoExitoso(PhoneAuthCredential credential){

        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    alertDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Ingresado con exito", Toast.LENGTH_SHORT).show();
                    irVentanaPrincipal();
                }else {
                    String err =task.getException().toString();
                    Toast.makeText(LoginActivity.this, "Error:" + err, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null){
            irVentanaPrincipal();
        }
    }

    private void irVentanaPrincipal(){

        Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("phone", phoneNumber);
        intent.putExtra("papel", "usuario");
        startActivity(intent);
        finish();
    }
}