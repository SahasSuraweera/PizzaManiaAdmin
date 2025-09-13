package com.example.pizzamaniaadmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddPizzaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    EditText etName, etSize, etPrice, etDesc;
    Button btnSave;
    ImageView pizzaImageView;

    Uri selectedImageUri;
    String pizzaId = null; // If editing existing pizza
    String currentImageUrl = null;

    DatabaseReference databaseReference;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pizza);

        databaseReference = FirebaseDatabase.getInstance().getReference("Pizzas");
        storageReference = FirebaseStorage.getInstance().getReference("PizzaImages");

        etName = findViewById(R.id.etName);
        etSize = findViewById(R.id.etSize);
        etPrice = findViewById(R.id.etPrice);
        etDesc = findViewById(R.id.etDesc);
        btnSave = findViewById(R.id.btnSave);
        pizzaImageView = findViewById(R.id.pizzaImageView);

        // Check if editing existing pizza
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("pizza")) {
            PizzaModel pizza = (PizzaModel) intent.getSerializableExtra("pizza");
            pizzaId = pizza.getId();
            currentImageUrl = pizza.getImageUrl();

            etName.setText(pizza.getName());
            etSize.setText(pizza.getSize());
            etPrice.setText(String.valueOf(pizza.getPrice()));
            etDesc.setText(pizza.getDescription());
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                // If using Glide, otherwise skip
                pizzaImageView.setImageURI(null); // placeholder
            }
        }

        // Select image
        pizzaImageView.setOnClickListener(v -> {
            Intent pick = new Intent();
            pick.setType("image/*");
            pick.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(pick, "Select Pizza Image"), PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String size = etSize.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty() || size.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);

            if (pizzaId == null) {
                // New pizza
                if (selectedImageUri == null) {
                    Toast.makeText(this, "Select an image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadPizza(name, size, price, desc, selectedImageUri);
            } else {
                // Update pizza
                if (selectedImageUri != null) {
                    uploadPizza(name, size, price, desc, selectedImageUri); // update with new image
                } else {
                    updatePizza(name, size, price, desc, currentImageUrl); // keep existing image
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            pizzaImageView.setImageURI(selectedImageUri);
        }
    }

    private void uploadPizza(String name, String size, double price, String description, Uri imageUri) {
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    String id = pizzaId != null ? pizzaId : databaseReference.push().getKey();
                    PizzaModel pizza = new PizzaModel(id, name, size, price, description, imageUrl);
                    databaseReference.child(id).setValue(pizza);

                    Toast.makeText(this, pizzaId != null ? "Pizza updated!" : "Pizza added!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePizza(String name, String size, double price, String description, String imageUrl) {
        PizzaModel pizza = new PizzaModel(pizzaId, name, size, price, description, imageUrl);
        databaseReference.child(pizzaId).setValue(pizza);
        Toast.makeText(this, "Pizza updated!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
