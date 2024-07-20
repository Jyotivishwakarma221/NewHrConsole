package com.investmango.hrconsole.admin.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class AttendanceGraphFragment extends Fragment {
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private ImageView right_Arrow;
    private SharedPreferences preferences;
    private PieChart pieChart;
    private BarChart barChart;
    private int currentMonthIndex;
    private JSONArray monthlyAttendanceData;
    TextView textView_month;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_graph, container, false);
        pieChart = view.findViewById(R.id.pie_Chartsss);
        barChart = view.findViewById(R.id.barChart);

        right_Arrow = view.findViewById(R.id.rightArrow);
        textView_month = view.findViewById(R.id.textViewmonth);

        // Initialize your API client and preferences
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Set up the SwipeRefreshLayout to listen for refresh events
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchAndDisplayCurrentMonthData();
            Toast.makeText(getContext(), "Refreshing", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });

        fetchAndDisplayCurrentMonthData();

        right_Arrow.setOnClickListener(v -> {
            // Handle the right arrow click
            currentMonthIndex = (currentMonthIndex + 1) % monthlyAttendanceData.length();
            displayMonthlyData(currentMonthIndex);

            // Update textView_month with the current month
            try {
                JSONObject currentMonthData = monthlyAttendanceData.getJSONObject(currentMonthIndex);
                String month = currentMonthData.getString("month");
                textView_month.setText(month);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to update month", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void fetchAndDisplayCurrentMonthData() {
        Call<ResponseBody> call = apiInterface.getMonthlyAttendance(token, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody>  call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        monthlyAttendanceData = new JSONArray(responseString);

                        if (monthlyAttendanceData.length() > 0) {
                            // By default, display data for the first month
                            currentMonthIndex = 0;
                            displayMonthlyData(currentMonthIndex);
                            // Update textView_month with the current month
                            try {
                                JSONObject currentMonthData = monthlyAttendanceData.getJSONObject(currentMonthIndex);
                                String month = currentMonthData.getString("month");
                                textView_month.setText(month);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Failed to parse response data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed to parse response data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to fetch data from the API", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMonthlyData(int monthIndex) {
        if (monthlyAttendanceData != null && monthIndex >= 0 && monthIndex < monthlyAttendanceData.length()) {
            try {
                JSONObject currentMonthData = monthlyAttendanceData.getJSONObject(monthIndex);
                populateCharts(currentMonthData);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to parse monthly data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateCharts(JSONObject currentMonthData) throws JSONException {
        String month = currentMonthData.getString("month");
        long totalWorkingDays = currentMonthData.getLong("totalWorkingDays");
        long totalPresent = currentMonthData.getLong("totalPresent");
        long totalAbsent = currentMonthData.getLong("totalAbsent");

        // Populate the bar chart and pie chart with this data
        populateBarChart(month, totalWorkingDays, totalPresent, totalAbsent);
        populatePieChart(totalPresent, totalAbsent);
    }

    // Method to populate the Bar Chart
    private void populateBarChart(String month, long totalWorkingDays, long totalPresent, long totalAbsent) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, totalWorkingDays));
        barEntries.add(new BarEntry(1, totalPresent));
        barEntries.add(new BarEntry(2, totalAbsent));

        BarDataSet barDataSet = new BarDataSet(barEntries, month);
        barDataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark),
                ContextCompat.getColor(requireContext(), R.color.green),
                ContextCompat.getColor(requireContext(), R.color.Button)
        );

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barData.setBarWidth(0.7f);
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        barDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        barDataSet.setValueFormatter(new IntegerValueFormatter());
        XAxis xAxis = barChart.getXAxis();

        xAxis.setDrawLabels(true);
        xAxis.setLabelCount(3); // Set the number of labels to display
        xAxis.setGranularity(1); // Set the granularity (adjust as needed)
        barChart.getViewPortHandler().setMaximumScaleX(2); // Adjust the scale factor as needed
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Total Working Days", "Present", "Absent"}));
        barChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTextSize(12f); // Set text size for values inside the bars
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftYAxis = barChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);

        barChart.invalidate();
    }

    // Method to populate the Pie Chart
    private void populatePieChart(long totalPresent, long totalAbsent) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(totalPresent, "Present"));
        pieEntries.add(new PieEntry(totalAbsent, "Absent"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Employee Attendance");
        pieDataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.green),
                ContextCompat.getColor(requireContext(), R.color.Button)
        );

        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setValueLinePart1OffsetPercentage(100f);
        pieDataSet.setValueFormatter(new IntegerValueFormatter());

        pieChart.setData(pieData);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(10f);

        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    static class IntegerValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value); // Convert the float to an integer and return it as a string
        }
    }
}
