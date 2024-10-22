package com.investmango.hrconsole.user.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.EmpPerformance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeePerformanceFragment extends Fragment {
    private float cumulativeTotalScore = 0.0f;
    private float cumulativeObtainedScore = 0.0f;
    private String token;
    private TextView created_Date;
    private RatingBar attendance_Rating;
    private RatingBar job_Knowledge;
    private RatingBar skills;
    private RatingBar work_Quality;
    private RatingBar Initiative;
    private RatingBar team_Work;
    private RatingBar general_Conduct;
    private RatingBar discipline_Rating;
    private TextView total_Score;
    private TextView obtained_Score;
    private TextView performance_Score;
    private TextView comment_Score;
    private BarChart barChart;
    private PieChart pieChart;
    private ImageView backButton;
    private long userId;
    private ApiInterface apiInterface;
    private int currentIndex = 0;
    private List<EmpPerformance> empPerformanceList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_performance, container, false);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        backButton = view.findViewById(R.id.backButton);
        created_Date = view.findViewById(R.id.created_Date);
        attendance_Rating = view.findViewById(R.id.attendance_Rating);
        job_Knowledge = view.findViewById(R.id.job_Knowledge);
        skills = view.findViewById(R.id.skills);
        work_Quality = view.findViewById(R.id.work_Quality);
        Initiative = view.findViewById(R.id.Int);
        team_Work = view.findViewById(R.id.team_Work);
        general_Conduct = view.findViewById(R.id.general_Conduct);
        discipline_Rating = view.findViewById(R.id.discipline_Rating);
        total_Score = view.findViewById(R.id.total_Score);
        obtained_Score = view.findViewById(R.id.obtained_Score);
        performance_Score = view.findViewById(R.id.performance_Score);
        comment_Score = view.findViewById(R.id.comment_Score);
        barChart = view.findViewById(R.id.bar_Chart);
        pieChart = view.findViewById(R.id.pie_Chart);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            getEmployeePerformanceList(token, userId);
            Toast.makeText(getContext(), "Refreshing", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Get the saved token from SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        backButton.setOnClickListener(v -> {
            if (empPerformanceList != null && !empPerformanceList.isEmpty()) {
                // Increment the index and ensure it stays within bounds
                currentIndex++;
                if (currentIndex >= empPerformanceList.size()) {
                    currentIndex = 0;
                }
                updateUIWithPerformanceData(empPerformanceList.get(currentIndex));
            }
        });

        if (empPerformanceList != null && !empPerformanceList.isEmpty()) {
            updateUIWithPerformanceData(empPerformanceList.get(currentIndex));
        } else {
            getEmployeePerformanceList(token, userId);
        }
        return view;
    }

    private void updateUIWithPerformanceData(EmpPerformance empPerformance) {
        if (empPerformance != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(empPerformance.getCreatedOn()));
            created_Date.setText(formattedDate);
            attendance_Rating.setRating(empPerformance.getAttendance());
            job_Knowledge.setRating(empPerformance.getJobKnowledge());
            skills.setRating(empPerformance.getSkills());
            work_Quality.setRating(empPerformance.getWorkQuality());
            Initiative.setRating(empPerformance.getInitiative());
            team_Work.setRating(empPerformance.getTeamWork());
            general_Conduct.setRating(empPerformance.getGeneralConduct());
            discipline_Rating.setRating(empPerformance.getDiscipline());
            total_Score.setText(String.valueOf(empPerformance.getTotalScore()));
            obtained_Score.setText(String.valueOf(empPerformance.getObtainedScore()));
            performance_Score.setText(empPerformance.getOverAll());
            comment_Score.setText(empPerformance.getComment());
        }
    }
    private void getEmployeePerformanceList(String token, long userId) {
        Call<List<EmpPerformance>> call = apiInterface.getSingleEmployeeAllPerformanceByEmpId(userId);
        call.enqueue(new Callback<List<EmpPerformance>>() {
            @Override
            public void onResponse(@NonNull Call<List<EmpPerformance>> call, @NonNull Response<List<EmpPerformance>> response) {
                if (response.isSuccessful()) {
                    empPerformanceList = response.body();
                    if (empPerformanceList != null && !empPerformanceList.isEmpty()) {
                        // Loop through the list of employee performances
                        for (EmpPerformance empPerformance : empPerformanceList) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            String formattedDate = dateFormat.format(new Date(empPerformance.getCreatedOn()));
                            created_Date.setText(formattedDate);

                            attendance_Rating.setRating(empPerformance.getAttendance());
                            job_Knowledge.setRating(empPerformance.getJobKnowledge());
                            skills.setRating(empPerformance.getSkills());
                            work_Quality.setRating(empPerformance.getWorkQuality());
                            Initiative.setRating(empPerformance.getInitiative());
                            team_Work.setRating(empPerformance.getTeamWork());
                            general_Conduct.setRating(empPerformance.getGeneralConduct());
                            discipline_Rating.setRating(empPerformance.getDiscipline());

                            total_Score.setText(String.valueOf(empPerformance.getTotalScore()));
                            obtained_Score.setText(String.valueOf(empPerformance.getObtainedScore()));
                            performance_Score.setText(empPerformance.getOverAll());
                            comment_Score.setText(empPerformance.getComment());

                            cumulativeTotalScore += empPerformance.getTotalScore();
                            cumulativeObtainedScore += empPerformance.getObtainedScore();

                            // Create a BarChart and a PieChart to display cumulative scores
                            createBarChart(barChart, cumulativeTotalScore, cumulativeObtainedScore);
                            createPieChart(pieChart, cumulativeTotalScore, cumulativeObtainedScore);
                        }
                    }
                } else {
                    Log.e("EmployeePerformance", "Failed to fetch empPerformance list");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EmpPerformance>> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }
private void createPieChart(PieChart pieChart, float totalScore, float obtainedScore) {
    ArrayList<PieEntry> entries = new ArrayList<>();
    entries.add(new PieEntry(totalScore, "Total Score"));
    entries.add(new PieEntry(obtainedScore, "Obtained Score"));
    PieDataSet pieDataSet = new PieDataSet(entries, "Scores");

    // Define the colors for the entries
    pieDataSet.setColors(ContextCompat.getColor(requireContext(), R.color.skyBlue),
            ContextCompat.getColor(requireContext(), R.color.green));

    pieDataSet.setValueFormatter(new PercentFormatter());
    PieData pieData = new PieData(pieDataSet);
    pieChart.setHoleRadius(0f);
    pieChart.setEntryLabelTextSize(12f);
    pieDataSet.setValueTextSize(15f);
    pieDataSet.setSelectionShift(5f);
    pieDataSet.setSliceSpace(3f);
    pieData.setValueTextColor(Color.WHITE);
    pieData.setValueTypeface(Typeface.DEFAULT_BOLD);
    pieDataSet.setValueFormatter(new MonthlyTaskFragment.IntegerValueFormatter());
    pieChart.setData(pieData);
    pieChart.invalidate();
}
    private void createBarChart(BarChart barChart, float totalScore, float obtainedScore) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, totalScore));
        entries.add(new BarEntry(1, obtainedScore));

        BarDataSet barDataSet = new BarDataSet(entries, "Scores");
        // Define the colors for the entries
        barDataSet.setColors(ContextCompat.getColor(requireContext(), R.color.skyBlue), ContextCompat.getColor(requireContext(), R.color.green));

        barDataSet.setValueFormatter(new PercentFormatter());
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f);
        barChart.setTouchEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setLabelCount(2);
        xAxis.setGranularity(1);
        barChart.getViewPortHandler().setMaximumScaleX(2);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Total Score", "Obtained Score"}));
        barChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTextSize(12f); // Set text size for values inside the bars
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.setDescription(null);
        barChart.setData(barData);
        barChart.invalidate();

        YAxis leftYAxis = barChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f);
        barDataSet.setValueTextSize(14f); // Set text size for values inside the bars
        barDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black)); // Set text color for values
        barDataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // Set text style for values
        barDataSet.setFormSize(15f); // Set size of the form indicating the legend
        barChart.getAxisRight().setEnabled(false);
    }

}
