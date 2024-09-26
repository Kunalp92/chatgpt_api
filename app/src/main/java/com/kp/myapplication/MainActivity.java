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
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/completions";
    private static final String API_KEY = "YOUR_OPENAI_API_KEY";  // Replace with your OpenAI API key
    private EditText answerEditText;
    private TextView feedbackTextView, questionTextView;
    private Button submitAnswerButton;

    // Variables to handle multiple questions
    private ArrayList<String> questions;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 5; // We want to ask 5 questions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elements
        questionTextView = findViewById(R.id.questionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        submitAnswerButton = findViewById(R.id.submitAnswerButton);

        // Prepare a list of questions
        prepareQuestionList();

        // Show the first question
        displayNextQuestion();

        // Submit answer button click listener
        submitAnswerButton.setOnClickListener(view -> {
            String userAnswer = answerEditText.getText().toString();
            if (!userAnswer.isEmpty()) {
                sendAnswerToGPT(userAnswer);  // Send answer to GPT for analysis
            } else {
                Toast.makeText(MainActivity.this, "Please enter your answer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to create a list of interview questions
    private void prepareQuestionList() {
        questions = new ArrayList<>();
        questions.add("What is your greatest strength?");
        questions.add("Why should we hire you?");
        questions.add("Tell me about a challenge you faced and how you handled it.");
        questions.add("Where do you see yourself in five years?");
        questions.add("Describe a situation where you demonstrated leadership.");
        questions.add("What are your weaknesses?");
        questions.add("Why do you want to work for this company?");
        questions.add("Tell me about a time you failed and how you handled it.");
        questions.add("How do you handle pressure and stress?");
        questions.add("What motivates you to do your best work?");

        // Shuffle the questions to ensure randomness
        Collections.shuffle(questions);
    }

    // Method to display the next question
    private void displayNextQuestion() {
        if (currentQuestionIndex < totalQuestions) {
            String currentQuestion = questions.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion);
            currentQuestionIndex++;
        } else {
            // All questions have been asked
            questionTextView.setText("Interview Complete. Thank you!");
            submitAnswerButton.setEnabled(false); // Disable the submit button
        }
    }

    // Method to send user answer to OpenAI GPT model for analysis
    private void sendAnswerToGPT(String answer) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();

        try {
            // Building the request body
            jsonBody.put("model", "text-davinci-003");  // Use the GPT model
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
                e.printStackTrace();
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
                        runOnUiThread(() -> {
                            feedbackTextView.setText(feedback.trim());
                            displayNextQuestion();  // Move to the next question after showing feedback
                            answerEditText.setText("");  // Clear the answer field
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> feedbackTextView.setText("Error in response format."));
                    }
                } else {
                    runOnUiThread(() -> feedbackTextView.setText("Error: " + response.message()));
                }
            }
        });
    }

}