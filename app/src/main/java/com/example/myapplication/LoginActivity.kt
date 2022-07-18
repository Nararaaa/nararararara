package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var googleSignInClient: GoogleSignInClient;
    private lateinit var signInButton: SignInButton;
    private var Tag = "mainTag";
    private var Google_Login_code = 123;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        signInButton = findViewById(R.id.google_signIn_button)

        // 구글 로그인 구성
        val gos = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gos)

        // firebase 초기화
        auth = FirebaseAuth.getInstance();
    }

    // 사용자 로그인 여부 확인
    public override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Log.e("Google account", "로그인 안되있음")
        } else {
            Log.e("Google account", "로그인 완료된 상태")
        }
    }

    // 로그인 요청
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Google_Login_code){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var account: GoogleSignInAccount? = null;
            try {
                // 구글로그인 성공, firebase 인증
                account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account);
            }catch (e :ApiException){
                Log.w(Tag,"Google sign in failed", e);
                Toast.makeText(applicationContext, "Google sign in failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    // firebase 인증
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?){
        Log.d(Tag,"firebaseAuthWithGoogle: " + account?.id);
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null);
        auth.signInWithCredential(credential)
            ?.addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    // 로그인 성공
                    Log.d(Tag, "signInWithCredential:success");
                    var user = auth.currentUser;
                    }
                else
                {
                    // 로그인 실패
                    Log.w(Tag, "signInWithCredential:failure", it.exception);
                }
            }
    }

    // SignIn
    private fun signIn(){
        var signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Google_Login_code);
    }

    // SignOut
    private fun signOut(){
        // firebase signOut
        auth.signOut();

        // google signOut
        googleSignInClient.signOut().addOnCompleteListener(
            this
        ) { Toast.makeText(applicationContext, "Complete", Toast.LENGTH_LONG).show() }
    }

    private fun revokeAccess(){
        // firebase signOut
        auth.signOut();

        // go
        googleSignInClient.revokeAccess().addOnCanceledListener(
            this
        ) {Toast.makeText(applicationContext, "Complete", Toast.LENGTH_LONG).show() }
    }

}