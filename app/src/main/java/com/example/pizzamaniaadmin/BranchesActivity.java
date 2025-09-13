package com.example.pizzamaniaadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class BranchesActivity extends AppCompatActivity {

    private Button btnAddBranch, btnViewBranches;
    private LinearLayout branchListContainer;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches);

        btnAddBranch = findViewById(R.id.btnAddBranch);
        btnViewBranches = findViewById(R.id.btnViewBranches);
        branchListContainer = findViewById(R.id.branchListContainer);

        dbRef = FirebaseDatabase.getInstance("https://pizzamania-d2775-default-rtdb.firebaseio.com/")
                .getReference("branches");

        // Add sample branches if none exist
        addSampleBranches();

        btnAddBranch.setOnClickListener(v -> showAddBranchDialog());
        btnViewBranches.setOnClickListener(v -> loadBranches());
    }

    private void addSampleBranches() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> branches = new HashMap<>();
                    branches.put("Colombo", new Branch(
                            "Colombo",
                            "123 Colombo Street",
                            6.9271,
                            79.8612,
                            "9:00 AM - 9:00 PM",
                            "Mon-Sat"
                    ));
                    branches.put("Galle", new Branch(
                            "Galle",
                            "45 Galle Road",
                            6.0535,
                            80.2210,
                            "10:00 AM - 8:00 PM",
                            "Mon-Sun"
                    ));
                    dbRef.setValue(branches);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddBranchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Branch");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16,16,16,16);

        final EditText etName = new EditText(this);
        etName.setHint("Branch Name");
        layout.addView(etName);

        final EditText etAddress = new EditText(this);
        etAddress.setHint("Address");
        layout.addView(etAddress);

        final EditText etLat = new EditText(this);
        etLat.setHint("Latitude");
        etLat.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        layout.addView(etLat);

        final EditText etLng = new EditText(this);
        etLng.setHint("Longitude");
        etLng.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        layout.addView(etLng);

        final EditText etHours = new EditText(this);
        etHours.setHint("Opening Hours (e.g. 9:00 AM - 9:00 PM)");
        layout.addView(etHours);

        final EditText etDays = new EditText(this);
        etDays.setHint("Opening Days (e.g. Mon-Sat)");
        layout.addView(etDays);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String latStr = etLat.getText().toString().trim();
            String lngStr = etLng.getText().toString().trim();
            String hours = etHours.getText().toString().trim();
            String days = etDays.getText().toString().trim();

            if(name.isEmpty() || address.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || hours.isEmpty() || days.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);

            Branch newBranch = new Branch(name, address, lat, lng, hours, days);
            dbRef.child(name).setValue(newBranch);
            Toast.makeText(this, "Branch added", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadBranches() {
        branchListContainer.removeAllViews();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        Branch branch = snap.getValue(Branch.class);
                        if(branch != null){
                            TextView tv = new TextView(BranchesActivity.this);
                            tv.setText(branch.getName() + " - " + branch.getAddress() +
                                    "\nLat: " + branch.getLatitude() + ", Lng: " + branch.getLongitude() +
                                    "\nHours: " + branch.getOpeningHours() + ", Days: " + branch.getOpeningDays());
                            tv.setPadding(0,16,0,16);
                            tv.setTextSize(16f);
                            branchListContainer.addView(tv);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Branch Model
    public static class Branch {
        private String name, address, openingHours, openingDays;
        private double latitude, longitude;

        public Branch() {}

        public Branch(String name, String address, double latitude, double longitude, String openingHours, String openingDays) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.openingHours = openingHours;
            this.openingDays = openingDays;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getOpeningHours() { return openingHours; }
        public String getOpeningDays() { return openingDays; }
    }
}
