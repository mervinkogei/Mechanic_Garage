package com.example.mechanicgarage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mechanicgarage.models.AppointmentRequest;
import com.example.mechanicgarage.models.Timeslot;
import com.example.mechanicgarage.utils.DBConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppointmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppointmentFragment extends Fragment {
    private static final String TAG = "AppointmentFragment";


    @BindView(R.id.extra_note_input)
    EditText extraNoteInput;
    @BindView(R.id.appointment_time)
    Spinner appointmentTime;

    private FirebaseUser user;
    private FirebaseFirestore firestoreDB;


    private ArrayAdapter<String> timeslotsAdapter;
    private List<Timeslot> availibleSlots = new ArrayList<>();
    private Set<String> markedCheckboxes = new HashSet<>();
    private Timeslot timeSlot;

    public static AppointmentFragment newInstance() {
        return new AppointmentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        init();
        setUi();
    }

    private void setUi() {
        timeslotsAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item);
        appointmentTime.setAdapter(timeslotsAdapter);
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();

        getAvailableSlots();
    }

    @OnCheckedChanged({R.id.required_service_big, R.id.required_service_brakes,
            R.id.required_service_small, R.id.required_service_diagnostics,
            R.id.required_service_other, R.id.required_service_tyres})
    void onCheckedChange(CompoundButton button, boolean checked) {
        if (checked) {
            markedCheckboxes.add(button.getText().toString());
        } else {
            markedCheckboxes.remove(button.getText().toString());
        }
    }

    @OnItemSelected(R.id.appointment_time)
    void onTimeSlotSelection(int position) {
        timeSlot = availibleSlots.get(position);
    }

    @OnClick(R.id.submit_service_request)
    void onSubmitRequestClicked() {
        if (markedCheckboxes.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "no_service_checked_error, Try Again", Toast.LENGTH_SHORT).show();
            return;
        }

        String extraNote = extraNoteInput.getText().toString().trim();
        List<String> serviceTypes = new ArrayList<>(markedCheckboxes);
        AppointmentRequest request = new AppointmentRequest(serviceTypes, extraNote, user.getUid(), timeSlot.getTime());
        Log.d(TAG, "onSubmitRequestClicked: saving request: " + request.toString());

        addUserRequest(request);
    }

    private void addUserRequest(final AppointmentRequest request) {
        firestoreDB.collection(DBConstants.DB_REQUESTS_COLLECTION).add(request)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "onSuccess: request saved: " + documentReference.getId());
                        timeSlot.setReqId(documentReference.getId());
                        timeSlot.setTaken(true);
                        requestSaved();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed" + e.getMessage());
                    }
                });

    }

    private void requestSaved() {
        Map<String, Object> data = new HashMap<>();
        data.put("taken", timeSlot.isTaken());
        data.put("reqId", timeSlot.getReqId());
        firestoreDB.collection(DBConstants.DB_SCHEDULE_COLLECTION).document(timeSlot.getUid()).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity().getApplicationContext(), "request_success", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorMessage();
            }
        });
    }

    private void getAvailableSlots() {
        long currentTime = System.currentTimeMillis() / 1000;
        firestoreDB.collection(DBConstants.DB_SCHEDULE_COLLECTION).whereGreaterThan("time", currentTime).orderBy("time").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        availibleSlots.clear();
                        for (QueryDocumentSnapshot docSnapshot : queryDocumentSnapshots) {
                            if (!docSnapshot.getBoolean("taken")) {
                                Timeslot slot = docSnapshot.toObject(Timeslot.class);
                                slot.setUid(docSnapshot.getId());
                                availibleSlots.add(slot);
                            }
                        }
                        slotsFetched();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showErrorMessage();
                    }
                });
    }

    private void slotsFetched() {
        List<String> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (Timeslot slot : availibleSlots) {
            cal.setTimeInMillis(slot.getTime() * 1000L);
            dates.add(DateFormat.format("dd-MM-yyyy HH:mm", cal).toString());
        }
        timeslotsAdapter.addAll(dates);
        timeslotsAdapter.notifyDataSetChanged();
    }

    private void showErrorMessage() {
        Toast.makeText(getActivity(), "crash_error_text", Toast.LENGTH_SHORT).show();
    }
}