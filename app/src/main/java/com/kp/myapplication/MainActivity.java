package com.kp.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/models";
    private static final String API_KEY = "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";  // Replace with your OpenAI API key
    private EditText answerEditText;
    private TextView feedbackTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elements
        TextView questionTextView = findViewById(R.id.questionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        Button submitAnswerButton = findViewById(R.id.submitAnswerButton);

        // List of questions
        String[] questions = {
                "What is your greatest strength?",
                "Why should we hire you?",
                "Tell me about a challenge you faced and how you handled it.",
                "Where do you see yourself in five years?",
                "Describe a situation where you demonstrated leadership."
        };

        // Pick a random question from the list
        String randomQuestion = questions[(int) (Math.random() * questions.length)];
        questionTextView.setText(randomQuestion);

        // Submit answer button click listener
        submitAnswerButton.setOnClickListener(view -> {
            String userAnswer = answerEditText.getText().toString();
            if (!userAnswer.isEmpty()) {
                sendAnswerToGPT(userAnswer);
            } else {
                Toast.makeText(MainActivity.this, "Please enter your answer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to send user answer to OpenAI GPT model for analysis
    private void sendAnswerToGPT(String answer) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();

        try {
            // Building the request body
            jsonBody.put("model", "GPT-4 Turbo and GPT-4");  // Use the GPT model
            jsonBody.put("prompt", "Analyze the following answer: " + answer);
            jsonBody.put("max_tokens", 100);
            jsonBody.put("temperature", 0.5);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Creating request body
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Creating request
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        // Sending request to OpenAI API
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error scenario
                e.printStackTrace();
                // Update UI with failure feedback
                runOnUiThread(() -> feedbackTextView.setText("Network error, please try again!"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String feedback = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");

                        // Updating UI with feedback
                        runOnUiThread(() -> feedbackTextView.setText(feedback.trim()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> feedbackTextView.setText("Error in response format."));
                    }
                } else {
                    // Handle unsuccessful responses
                    runOnUiThread(() -> feedbackTextView.setText("Error: " + response.message()));
                }
            }
        });
    }

}