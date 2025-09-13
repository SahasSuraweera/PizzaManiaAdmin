package com.example.pizzamaniaadmin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder> {

    Context context;
    ArrayList<PizzaModel> pizzaList;
    DatabaseReference databaseReference;

    public PizzaAdapter(Context context, ArrayList<PizzaModel> pizzaList) {
        this.context = context;
        this.pizzaList = pizzaList;
        databaseReference = FirebaseDatabase.getInstance().getReference("Pizzas");
    }

    @NonNull
    @Override
    public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pizza, parent, false);
        return new PizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
        PizzaModel pizza = pizzaList.get(position);

        holder.tv1.setText(pizza.getName() + " (" + pizza.getSize() + ")");
        holder.tv2.setText("Rs. " + pizza.getPrice() + " - " + pizza.getDescription());

        // Load image from URL without Glide
        new DownloadImageTask(holder.imageView).execute(pizza.getImageUrl());

        // Update button click
        holder.btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddPizzaActivity.class);
            intent.putExtra("pizza", pizza); // PizzaModel should implement Serializable
            context.startActivity(intent);
        });

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            databaseReference.child(pizza.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        pizzaList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, pizzaList.size());
                        Toast.makeText(context, "Pizza deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return pizzaList.size();
    }

    static class PizzaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tv1, tv2;
        Button btnUpdate, btnDelete;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pizzaImage);
            tv1 = itemView.findViewById(R.id.textView1);
            tv2 = itemView.findViewById(R.id.textView2);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // AsyncTask to download image from URL
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlStr = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background); // placeholder
            }
        }
    }
}
