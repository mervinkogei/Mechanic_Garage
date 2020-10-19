package com.example.mechanicgarage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mechanicgarage.models.Car;
import com.example.mechanicgarage.models.User;
import com.example.mechanicgarage.utils.DBConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CarDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarDetailsFragment extends Fragment {

    private static final String TAG = "CarDetailsFragment";


    @BindView(R.id.car_make_input)
    EditText carMakeInput;
    @BindView(R.id.car_model_input)
    EditText carModelInput;
    @BindView(R.id.car_year_input)
    Spinner carYearInput;
    @BindView(R.id.car_engine_input)
    EditText carEngineInput;
    @BindView(R.id.car_vin_input)
    EditText carVinInput;
    @BindView(R.id.submit_car)
    Button carSubmit;

    private FirebaseUser user;
    private FirebaseFirestore firestoreDB;
    private User userData;
    private DocumentReference userDataReference;

    private String carMake;
    private String carModel;
    private String carEngine;
    private String carVIN;
    private int carYear;

    private ArrayAdapter<String> yearAdapter;


    public static CarDetailsFragment newInstance() {
        return new CarDetailsFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        setUi();
        init();
    }

    private void setUi() {
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1980; i--) {
            years.add(Integer.toString(i));
        }
        yearAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.spinner_item, years);
        carYearInput.setAdapter(yearAdapter);
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();
        userDataReference = firestoreDB.collection(DBConstants.DB_USERS_COLLECTION).document(user.getUid());

        getUserData();
    }

    @OnClick(R.id.submit_car)
    void onCarSubmitClicked() {
        if (!inputsValidated()) {
            return;
        }
        Car carToSave = new Car(user.getUid(), carMake, carModel, carYear, carEngine, carVIN);

        setUserData(carToSave);
        Log.d(TAG, "onCarSubmitClicked: saving a car: " + carToSave.toString());
    }

    private void getUserData() {
        userDataReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userData = documentSnapshot.toObject(User.class);
                if (userData.getCar() != null) {
                    updateUiWithCar(userData.getCar());
                    carSubmit.setText("update_car");
                } else {
                    carSubmit.setText("set_car");
                }
            }
        });
    }

    private void updateUiWithCar(Car car) {
        carMakeInput.setText(car.getMake());
        carModelInput.setText(car.getModel());
        carVinInput.setText(car.getCarVIN());
        carEngineInput.setText(car.getEngine());
        carYearInput.setSelection(yearAdapter.getPosition(Integer.toString(car.getYear())));
    }

    private void setUserData(final Car carToSave) {
        if (userData != null) {
            userData.setCar(carToSave);
            firestoreDB.collection(DBConstants.DB_USERS_COLLECTION).document(userData.getUid()).set(userData, SetOptions.merge());
            Toast.makeText(getActivity().getApplicationContext(), "Car Details saved successfully", Toast.LENGTH_SHORT).show();

            onCarSaved();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Car Details not captured", Toast.LENGTH_SHORT).show();
        }
    }


    private void onCarSaved() {
        getActivity().onBackPressed();
    }

    private boolean inputsValidated() {
        boolean error = false;
        carMake = carMakeInput.getText().toString().trim();
        carModel = carModelInput.getText().toString().trim();
        //this is safe because the array is populated by integers
        carYear = Integer.parseInt(carYearInput.getSelectedItem().toString());
        carEngine = carEngineInput.getText().toString().trim();
        carVIN = carVinInput.getText().toString().trim();

        if (carMake.isEmpty()) {
            error = true;
            carMakeInput.setError("empty_input_field_error");
        }
        if (carModel.isEmpty()) {
            error = true;
            carModelInput.setError("empty_input_field_error");
        }
        if (carEngine.isEmpty()) {
            error = true;
            carEngineInput.setError("empty_input_field_error");
        }
        if (carVIN.isEmpty()) {
            error = true;
            carVinInput.setError("empty_input_field_error");
        }
        if (carVIN.length() < 17) {
            error = true;
            carVinInput.setError("wrong_info_error");
        }

        return !error;
    }
}