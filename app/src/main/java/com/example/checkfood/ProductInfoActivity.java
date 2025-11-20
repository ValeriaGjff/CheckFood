package com.example.checkfood;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ProductInfoActivity extends AppCompatActivity {

    private TextView productName;
    private TextView ingredientsText;
    private ImageButton btnFav;
    private TextView statusText;
    private Button btnHome;

    private String currentBarcode = "";
    private String currentName = "";

    private FavoritesDbHelper favDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        productName     = findViewById(R.id.productName);
        ingredientsText = findViewById(R.id.ingredientsText);
        btnFav          = findViewById(R.id.btnFav);
        statusText      = findViewById(R.id.statusText);
        btnHome         = findViewById(R.id.btnHome);

        favDb = new FavoritesDbHelper(this);

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        String barcode = getIntent().getStringExtra("barcode");
        if (barcode == null || barcode.isEmpty()) {
            Toast.makeText(this, "Штрих-код не передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentBarcode = barcode;

        btnFav.setOnClickListener(v -> {
            if (currentBarcode.isEmpty()) return;
            boolean isFav = favDb.isFavorite(currentBarcode);
            if (isFav) {
                favDb.remove(currentBarcode);
                Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
            } else {
                favDb.add(currentBarcode, currentName.isEmpty() ? "Без названия" : currentName);
                Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
            }
            updateFavIcon();
        });

        ProductDbHelper db = new ProductDbHelper(this);
        String localName = db.getNameByBarcode(barcode);
        String localIng  = db.getIngredientsByBarcode(barcode);

        if (localIng != null && !localIng.isEmpty()) {
            currentName = (localName != null && !localName.isEmpty()) ? localName : "Без названия";
            applyProduct(currentName, localIng, barcode);
        } else {

            loadFromOpenFoodFacts(barcode);
        }
    }

    private void updateFavIcon() {
        boolean isFav = !currentBarcode.isEmpty() && favDb.isFavorite(currentBarcode);
        btnFav.setImageResource(R.drawable.ic_favorite);
        btnFav.setColorFilter(isFav ? 0xFFD56C6C : 0xFFB0B0B0);
    }

    private void applyProduct(String name, String ingredientsRaw, String barcode) {
        currentName = name;

        boolean isSafe = !containsHarmful(ingredientsRaw);

        String highlighted = highlightHarmful(ingredientsRaw);
        productName.setText(name);
        ingredientsText.setText(Html.fromHtml(highlighted, Html.FROM_HTML_MODE_LEGACY));

        setStatusView(isSafe);

        // Запись в историю
        new HistoryDbHelper(this).insert(barcode, name, System.currentTimeMillis(), isSafe);

        updateFavIcon();
    }

    private void setStatusView(boolean isSafe) {
        if (isSafe) {
            statusText.setText("Безопасно");
            statusText.setBackgroundResource(R.drawable.bg_status_safe);
        } else {
            statusText.setText("Обнаружены вредные вещества");
            statusText.setBackgroundResource(R.drawable.bg_status_danger);
        }
    }

    private void showNotFound() {
        productName.setText("Продукт не найден");
        ingredientsText.setText("Извиняемся за неудобства, но продукт не найден. Мы работаем над этим.");
        statusText.setText("Нет данных");
        statusText.setBackgroundResource(R.drawable.bg_status_danger);
        updateFavIcon();
    }

    private String highlightHarmful(String ing) {
        if (ing == null) return "";
        String[] harmful = new String[]{
                "глутамат","e621","аспартам","e951",
                "нитрит натрия","e250","бензоат","e211",
                "сорбат","e200","краситель","консервант"
        };
        String res = ing;
        for (String bad : harmful) {
            String pattern = "(?i)\\b" + bad + "\\b";
            res = res.replaceAll(pattern,
                    "<font color='#EF5350'>" + bad + "</font>");
        }
        return res;
    }

    private boolean containsHarmful(String ing) {
        if (ing == null) return false;
        String low = ing.toLowerCase(Locale.ROOT);
        String[] harmful = new String[]{
                "глутамат","e621","аспартам","e951",
                "нитрит натрия","e250","бензоат","e211",
                "сорбат","e200","краситель","консервант"
        };
        for (String h : harmful) {
            if (low.contains(h)) return true;
        }
        return false;
    }

    private void loadFromOpenFoodFacts(String barcode) {
        new Thread(() -> {
            try {
                String apiUrl = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                if (root.optInt("status") != 1) {
                    new Handler(Looper.getMainLooper()).post(this::showNotFound);
                    return;
                }

                JSONObject p = root.getJSONObject("product");
                String name = p.optString("product_name", "Без названия");

                String ingredients = p.optString("ingredients_text_ru",
                        p.optString("ingredients_text",
                                p.optString("ingredients_text_en", "")));

                if (ingredients.isEmpty() && p.has("ingredients")) {
                    try {
                        JSONArray arr = p.getJSONArray("ingredients");
                        StringBuilder ingSb = new StringBuilder();
                        for (int i = 0; i < arr.length(); i++) {
                            String t = arr.getJSONObject(i).optString("text", "");
                            if (!t.isEmpty()) {
                                if (ingSb.length() > 0) ingSb.append(", ");
                                ingSb.append(t);
                            }
                        }
                        ingredients = ingSb.toString();
                    } catch (Exception ignored) {}
                }

                final String fName = name;
                final String fIng  = ingredients;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (fIng == null || fIng.isEmpty()) {
                        productName.setText(fName);
                        ingredientsText.setText("Состав не найден");
                        setStatusView(true);
                    } else {
                        applyProduct(fName, fIng, barcode);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(ProductInfoActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}