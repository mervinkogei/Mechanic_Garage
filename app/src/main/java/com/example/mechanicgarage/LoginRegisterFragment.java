package com.example.mechanicgarage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mechanicgarage.models.User;
import com.example.mechanicgarage.utils.DBConstants;
import com.example.mechanicgarage.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginRegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginRegisterFragment extends Fragment {
    private static final String TAG = "LoginRegisterFragment";


    private static final String KEY_FRAGMENT_TYPE = "fragment_type";
    public static final int LOGIN_FRAGMENT = 111;
    public static final int REGISTER_FRAGMENT = 222;


    @BindView(R.id.email_input)
    EditText emailInput;
    @BindView(R.id.password_input)
    EditText passwordInput;
    @BindView(R.id.password_reinput)
    EditText passwordReinput;
    @BindView(R.id.name_input)
    EditText nameInput;
    @BindView(R.id.phone_input)
    EditText phoneInput;
    @BindView(R.id.submit_login_register)
    Button submitRegisterLogin;
    @BindView(R.id.toogle_register_login)
    Button toggleType;


    private int fragmentTypeFlag;

    private FirebaseAuth firebaseAuth;

    public static LoginRegisterFragment newInstance(int fragmentTypeKey) {
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_TYPE, fragmentTypeKey);
        LoginRegisterFragment fragment = new LoginRegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        getExtras();
        setUi();
        initAuth();
    }

    private void getExtras() {
        fragmentTypeFlag = getArguments().getInt(KEY_FRAGMENT_TYPE);
        if (fragmentTypeFlag != LOGIN_FRAGMENT && fragmentTypeFlag != REGISTER_FRAGMENT) {
            Log.d(TAG, "getExtras: fragment type not properly set, use available constants");
            getActivity().onBackPressed();
        }
    }

    private void setUi() {
        if (fragmentTypeFlag == REGISTER_FRAGMENT) {
            passwordReinput.setVisibility(View.VISIBLE);
            nameInput.setVisibility(View.VISIBLE);
            phoneInput.setVisibility(View.VISIBLE);
            toggleType.setText("login toogle");
        } else {
            toggleType.setText("register toogle");
        }
    }

    private void initAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
    }


    @OnClick(R.id.submit_login_register)
    void onSubmitClicked() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String password2 = passwordReinput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        boolean error = false;

        if (!StringUtils.isValidEmail(email)) {
            emailInput.setError(getString(R.string.login_email_error));
            error = true;
        }
        if (password.length() < 6) {
            passwordInput.setError(getString(R.string.login_password_error));
            error = true;
        }
        if (fragmentTypeFlag == REGISTER_FRAGMENT) {

            if (!password.equals(password2)) {
                passwordReinput.setError("login_password_different_error");
                passwordInput.setError("login_password_different_error");
                error = true;
            }
            if (name.isEmpty()) {
                nameInput.setError("login_name_error");
                error = true;
            }
            if (phone.isEmpty()) {
                phoneInput.setError("login_name_error");
                error = true;
            }
        }

        if (error) {
            return;
        }

        emailInput.onEditorAction(EditorInfo.IME_ACTION_DONE);
        passwordInput.onEditorAction(EditorInfo.IME_ACTION_DONE);

        if (fragmentTypeFlag == REGISTER_FRAGMENT) {
            User userToCreate = new User(email, name, phone, null, null);
            createUser(userToCreate, password);
        } else {
            signInUser(email, password);
        }

    }

    private void createUser(final User userToCreate, String password) {
        firebaseAuth.createUserWithEmailAndPassword(userToCreate.getEmail(), password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: created new user with email: " + userToCreate.getEmail());
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    UserProfileChangeRequest userUpdates = new UserProfileChangeRequest.Builder().setDisplayName(userToCreate.getName()).build();
                    user.updateProfile(userUpdates);
                    saveUserToDB(user, userToCreate);
                    Toast.makeText(getActivity().getApplicationContext(), "registration success", Toast.LENGTH_SHORT).show();
                    onSuccessfulSingIn();
                } else {
                    Log.w(TAG, "onComplete: failure", task.getException());
                    Toast.makeText(getActivity().getApplicationContext(), "registration error", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void saveUserToDB(FirebaseUser firebaseUser, User user) {
        user.setUid(firebaseUser.getUid());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(DBConstants.DB_USERS_COLLECTION).document(user.getUid()).set(user, SetOptions.merge());
    }

    private void signInUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: sign in success");
                    Toast.makeText(getActivity().getApplicationContext(), "Sign_in_success", Toast.LENGTH_SHORT).show();
                    onSuccessfulSingIn();
                } else {
                    Log.w(TAG, "onComplete: sign in failed", task.getException());
                    Toast.makeText(getActivity().getApplicationContext(), "sign_in_error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OnClick(R.id.toogle_register_login)
    void onToggleClicked() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (fragmentTypeFlag == LoginRegisterFragment.LOGIN_FRAGMENT) {
            fragmentTransaction.replace(R.id.fragment_container, LoginRegisterFragment.newInstance(LoginRegisterFragment.REGISTER_FRAGMENT)).addToBackStack(null);
        } else {
            fragmentTransaction.replace(R.id.fragment_container, LoginRegisterFragment.newInstance(LoginRegisterFragment.LOGIN_FRAGMENT)).addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    private void onSuccessfulSingIn() {
        getActivity().onBackPressed();
    }
}