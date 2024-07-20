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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonthlyTaskFragment extends Fragment {
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private ImageView leftArrow;
    private ImageView rightArrow;
    private ImageView rightArows;
    private TextView month;
    private SharedPreferences preferences;
    private PieChart pieChart;
    private BarChart barChart;
    private int currentMonthIndex;
    private JSONArray jsonArray;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_task, container, false);

        pieChart = view.findViewById(R.id.pieCharts);
        barChart = view.findViewById(R.id.barCharts);
        leftArrow = view.findViewById(R.id.leftArrow);

        month = view.findViewById(R.id.month);
        rightArows = view.findViewById(R.id.right_Arow);

        ApiClient apiClient = new ApiClient(requireActivity());
        apiInterface = apiClient.getApiInterface();
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        leftArrow.setOnClickListener(v -> showPreviousMonthData());
        rightArows.setOnClickListener(v -> showNextMonthData());
        taskMonthData();
        return view;
    }

    private void showNextMonthData() {
        if (jsonArray != null && currentMonthIndex < jsonArray.length() - 1) {
            try {
                // Move to the next month
                currentMonthIndex++;
                // Get data for the next month
                JSONObject nextMonthData = jsonArray.getJSONObject(currentMonthIndex);

                String nextMonth = nextMonthData.getString("Month");
                long nextTotalTask = nextMonthData.getLong("Total Task");
                long nextCompletedTask = nextMonthData.getLong("Status - Completed");
                long nextPendingTask = nextMonthData.getLong("Status - Pending");

                month.setText(nextMonth);
                populateBarChart(requireContext(),nextMonth, nextTotalTask, nextCompletedTask, nextPendingTask);
                populatePieChart(requireContext(),nextTotalTask, nextCompletedTask, nextPendingTask);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireActivity(), "No next data available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPreviousMonthData() {
        if (jsonArray != null && currentMonthIndex > 0) {
            try {
                // Move to the previous month
                currentMonthIndex-- ;
                // Get data for the previous month
                JSONObject previousMonthData = jsonArray.getJSONObject(currentMonthIndex);
                String previousMonth = previousMonthData.getString("Month");
                long previousTotalTask = previousMonthData.getLong("Total Task");
                long previousCompletedTask = previousMonthData.getLong("Status - Completed");
                long previousPendingTask = previousMonthData.getLong("Status - Pending");
                // Update UI with data for the previous month
                month.setText(previousMonth);
                Log.d("Previous Month","Previous Month-->" + " " + previousMonth);
                populateBarChart(requireContext(),previousMonth, previousTotalTask, previousCompletedTask, previousPendingTask);
                populatePieChart(requireContext(),previousTotalTask, previousCompletedTask, previousPendingTask);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireActivity(), "No previous data available", Toast.LENGTH_SHORT).show();
        }
    }

    private void taskMonthData() {
    Call<ResponseBody> call = apiInterface.getMonthlyTaskStatistics(token, userId);

    call.enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
            if (response.isSuccessful() && response.body() != null) {
                try {
                    String responseBody = response.body().string();
                    jsonArray = new JSONArray(responseBody);
                    // Initially, set the currentMonthIndex to the last month's index
                    currentMonthIndex = jsonArray.length() - 1;

                    // Check if there's data available
                    if (jsonArray.length() > 0) {
                        // Update the UI with data for the current month
                        JSONObject currentMonthData = jsonArray.getJSONObject(currentMonthIndex);
                        String currentMonth = currentMonthData.getString("Month");
                        month.setText(currentMonth);
                        Log.d("Current Month","Current Month---" + currentMonth);

                        // Populate the bar chart and pie chart with the data for the current month
                        long currentTotalTask = currentMonthData.getLong("Total Task");
                        long currentCompletedTask = currentMonthData.getLong("Status - Completed");
                        long currentPendingTask = currentMonthData.getLong("Status - Pending");
                        populateBarChart(requireContext(),currentMonth, currentTotalTask, currentCompletedTask, currentPendingTask);
                        populatePieChart(requireContext(),currentTotalTask, currentCompletedTask, currentPendingTask);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(requireActivity(), "No data available", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {

        }
    });
}
    private void populatePieChart(Context context,long totalTask, long completedTask, long pendingTask) {
        PieChart pieChart = requireView().findViewById(R.id.pieCharts);
        float completedPercentage = calculatePercentage(completedTask, totalTask);
        float pendingPercentage = calculatePercentage(pendingTask, totalTask);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(completedPercentage, "Completed"));
        pieEntries.add(new PieEntry(pendingPercentage, "Pending"));

        PieDataSet dataSet = new PieDataSet(pieEntries, "Task Status");

        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.green),
                ContextCompat.getColor(context, R.color.Button));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(15f);
        dataSet.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(true);
        pieData.setValueTextSize(15f);
        configurePieChartAppearance(pieChart);

        pieChart.setData(pieData);
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(10f);
        pieChart.setDescription(null);
        pieChart.invalidate();
        // Display data values
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new IntegerValueFormatter());
        dataSet.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueLinePart1OffsetPercentage(100f);

        // Format the data values as strings with a percentage sign (%) appended
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });
    }
    private void configurePieChartAppearance(PieChart chart) {
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(0f);
        chart.setTransparentCircleRadius(5f);
        chart.setEntryLabelTextSize(14f);
    }

    private void populateBarChart(Context context,String currentMonth, long totalTask, long completedTask, long pendingTask) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, totalTask));
        barEntries.add(new BarEntry(1, completedTask));
        barEntries.add(new BarEntry(2, pendingTask));

        BarDataSet dataSet = new BarDataSet(barEntries, currentMonth);
        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark),
                ContextCompat.getColor(context, R.color.green),
                ContextCompat.getColor(context, R.color.Button));

        BarData barData = new BarData(dataSet);
        dataSet.setValueFormatter(new IntegerValueFormatter());
        barData.setBarWidth(0.7f);
        barChart.setData(barData);
        barChart.setTouchEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);

        xAxis.setLabelCount(3); // Set the number of labels to display
        xAxis.setGranularity(1);
        barChart.getViewPortHandler().setMaximumScaleX(2); // Adjust the scale factor as needed

        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Total Task", "Completed Task", "Pending Task"}));
        barChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // Set text size for values inside the bars
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setFormSize(15f);
        barChart.invalidate();
        barChart.setDescription(null);
        configureBarChartAppearance(barChart);
    }

    private void configureBarChartAppearance(BarChart chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(false);

        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f);

        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);

        chart.setPinchZoom(false);
    }

    private float calculatePercentage(long part, long total) {
        if (total == 0) {
            return 0.0f;
        }
        return (float) (part * 100) / total;
    }

    static class IntegerValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }

}
