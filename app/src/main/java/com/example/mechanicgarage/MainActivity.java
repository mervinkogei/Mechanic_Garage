package com.example.mechanicgarage;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        FirebaseAuth.AuthStateListener {

    private static final String TAG = "MainActivity";


    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.action_bar)
    Toolbar toolbar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        setUi();
    }

    private void setUi() {
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
//        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        navView.setNavigationItemSelectedListener(this);
        navView.setCheckedItem(R.id.nav_info);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, GarageInfoFragment.newInstance()).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        item.setChecked(true);
        drawerLayout.closeDrawers();
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_register_login:
                if (firebaseAuth.getCurrentUser() == null) {
                    fragment = LoginRegisterFragment.newInstance(LoginRegisterFragment.LOGIN_FRAGMENT);
                } else {
                    firebaseAuth.signOut();
                }
                break;
            case R.id.nav_info:
                fragment = GarageInfoFragment.newInstance();
                break;

            case R.id.nav_car_details:
                fragment = CarDetailsFragment.newInstance();
                break;
            case R.id.nav_set_appointment:
                fragment = AppointmentFragment.newInstance();
                break;
        }

        if (fragment == null) {
            fragment = GarageInfoFragment.newInstance();
        }

        setFragment(fragment);
        return false;
    }


    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();


        View headerView = navView.getHeaderView(0);
        TextView headerText = headerView.findViewById(R.id.nav_header_text);
        Menu menu = navView.getMenu();
        MenuItem login = menu.findItem(R.id.nav_register_login);
        MenuItem appointment = menu.findItem(R.id.nav_set_appointment);
        MenuItem details = menu.findItem(R.id.nav_car_details);

        if (user != null) {
            headerText.setText(user.getDisplayName());
            login.setTitle(R.string.logout_item_text);
            appointment.setEnabled(true);
            details.setEnabled(true);
        } else {
            headerText.setVisibility(View.GONE);
            login.setTitle(R.string.login_item_text);
            appointment.setEnabled(false);
            details.setEnabled(false);
        }
    }

}